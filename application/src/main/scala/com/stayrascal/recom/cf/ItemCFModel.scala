package com.stayrascal.recom.cf

import java.util.Calendar

import com.stayrascal.recom.cf.common.SimilarityMeasures._
import com.stayrascal.recom.cf.entities.{History, Record, User, UserCompPair}
import com.stayrascal.service.application.domain.Event
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.slf4j.LoggerFactory

object ItemCFModel {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession.builder()
      .appName("Item CF Model")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    val recommender = new LinearItemCFModel(spark)
    //    val history = spark.read.csv(args(0))
    val history = spark.read.csv("./data/history/history.csv")
      .map(row => History(row.getString(0).toInt, row.getString(1).toInt, row.getString(2).toInt, row.getString(3).toFloat))
      .coalesce(spark.sparkContext.defaultParallelism)
      .cache()

    recommender.fit(history)

    showSimilarities(recommender)
    testRecommend(recommender, spark, args(1).toInt, args(2).toInt)
  }

  private def showSimilarities(recommender: LinearItemCFModel): Unit = {
    val start = Calendar.getInstance().getTimeInMillis
    recommender.getSimilarities.get
      .sort("compId", "followCompId1", "followCompId2", "cooc")
      .show()
    val end = Calendar.getInstance().getTimeInMillis
    println(s"Total cost: ${(end - start) / 1000f} s")
  }

  private def testRecommend(recommender: LinearItemCFModel, spark: SparkSession, userId: Int, compId: Int): Unit = {
    import spark.implicits._
    val users = spark.createDataset(Seq(UserCompPair(userId, compId)))
    val start = Calendar.getInstance().getTimeInMillis
    recommender.recommendForUser(users, 3).show(truncate = false)
    val end = Calendar.getInstance().getTimeInMillis
    println(s"Total cost: ${(end - start) / 1000f} s")
  }

}

class ItemCFModel(spark: SparkSession, similarityMeasure: String) extends Serializable {
  val logger = LoggerFactory.getLogger(getClass.getName)

  def this(spark: SparkSession) = this(spark, "cooc")

  val defaultParallelism: Int = spark.sparkContext.defaultParallelism

  var records: Option[Dataset[Record]] = None
  var similarities: Option[DataFrame] = None

  def getSimilarities: Option[DataFrame] = similarities

  import spark.implicits._

  def fit(events: Dataset[Event]): ItemCFModel = {
    logger.info("Try to fit records.")

    val views: Dataset[Record] = events.groupBy("userId", "itemId").count()
      .map(row => Record(row.getInt(0), row.getInt(1), row.getFloat(3)))
      .coalesce(defaultParallelism)

    this.records = Option(views)

    val viewDF =views.toDF("userId", "itemId", "view")
      .coalesce(defaultParallelism)

    val numRatersPerItem = viewDF.groupBy("itemId").count().alias("nor")
      .coalesce(defaultParallelism)

    val viewWithSize = viewDF.join(numRatersPerItem, "itemId")
      .coalesce(defaultParallelism)

    viewWithSize.join(viewWithSize, "userId")
      .toDF("userId", "item1", "view1", "nor1", "item2", "view2", "nor2")
      .selectExpr("userId",
        "item1", "view1", "nor1",
        "item2", "view2", "nor2",
        "view1 * view2 as product",
        "pow(view1, 2) as view1Pow",
        "pow(view2, 2) as view2Pow")
      .coalesce(defaultParallelism)
      .createOrReplaceTempView("joined")

    val sparseMatrix = spark.sql(
      """
        | SELECT item1, item2,
        | count(userId) as size,
        | sum(product) as dotProduct,
        | sum(view1) as viewSum1,
        | sum(view2) as viewSum2,
        | sum(view1Pow) as viewSumOfSq1,
        | sum(view2Pow) as viewSumOfSq2,
        | first(nor1) as nor1,
        | first(nor2) as nor2
        | FROM joined
        | WHERE item1 < item2
        | GROUP BY item1, item2
      """.stripMargin)
      .coalesce(defaultParallelism)
      .cache()

    var sim = sparseMatrix.map(row => {
      val size = row.getAs[Long](2)
      val dotProduct =row.getAs[Double](3)
      val viewSum1 = row.getAs[Double](4)
      val viewSum2 = row.getAs[Double](5)
      val viewSumOfSq1 = row.getAs[Double](6)
      val viewSumOfSq2 = row.getAs[Double](7)
      val numRaters1 = row.getAs[Long](8)
      val numRaters2 = row.getAs[Long](9)

      val cooc = coocurrence(size, numRaters1, numRaters2)
      val corr = correlation(size, dotProduct, viewSum1, viewSum2, viewSumOfSq1, viewSumOfSq2)
      val regCorr = regularizedCorrelation(size, dotProduct, viewSum1, viewSum2, viewSumOfSq1, viewSumOfSq2, 0.1, 0.01)
      val cosSim = cosineSimilarity(dotProduct, scala.math.sqrt(viewSumOfSq1), scala.math.sqrt(viewSumOfSq2))
      val impCosSim = improvedCosineSimilarity(dotProduct, scala.math.sqrt(viewSumOfSq1), scala.math.sqrt(viewSumOfSq2), size, numRaters1, numRaters2)
      val jaccard = jaccardSimilarity(size, numRaters1, numRaters2)
      (row.getInt(0), row.getInt(1), cooc, corr, regCorr, cosSim, impCosSim, jaccard)
    }).toDF("itemId_01", "itemId_02", "cooc", "corr", "regCorr", "cosSim", "impCosSim", "jaccard")

    sim.withColumnRenamed("itemId_01", "itemId_02")
        .withColumnRenamed("itemId_02", "itemId_01")
        .union(sim)
        .repartition(defaultParallelism)
        .cache()
    similarities = Option(sim)
    this
  }

  def recommendForUser(users: Dataset[User], num: Int): DataFrame = {
    var sim = similarities.get.select("itemId_01", "itemId_02", similarityMeasure)
    val eventsDs = records.get
    val project: DataFrame = users
      .selectExpr("userId as user")
      .join(eventsDs, $"user" <=> eventsDs("userId"), "left")
      .drop($"user")
      .select("userId", "itemId", "view")

    project.join(sim, $"itemId" <=> sim("itemId_01"))
      .selectExpr("userId", "itemId_01 as relatedItem", "itemId_02 as otherItem", similarityMeasure,
        s"$similarityMeasure * view as simProduct")
      .coalesce(defaultParallelism)
      .createOrReplaceTempView("tempTable")

    spark.sql(
      s"""
         | SELECT userId, otherItem,
         | sum(simProduct) / sum($similarityMeasure) as rating
         | FROM tempTable
         | GROUP BY userId, otherItem
         | ORDER BY userId asc, rating desc
       """.stripMargin)
      .rdd
      .map(row => (row.getInt(0), (row.getInt(1), row.getDouble(2))))
      .groupByKey()
      .mapValues(xs => {
        var sequence = Seq[(Int, Double)]()
        val iter = xs.iterator
        var count = 0
        while (iter.hasNext && count < num) {
          val rat = iter.next()
          if (rat._2 ! = Double.NaN) {
            sequence :+= (rat._1, rat._2)
          }
          count +=1
        }
        sequence
      }).toDF("userId", "recommended")
  }
}

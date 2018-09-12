package com.stayrascal.recom.cf

import java.util.Calendar

import com.stayrascal.recom.cf.entities.{History, UserCompPair}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

object LinearItemCFModel {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession.builder()
      .appName("Linear Item CF Model")
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
    val users = spark.createDataset(Seq(UserCompPair(userId, compId)))
    val start = Calendar.getInstance().getTimeInMillis
    recommender.recommendForUser(users, 3).show(truncate = false)
    val end = Calendar.getInstance().getTimeInMillis
    println(s"Total cost: ${(end - start) / 1000f} s")
  }

}

class LinearItemCFModel(spark: SparkSession, similarityMeasure: String) extends Serializable {
  def this(spark: SparkSession) = this(spark, "cooc")

  val defaultParallelism: Int = spark.sparkContext.defaultParallelism

  var history: Option[Dataset[History]] = None
  var similarities: Option[DataFrame] = None

  def getSimilarities: Option[DataFrame] = similarities

  import spark.implicits._

  private def reverseSimilaritiesAndUnion(sim: DataFrame): DataFrame = {
    sim.cache()
    val finalSim = sim.map(row => {
      (row.getInt(0), row.getInt(2), row.getInt(1), row.getDouble(3))
    }).toDF("compId", "followCompId1", "followCompId2", "cooc")
      .union(sim)
      .coalesce(defaultParallelism)
      .cache()

    sim.unpersist()
    finalSim
  }

  def fit(history: Dataset[History]): LinearItemCFModel = {
    this.history = Option(history)

    val numRaters = history.groupBy("compId", "followCompId").count()
      .toDF("compId", "followCompId", "numRaters")
      .coalesce(defaultParallelism)

    val historyWithSize = history.join(numRaters, Seq("compId", "followCompId"))
      .select("userId", "compId", "followCompId", "numRaters")
      .coalesce(defaultParallelism)

    historyWithSize.join(historyWithSize, Seq("userId", "compId"))
      .toDF("userId", "compId", "followCompId1", "numRaters1", "followCompId2", "numRaters2")
      .where($"followCompId1" <= $"followCompId2")
      .coalesce(defaultParallelism)
      .createOrReplaceTempView("joined")

    val sparseMatrix = spark.sql(
      """
        |SELECT compId,
        |followCompId1,
        |followCompId2,
        |count(userId) as size,
        |first(numRaters1) as numRaters1,
        |first(numRaters2) as numRaters2
        |FROM joined
        |GROUP BY compId, followCompId1, followCompId2
      """.stripMargin
    ).coalesce(defaultParallelism)

    val sim = sparseMatrix.map(row => {
      val compId = row.getInt(0)
      val followCompId1 = row.getInt(1)
      val followCompId2 = row.getInt(2)
      val size = row.getLong(3)
      val numRaters1 = row.getLong(4)
      val numRaters2 = row.getLong(5)

      val cooc = coocurrence(size, numRaters1, numRaters2)
      (compId, followCompId1, followCompId2, cooc)
    }).toDF("compId", "followCompId1", "followCompId2", "cooc")
      .coalesce(defaultParallelism)

    similarities = Option(reverseSimilaritiesAndUnion(sim))
    this
  }

  def recommendForUser(users: Dataset[UserCompPair], num: Int): DataFrame = {
    val history = this.history.get
    val sim = similarities.get
      .select("compId", "followCompId1", "followCompId2", similarityMeasure)

    val project = users
      .join(history, Seq("userId", "compId"))
      .selectExpr("userId", "compId", "followCompId as followCompId1", "count")
      .coalesce(defaultParallelism)

    project.join(sim, Seq("compId", "followCopId1"))
      .selectExpr("userId", "compId", "followCompId1", "followCompId2", similarityMeasure, s"$similarityMeasure * count as simProduct")
      .coalesce(defaultParallelism)
      .createOrReplaceTempView("tempView")

    spark.sql(
      s"""
         |SELECT userId, compId, followCompId2 as otherCompId, sum(simProduct) / sum($similarityMeasure) as count
         |FROM tempView
         |GROUP BY userId, compId, followCompId2
         |ORDER BY userId asc, count desc
       """.stripMargin)
      .rdd
      .map(row => ((row.getInt(0), row.getInt(1)), (row.getInt(2), row.getDouble(3))))
      .groupByKey()
      .coalesce(defaultParallelism)
      .mapValues(xs => {
        var sequence = Seq[(Int, Double)]()
        val iter = xs.iterator
        var count = 0
        while (iter.hasNext && count < num) {
          val rat = iter.next()
          if (rat._2 != Double.NaN) {
            sequence :+= (rat._1, rat._2)
          }
          count += 1
        }
        sequence
      })
      .flatMap(xs => xs._2.map(xs2 => (xs._1._1, xs._1._2, xs2._1, xs2._2)))
      .toDF("userId", "compId", "followCompId", "prediction")
  }
}

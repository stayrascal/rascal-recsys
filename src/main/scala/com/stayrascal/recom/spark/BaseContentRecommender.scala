package com.stayrascal.recom.spark

import com.hankcs.hanlp.HanLP
import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Dataset, SparkSession}

import scala.util.control.Breaks

object BaseContentRecommender {

  case class Movie(id: Int, title: String, genres: Seq[String])

  case class User(id: Int, gender: String, age: Int, occupation: Int, zipCode: String)

  case class Tag(userId: Int, movieId: Int, tag: String, timestamp: Long)

  def parseMovie(str: String): Movie = {
    val fields = str.split("::")
    assert(fields.size == 3)
    Movie(fields(0).toInt, fields(1), fields(2).split("\\|").toSeq.take(10))
  }

  def parseUser(str: String): User = {
    val fields = str.split("::")
    assert(fields.size == 5)
    User(fields(0).toInt, fields(1), fields(2).toInt, fields(3).toInt, fields(4))
  }

  def parseRating(str: String): Rating = {
    val fields = str.split("::")
    assert(fields.size == 4)
    Rating(fields(0).toInt, fields(1).toInt, fields(2).toInt)
  }

  val spark = SparkSession.builder()
    .appName("Base content Recommend")
    .master("local[*]")
    .getOrCreate()

  import spark.implicits._

  def main(args: Array[String]): Unit = {
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

    val (movieDs, userDs, ratingDs, tagDs) = readData

    val tagsStandardize: Dataset[(Int, String)] = standardizeTag(tagDs)
    val movieTag: RDD[(Int, Map[(Int, String), Int])] = handMovieTag(tagsStandardize)


    import scala.collection.JavaConversions._
    val moviesGenresTitleYear = movieDs.rdd.map { item =>
      val titleWords = HanLP.extractKeyword(item.title, 10).toList
      val year = MovieYearRegex.movieYearReg(item.title)
      (item.id, (item.genres, titleWords, year))
    }


    val movieAvgRate: Dataset[(Int, Double)] = ratingDs
      .groupBy($"product")
      .agg(avg($"rating"))
      .map { row => (row.get(0).toString.toInt, row.get(1).toString.toDouble) }

    movieAvgRate.show()
    movieTag.toDF().show()
    // movieTag: (movieId, Map[(movieId, tag), count])
    // movieAvgRate: (movieId, avgRate)
    // moviesGenresTitleYear: (movieId, (genres, titleList, year))
    val movieContent = movieTag.join(movieAvgRate.rdd).join(moviesGenresTitleYear)
      .map { f =>
        val movieId: Int = f._1
        val tagMap: Map[(Int, String), Int] = f._2._1._1
        val avgRate: Double = f._2._1._2
        val genres: Seq[String] = f._2._2._1
        val titleWords = f._2._2._2
        val year: Int = f._2._2._3
        (movieId, tagMap, titleWords, year, genres, avgRate)
      }

    val movieContentTmp = movieContent.filter(_._6.asInstanceOf[java.math.BigDecimal].doubleValue() >= 3.5).collect()
    movieContent.map { movie =>
      val currentMovieId = movie._1
      val currentTagMap = movie._2
      val currentTitleWords = movie._3
      val currentYear = movie._4
      val currentGenresList = movie._5
      val currentAvgRate = movie._6.asInstanceOf[java.math.BigDecimal].doubleValue()
      movieContentTmp.map { recommendMovie =>

      }

    }

    spark.stop()


    /*movieDs
      .filter(_.id != null)
      .map { movie =>
        val titleWords = HL
      }*/
  }

  private def standardizeTag(tagDs: Dataset[Tag]) = {
    val tagsStandardize: Dataset[(Int, String)] = tagDs
      .map { tagObj =>
        val tag = if (tagObj.tag.split(" ").length <= 3) {
          tagObj.tag
        } else {
          HanLP.extractKeyword(tagObj.tag, 20).toArray().toSet.mkString(" ")
        }
        (tagObj.movieId, tag)
      }
    tagsStandardize
  }

  private def readData = {
    val dataHomeDir = "/Users/zpwu/workspace/scala/rascal-recsys/src/main/resources/"
    val movieDs: Dataset[Movie] = spark.read.textFile(dataHomeDir + "movies.dat").map(parseMovie).cache()
    val userDs: Dataset[User] = spark.read.textFile(dataHomeDir + "users.dat").map(parseUser).cache()
    val ratingDs: Dataset[Rating] = spark.read.textFile(dataHomeDir + "ratings.dat").map(parseRating).cache()
    val tagDs: Dataset[Tag] = spark.read
      .option("header", true)
      .option("charset", "UTF8")
      .option("delimiter", ",")
      .option("inferSchema", true)
      .csv(dataHomeDir + "tags.csv").as[Tag]

    /*movieDs.printSchema()
    userDs.printSchema()
    ratingDs.printSchema()
    tagDs.printSchema()*/
    (movieDs, userDs, ratingDs, tagDs)
  }

  /**
    * 计算两个词的编辑距离,当前缀重叠度大于等于2/7时，进行字符串合并，从长的往短的合并
    *
    * @param str1 str1
    * @param str2 str2
    * @return
    */
  def getEditSize(str1: String, str2: String) = {
    if (str2.length > str1.length) {
      0
    } else {
      var count = 0
      val loop = new Breaks
      //以较短的str2为中心，进行遍历，并逐个比较字符
      val str2Length = str2.getBytes().length
      var i = 0
      for (i <- 0 until str2Length) {
        if (str2.getBytes()(i) == str1.getBytes()(i)) {
          count += 1
        } else {
          loop.break()
        }
      }
      //计算重叠度,当前缀重叠度大于等于2/7时，进行字符串合并，从长的往短的合并
      if (count.asInstanceOf[Double] / str1.getBytes().length.asInstanceOf[Double] >= (1 - 0.286)) {
        1
      } else {
        0
      }
    }
  }

  /**
    * 很多 tag 其实说的是同一个东西，我们需要进行一定程度上的合并
    *
    * @param tagsStandardize (movieId, tag)
    * @return (movieId, Map(tag, count))
    */
  private def handMovieTag(tagsStandardize: Dataset[(Int, String)]) = {
    val tagsStandardizeTmp = tagsStandardize.collect()
    val tagSimi: Dataset[((Int, String), Int)] = tagsStandardize
      .map { item =>
        var retTag: String = item._2
        if (item._2.toString.split(" ").size == 1) {
          var simiTmp = ""
          val tagsTmpStand = tagsStandardizeTmp
            //            .filter(_._2.split(" ").size != 1)
            .filter(item._2.toString.size > _._2.toString.size)
            .sortBy(_._2.toString.size)
          val loop = new Breaks
          tagsTmpStand.foreach { tagTmp =>
            val flag = getEditSize(item._2.toString, tagTmp._2.toString)
            if (flag == 1) {
              retTag = tagTmp._2
              loop.break()
            }
          }
          ((item._1, retTag), 1)
        } else {
          ((item._1, item._2), 1)
        }
      }
    print(tagSimi.count())

    tagSimi.rdd.takeSample(false, 10).foreach(println)

    val movieTag: RDD[(Int, Map[(Int, String), Int])] = tagSimi.rdd
      .reduceByKey(_ + _)
      .groupBy(key => key._1._1)
      .map { f =>
        (f._1, f._2.map(ff => (ff._1, ff._2)).toList.sortBy(_._2).reverse.take(10).toMap)
      }

    movieTag
  }
}

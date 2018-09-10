package com.stayrascal.recom.spark

import com.hankcs.hanlp.HanLP
import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Dataset, SparkSession}

object BaseContentRecommender {

  case class Movie(id: Int, title: String, genres: Seq[String])

  case class User(id: Int, gender: String, age: Int, occupation: Int, zipCode: String)

  case class Tag(userId: Int, movieId: Int, tag: String, timestamp: Long)

  val spark = SparkSession.builder()
    .appName("Base content Recommend")
    .master("local[*]")
    .config("spark.executor.memory", "2g")
    .config("spark.driver.memory", "4g")
    .getOrCreate()

  import spark.implicits._

  def main(args: Array[String]): Unit = {
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

    val (movieDs, userDs, ratingDs, tagDs) = readData

    val tagsStandardize: Dataset[(Int, String)] = standardizeTag(tagDs)
    val movieTag: RDD[(Int, Map[(Int, String), Int])] = handMovieTag(tagsStandardize)
    val moviesGenresTitleYear: RDD[(Int, (Seq[String], List[String], Int))] = extractMovieGenres(movieDs)
    val movieAvgRate: Dataset[(Int, Double)] = extractMovieAvgRate(ratingDs)
    val movieContent = joinMovieContent(movieTag, movieAvgRate.rdd, moviesGenresTitleYear)
    val movieContentBase: RDD[(Int, Int, Double)] = calculateCandidateMovie(movieContent)

    println(movieContentBase.count())
    movieContentBase.toDF().show()

    // TODO save result to redis
    spark.stop()
  }

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

  def getCosList(strSeq1: Seq[String], strSeq2: Seq[String]): Double = {
    getCosList(strSeq1.toList, strSeq2.toList)
  }

  def getCosList(strList1: List[String], strList2: List[String]): Double = {
    var xySum: Double = 0
    var aSquareSum: Double = 0
    var bSquareSum: Double = 0

    strList1.union(strList2).foreach { str =>
      if (strList1.contains(str)) aSquareSum += 1
      if (strList2.contains(str)) bSquareSum += 1
      if (strList1.contains(str) && strList2.contains(str)) xySum += 1
    }

    if (aSquareSum != 0 && bSquareSum != 0) {
      xySum / (Math.sqrt(aSquareSum) * Math.sqrt(bSquareSum))
    } else {
      0d
    }
  }

  def getYearSimilarity(year1: Int, year2: Int): Double = {
    val count = Math.abs(year1 - year2)
    if (count > 10) 0 else (1 - count / 10)
  }

  def getRateSimilarity(rate: Double): Double = {
    if (rate >= 5) 1 else rate / 5
  }

  def getCosTags(tagMap1: Map[(Int, String), Int], tagMap2: Map[(Int, String), Int]): Double = {
    var xySum: Double = 0
    var aSquareSum: Double = 0
    var bSquareSum: Double = 0

    val tagsA = tagMap1.keys.toList
    val tagsB = tagMap2.keys.toList

    tagsA.union(tagsB).foreach { tag =>
      if (tagsA.contains(tag)) aSquareSum += tagMap1(tag) * tagMap1(tag)
      if (tagsB.contains(tag)) bSquareSum += tagMap2(tag) * tagMap2(tag)
      if (tagsA.contains(tag) && tagsB.contains(tag)) xySum += tagMap1(tag) + tagMap2(tag)
    }

    if (aSquareSum != 0 && bSquareSum != 0) {
      xySum / (Math.sqrt(aSquareSum) * Math.sqrt(bSquareSum))
    } else {
      0d
    }
  }

  private def calculateCandidateMovie(movieContent: RDD[(Int, Map[(Int, String), Int], List[String], Int, Seq[String], Double)]) = {
    val movieContentTmp = movieContent.filter(_._6 >= 3.5).collect()

    println("Total movie count: " + movieContent.count())
    println("High Quality movie count: " + movieContentTmp.length)

    val movieContentBase = movieContent.map { movie =>
      val currentMovieId = movie._1
      val currentTag = movie._2
      val currentTitleWords = movie._3
      val currentYear = movie._4
      val currentGenresList = movie._5
      val currentAvgRate = movie._6
      val recommendMovies = movieContentTmp
        .filter(m => m._1 != currentMovieId)
        .map { recommendMovie =>
          val simiTag = getCosTags(currentTag, recommendMovie._2)
          val simiTitle = getCosList(currentTitleWords, recommendMovie._3)
          val simiGenre = getCosList(currentGenresList, recommendMovie._5)
          val simiYear = getYearSimilarity(currentYear, recommendMovie._4)
          val simiRate = getRateSimilarity(recommendMovie._6)
          val score = 0.4 * simiGenre + 0.25 * simiTag + 0.1 * simiYear + 0.05 * simiTitle + 0.2 * simiRate
          (recommendMovie._1, score)
        }.toList.sortBy(_._2).reverse.take(20)
      (currentMovieId, recommendMovies)
    }.flatMap(tup => tup._2.map(k => (tup._1, k._1, k._2)))
    movieContentBase
  }

  /**
    *
    * @param movieTag              (movieId, Map[(movieId, tag), count])
    * @param movieAvgRate          (movieId, avgRate)
    * @param moviesGenresTitleYear (movieId, (genres, titleList, year))
    * @return
    */
  private def joinMovieContent(movieTag: RDD[(Int, Map[(Int, String), Int])],
                               movieAvgRate: RDD[(Int, Double)],
                               moviesGenresTitleYear: RDD[(Int, (Seq[String], List[String], Int))]) = {
    val movieContent = movieTag.join(movieAvgRate).join(moviesGenresTitleYear)
      .map { f =>
        val movieId: Int = f._1
        val tags = f._2._1._1
        val avgRate: Double = f._2._1._2
        val genres: Seq[String] = f._2._2._1
        val titleWords = f._2._2._2
        val year: Int = f._2._2._3
        (movieId, tags, titleWords, year, genres, avgRate)
      }
    movieContent
  }

  private def extractMovieAvgRate(ratingDs: Dataset[Rating]) = {
    val movieAvgRate: Dataset[(Int, Double)] = ratingDs
      .groupBy($"product")
      .agg(avg($"rating"))
      .map { row => (row.get(0).toString.toInt, row.get(1).toString.toDouble) }
    movieAvgRate
  }

  private def extractMovieGenres(movieDs: Dataset[Movie]) = {
    import scala.collection.JavaConversions._
    val moviesGenresTitleYear = movieDs.rdd.map { item =>
      val titleWords = HanLP.extractKeyword(item.title, 10).toList
      val year = MovieYearRegex.movieYearReg(item.title)
      (item.id, (item.genres, titleWords, year))
    }
    moviesGenresTitleYear
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
    // TODO Read data from Hbase
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
      val samePrefix = str2.zip(str1).takeWhile(Function.tupled(_ == _)).map(_._1).mkString
      val count = samePrefix.length

      //计算重叠度,当前缀重叠度大于等于2/7时，进行字符串合并，从长的往短的合并
      if (count.asInstanceOf[Double] / str1.getBytes().length.asInstanceOf[Double] >= (1 - 0.286)) {
        1
      } else {
        0
      }
    }
  }

  private def handleMovieTag(tags: Dataset[(Int, String)]) = {
    val movieTag = tags.map(tag => ((tag._1, tag._2), 1)).rdd
      .reduceByKey(_ + _)
      .groupBy(_._1._1)
      .map { f =>
        (f._1, f._2.map(ff => (ff._1, ff._2)).toList.sortBy(_._2).reverse.take(10).toMap)
      }

  }

  /**
    * 很多 tag 其实说的是同一个东西，我们需要进行一定程度上的合并
    *
    * @param tagsStandardize (movieId, tag)
    * @return (movieId, Map(tag, count))
    */
  private def handMovieTag(tagsStandardize: Dataset[(Int, String)]) = {
    /*val tagsStandardizeTmp = tagsStandardize.collect()
    val tagSimi: Dataset[((Int, String), Int)] = tagsStandardize
      .map { item =>
        var retTag: String = item._2
        if (item._2.toString.split(" ").size == 1) {
          var simiTmp = ""
          val tagsTmpStand = tagsStandardizeTmp
            //            .filter(_._2.split(" ").size != 1)
            .filter(item._2.toString.size > _._2.toString.size)
            .sortBy(_._2.toString.size)

          retTag = tagsTmpStand
            .filter(tagTmp => getEditSize(item._2.toString, tagTmp._2.toString) == 1)
            .headOption
            .map(_._2)
            .getOrElse(item._2)
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
      }*/

    val movieTag = tagsStandardize.map(tag => ((tag._1, tag._2), 1)).rdd
      .reduceByKey(_ + _)
      .groupBy(_._1._1)
      .map { f =>
        (f._1, f._2.map(ff => (ff._1, ff._2)).toList.sortBy(_._2).reverse.take(10).toMap)
      }
    movieTag
  }
}

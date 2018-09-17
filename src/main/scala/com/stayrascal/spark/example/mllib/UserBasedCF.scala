package com.stayrascal.spark.example.mllib

import java.io.File

import org.apache.spark.mllib.linalg.distributed.{CoordinateMatrix, MatrixEntry}
import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.sql.SparkSession

object UserBasedCF {
  private def readData(spark: SparkSession) = {
    val movieLensHomeDir = "/Users/zpwu/workspace/scala/rascal-recsys/src/main/resources"

    val ratings = spark.sparkContext.textFile(new File(movieLensHomeDir, "ratings.dat").toString).map { line =>
      val fields = line.split("::")
      //timestamp, user, product, rating
      (fields(3).toLong % 10, Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble))
    }
    val movies = spark.sparkContext.textFile(new File(movieLensHomeDir, "movies.dat").toString).map { line =>
      val fields = line.split("::")
      //movieId, movieName
      (fields(0).toInt, fields(1))
    }.collectAsMap()

    val numRatings = ratings.count()
    val numUser = ratings.map(x => x._2.user).distinct().count()
    val numMovie = ratings.map(_._2.product).distinct().count()

    println("Got " + numRatings + " ratings from " + numUser + " users on " + numMovie + " movies.")
    (ratings, movies)
  }

  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession.builder()
      .master("local[*]")
      .getOrCreate()

    val (ratingsRDD, movies) = readData(spark)

    val ratingData = ratingsRDD.map(rate => MatrixEntry(rate._2.user - 1, rate._2.product - 1, rate._2.rating))
    val ratings = new CoordinateMatrix(ratingData)

    val matrix = ratings.transpose.toRowMatrix
    val exactSimilarities = matrix.columnSimilarities()
    val approxSimilarities = matrix.columnSimilarities(0.1)

    val exactEntries = exactSimilarities.entries.map { case MatrixEntry(i, j, u) => ((i, j), u) }
    val approxEntries = approxSimilarities.entries.map { case MatrixEntry(i, j, u) => ((i, j), u) }

    val MAE = exactEntries.leftOuterJoin(approxEntries).values.map {
      case (u, Some(v)) =>
        math.abs(u - v)
      case (u, None) =>
        math.abs(u)
    }.mean()

    println(s"Average absolute error in estimate is: $MAE")


    val ratingOfUser1 = exactSimilarities.entries.take(1).map(_.value)
    val avgRatingOfUser1: Double = ratingOfUser1.sum / ratingOfUser1.size

    val ratingToItem1 = matrix.rows.take(1)(0)
    val weights: Array[Double] = approxSimilarities.entries.filter(_.i == 0).sortBy(_.j).map(_.value).collect()
    var weighted: Double = (0 to 2).map(t => weights(t) * ratingToItem1(t)).sum / weights.sum
    println("The prediction of user1 for item1: " + (avgRatingOfUser1 + weighted))

    spark.stop()

  }
}

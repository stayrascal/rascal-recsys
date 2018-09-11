package com.stayrascal.recom.spark

import java.io.File

import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.util.Random

object ALSCFRecommender {

  def main(args: Array[String]): Unit = {
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

    val spark = SparkSession.builder()
      .appName("MovieLens ALS")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    val (
      ratings: RDD[(Long, Rating)],
      movies: collection.Map[Int, String]) = readData(spark)

    val (
      trainSet: RDD[Rating],
      validationSet: RDD[Rating],
      testSet: RDD[Rating]) = splitData(spark, ratings, movies)

    var (
      bestModel: Option[MatrixFactorizationModel],
      bestRanks: Int,
      bestIters: Int,
      bestLambdas: Double) = trainBestModel(trainSet, validationSet)

    val testRmse = computeRmse(bestModel.get, testSet)
    println(s"The best model was trained with rank=$bestRanks, Iter=$bestIters, Lambda=$bestLambdas and compute RMSE on test is $testRmse")

    compareToBaseline(trainSet, validationSet, testSet, testRmse)

    val recommendResult = recommendMovies(spark, ratings.map(_._2), movies, bestModel)
    recommendResult.toDF().show()
  }

  private def computeRmse(model: MatrixFactorizationModel, data: RDD[Rating]) = {
    val prediction: RDD[Rating] = model.predict(data.map(x => (x.user, x.product)))
    val predDataJoined = prediction
      .map(x => ((x.user, x.product), x.rating))
      .join(data.map(x => ((x.user, x.product), x.rating)))
      .values
    new RegressionMetrics(predDataJoined).rootMeanSquaredError
  }

  private def trainBestModel(trainSet: RDD[Rating], validationSet: RDD[Rating]) = {
    var bestRmse = Double.MaxValue
    var bestModel: Option[MatrixFactorizationModel] = None
    var bestRanks = -1
    var bestIters = 0
    var bestLambdas = -1.0

    val numRanks = List(8, 12)
    val numIters = List(10, 20)
    val numLambdas = List(0.1, 10.0)
    for (rank <- numRanks; iter <- numIters; lambda <- numLambdas) {
      val model = ALS.train(trainSet, rank, iter, lambda)
      val validationRmse = computeRmse(model, validationSet)
      println("RMSE(validation) = " + validationRmse + " with ranks=" + rank + ", iter=" + iter + ", Lambda=" + lambda)

      if (validationRmse < bestRmse) {
        bestModel = Some(model)
        bestRmse = validationRmse
        bestIters = iter
        bestLambdas = lambda
        bestRanks = rank
      }
    }
    (bestModel, bestRanks, bestIters, bestLambdas)
  }

  private def compareToBaseline(trainSet: RDD[Rating], validationSet: RDD[Rating], testSet: RDD[Rating], testRmse: Double) = {
    val meanRating = trainSet.union(validationSet).map(_.rating).mean()
    val bestlineRmse = new RegressionMetrics(testSet.map(x => (x.rating, meanRating))).rootMeanSquaredError
    val improvement = (bestlineRmse - testRmse) / bestlineRmse * 100
    println("The best model improves the baseline by " + "%1.2f".format(improvement) + "%.")
  }

  private def recommendMovies(spark: SparkSession,
                              ratingRDD: RDD[Rating],
                              movies: collection.Map[Int, String],
                              bestModel: Option[MatrixFactorizationModel]) = {

    val userMoviesMap = ratingRDD
      .groupBy(_.user)
      .map(item => (item._1, item._2.map(_.product).toSet))
      .collect()
      .toMap

    val recommendMovies = ratingRDD
      .map(_.user)
      .distinct()
      .collect()
      .map { user =>
        val oldMovies = userMoviesMap.getOrElse(user, scala.collection.immutable.Set.empty)
        val candidates: RDD[Int] = spark.sparkContext.parallelize(movies.keys.filter(!oldMovies.contains(_)).toSeq)
        val recommendations = bestModel.get
          .predict(candidates.map(x => (user, x)))
          .sortBy(-_.rating)
          .take(20)
        (user, recommendations)
      }.flatMap(f => f._2.map(ff => (f._1, ff.product, ff.rating / 5)))
    spark.sparkContext.parallelize(recommendMovies)
  }

  private def splitData(spark: SparkSession, ratings: RDD[(Long, Rating)], movies: collection.Map[Int, String]) = {
    val topMovies = ratings.map(_._2.product).countByValue().toSeq.sortBy(-_._2).take(50).map(_._1)
    val random = new Random(0)
    val selectMovies = topMovies.filter(x => random.nextDouble() < 0.2).map(x => (x, movies(x)))

    val numPartitions = 10
    val trainSet = ratings.filter(x => x._1 < 6).map(_._2).repartition(numPartitions).persist()
    val validationSet = ratings.filter(x => x._1 >= 6 && x._1 < 8).map(_._2).persist()
    val testSet = ratings.filter(x => x._1 >= 8).map(_._2).persist()

    val numTrain = trainSet.count()
    val numValidation = validationSet.count()
    val numTest = testSet.count()

    println("Training data: " + numTrain + " Validation data: " + numValidation + " Test data: " + numTest)
    (trainSet, validationSet, testSet)
  }

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
}

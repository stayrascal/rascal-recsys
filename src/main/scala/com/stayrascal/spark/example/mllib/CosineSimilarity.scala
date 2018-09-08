package com.stayrascal.spark.example.mllib

import org.apache.spark.mllib.linalg.distributed.{MatrixEntry, RowMatrix}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import scopt.OptionParser

object CosineSimilarity {

  case class Params(inputFile: String = null, threshold: Double = 0.1) extends AbstractParams[Params]

  def main(args: Array[String]): Unit = {
    val defaultParams = Params()

    val parser = new OptionParser[Params]("CosineSimilarity") {
      head("CosineSimilarity: an example app.")
      opt[Double]("threshold")
        .required()
        .text(s"threshold similarity: to trade off computation vs quality estimate")
        .action((x, c) => c.copy(threshold = x))
      arg[String]("<inputFile>")
        .required()
        .text(s"input file, one row per line, space-separated")
        .action((x, c) => c.copy(inputFile = x))
      note(
        """
          |For example, the following command runs this app on a dataset:
          |
          | ./bin/spark-submit  --master local --class com.stayrascal.spark.example.mllib.CosineSimilarity \
          | target/scala-2.11/rascal-recsys-assembly-0.0.1.jar \
          | --threshold 0.1 /Users/zpwu/workspace/bigdata/data-platform/spark/data/mllib/sample_svm_data.txt
        """.stripMargin)
    }

    parser.parse(args, defaultParams).map { params =>
      run(params)
    } getOrElse {
      System.exit(1)
    }
  }

  def run(params: Params) {
    val spark = SparkSession.builder()
      .appName("CosineSimilarity")
      .getOrCreate()

    // Load and parse the data file.
    val rows: RDD[Vector] = spark.sparkContext.textFile(params.inputFile).map { line =>
      val values = line.split(' ').map(_.toDouble)
      Vectors.dense(values)
    }.cache()

    val mat = new RowMatrix(rows)

    // Compute similar columns perfectly, with brute force.
    val exact = mat.columnSimilarities()

    // Compute similar columns with estimation using DIMSUM
    val approx = mat.columnSimilarities(params.threshold)

    val exactEntries = exact.entries.map { case MatrixEntry(i, j, u) => ((i, j), u) }
    val approxEntries = approx.entries.map { case MatrixEntry(i, j, v) => ((i, j), v) }
    val MAE = exactEntries.leftOuterJoin(approxEntries).values.map {
      case (u, Some(v)) =>
        math.abs(u - v)
      case (u, None) =>
        math.abs(u)
    }.mean()

    println(s"Average absolute error in estimate is: $MAE")

    spark.stop()
  }

}

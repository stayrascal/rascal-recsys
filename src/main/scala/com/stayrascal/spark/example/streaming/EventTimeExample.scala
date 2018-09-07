package com.stayrascal.spark.example.streaming

import java.sql.Timestamp

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.OutputMode

object EventTimeExample {

  case class Stock(time: Timestamp, symbol: String, value: Double)

  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession.builder()
      .master("local[*]")
      .appName("Event Time Example")
      .getOrCreate()

    import spark.implicits._
    spark.sparkContext.setLogLevel("ERROR")

    val socketStreamDs = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 5050)
      .load()
      .as[String]

    val stockDs = socketStreamDs.map(value => {
      val columns = value.split(",")
      Stock(new Timestamp(columns(0).toLong), columns(1), columns(2).toDouble)
    })

    val windowedCount = stockDs.groupBy(window($"time", "10 seconds")).sum("value")

    val query = windowedCount.writeStream
      .format("console")
      .option("truncate", "false")
      .outputMode(OutputMode.Complete())

    query.start().awaitTermination()
  }

}

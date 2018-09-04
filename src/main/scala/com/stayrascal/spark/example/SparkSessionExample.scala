package com.stayrascal.spark.example

import org.apache.spark.sql.SparkSession

object SparkSessionExample {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder
      .master("local[*]")
      .appName("spark session example")
      .getOrCreate()

    val df = spark.read
      .option("header", "true")
      .csv("src/main/resources/sales.csv")

    df.show()
  }
}

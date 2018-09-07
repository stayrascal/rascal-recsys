package com.stayrascal.spark.example

import org.apache.spark.sql.SparkSession

case class Sales(transactionId: Int, customerId: Int, itemId: Int, amountPaid: Double)

object SparkSessionExample {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder
      .master("local[*]")
      .appName("spark session example")
      .getOrCreate()
    import spark.implicits._

    val df = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv("src/main/resources/sales.csv")

    val ds = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv("src/main/resources/sales.csv")
      .as[Sales]

    val selectedDF = df.select("itemId")

    val selectedDS = ds.map(_.itemId)

    println(selectedDF.queryExecution.optimizedPlan.numberedTreeString)

    println(selectedDS.queryExecution.optimizedPlan.numberedTreeString)
  }
}

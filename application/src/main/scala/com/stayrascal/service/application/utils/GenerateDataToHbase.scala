package com.stayrascal.service.application.utils

import com.stayrascal.recom.cf.TestDataGenerator
import com.stayrascal.recom.cf.entities.Component
import com.stayrascal.service.application.constraints.Schemas.{HBaseComponentSchema, HBaseHistorySchema, HBaseUsersSchema}
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.phoenix.spark._
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.JavaConverters._

object GenerateDataToHbase {
  val spark: SparkSession = SparkSession.builder()
    .master("local[*]")
    .getOrCreate()

  val conf = HBaseConfiguration.create()

  import spark.implicits._

  def generateRecordByExistComponents(compsTable: DataFrame,
                                      numCompTuples: Int,
                                      numUsers: Int,
                                      numHistory: Int,
                                      maxCount: Float): (DataFrame, DataFrame, DataFrame) = {
    val users = TestDataGenerator.generateUsers(numUsers)
    val comps = compsTable.map(row => Component(row.getInt(0), row.getString(1))).collectAsList()
    val compTuples = TestDataGenerator.generateComponentTuples(numCompTuples, comps.asScala.toIndexedSeq)
    val history = TestDataGenerator.generateHistoryWithName(numHistory, maxCount, users, compTuples)
    (
      users.toDF("ID", "UUID"),
      compsTable,
      history.map(record => (record.user, record.comp, record.followComp, record.count.toInt)).toDF("USERNAME", "COMPNAME", "FOLLOWCOMPNAME", "FREQ")
    )
  }

  def main(args: Array[String]): Unit = {
    val compsTable = spark.sqlContext.phoenixTableAsDataFrame(
      HBaseComponentSchema.TABLE_NAME,
      Seq(HBaseComponentSchema.ID_QUALIFIER, HBaseComponentSchema.NAME_QUALIFIER),
      conf = conf).coalesce(1)
    val tables = generateRecordByExistComponents(compsTable, 300, 100, 1000, 5f)
    saveToHbase(tables)
  }

  def saveToHbase(tables: (DataFrame, DataFrame, DataFrame)): Unit = {
    val conf = HBaseConfiguration.create()
    tables._1.toDF("ID", "UUID").saveToPhoenix(HBaseUsersSchema.TABLE_NAME, conf = conf)
    tables._3.toDF("USERNAME", "COMPNAME", "FOLLOWCOMPNAME", "FREQ").saveToPhoenix(HBaseHistorySchema.TABLE_NAME, conf = conf)
  }

}

package com.stayrascal.service.application.utils

import com.stayrascal.service.application.constraints.Schemas.{HBaseThesaurusBelongSchema, HBaseThesaurusGroupSchema}
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.phoenix.spark._
import org.apache.spark.sql.SparkSession

object ExportThesaurusToCSV {

  val sparkSession: SparkSession = SparkSession.builder()
    .master("local[*]")
    .getOrCreate()

  val conf = HBaseConfiguration.create()

  def main(args: Array[String]): Unit = {
    sparkSession.sqlContext.phoenixTableAsDataFrame(
      HBaseThesaurusGroupSchema.TABLE_NAME
      , Seq(HBaseThesaurusGroupSchema.GROUPID_QUALIFIER, HBaseThesaurusGroupSchema.SYNONYMS_QUALIFIER)
      , conf = conf).coalesce(1).write.csv("./example/dict/thesaurus/group")
    sparkSession.sqlContext.phoenixTableAsDataFrame(
      HBaseThesaurusBelongSchema.TABLE_NAME
      , Seq(HBaseThesaurusBelongSchema.WORD_QUALIFIER, HBaseThesaurusBelongSchema.GROUPID_QUALIFIER)
      , conf = conf).coalesce(1).write.csv("./example/dict/thesaurus/belong")
  }
}

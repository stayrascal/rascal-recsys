package com.stayrascal.service.application.config

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.springframework.context.annotation.{Bean, Configuration}

@Configuration
class SparkCustomConfiguration {
  @Bean
  def sparkConf(): SparkConf = {
    new SparkConf()
      .setMaster("local[*]")
      .setAppName("Recommender System")
      .set("spark.driver.allowMutipleContexts", "true")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.kryo.registrator", "com.stayrascal.service.application.config.MyKyroRegister")
  }

  @Bean
  def sparkSession(): SparkSession = {
    SparkSession.builder().config(sparkConf()).getOrCreate()
  }
}

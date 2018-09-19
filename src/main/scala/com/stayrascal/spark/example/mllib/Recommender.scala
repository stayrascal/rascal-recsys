package com.stayrascal.spark.example.mllib

import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.rdd.RDD

trait Recommender {

  implicit class AugmentParams(val params: Map[String, Any]) {
    def getInt(key: String) = params(key).asInstanceOf[Number].intValue

    def getDouble(key: String) = params(key).asInstanceOf[Number].doubleValue

    def getBoolean(key: String) = params(key).asInstanceOf[Boolean]

    def getString(key: String) = params(key).toString
  }

  def recommend(trainingSet: RDD[Rating], params: Map[String, Any]): RDD[(Int, Seq[Rating])]
}

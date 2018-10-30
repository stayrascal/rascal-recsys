package com.stayrascal.service.application.prediction

import com.stayrascal.service.application.domain.{Prediction, Recommendation}
import org.apache.spark.sql.DataFrame

trait PredictionService {
  def makePrediction(): DataFrame

  def storePrediction(prediction: DataFrame): Unit

  def getPrediction(userName: String, compName: String, num: Int): java.util.List[Prediction]

  def recommend(userId: Long, itemId: Long, num:Int, measureType: String): java.util.List[Recommendation]

  def init(): Unit

  def clean(): Unit
}

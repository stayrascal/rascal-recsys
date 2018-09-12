package com.stayrascal.service.application.prediction

import com.stayrascal.service.application.domain.Prediction
import org.apache.spark.sql.DataFrame

trait PredictionService {
  def makePrediction(): DataFrame

  def storePrediction(prediction: DataFrame): Unit

  def getPrediction(userName: String, compName: String, num: Int): java.util.List[Prediction]

  def init(): Unit

  def clean(): Unit
}

package com.stayrascal.service.application.streaming

import com.stayrascal.service.application.common.EventFormatUtil
import com.stayrascal.service.application.history.HistoryKafkaProperties
import com.stayrascal.service.application.item.ItemService
import com.stayrascal.service.application.user.UserService
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.{LabeledPoint, StreamingLinearRegressionWithSGD}
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.springframework.beans.factory.annotation.Autowired

class StreamingAlgorithm(@Autowired val sparkSession: SparkSession,
                         @Autowired val properties: HistoryKafkaProperties,
                         @Autowired val userService: UserService,
                         @Autowired val itemService: ItemService) {

  private val ssc = new StreamingContext(sparkSession.sparkContext, Seconds(2))
  private val model: StreamingLinearRegressionWithSGD = null

  def loadDataFromKafka: DStream[String] = {
    ssc.checkpoint(properties.getCheckpointDir)

    KafkaUtils.createDirectStream(
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](properties.getTopics, properties.getKafkaParamsConsumer)
    )
      .map(_.value())
  }

  def onlineTraining = {
    val dataStream = loadDataFromKafka.filter(EventFormatUtil.isValidEvent)
      .map(kv => {
        val parts = kv.split(",")
        val userOpt = userService.selectUserById(parts(0).toInt)
        val itemOpt = itemService.searchItem(parts(1).toLong)

        LabeledPoint(1.0, Vectors.dense(Array(1.0, parts(0).toDouble)))
      })
      .cache()

    model
      .setInitialWeights(Vectors.zeros(2))
      .trainOn(dataStream)
  }

  def predict(userStream: DStream[LabeledPoint]) = {
    model.predictOnValues(userStream.map(lp => (lp.label, lp.features)))
  }
}

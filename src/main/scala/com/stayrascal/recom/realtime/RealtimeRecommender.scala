package com.stayrascal.recom.realtime

import com.alibaba.fastjson.JSON
import com.stayrascal.recom.common.{Constants, RedisUtil}
import kafka.serializer.StringDecoder
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.slf4j.{Logger, LoggerFactory}

object RealtimeRecommender {

  val logger: Logger = LoggerFactory.getLogger(RealtimeRecommender.getClass)

  def main(args: Array[String]): Unit = {
    val Array(brokers, topics) = Array(Constants.KAFKA_ADDR, Constants.KAFKA_TOPICS)

    val spark: SparkSession = SparkSession.builder()
      .master("local[*]")
      .appName("Realtime Recommender")
      .getOrCreate()
    val sparkConf = spark.sparkContext
    val ssc = new StreamingContext(sparkConf, Seconds(2L))

    val topicsSet = topics.split(",").toSet
    val kafkaParams = Map[String, String](
      ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokers,
      "auto.offset.reset" -> "smallest"
    )

    val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topicsSet)

    messages
      .map(_._2)
      .map(event => JSON.parseObject(event, classOf[NewClickEvent]))
      .mapPartitions { iter =>
        val jedis = RedisUtil.getJedis
        iter.map { event =>
          logger.info("NewClickEvent: {}", event)
          val userId = event.getUserId
          val itemId = event.getItemId
          val key = "II:" + itemId
          val value = jedis.get(key)
          jedis.set("RUI:" + userId, value)
          logger.info("Recommend to user: {}, items: {}", userId, value)
        }
      }
      .print()

    ssc.start()
    ssc.awaitTermination()
  }
}
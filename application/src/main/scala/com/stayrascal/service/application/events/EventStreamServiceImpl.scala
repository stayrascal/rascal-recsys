package com.stayrascal.service.application.events

import java.sql.Connection
import java.util.concurrent.CompletableFuture

import com.stayrascal.service.application.common.{EventFormatUtil, PhoenixPool}
import com.stayrascal.service.application.event.EventKafkaProperties
import com.stayrascal.service.application.history.HistoryDBUtil
import com.stayrascal.service.application.repository.EventRepository
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class EventStreamServiceImpl(@Autowired val sparkSession: SparkSession,
                             @Autowired val properties: EventKafkaProperties,
                             @Autowired val eventRepository: EventRepository)
  extends EventStreamService with DisposableBean with Serializable {

  private val ssc = new StreamingContext(sparkSession.sparkContext, Seconds(2))

  private val eventProducer = new KafkaProducer[String, String](properties.getKafkaParamsProducer)

  override def init(): Unit = {
    CompletableFuture.runAsync(new Runnable {
      override def run(): Unit = {
        saveEvent(getEventStream)
        ssc.start()
        ssc.awaitTermination()
      }
    })
  }

  override def addEvent(event: String): Unit = {
    if (!EventFormatUtil.isValidEvent(event)) {
      throw new EventFormatException(s"Invalid format for history string: $event")
    }
    val topic = properties.getTopics.get(0)
    eventProducer.send(new ProducerRecord[String, String](topic, event), new Callback {
      override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {}
    })
  }

  override def getEventStream: DStream[(Long, Long, String, String)] = {
    ssc.checkpoint(properties.getCheckpointDir)
    KafkaUtils.createDirectStream(
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](properties.getTopics, properties.getKafkaParamsConsumer)
    ).map(_.value())
      .filter(EventFormatUtil.isValidateHistory)
      .map(event => {
        val parts = event.split("-")
        ((Long) parts (0), (Long) parts (1), parts(2), parts(4))
      })

  }

  override def saveEvent(eventStream: DStream[(Long, Long, String, String)]): Unit = {
    eventStream.foreachRDD { rdd => {
      rdd.foreachPartition(partitionEvents => {
        if (partitionEvents.nonEmpty) {
          var conn: Option[Connection] = None
          try {
            conn = Option.apply(PhoenixPool.getConnection)
            val stat = conn.get.createStatement()
            conn.get.setAutoCommit(false)
            partitionEvents.foreach(tuple => {
              stat.addBatch(HistoryDBUtil.generateUpsertEventSql(tuple._1, tuple._2, tuple._3, tuple._4))
            })
            stat.executeBatch()
            conn.get.commit()
          } catch {
            case e: Exception =>
              e.printStackTrace()
          } finally {
            if (conn.isDefined) {
              conn.get.close()
            }
          }
        }
      })
    }
    }
  }

  override def destroy(): Unit = {
    eventProducer.close()
    ssc.stop()
  }
}

package com.stayrascal.service.application.history

import java.sql.Connection
import java.util
import java.util.concurrent.CompletableFuture

import com.stayrascal.service.application.common.{EventFormatUtil, PhoenixPool}
import com.stayrascal.service.application.domain.{HistoryRecord, NumOfUsers, TotalFreq}
import com.stayrascal.service.application.repository.HistoryRepository
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class HistoryServiceImpl(@Autowired val sparkSession: SparkSession,
                         @Autowired val properties: HistoryKafkaProperties,
                         @Autowired val historyRepository: HistoryRepository) extends HistoryService with DisposableBean with Serializable {
  private val ssc = new StreamingContext(sparkSession.sparkContext, Seconds(2))

  private val historyRecordProducer = new KafkaProducer[String, String](properties.getKafkaParamsProducer)

  override def init(): Unit = {
    CompletableFuture.runAsync(new Runnable {
      override def run(): Unit = {
        saveHistoryRecords(getHistoryRecordStream)
        ssc.start()
        ssc.awaitTermination()
      }
    })
  }

  /**
    * 从kafka的topic中获得历史记录，并连接到Spark streaming中
    *
    * @return DStream
    */
  override def getHistoryRecordStream: DStream[(String, String, String, Long)] = {
    ssc.checkpoint(properties.getCheckpointDir)

    KafkaUtils.createDirectStream(
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](properties.getTopics, properties.getKafkaParamsConsumer)
    )
      .map(_.value())
      .filter(EventFormatUtil.isValidateHistory)
      .map(x => (x, 1L))
      // 窗口时间设定为4s，并统计这段时间内用户使用构件的次数
      .reduceByKeyAndWindow(_ + _, _ - _, Seconds(4), Seconds(4), 2)
      .map(kv => {
        val parts = kv._1.split(",")
        (parts(0), parts(1), parts(2), kv._2)
      })
      //  过滤掉不必要的记录，减少数据库连接
      .filter(tuple => tuple._4 != 0)
  }

  /**
    * 将历史记录保存到数据库
    *
    * @param historyStream
    */
  override def saveHistoryRecords(historyStream: DStream[(String, String, String, Long)]): Unit = {
    historyStream.foreachRDD { rdd =>
      rdd.foreachPartition(partitionRecords => {
        if (partitionRecords.nonEmpty) {
          var conn: Option[Connection] = None
          try {
            conn = Option.apply(PhoenixPool.getConnection)
            val stat = conn.get.createStatement()
            conn.get.setAutoCommit(false)
            partitionRecords.foreach(tuple => {
              stat.addBatch(HistoryDBUtil.generateUpsertSQL(tuple._1, tuple._2, tuple._3, tuple._4))
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

  override def destroy(): Unit = {
    historyRecordProducer.close()
    ssc.stop()
  }

  override def addHistoryRecord(historyRecord: String): Unit = {
    if (!EventFormatUtil.isValidateHistory(historyRecord)) {
      throw new HistoryFormatException(s"Invalid format for history string: $historyRecord")
    }
    val topic = properties.getTopics.get(0)
    historyRecordProducer.send(new ProducerRecord[String, String](topic, historyRecord), new Callback {
      override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {}
    })
  }

  /**
    * 获得有多少用户在使用完构件1之后又使用其它构件
    *
    * @param compName 构件1的名字
    * @return 用户数
    */
  override def getNumOfUsers(compName: String): util.List[NumOfUsers] = {
    historyRepository.getNumOfUsers(compName)
  }

  /**
    * 获得用户使用构件1之后又使用了哪些构件
    *
    * @param userName 用户名
    * @param compName 构件1的名字
    * @return 使用历史
    */
  override def getHistoryForUser(userName: String, compName: String): util.List[HistoryRecord] = {
    historyRepository.getHistoryForUser(userName, compName)
  }

  /**
    * 获得使用完构件1又使用其它构件的总次数
    *
    * @param compName 构件1的名字
    * @return 总次数
    */
  override def getTotalFreq(compName: String): util.List[TotalFreq] = {
    historyRepository.getTotalFreq(compName)
  }

  /**
    * 获得受欢迎的构件对的使用人数
    *
    * @param limit 指定构件对的数量
    * @return
    */
  override def getPopularUsagesPopulation(limit: Int): util.List[NumOfUsers] = {
    historyRepository.getPopularUsagesPopulation(limit)
  }

  /**
    * 获得受欢迎的构件对的使用次数
    *
    * @param limit 指定构件对的数量
    * @return
    */
  override def getPopulatedUsagesCount(limit: Int): util.List[TotalFreq] = {
    historyRepository.getPopulatedUsagesCount(limit)
  }
}

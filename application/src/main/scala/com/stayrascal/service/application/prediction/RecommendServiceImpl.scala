package com.stayrascal.service.application.prediction

import java.util.concurrent.TimeUnit
import java.{lang, util}

import com.stayrascal.recom.cf.ItemCFModel
import com.stayrascal.recom.cf.entities.Event
import com.stayrascal.service.application.constraints.Schemas.{HBaseEventsSchema, HBaseRecommendationSchema, HBaseUsersSchema}
import com.stayrascal.service.application.domain.{Prediction, Recommendation}
import com.stayrascal.service.application.repository.RecommendRepository
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import org.apache.hadoop.conf.Configuration
import org.apache.phoenix.spark._
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RecommendServiceImpl(@Autowired spark: SparkSession,
                           @Autowired val hbaseConfig: Configuration,
                           @Autowired val recommendRepository: RecommendRepository) extends PredictionService with Serializable with DisposableBean {
  private val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  private val parallelism = spark.sparkContext.defaultParallelism

  private val itemCFModel = new ItemCFModel(spark)

  private var users: Option[DataFrame] = None
  private var items: Option[DataFrame] = None
  private var events: Option[Dataset[Event]] = None

  override def makePrediction(): DataFrame = {
    val userSet = getEvent
  }


  private def getEvent: Dataset[Event] = {
    events.getOrElse {
      loadEvents().coalesce(parallelism).createOrReplaceTempView("events")
      getUsers.createOrReplaceTempView("users")

    }
  }

  private def getUsers: DataFrame = {
    users.getOrElse {
      val usrs = loadUsers().coalesce(parallelism).cache()
      users = Option(usrs)
      usrs
    }
  }

  private def loadEvents(): DataFrame = {
    spark.sqlContext.phoenixTableAsDataFrame(
      HBaseEventsSchema.TABLE_NAME,
      Seq(
        HBaseEventsSchema.USER_QUALIFIER,
        HBaseEventsSchema.ITEM_QUALIFIER,
        HBaseEventsSchema.ACTION_QUALIFIER
      ), conf = hbaseConfig
    )
  }

  def loadUsers(): DataFrame = {
    spark.sqlContext.phoenixTableAsDataFrame(
      HBaseUsersSchema.TABLE_NAME,
      Seq(HBaseUsersSchema.UUID_QUALIFIER, HBaseUsersSchema.ID_QUALIFIER),
      conf = hbaseConfig
    )
  }

  override def storePrediction(prediction: DataFrame): Unit = {
    prediction.saveToPhoenix(HBaseRecommendationSchema.TABLE_NAME, conf = hbaseConfig)
  }

  override def getPrediction(userName: String, compName: String, num: Int): util.List[Prediction] = ???

  override def recommend(userId: Long, itemId: Long, num: Int, measureType: String): util.List[Recommendation] = {
    recommendRepository.getRecommendation(userId, itemId, num, measureType)
  }

  override def init(): Unit = {
    Observable.interval(10, TimeUnit.MINUTES)
      .subscribeOn(Schedulers.computation())
      .subscribe(new Consumer[lang.Long] {
        override def accept(t: lang.Long): Unit = {
          logger.info("Try to make recommendation and save.")
          storePrediction(makePrediction())
          clean()
          logger.info("Recommendation has made and saved.")
        }
      })
  }

  override def clean(): Unit = {
    logger.info("Clean caches....")
    users.foreach(user => user.unpersist())
    items.foreach(item => item.unpersist())
    events.foreach(event => event.unpersist())
    itemCFModel.getSimilarities.get.unpersist()
    users = None
    items = None
    events = None
  }

  override def destroy(): Unit = {
    spark.stop()
  }
}

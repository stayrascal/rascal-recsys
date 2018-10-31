package com.stayrascal.service.application.prediction

import java.util.concurrent.TimeUnit
import java.{lang, util}

import com.stayrascal.recom.cf.ItemCFModel
import com.stayrascal.recom.cf.entities.{Event, User}
import com.stayrascal.service.application.constraints.Limits.MAX_RECOMMEND_COMP_NUM
import com.stayrascal.service.application.constraints.Schemas.{HBaseEventsSchema, HBaseItemsSchema, HBaseRecommendationSchema, HBaseUsersSchema}
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
class RecommendService(@Autowired spark: SparkSession,
                       @Autowired val hbaseConfig: Configuration,
                       @Autowired val recommendRepository: RecommendRepository) extends PredictionService with Serializable with DisposableBean {
  private val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  import spark.implicits._

  private val parallelism = spark.sparkContext.defaultParallelism

  private val itemCFModel = new ItemCFModel(spark)

  private var users: Option[DataFrame] = None
  private var items: Option[DataFrame] = None
  private var events: Option[Dataset[Event]] = None

  override def makePrediction(): DataFrame = {
    val userSet = getEvent.select("userId")
      .map(row => User(row.getLong(0).toInt, null))
    val recommendationModel = itemCFModel
      .fit(getEvent)
    val coocDF = recommendationModel
      .recommendForUser(userSet, MAX_RECOMMEND_COMP_NUM, "cooc")
      .coalesce(parallelism)
      .cache()
    val corrDF = recommendationModel
      .recommendForUser(userSet, MAX_RECOMMEND_COMP_NUM, "corr")
      .coalesce(parallelism)
      .cache()
    val regCorrDF = recommendationModel
      .recommendForUser(userSet, MAX_RECOMMEND_COMP_NUM, "regCorr")
      .coalesce(parallelism)
      .cache()
    val cosSimDF = recommendationModel
      .recommendForUser(userSet, MAX_RECOMMEND_COMP_NUM, "cosSim")
      .coalesce(parallelism)
      .cache()
    val impCosSimDF = recommendationModel
      .recommendForUser(userSet, MAX_RECOMMEND_COMP_NUM, "impCosSim")
      .coalesce(parallelism)
      .cache()
    val jaccardDF = recommendationModel
      .recommendForUser(userSet, MAX_RECOMMEND_COMP_NUM, "jaccard")
      .coalesce(parallelism)
      .cache()
    val recommendation = coocDF
      .union(corrDF)
      .union(regCorrDF)
      .union(cosSimDF)
      .union(impCosSimDF)
      .union(jaccardDF)
      .coalesce(parallelism)
      .toDF("USERID", "ITEMID", "SCORE", "MEASURETYPE")
    coocDF.unpersist()
    corrDF.unpersist()
    regCorrDF.unpersist()
    cosSimDF.unpersist()
    impCosSimDF.unpersist()
    jaccardDF.unpersist()
    recommendation
  }


  private def getEvent: Dataset[Event] = {
    events.getOrElse {
      loadEvents().coalesce(parallelism).createOrReplaceTempView("events")
      getUsers.createOrReplaceTempView("users")
      getItems.createOrReplaceTempView("items")
      val eventDF = spark.sql(
        """
          | SELECT userId, itemId
          | FROM events
        """.stripMargin
      )
        .map(row => Event(row.getLong(0), row.getLong(1)))
        .coalesce(parallelism)
        .cache()
      events = Option(eventDF)
      eventDF
    }
  }

  private def getItems: DataFrame = {
    items.getOrElse {
      val itemDF = loadItems().coalesce(parallelism).cache()
      items = Option(itemDF)
      itemDF
    }
  }

  private def getUsers: DataFrame = {
    users.getOrElse {
      val usrs = loadUsers().coalesce(parallelism).cache()
      users = Option(usrs)
      usrs
    }
  }

  private def loadItems(): DataFrame = {
    spark.sqlContext.phoenixTableAsDataFrame(
      HBaseItemsSchema.TABLE_NAME,
      Seq(HBaseItemsSchema.ID_QUALIFIER),
      conf = hbaseConfig
    )
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
    prediction.saveToPhoenix(
      HBaseRecommendationSchema.TABLE_NAME,
      conf = hbaseConfig)
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

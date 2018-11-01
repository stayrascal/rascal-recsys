package com.stayrascal.service.application.prediction

import java.util.concurrent.TimeUnit
import java.{lang, util}

import com.stayrascal.recom.cf.LinearItemCFModel
import com.stayrascal.recom.cf.entities.{History, User, UserCompPair}
import com.stayrascal.service.application.constraints.Limits.MAX_RECOMMEND_COMP_NUM
import com.stayrascal.service.application.constraints.Schemas.{HBaseComponentSchema, HBaseHistorySchema, HBasePredictionSchema, HBaseUsersSchema}
import com.stayrascal.service.application.domain.{Prediction, Recommendation}
import com.stayrascal.service.application.repository.PredictionRepository
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
import org.springframework.transaction.annotation.Transactional;

//@Service
class PredictionServiceImpl(@Autowired spark: SparkSession,
                            @Autowired val hbaseConfig: Configuration,
                            @Autowired val predictionRepository: PredictionRepository) extends PredictionService with Serializable with DisposableBean {
  private val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  import spark.implicits._

  private val parallelism = spark.sparkContext.defaultParallelism

  private val itemCFModel = new LinearItemCFModel(spark)

  private var users: Option[DataFrame] = None
  private var components: Option[DataFrame] = None
  private var history: Option[Dataset[History]] = None

  override def init(): Unit = {
    Observable.interval(10, TimeUnit.MINUTES)
      .subscribeOn(Schedulers.computation())
      .subscribe(new Consumer[lang.Long] {
        override def accept(t: lang.Long): Unit = {
          logger.info("Try to make prediction and save.")
          storePrediction(makePrediction())
          clean()
          logger.info("Prediction has made and saved.")
        }
      })
  }

  override def clean(): Unit = {
    logger.info("Clean caches....")
    users.foreach(user => user.unpersist())
    components.foreach(component => component.unpersist())
    history.foreach(his => his.unpersist())
    itemCFModel.getSimilarities.get.unpersist()
    users = None
    components = None
    history = None
  }

  /**
    * 将预测存储到数据库
    *
    * @param prediction 预测
    */
  @Transactional
  override def storePrediction(prediction: DataFrame): Unit = {
    prediction.saveToPhoenix(HBasePredictionSchema.TABLE_NAME, conf = hbaseConfig)
  }

  override def makePrediction(userSet: Dataset[User] = null, measureType: String = "cooc"): DataFrame = {
    val userSet = getHistory.select("userId", "COMPID")
      .map(row => UserCompPair(row.getInt(0), row.getInt(1)))
    val prediction = itemCFModel
      .fit(getHistory)
      .recommendForUser(userSet, MAX_RECOMMEND_COMP_NUM)
      .coalesce(parallelism)
      .cache()

    prediction.createOrReplaceTempView("prediction")
    getUsers.createOrReplaceTempView("users")
    getComponents.createOrReplaceTempView("components")
    var predict = spark.sql(
      """
        | SELECT b.uuid as USERNAME, a.COMPNAME as COMPNAME, a.FOLLOWCOMPNAME as FOLLOWCOMPNAME, prediction
        | FROM
        |  (SELECT userId, COMPNAME, b.NAME as FOLLOWCOMPNAME, prediction
        |   FROM
        |    (SELECT a.userId as userId, b.NAME as COMPNAME, a.followCompId as followCompId, prediction
        |      FROM prediction a
        |      JOIN components b on a.compId = b.ID
        |    ) a
        |    JOIN components b on a.followCompId = b.ID) a
        |  JOIN users b
        | ON a.userId = b.id
      """.stripMargin)
    predict = predict.coalesce(parallelism)
    prediction.unpersist()
    predict
  }

  private def loadComponents(): DataFrame = {
    spark.sqlContext.phoenixTableAsDataFrame(
      HBaseComponentSchema.TABLE_NAME,
      Seq(HBaseComponentSchema.NAME_QUALIFIER, HBaseComponentSchema.ID_QUALIFIER),
      conf = hbaseConfig
    )
  }

  private def getComponents: DataFrame = {
    components.getOrElse {
      val comps = loadComponents().coalesce(parallelism).cache()
      components = Option(comps)
      comps
    }
  }

  private def loadUsers(): DataFrame = {
    spark.sqlContext.phoenixTableAsDataFrame(
      HBaseUsersSchema.TABLE_NAME,
      Seq(HBaseUsersSchema.UUID_QUALIFIER, HBaseUsersSchema.ID_QUALIFIER),
      conf = hbaseConfig)
  }

  private def getUsers: DataFrame = {
    users.getOrElse {
      val usrs = loadUsers().coalesce(parallelism).cache()
      users = Option(usrs)
      usrs
    }
  }

  private def loadHistory(): DataFrame = {
    spark.sqlContext.phoenixTableAsDataFrame(
      HBaseHistorySchema.TABLE_NAME,
      Seq(
        HBaseHistorySchema.USER_QUALIFIER,
        HBaseHistorySchema.COMP_QUALIFIER,
        HBaseHistorySchema.FOLLOW_COMP_QUALIFIER,
        HBaseHistorySchema.FREQ_QUALIFIER
      ), conf = hbaseConfig
    )
  }

  /**
    * 获得用户历史记录，格式为
    * (userId, compId, followCompId, freq)
    *
    * @return Dataset[History]
    */
  private def getHistory: Dataset[History] = {
    history.getOrElse {
      loadHistory().coalesce(parallelism).createOrReplaceTempView("history")
      getUsers.createOrReplaceTempView("users")
      getComponents.createOrReplaceTempView("components")
      val his = spark.sql(
        """
          | SELECT b.id as userId, a.COMPID as COMPID, a.FOLLOWCOMPID as FOLLOWCOMPID, FREQ
          | FROM
          |   (SELECT USERNAME, COMPID, b.ID as FOLLOWCOMPID, FREQ
          |    FROM
          |     (SELECT a.userName as userName, b.id as compId, a.followCompName as followCompName, freq
          |      FROM history a
          |      JOIN components b on a.compName = b.name
          |     ) a
          |    JOIN components b on a.followCompName = b.name) a
          | JOIN users b
          | ON a.userName = b.uuid
        """.stripMargin)
        .map(row => History(row.getInt(0), row.getInt(1), row.getInt(2), row.getLong(3)))
        .coalesce(parallelism)
        .cache()
      history = Option(his)
      his
    }
  }

  override def getPrediction(userName: String, compName: String, num: Int): util.List[Prediction] = {
    predictionRepository.getPrediction(userName, compName, num)
  }

  override def destroy(): Unit = {
    spark.stop()
  }

  override def recommend(userId: Long, num: Int, measureType: String): util.List[Recommendation] = ???
}

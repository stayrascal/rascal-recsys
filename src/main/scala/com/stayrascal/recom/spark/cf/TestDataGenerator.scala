package com.stayrascal.recom.spark.cf

import org.apache.spark.sql._
import com.stayrascal.recom.spark.cf.entities.{Component, History, HistoryWithName, User}

import scala.collection.immutable

object TestDataGenerator {

  /**
    * 生成指定数量的构件
    *
    * @param num       构件数量
    * @param startWith 构件名以指定的字符作为开头
    * @return
    */
  def generateComponents(num: Int, startWith: Array[String]): immutable.IndexedSeq[Component] = {
    for (i <- 0 until num)
      yield Component(i, startWith(i % startWith.length) + i)
  }

  /**
    * 从一组构件中随机抽取两个构成一个元组
    *
    * @param num        元组的数量
    * @param components 构件
    * @return
    */
  def generateComponentTuples(num: Int, components: IndexedSeq[Component]): immutable.IndexedSeq[(Component, Component)] = {
    for (_ <- 0 until num)
      yield (components(math.floor(math.random * components.length).toInt), components(math.floor(math.random * components.length).toInt))
  }

  /**
    * 生成指定数量的用户
    *
    * @param num 用户数
    * @return
    */
  def generateUsers(num: Int): immutable.IndexedSeq[User] = {
    for (i <- 0 until num)
      yield User(i, i.toString)
  }

  def generateHistory(num: Int, maxCount: Float, users: IndexedSeq[User], componentTuples: IndexedSeq[(Component, Component)]): IndexedSeq[History] = {
    var result: Array[History] = Array()
    for (_ <- 0 until num) {
      val randomUserId = users(math.floor(math.random * users.length).toInt).userId
      val randomCompTuple = componentTuples(math.floor(math.random * componentTuples.length).toInt)
      val randomCount = math.ceil(math.random * maxCount).toFloat
      result = result :+ History(randomUserId, randomCompTuple._1.compId, randomCompTuple._2.compId, randomCount)
    }
    result
  }

  def generateHistoryWithName(num: Int, maxCount: Float, users: IndexedSeq[User], componentTuples: IndexedSeq[(Component, Component)]): IndexedSeq[HistoryWithName] = {
    var result: Array[HistoryWithName] = Array()
    for (_ <- 0 until num) {
      val randomUser = users(math.floor(math.random * users.length).toInt)
      val randomCompTuple = componentTuples(math.floor(math.random * componentTuples.length).toInt)
      val randomCount = math.ceil(math.random * maxCount).toFloat
      result = result :+ HistoryWithName(randomUser.userName, randomCompTuple._1.compName, randomCompTuple._2.compName, randomCount)
    }
    result
  }

  def generateRecords(numComps: Int, numCompTuples: Int, numUsers: Int, numHistory: Int, maxCount: Float, compStartWith: Array[String])
  : (immutable.IndexedSeq[User], immutable.IndexedSeq[Component], IndexedSeq[History]) = {
    val startWith = compStartWith
    val components = generateComponents(numComps, startWith)
    val componentTuples = generateComponentTuples(numCompTuples, components)
    val users = generateUsers(numUsers)
    val history = generateHistory(numHistory, maxCount, users, componentTuples)
    (users, components, history)
  }

  def generateRecordsWithName(numComps: Int, numCompTuples: Int, numUsers: Int, numHistory: Int, maxCount: Float, compStartWith: Array[String])
  : (immutable.IndexedSeq[User], immutable.IndexedSeq[Component], IndexedSeq[HistoryWithName]) = {
    val startWith = compStartWith
    val components = generateComponents(numComps, startWith)
    val componentTuples = generateComponentTuples(numCompTuples, components)
    val users = generateUsers(numUsers)
    val history = generateHistoryWithName(numHistory, maxCount, users, componentTuples)
    (users, components, history)
  }

  /**
    * 生成测试数据表
    *
    * 为了生成较理想的数据，应满足如下要求：
    * - numCompTuples >= 3 * numComps
    * - numCompTuples >> numUsers
    * - numHistory  >> numCompTuples
    *
    * @param numComps      构件数
    * @param numCompTuples 构件二元组数
    * @param numUsers      用户数
    * @param numHistory    历史记录数
    * @param maxCount      最大使用次数
    * @param compStartWith 构件名以指定字符开始
    * @return (users, components, history)
    */
  def generateTable(numComps: Int, numCompTuples: Int, numUsers: Int, numHistory: Int, maxCount: Float, compStartWith: Array[String])
  : (DataFrame, DataFrame, DataFrame) = {
    val spark: SparkSession = SparkSession.builder()
      .master("local")
      .getOrCreate()

    import spark.implicits._
    val records = generateRecords(numComps, numCompTuples, numUsers, numHistory, maxCount, compStartWith)
    records._3.toDF().coalesce(1).createOrReplaceTempView("oriHistoryTable")

    val historyTable = spark.sql(
      """
        |SELECT first(userId) as userId
        |,  first(compId) as compId
        |,  first(followCompId) as followCompId
        |,  first(count)  as  count
        |FROM oriHistoryTable
        |GROUP BY userId, compId, followCompId
      """.stripMargin)
      .sort("userId", "compId")

    val userTable = records._1.toDF()
    val componentsTable = records._2.toDF()

    (userTable, componentsTable, historyTable)
  }

  def generateTableWithName(numComps: Int, numCompTuples: Int, numUsers: Int, numHistory: Int, maxCount: Float, compStartWith: Array[String])
  : (DataFrame, DataFrame, DataFrame) = {
    val spark: SparkSession = SparkSession.builder()
      .master("local")
      .getOrCreate()

    import spark.implicits._
    val records = generateRecordsWithName(numComps, numCompTuples, numUsers, numHistory, maxCount, compStartWith)
    records._3.toDF().coalesce(1).createOrReplaceTempView("oriHistoryTable")

    val historyTable = spark.sql(
      """
        |SELECT first(user) as user
        |,  first(comp) as comp
        |,  first(followComp) as followComp
        |,  first(count)  as  count
        |FROM oriHistoryTable
        |GROUP BY user, comp, followComp
      """.stripMargin)
      .sort("user", "comp")

    val userTable = records._1.toDF()
    val componentsTable = records._2.toDF()

    (userTable, componentsTable, historyTable)
  }

  def main(args: Array[String]): Unit = {
//    val withName = args(0).toBoolean
    val withName = false
    var tables: (DataFrame, DataFrame, DataFrame) = null
    if (withName) {
      tables = generateTableWithName(200, 600, 100, 100000, 5f, Array("A", "B", "C", "D"))
    } else {
      tables = generateTable(200, 600, 100, 100000, 5f, Array("A", "B", "C", "D"))
    }
//    saveTables(tables, args(1))
    saveTables(tables, "./data")
  }

  def saveTables(tables: (DataFrame, DataFrame, DataFrame), outputDir: String): Unit = {
    tables._1.write.mode(SaveMode.Overwrite).csv(outputDir + "/users")
    tables._2.write.mode(SaveMode.Overwrite).csv(outputDir + "/components")
    tables._3.write.mode(SaveMode.Overwrite).csv(outputDir + "/history")
  }


}

package com.stayrascal.service.application.common

import java.sql.Connection

import com.mchange.v2.c3p0.ComboPooledDataSource


/**
  * 由于数据库连接对象几乎无法进行序列化，为了在spark streaming中连接数据库
  * ，需将数据连接过程放在worker端，而非driver端
  *
  * @see <a href="https://spark.apache.org/docs/2.1.1/streaming-programming-guide.html#design-patterns-for-using-foreachrdd">
  *      Design Patterns for using foreachRDD
  *      </a>
  */
private class PhoenixPool extends Serializable {
  private val connectPool: ComboPooledDataSource = new ComboPooledDataSource(true)
  connectPool.setJdbcUrl("jdbc:phoenix:localhost:2181")
  connectPool.setAcquireIncrement(5)
  connectPool.setMaxStatements(180)

  def getConnection: Connection = {
    try {
      connectPool.getConnection()
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        null
    }
  }
}

object PhoenixPool {
  private var phoenixPool: PhoenixPool = _

  def getConnection: Connection = {
    synchronized {
      if (phoenixPool == null) {
        phoenixPool = new PhoenixPool()
      }
    }
    phoenixPool.getConnection
  }
}

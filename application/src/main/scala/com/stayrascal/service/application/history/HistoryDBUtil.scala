package com.stayrascal.service.application.history

import java.sql.Date

object HistoryDBUtil {
  def generateUpsertSQL(userName: String, compName: String, followCompName: String, freq: Long): String = {
    s"""
       |UPSERT INTO history(userName, compName, followCompName, freq)
       |VALUES ( '$userName' ,'$compName' , '$followCompName' , $freq )
       |ON DUPLICATE KEY
       |UPDATE freq = freq + $freq
              """.stripMargin
  }

  def generateUpsertEventSql(userId: Long, itemId: Long, action: String, otherItems: String): String = {
    val currentDate = new Date(System.currentTimeMillis())
    s"""
       | UPSERT INTO events (id, userId, itemId, action, createTime, otherItems)
       | VALUES (NEXT VALUE FOR events_seq, '$userId', '$itemId', '$action', '$otherItems', '$currentDate')
     """.stripMargin
  }

}

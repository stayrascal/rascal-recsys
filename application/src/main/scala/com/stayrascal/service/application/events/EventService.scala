package com.stayrascal.service.application.events

import org.apache.spark.streaming.dstream.DStream

trait EventStreamService {
  def init(): Unit

  def addEvent(event: String)

  def getEventStream: DStream[(String, String, String, String)]

  def saveEvent(eventStream: DStream[(String, String, String, String)])
}

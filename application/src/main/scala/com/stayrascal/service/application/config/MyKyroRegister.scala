package com.stayrascal.service.application.config

import com.esotericsoftware.kryo.Kryo
import com.stayrascal.recom.cf.LinearItemCFModel
import com.stayrascal.service.application.history.HistoryService
import com.stayrascal.service.application.prediction.PredictionService
import org.apache.spark.serializer.KryoRegistrator

class MyKyroRegister extends KryoRegistrator {
  override def registerClasses(kryo: Kryo): Unit = {
    kryo.register(classOf[LinearItemCFModel])
    kryo.register(classOf[HistoryService])
    kryo.register(classOf[PredictionService])
  }
}

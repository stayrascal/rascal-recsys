package com.stayrascal.service.application.config

import com.esotericsoftware.kryo.Kryo
import org.apache.spark.serializer.KryoRegistrator

class MyKyroRegister extends KryoRegistrator {
  override def registerClasses(kryo: Kryo): Unit = {
    kryo.register(classOf[LinearItemCFModel])
    kryo.register(classOf[HistoryS])
  }
}

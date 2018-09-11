package com.stayrascal.recom.spark.cf.entities

case class User(userId: Int, userName: String)

case class Component(compId: Int, compName: String)

case class History(userId: Int, compId: Int, followCompId: Int, count: Float)

case class HistoryWithName(user: String, comp: String, followComp: String, count: Float)

case class UserCompPair(userId: Int, compId: Int)

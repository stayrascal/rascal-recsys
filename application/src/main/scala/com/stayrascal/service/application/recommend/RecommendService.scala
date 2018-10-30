package com.stayrascal.service.application.recommend

import com.stayrascal.service.application.domain.{Prediction, Recommendation}

trait RecommendService {
  def recommendForUser(userName: String, compName: String, num: Int): java.util.List[Prediction]

  def recommendForUser(userId: Long, itemId: Long, num: Int, measureType: String): java.util.List[Recommendation]
}

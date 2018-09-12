package com.stayrascal.service.application.recommend

import com.stayrascal.service.application.domain.Prediction

trait RecommendService {
  def recommendForUser(userName: String, compName: String, num: Int): java.util.List[Prediction]
}

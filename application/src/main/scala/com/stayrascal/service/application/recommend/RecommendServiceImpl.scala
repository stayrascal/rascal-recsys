package com.stayrascal.service.application.recommend

import java.util

import com.stayrascal.service.application.component.ComponentService
import com.stayrascal.service.application.domain.{Prediction, Recommendation, User}
import com.stayrascal.service.application.prediction.PredictionService
import com.stayrascal.service.application.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RecommendServiceImpl(@Autowired userService: UserService,
                           @Autowired val componentService: ComponentService,
                           @Autowired val predictionService: PredictionService
                          ) extends RecommendService {

  override def recommendForUser(userName: String, compName: String, num: Int): util.List[Prediction] = {
    val userQuantum = userService.selectUserByUUID(userName)
    val compQuantum = componentService.searchComp(compName)
    if (!userQuantum.isPresent) {
      userService.addUser(new User(userName))
    }
    predictionService.getPrediction(userName, compName, num)
  }

  override def recommendForUser(userId: Long, itemId: Long, num: Int, measureType: String): util.List[Recommendation] = {
    predictionService.recommend(userId, itemId, num, measureType)
  }
}

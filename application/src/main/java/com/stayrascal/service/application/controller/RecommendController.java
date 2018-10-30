package com.stayrascal.service.application.controller;

import com.stayrascal.service.application.domain.Prediction;
import com.stayrascal.service.application.domain.Recommendation;
import com.stayrascal.service.application.dto.result.base.Result;
import com.stayrascal.service.application.dto.result.recommend.PredictionResult;
import com.stayrascal.service.application.dto.result.recommend.RecommendResult;
import com.stayrascal.service.application.recommend.RecommendService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class RecommendController {

  private RecommendService service;

  @Autowired
  public RecommendController(RecommendService service) {
    this.service = service;
  }

  @GetMapping(value = "/api/v1/components/recommend")
  public ResponseEntity<Result> recommend(@RequestParam("uuid") String uuid,
                                          @RequestParam("compName") String compName,
                                          @RequestParam("num") int num) {
    List<Prediction> result = service.recommendForUser(uuid, compName, num);
    return ResponseEntity.ok(new PredictionResult(result));
  }

  @GetMapping(value = "/api/v1/recommend")
  public ResponseEntity<Result> recommend(@RequestParam("userId") Long userId,
                                          @RequestParam("itemId") Long itemId,
                                          @RequestParam("num") int num,
                                          @RequestParam("type") String measureType) {
    List<Recommendation> result = service.recommendForUser(userId, itemId, num, measureType);
    return ResponseEntity.ok(new RecommendResult(result));
  }
}
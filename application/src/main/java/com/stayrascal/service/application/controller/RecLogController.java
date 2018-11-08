package com.stayrascal.service.application.controller;

import com.stayrascal.service.application.domain.RecLog;
import com.stayrascal.service.application.dto.result.base.Result;
import com.stayrascal.service.application.dto.result.querylog.RecLogQueryResult;
import com.stayrascal.service.application.reclog.RecLogService;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*")
public class RecLogController {
  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private RecLogService service;

  @PostMapping(value = "/api/v1/" +
    "")
  public ResponseEntity<Result> addQueryLog(@RequestBody RecLog log) {
    logger.debug("Adding recommendation history for user: {}", log.getUserId());
    RecLogQueryResult recResult = new RecLogQueryResult(Collections.singletonList(service.addRecLog(log)));
    logger.debug("Recommendation log: {} click {} among {}.", log.getUserId(), log.getClickItemId(), log.getOtherItems());
    return ResponseEntity.ok(recResult);
  }
}

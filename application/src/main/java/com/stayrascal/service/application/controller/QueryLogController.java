package com.stayrascal.service.application.controller;

import com.stayrascal.service.application.domain.QueryLog;
import com.stayrascal.service.application.dto.result.base.Result;
import com.stayrascal.service.application.dto.result.querylog.QueryLogQueryResult;
import com.stayrascal.service.application.querylog.QueryLogService;

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
public class QueryLogController {
  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private QueryLogService service;

  @PostMapping(value = "/api/v1/query")
  public ResponseEntity<Result> addQueryLog(@RequestBody QueryLog log) {
    logger.debug("Adding event for user: {}", log.getUserId());
    QueryLogQueryResult queryResult = new QueryLogQueryResult(Collections.singletonList(service.addQueryLog(log)));
    logger.debug("Query log: {} query {} got {} items, and click {} added.", log.getUserId(), log.getQuery(), log.getResultCnt(), log.getClickItemId());
    return ResponseEntity.ok(queryResult);
  }
}

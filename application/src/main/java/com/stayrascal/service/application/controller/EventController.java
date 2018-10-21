package com.stayrascal.service.application.controller;

import com.stayrascal.service.application.domain.Event;
import com.stayrascal.service.application.dto.result.base.Result;
import com.stayrascal.service.application.dto.result.event.EventQueryResult;
import com.stayrascal.service.application.event.EventService;

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
public class EventController {
  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private EventService eventService;

  @PostMapping(value = "/api/v1/events")
  public ResponseEntity<Result> addEvent(@RequestBody Event event) {
    logger.debug("Adding event for user: {}", event.getItemId());
    EventQueryResult queryResult = new EventQueryResult(Collections.singletonList(eventService.addEvent(event)));
    logger.debug("Event: {} {} {} added.", event.getUserId(), event.getAction().name(), event.getItemId());
    return ResponseEntity.ok(queryResult);
  }
}

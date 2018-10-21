package com.stayrascal.service.application.dto.result.event;

import com.stayrascal.service.application.domain.Event;
import com.stayrascal.service.application.dto.result.QueryResult;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EventQueryResult extends QueryResult<Event> {
  public EventQueryResult(int numFound, int start, List<Event> data) {
    super(numFound, start, data);
  }

  public EventQueryResult(Integer numFound, List<Event> data) {
    super(numFound, data);
  }

  public EventQueryResult(List<Event> data) {
    super(data);
  }

  @JsonProperty("events")
  @Override
  public List<Event> getData() {
    return super.getData();
  }
}

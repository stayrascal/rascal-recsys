package com.stayrascal.service.application.dto.result.querylog;

import com.stayrascal.service.application.domain.QueryLog;
import com.stayrascal.service.application.dto.result.QueryResult;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryLogQueryResult extends QueryResult<QueryLog> {
  public QueryLogQueryResult(int numFound, int start, List<QueryLog> data) {
    super(numFound, start, data);
  }

  public QueryLogQueryResult(Integer numFound, List<QueryLog> data) {
    super(numFound, data);
  }

  public QueryLogQueryResult(List<QueryLog> data) {
    super(data);
  }

  @JsonProperty("events")
  @Override
  public List<QueryLog> getData() {
    return super.getData();
  }
}

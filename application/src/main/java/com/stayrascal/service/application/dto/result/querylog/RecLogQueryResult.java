package com.stayrascal.service.application.dto.result.querylog;

import com.stayrascal.service.application.domain.RecLog;
import com.stayrascal.service.application.dto.result.QueryResult;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RecLogQueryResult extends QueryResult<RecLog> {
  public RecLogQueryResult(int numFound, int start, List<RecLog> data) {
    super(numFound, start, data);
  }

  public RecLogQueryResult(Integer numFound, List<RecLog> data) {
    super(numFound, data);
  }

  public RecLogQueryResult(List<RecLog> data) {
    super(data);
  }

  @JsonProperty("recs")
  @Override
  public List<RecLog> getData() {
    return super.getData();
  }
}

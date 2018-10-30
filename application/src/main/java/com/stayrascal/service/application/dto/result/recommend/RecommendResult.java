package com.stayrascal.service.application.dto.result.recommend;

import com.stayrascal.service.application.domain.Recommendation;
import com.stayrascal.service.application.dto.result.QueryResult;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RecommendResult extends QueryResult<Recommendation> {


  public RecommendResult(int numFound, int start, List<Recommendation> data) {
    super(numFound, start, data);
  }

  public RecommendResult(int numFound, List<Recommendation> data) {
    super(numFound, data);
  }

  public RecommendResult(List<Recommendation> data) {
    super(data);
  }

  @JsonProperty("recommendation")
  @Override
  public List<Recommendation> getData() {
    return super.getData();
  }
}

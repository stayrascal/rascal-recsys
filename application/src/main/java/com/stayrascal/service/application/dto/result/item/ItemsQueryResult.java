package com.stayrascal.service.application.dto.result.item;

import com.stayrascal.service.application.domain.Item;
import com.stayrascal.service.application.dto.result.QueryResult;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemsQueryResult extends QueryResult<Item> {

  public ItemsQueryResult(int numFound, int start, List<Item> data) {
    super(numFound, start, data);
  }

  public ItemsQueryResult(int numFound, List<Item> data) {
    super(numFound, data);
  }

  public ItemsQueryResult(List<Item> data) {
    super(data);
  }

  @JsonProperty("items")
  @Override
  public List<Item> getData() {
    return super.getData();
  }
}

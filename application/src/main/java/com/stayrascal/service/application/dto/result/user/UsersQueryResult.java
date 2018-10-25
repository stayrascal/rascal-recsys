package com.stayrascal.service.application.dto.result.user;

import com.stayrascal.service.application.domain.User;
import com.stayrascal.service.application.dto.result.QueryResult;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UsersQueryResult extends QueryResult<User> {

  public UsersQueryResult(int numFound, int start, List<User> data) {
    super(numFound, start, data);
  }

  public UsersQueryResult(int numFound, List<User> data) {
    super(numFound, data);
  }

  public UsersQueryResult(List<User> data) {
    super(data);
  }

  @JsonProperty("users")
  @Override
  public List<User> getData() {
    return super.getData();
  }
}

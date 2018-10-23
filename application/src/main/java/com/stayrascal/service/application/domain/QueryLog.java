package com.stayrascal.service.application.domain;

import java.sql.Date;

public class QueryLog {
  private long id;
  private long userId;
  private String query;
  private int resultCnt;
  private long clickItemId;
  private Date createTime;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getUserId() {
    return userId;
  }

  public void setUserId(long userId) {
    this.userId = userId;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public int getResultCnt() {
    return resultCnt;
  }

  public void setResultCnt(int resultCnt) {
    this.resultCnt = resultCnt;
  }

  public long getClickItemId() {
    return clickItemId;
  }

  public void setClickItemId(long clickItemId) {
    this.clickItemId = clickItemId;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }
}

package com.stayrascal.service.application.domain;

import com.stayrascal.service.application.common.enumeration.Action;

import java.sql.Date;

public class Event {
  private long id;
  private long userId;
  private long itemId;
  private Action action;
  private String otherItems;
  private Date createTime;

  public Event() {
  }

  public Event(long userId, long itemId) {
    this.userId = userId;
    this.itemId = itemId;
  }

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

  public long getItemId() {
    return itemId;
  }

  public void setItemId(long itemId) {
    this.itemId = itemId;
  }

  public Action getAction() {
    return action;
  }

  public void setAction(Action action) {
    this.action = action;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getOtherItems() {
    return otherItems;
  }

  public void setOtherItems(String otherItems) {
    this.otherItems = otherItems;
  }
}

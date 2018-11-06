package com.stayrascal.service.application.domain;

import java.sql.Date;

public class RecLog {
  private long userId;
  private long clickItemId;
  private String otherItems;
  private Date createTime;

  public long getUserId() {
    return userId;
  }

  public void setUserId(long userId) {
    this.userId = userId;
  }

  public String getOtherItems() {
    return otherItems;
  }

  public void setOtherItems(String otherItems) {
    this.otherItems = otherItems;
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

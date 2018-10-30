package com.stayrascal.service.application.domain;

import java.util.Objects;

public class Recommendation {
  private long userId;
  private long itemId;
  private String measureType;
  private float score;

  public Recommendation() {
  }

  public Recommendation(long userId, long itemId, String measureType, float score) {
    this.userId = userId;
    this.itemId = itemId;
    this.measureType = measureType;
    this.score = score;
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

  public String getMeasureType() {
    return measureType;
  }

  public void setMeasureType(String measureType) {
    this.measureType = measureType;
  }

  public float getScore() {
    return score;
  }

  public void setScore(float score) {
    this.score = score;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Recommendation that = (Recommendation) obj;
    return Objects.equals(userId, that.getUserId()) &&
      Objects.equals(itemId, that.getItemId()) &&
      Objects.equals(measureType, that.measureType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, itemId, measureType);
  }

  @Override
  public String toString() {
    return "Recommendation{" +
      "userId='" + userId + '\'' +
      ", itemId='" + itemId + '\'' +
      ", measureType='" + measureType + '\'' +
      ", score=" + score +
      '}';
  }
}

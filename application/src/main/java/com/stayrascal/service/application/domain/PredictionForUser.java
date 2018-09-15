package com.stayrascal.service.application.domain;

import java.util.Objects;

public class PredictionForUser {
    private int userId;
    private int itemId;
    private Float prediction;

    public PredictionForUser(int userId, int itemId, Float prediction) {
        this.userId = userId;
        this.itemId = itemId;
        this.prediction = prediction;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public Float getPrediction() {
        return prediction;
    }

    public void setPrediction(Float prediction) {
        this.prediction = prediction;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PredictionForUser that = (PredictionForUser) obj;
        return Objects.equals(userId, that.getItemId()) &&
                Objects.equals(itemId, that.getItemId()) &&
                Objects.equals(prediction, that.getPrediction());
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, itemId, prediction);
    }

    @Override
    public String toString() {
        return "Prediction{" +
                "userId=" + userId +
                ", itemId=" + itemId +
                ", prediction=" + prediction +
                "}";
    }
}

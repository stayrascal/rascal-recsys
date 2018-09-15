package com.stayrascal.service.application.domain;

public class UserBehavior {
    private int userId;
    private int itemId;
    private String action = "view";
    private Long freq;

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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getFreq() {
        return freq;
    }

    public void setFreq(Long freq) {
        this.freq = freq;
    }

    @Override
    public String toString() {
        return "Item" +
                "userId='" + userId +
                "', itemId=" + itemId +
                "', action='" + action +
                "', freq=" + freq +
                '}';
    }
}

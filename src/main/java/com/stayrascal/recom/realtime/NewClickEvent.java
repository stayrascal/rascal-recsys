package com.stayrascal.recom.realtime;

public class NewClickEvent {
    private long userId;
    private long itemId;

    public NewClickEvent() {
        userId = -1L;
        itemId = -1L;
    }

    public NewClickEvent(long userId, long itemId) {
        this.userId = userId;
        this.itemId = itemId;
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
}

package com.stayrascal.recom.common;

public class ItemSimilarity implements Comparable<ItemSimilarity> {
    private long itemId;
    private double similarity;

    public ItemSimilarity() {
        this.itemId = -1;
        this.similarity = 0d;
    }

    public ItemSimilarity(long itemId, double similarity) {
        this.itemId = itemId;
        this.similarity = similarity;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ItemSimilarity)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        ItemSimilarity similarityItem = (ItemSimilarity) obj;
        return this.itemId == similarityItem.itemId && this.similarity == similarityItem.similarity;
    }

    @Override
    public int hashCode() {
        return (int) (itemId + similarity);
    }

    @Override
    public int compareTo(ItemSimilarity obj) {
        if (this.similarity > obj.similarity) {
            return 1;
        } else if (this.similarity < obj.similarity) {
            return -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "itemId: " + itemId + ",similarity:" + similarity;
    }
}

package com.stayrascal.service.application.domain;

public class NumOfUsers {
    private String compName;

    private String followCompName;

    private Integer quantity;

    public String getCompName() {
        return compName;
    }

    public void setCompName(String compName) {
        this.compName = compName;
    }

    public String getFollowCompName() {
        return followCompName;
    }

    public void setFollowCompName(String followCompName) {
        this.followCompName = followCompName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "NumOfUsers{" +
                "compName='" + compName + '\'' +
                ", followCompName='" + followCompName + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}

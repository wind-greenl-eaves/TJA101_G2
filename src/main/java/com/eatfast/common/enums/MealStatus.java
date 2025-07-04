package com.eatfast.common.enums;

public enum MealStatus {
    UNAVAILABLE("下架"),
    AVAILABLE("上架");

    private final String description;

    MealStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
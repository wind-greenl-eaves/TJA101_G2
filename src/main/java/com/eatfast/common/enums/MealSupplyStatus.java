package com.eatfast.common.enums;

/**
 * 餐點供應狀態枚舉
 * 用於標示門市中各餐點的供應情況
 */
public enum MealSupplyStatus {
    AVAILABLE("供應中"),      // 餐點正常供應
    SOLD_OUT("售完"),        // 餐點已售完
    UNAVAILABLE("未供應");    // 餐點暫不供應

    private final String displayName;

    MealSupplyStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
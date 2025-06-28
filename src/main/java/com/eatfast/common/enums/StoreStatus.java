package com.eatfast.common.enums;

/**
 * 門市營業狀態的標準化列舉 (Enum)。
 * 職責: 定義所有可能的門市狀態，並提供與中文描述的對應關係。
 */
public enum StoreStatus {
    
    OPERATING("營業中"),
    CLOSED("休息中"),
    REMODELING("裝修中"),
    HEADQUARTERS("總部營運"); // 對應資料庫中的'總部營運'

    private final String description;

    StoreStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

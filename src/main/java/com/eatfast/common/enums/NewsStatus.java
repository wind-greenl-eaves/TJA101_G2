package com.eatfast.common.enums;

/**
 * 升級版的消息狀態枚舉，帶有中文顯示名稱
 */
public enum NewsStatus {

    // ✅【已修正】調整順序，讓它符合資料庫的定義 (0=草稿, 1=已發布)
    DRAFT("草稿"),         // 現在這個是第 0 位 (ordinal = 0)
    PUBLISHED("已發布"),   // 現在這個是第 1 位 (ordinal = 1)
    ARCHIVED("已封存");    // 這個是第 2 位 (ordinal = 2)

    // 底下的邏輯完全不需要變動
    private final String displayName;

    NewsStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

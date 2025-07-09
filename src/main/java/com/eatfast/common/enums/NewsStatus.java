package com.eatfast.common.enums;

/**
 * 升級版的消息狀態枚舉，帶有中文顯示名稱
 */
public enum NewsStatus {

    // 1. 為每個枚舉值，在括號中加上它的中文顯示名稱
    PUBLISHED("已發布"),
    DRAFT("草稿"),
    ARCHIVED("已封存");

    // 2. 新增一個 final 欄位來儲存這個中文名稱
    private final String displayName;

    // 3. 建立建構子，讓枚舉在建立時能把中文名稱存到欄位裡
    NewsStatus(String displayName) {
        this.displayName = displayName;
    }

    // 4. 提供一個 public 的 getter 方法，讓 Thymeleaf 可以透過這個方法取得中文名稱
    public String getDisplayName() {
        return displayName;
    }
}
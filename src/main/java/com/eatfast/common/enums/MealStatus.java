/*
 * 檔案路徑: src/main/java/com/eatfast/common/enums/MealStatus.java
 * 說明: 確保有 public 的 getDescription() 方法
 */
package com.eatfast.common.enums;

// [不可變動的關鍵字/語法]: public enum
public enum MealStatus {
    // [可自定義名稱]: AVAILABLE, UNAVAILABLE
    UNAVAILABLE("下架"),
    AVAILABLE("上架");

    // [可自定義變數名稱]: description
    // 這是私有屬性，需要公開的 getter 方法才能被 Thymeleaf 存取
    private final String description;

    // [不可變動的關鍵字/語法]: private MealStatus
    // [可自定義參數名稱]: description
    private MealStatus(String description) {
        this.description = description;
    }

    // [不可變動的關鍵字/語法]: public String getDescription()
    // 說明: 這是非常重要的部分！Thymeleaf 會透過這個 public getter 來取得 description 的值。
    public String getDescription() {
        return description;
    }
}
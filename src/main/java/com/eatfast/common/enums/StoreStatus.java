/*
 * ================================================================
 *  門市狀態 Enum (StoreStatus)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/common/enums/StoreStatus.java
 * - 作用: 作為 StoreEntity 的狀態定義，取代 "魔術字串"，提升型別安全。
 */
package com.eatfast.common.enums;

// [不可變動的關鍵字/語法]: public enum
// 說明: 'enum' 是 Java 用於定義列舉型別的關鍵字。
public enum StoreStatus {
    // [可自定義名稱]: OPERATING, RESTING, RENOVATING
    // 說明: 這些是列舉的實例，它們的名稱將會被直接存入資料庫。
    OPERATING("營業中"),
    RESTING("休息中"),
    RENOVATING("裝修中");

    private final String description;

    // [不可變動的關鍵字/語法]: private StoreStatus(String)
    // 說明: enum 的建構子，用於初始化每個列舉實例的額外屬性。
    private StoreStatus(String description) {
        this.description = description;
    }

    // [可自定義方法]: getDescription
    // 說明: 提供一個方法讓外部可以取得狀態的中文描述。
    public String getDescription() {
        return description;
    }
}
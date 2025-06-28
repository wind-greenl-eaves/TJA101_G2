/*
 * ================================================================
 *  供應狀態 Enum (SupplyStatus) - 【新增】
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/common/enums/SupplyStatus.java
 * - 作用: 為 StoreMealStatusEntity 的 status 欄位提供型別安全。
 */
package com.eatfast.common.enums;

// [不可變動的關鍵字/語法]: public enum
// 說明: 'enum' 是 Java 用於定義列舉型別的關鍵字。
public enum SupplyStatus {
    // [可自定義名稱]: SOLD_OUT, SUPPLYING
    // 說明: 這些是列舉的實例，其順序(ORDINAL)對應資料庫的 0 和 1。
    SOLD_OUT,   // 已售完 (對應 0)
    SUPPLYING   // 供應中 (對應 1)
}
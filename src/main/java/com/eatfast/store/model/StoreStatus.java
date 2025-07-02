/*
 * ================================================================
 * 檔案 1: StoreStatus.java (★★ 核心修正 ★★)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/store/model/StoreStatus.java
 * - 核心改動: 將 enum 宣告為 public，使其能被其他套件 (如 service, controller) 存取。
 */
package com.eatfast.store.model;

/**
 * 門市營業狀態列舉。
 * 宣告為 public，才能被專案中不同 package 的類別引用。
 */
public enum StoreStatus {
    O, // 代表營業中 (Open)
    R, // 代表休息中 (Resting)
    E  // 代表已停業 (Ended)
}
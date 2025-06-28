/*
 * ================================================================
 * 檔案 1: OrderStatus.java (★★ 新增的獨立檔案 ★★)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/orderlist/model/OrderStatus.java
 * - 核心改動:
 * 1. 獨立檔案: 將 OrderStatus Enum 移至其專屬的 .java 檔案。
 * 2. 公開權限: 將其宣告為 public，使其能被專案中任何位置的程式碼引用。
 */
package com.eatfast.orderlist.model;

/**
 * 訂單狀態列舉 (Order Status Enum)
 * <p>
 * ORDINAL 順序: PENDING(0), PREPARING(1), COMPLETED(2), CANCELLED(3)，
 * 與資料庫儲存的 0, 1, 2, 3 設計一致。
 */
public enum OrderStatus {
    PENDING,     // 待處理 (0)
    PREPARING,   // 準備中 (1)
    COMPLETED,   // 已完成 (2)
    CANCELLED    // 已取消 (3)
}
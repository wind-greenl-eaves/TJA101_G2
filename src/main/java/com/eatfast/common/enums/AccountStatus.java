/*
 * 檔案路徑: src/main/java/com/eatfast/common/enums/AccountStatus.java
 * 說明:
 * 這是為取代 status 欄位的整數 (0, 1) 而建立的共享列舉。
 * 1. 提升可讀性: `AccountStatus.ACTIVE` 比數字 `1` 更容易理解。
 * 2. 提升型別安全: 編譯器會確保只能傳入此列舉定義好的值，避免傳入無效數字。
 * 3. 順序對應資料庫: ORDINAL 順序 (ACTIVE=0, INACTIVE=1) 可依需求調整，此處假設 1 為啟用。
 */
package com.eatfast.common.enums; // 建議的共通 package 路徑

public enum AccountStatus {
    INACTIVE, // 停用 (對應資料庫的 0)
    ACTIVE    // 啟用 (對應資料庫的 1)
}
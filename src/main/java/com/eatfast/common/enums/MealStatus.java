/*
 * 檔案路徑: src/main/java/com/eatfast/common/enums/MealStatus.java
 * 說明:
 * 為了取代 meal.status 欄位的整數 (0, 1) 而建立的共享列舉。
 * 1. 提升可讀性: MealStatus.AVAILABLE 比數字 1 更容易理解。
 * 2. 提升型別安全: 編譯器會確保只能傳入此列舉定義好的值。
 */
package com.eatfast.common.enums;

// [不可變動的關鍵字/語法]: public enum
// 說明: 'enum' 是 Java 用於定義列舉型別的關鍵字。
public enum MealStatus {
	// [可自定義名稱]: UNAVAILABLE, AVAILABLE
	// 說明: 這些是列舉的實例，其順序(ORDINAL)很重要。
	UNAVAILABLE, // 下架 (對應資料庫的 0)
	AVAILABLE // 上架 (對應資料庫的 1)
}
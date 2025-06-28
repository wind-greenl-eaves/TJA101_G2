/*
 * ================================================================
 * 共享的 Gender Enum (Single Source of Truth)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/common/enums/Gender.java
 * - 作用: 作為整個專案唯一的 Gender 定義，所有需要用到的地方都必須 import 此檔案。
 */
package com.eatfast.common.enums;

// [不可變動的關鍵字/語法]: public enum
// 說明: 'enum' 是 Java 用於定義列舉型別的關鍵字。
public enum Gender {
    // [可自定義名稱]: M, F, O
    // 說明: 這些是列舉的實例。
    M, // 男性
    F, // 女性
    O  // 其他
}
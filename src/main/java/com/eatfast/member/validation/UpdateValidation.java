/*
 * ================================================================
 * 檔案: 會員更新驗證群組 (UpdateValidation)
 * ================================================================
 * - 核心作用: 定義會員資料更新時的驗證群組
 * - 用途: 與 @Validated(UpdateValidation.class) 配合使用
 */
package com.eatfast.member.validation;

/**
 * 會員更新驗證群組介面
 * 用於區分新增和更新時的不同驗證規則
 */
public interface UpdateValidation {
    // 空介面，作為驗證群組標記使用
}
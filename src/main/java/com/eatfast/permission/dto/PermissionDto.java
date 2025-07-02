package com.eatfast.permission.dto; // 可自定義的 package 路徑

/**
 * [可自定義的類別名稱]: PermissionDto
 * 權限資料傳輸物件 (Data Transfer Object)。
 * 用於在 Service 層與 Controller/View 層之間傳遞權限資訊，避免直接暴露資料庫實體。
 */
public class PermissionDto {

    /** 權限系統 ID (主鍵) */
    private Long permissionId;

    /** 權限功能說明 */
    private String description;

    // ================================================================
    //             標準 Getters and Setters
    // ================================================================

    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
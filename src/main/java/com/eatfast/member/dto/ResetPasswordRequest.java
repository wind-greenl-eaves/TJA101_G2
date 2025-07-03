package com.eatfast.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 密碼重設請求 DTO
 * 用於處理會員透過重設連結設定新密碼的資料
 */
public class ResetPasswordRequest {

    /**
     * 重設密碼的 Token
     */
    @NotBlank(message = "重設連結無效")
    private String token;

    /**
     * 新密碼
     */
    @NotBlank(message = "請輸入新密碼")
    @Size(min = 8, max = 100, message = "密碼長度必須介於 8 到 100 個字元之間")
    private String newPassword;

    /**
     * 確認新密碼
     */
    @NotBlank(message = "請再次輸入新密碼")
    private String confirmPassword;

    // Constructors
    public ResetPasswordRequest() {}

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    /**
     * 檢查兩次輸入的密碼是否一致
     */
    public boolean isPasswordMatching() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }

    @Override
    public String toString() {
        return "ResetPasswordRequest{token='" + token + "', passwordLength=" + 
               (newPassword != null ? newPassword.length() : 0) + "}";
    }
}
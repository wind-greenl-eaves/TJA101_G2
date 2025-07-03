package com.eatfast.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 忘記密碼請求 DTO
 * 用於處理會員忘記密碼時提交的資料
 */
public class ForgotPasswordRequest {

    /**
     * 電子郵件地址
     * 用於找到對應的會員帳號並發送重設密碼連結
     */
    @NotBlank(message = "請輸入您的電子郵件地址")
    @Email(message = "請輸入有效的電子郵件格式")
    private String email;

    // Constructors
    public ForgotPasswordRequest() {}

    public ForgotPasswordRequest(String email) {
        this.email = email;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "ForgotPasswordRequest{email='" + email + "'}";
    }
}
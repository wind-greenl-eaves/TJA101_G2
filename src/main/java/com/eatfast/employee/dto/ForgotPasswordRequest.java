package com.eatfast.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 忘記密碼請求 DTO
 * 用於接收忘記密碼表單資料
 */
public class ForgotPasswordRequest {

    /** 員工帳號或電子郵件 */
    @NotBlank(message = "員工帳號或電子郵件不可為空")
    @Size(max = 100, message = "輸入長度不可超過 100 個字元")
    private String accountOrEmail;

    // ================================================================
    //             建構子
    // ================================================================
    public ForgotPasswordRequest() {}

    public ForgotPasswordRequest(String accountOrEmail) {
        this.accountOrEmail = accountOrEmail;
    }

    // ================================================================
    //             Getters and Setters
    // ================================================================
    public String getAccountOrEmail() {
        return accountOrEmail;
    }

    public void setAccountOrEmail(String accountOrEmail) {
        this.accountOrEmail = accountOrEmail;
    }

    @Override
    public String toString() {
        return "ForgotPasswordRequest{" +
                "accountOrEmail='" + accountOrEmail + '\'' +
                '}';
    }
}
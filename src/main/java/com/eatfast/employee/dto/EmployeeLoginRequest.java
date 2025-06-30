package com.eatfast.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 員工登入請求 DTO
 * 用於接收員工登入表單資料
 */
public class EmployeeLoginRequest {

    /** 員工登入帳號 */
    @NotBlank(message = "員工帳號不可為空")
    @Size(max = 50, message = "帳號長度不可超過 50 個字元")
    private String account;

    /** 員工登入密碼 */
    @NotBlank(message = "登入密碼不可為空")
    @Size(max = 255, message = "密碼長度不可超過 255 個字元")
    private String password;

    // ================================================================
    //             建構子
    // ================================================================
    public EmployeeLoginRequest() {}

    public EmployeeLoginRequest(String account, String password) {
        this.account = account;
        this.password = password;
    }

    // ================================================================
    //             Getters and Setters
    // ================================================================
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "EmployeeLoginRequest{" +
                "account='" + account + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}
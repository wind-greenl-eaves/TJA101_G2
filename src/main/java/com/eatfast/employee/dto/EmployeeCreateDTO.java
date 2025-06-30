package com.eatfast.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class EmployeeCreateDTO {
    
    @NotBlank(message = "員工姓名不可為空")
    @Size(min = 2, max = 50, message = "姓名長度必須在2到50個字元之間")
    private String username;

    @NotBlank(message = "登入帳號不可為空")
    @Size(min = 4, max = 50, message = "帳號長度必須在4到50個字元之間")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "帳號只能包含英文字母、數字和底線")
    private String account;

    @NotBlank(message = "電子郵件不可為空")
    @Email(message = "請輸入有效的電子郵件地址")
    private String email;

    @NotBlank(message = "聯絡電話不可為空")
    @Pattern(regexp = "^09\\d{8}$", message = "請輸入有效的手機號碼（格式：09xxxxxxxx）")
    private String phone;

    @NotBlank(message = "身分證字號不可為空")
    @Pattern(regexp = "^[A-Z][12]\\d{8}$", message = "請輸入有效的身分證字號")
    private String nationalId;

    @NotBlank(message = "密碼不可為空")
    @Size(min = 8, message = "密碼長度至少要8個字元")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", 
            message = "密碼必須包含至少一個字母和一個數字")
    private String password;

    @NotBlank(message = "員工角色不可為空")
    private String role;

    @NotBlank(message = "性別不可為空")
    private String gender;

    @NotNull(message = "門市編號不可為空")
    private Long storeId;

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }
}
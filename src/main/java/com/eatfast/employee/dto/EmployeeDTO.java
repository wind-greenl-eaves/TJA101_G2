package com.eatfast.employee.dto;

import com.eatfast.common.enums.AccountStatus;
import com.eatfast.common.enums.EmployeeRole;
import com.eatfast.common.enums.Gender;
import java.time.LocalDateTime;

/**
 * [可自定義的類別名稱]: EmployeeDto
 * 資料傳輸物件 (Data Transfer Object)。
 * 作為 Service 層與 Controller 層之間的資料載體，僅包含安全且必要的員工資訊。
 */
public class EmployeeDTO {

    /** 員工系統 ID (主鍵) */
    private Long employeeId;

    /** 員工真實姓名 */
    private String username;

    /** 員工登入帳號 */
    private String account;

    /** 員工聯絡電子郵件 */
    private String email;

    /** 員工聯絡電話 */
    private String phone;

    /** 員工角色 (Enum) */
    private EmployeeRole role;

    /** 帳號狀態 (Enum: 啟用/停用) */
    private AccountStatus status;

    /** 員工性別 (Enum) */
    private Gender gender;

    /** 員工身分證字號 */
    private String nationalId;

    /** 所屬門市 ID (外鍵) */
    private Long storeId;

    /** 所屬門市名稱 (來自關聯查詢) */
    private String storeName;

    /** 帳號建立時間 */
    private LocalDateTime createTime;

    /** 員工照片 URL */
    private String photoUrl;

    /** 員工密碼 */
    private String password;

    /** 員工明文密碼 (不儲存於資料庫) */
    private String rawPassword;

    // ================================================================
    //             標準 Getters and Setters
    // ================================================================

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

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

    public EmployeeRole getRole() {
        return role;
    }

    public void setRole(EmployeeRole role) {
        this.role = role;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRawPassword() {
        return rawPassword;
    }

    public void setRawPassword(String rawPassword) {
        this.rawPassword = rawPassword;
    }
}
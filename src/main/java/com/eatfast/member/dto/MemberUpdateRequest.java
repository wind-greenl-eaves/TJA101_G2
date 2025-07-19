/*
 * ================================================================
 * 檔案: 會員更新請求 DTO (資料傳輸物件)
 * ================================================================
 * - 核心作用:
 * 1. 職責單一: 專門承載「更新會員」時的資料。
 * 2. 安全性: 包含 memberId 用於定位，並刻意不含 account 和 password。
 */

/**
 * 【路徑】com.eatfast.member.dto:
 * 定義此 DTO 屬於 member (會員) 功能模組下的 dto (資料傳輸物件) 層。
 */
package com.eatfast.member.dto;

/**
 * 【路徑】引入必要的外部類別。
 */
import com.eatfast.common.enums.Gender;
import com.eatfast.member.validation.UpdateValidation;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * POJO (Plain Old Java Object)，只負責承載「更新」操作所需的資料。
 */

public class MemberUpdateRequest {

    //================================================================
    // 欄位定義 (Fields) & 驗證規則 (Validations)
    //================================================================
    
    /**
     * 會員 ID，用於定位要更新的紀錄。
     * - @NotNull: 更新時，此欄位為必填。
     */
    @NotNull(message = "更新時會員 ID 不可為空")
    private Long memberId;
    
    /**
     * 會員姓名。
     * - @NotBlank: 不可為空。
     * - @Size: 限制長度。
     */
    @NotBlank(message = "會員姓名：請勿空白", groups = UpdateValidation.class)
    @Size(min = 2, max = 20, message = "會員姓名：長度必須介於 2 到 20 個字元之間", groups = UpdateValidation.class)
    private String username;

    /**
     * 電子郵件。
     * - @Email: 驗證 Email 格式。
     */
    @NotBlank(message = "電子郵件：請勿空白", groups = UpdateValidation.class)
    @Email(message = "電子郵件：請填寫有效的格式", groups = UpdateValidation.class)
    @Size(max = 100, message = "電子郵件：長度不可超過 100 個字元", groups = UpdateValidation.class)
    private String email;

    /**
     * 連絡電話。
     * 【修正】統一電話號碼驗證正則表達式，與其他檔案保持一致
     * 支援格式：0912345678, 0912-345-678, 09-12345678, 098-185-569, 098-702-696, (02)12345678, 02-12345678
     */
    @NotBlank(message = "連絡電話：請勿空白", groups = UpdateValidation.class)
    @Pattern(regexp = "^(09\\d{8}|09\\d{1}[\\s-]\\d{3}[\\s-]\\d{3}|09\\d{2}[\\s-]\\d{3}[\\s-]\\d{3}|09[\\s-]\\d{8}|0[2-8][\\s-]?\\d{7,8}|\\(0[2-8]\\)\\d{7,8})$", 
             message = "連絡電話：請填寫有效的電話號碼格式（如：0912345678、0912-345-678、098-185-569、02-12345678）", 
             groups = UpdateValidation.class)
    private String phone;
    
    /**
     * 會員生日。
     * - @Past: 當有值時，驗證必須是過去的日期。
     * - 允許為 null，表示不修改生日欄位。
     */
    @Past(message = "會員生日：必須為過去的日期", groups = UpdateValidation.class)
    private LocalDate birthday;
    
    /**
     * 性別。
     */
    @NotNull(message = "性別：請勿空白", groups = UpdateValidation.class)
    private Gender gender;
    
    /**
     * 登入帳號（唯讀顯示用）。
     * 此欄位僅用於顯示，不參與更新驗證。
     */
    private String account;
    
    /**
     * 【修正】帳號啟用狀態。
     * 統一使用 isEnabled 命名，與 MemberEntity 保持一致
     */
    private Boolean isEnabled;
    
    /**
     * 會員註冊時間（唯讀顯示用）。
     * 此欄位僅用於顯示，不參與更新驗證。
     */
    private java.time.LocalDateTime createdAt;
    
    //================================================================
    // Getters and Setters
    //================================================================
    
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    
    // 新增的 getter/setter 方法
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
    
    /**
     * 【修正】統一方法命名 - 與 MemberEntity 保持一致
     */
    public Boolean isEnabled() { return isEnabled; }
    public void setEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }
    
    /**
     * 【新增】提供別名方法以維持向後兼容性
     */
    public Boolean getEnabled() { return isEnabled; }
    public Boolean getIsEnabled() { return isEnabled; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
}
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
 * POJO (Plain Old Java Object)，只負責攜帶「更新」操作所需的資料。
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
     * - @Pattern: 使用正規表示式驗證格式。
     */
    @NotBlank(message = "連絡電話：請勿空白", groups = UpdateValidation.class)
    @Pattern(regexp = "^09\\d{2}-?\\d{3}-?\\d{3}$", message = "連絡電話：請填寫有效的台灣手機號碼格式 (例如 0912-345-678)", groups = UpdateValidation.class)
    private String phone;
    
    /**
     * 會員生日。
     * - @Past: 驗證必須是過去的日期。
     */
    @NotNull(message = "會員生日：請勿空白", groups = UpdateValidation.class)
    @Past(message = "會員生日：必須為過去的日期", groups = UpdateValidation.class)
    private LocalDate birthday;
    
    /**
     * 性別。
     */
 // 【檔案路徑配對】: gender 欄位的型別必須是共享的 Gender
    @NotNull(message = "性別：請勿空白", groups = UpdateValidation.class)
    private Gender gender;
    
    /**
     * 帳號啟用狀態。
     */
    @NotNull(message = "帳號啟用狀態不可為空")
    private Boolean isEnabled;

    //================================================================
    // Getters and Setters
    //================================================================
    // 標準 Java 樣板程式碼，用於讓外部存取私有欄位。
    
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
    public Boolean getIsEnabled() { return isEnabled; }
    public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }
}
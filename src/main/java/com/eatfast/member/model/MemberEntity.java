package com.eatfast.member.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import com.eatfast.member.validation.CreateValidation;
import com.eatfast.member.validation.UpdateValidation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 									會員實體 (Member Entity)
 *
 * 核心特性:
 * 1.  資料庫映射: 對應 `member` 資料表，並於此處定義 `account` 與 `email` 的唯一約束。
 * 2.  軟刪除 (Soft Delete): 整合 `@SQLDelete` 與 `@SQLRestriction`，使刪除操作變為更新，且查詢時自動過濾已刪除資料。
 * 3.  分組驗證 (Validation Groups): 使用 `CreateValidation` 與 `UpdateValidation` 標記介面，區分新增與更新時的驗證規則。
 * 4.  不可變欄位: `account` 與 `createdAt` 透過 `@Column(updatable = false)` 設定，防止在更新時被修改。
 * 5.  欄位驗證: 使用 JSR-380 (Bean Validation) 註解，確保欄位符合業務規則。
 * 6.  性別欄位: 使用 `Gender` Enum 增強型別安全，並透過 `@Enumerated(EnumType.STRING)` 將 Enum 名稱映射至資料庫。
 */
@Entity
@Table(name = "member",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_account", columnNames = "account"),
        @UniqueConstraint(name = "uk_email", columnNames = "email")
    })
@SQLDelete(sql = "UPDATE member SET is_enabled = false WHERE member_id = ?")
@SQLRestriction("is_enabled = true")
public class MemberEntity {

    //================================================================
    // 							欄位定義 (Fields)
    //================================================================

    /** 會員編號 (主鍵): 對應資料庫的 `member_id`，使用資料庫自增策略 (IDENTITY)。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    /** 會員真實姓名: 對應資料庫的 `username` 欄位。 */
    @NotBlank(message = "使用者姓名不可為空", groups = {CreateValidation.class, UpdateValidation.class})
    @Size(min = 2, max = 20, message = "使用者姓名長度必須在 2 到 20 個字之間", groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "username", nullable = false, length = 20)
    private String username;

    /** 登入帳號: 經由 `updatable = false` 設定，此欄位在建立後不可修改。 */
    @NotBlank(message = "登入帳號不可為空", groups = CreateValidation.class)
    @Size(min = 4, max = 50, message = "登入帳號長度必須在 4 到 50 個字之間", groups = CreateValidation.class)
    @Column(name = "account", nullable = false, updatable = false, length = 50)
    private String account;

    /** 登入密碼: 應儲存經 BCrypt 加密後的雜湊值。 */
    @NotBlank(message = "密碼不可為空", groups = CreateValidation.class)
    @Size(min = 8, message = "密碼長度至少需要 8 個字元", groups = CreateValidation.class)
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /** 電子郵件: 作為登入或通知的唯一識別之一。 */
    @NotBlank(message = "電子郵件不可為空", groups = {CreateValidation.class, UpdateValidation.class})
    @Email(message = "電子郵件格式不正確", groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    /** 連絡電話: 格式為 09xx-xxxxxx。 */
    @NotBlank(message = "連絡電話不可為空", groups = {CreateValidation.class, UpdateValidation.class})
    @Pattern(regexp = "^09\\d{2}-\\d{6}$", message = "電話號碼格式不正確，應為 09xx-xxxxxx", groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    /** 會員生日: 儲存會員的出生日期，驗證規則確保其為過去的日期。 */
    @NotNull(message = "生日不可為空", groups = {CreateValidation.class, UpdateValidation.class})
    @Past(message = "生日必須是過去的日期", groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "birthday", nullable = false)
    private LocalDate birthday; // LocalDate 代表日期，不包含時間資訊，適合用於生日等場景。

    /** 性別: 使用 Gender Enum 增強型別安全。透過 `@Enumerated(EnumType.STRING)` 將 Enum 名稱映射至資料庫。 */
    @NotNull(message = "性別不可為空", groups = {CreateValidation.class, UpdateValidation.class})
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 1)
    private Gender gender;

    /** 軟刪除標記: 此欄位的狀態由類別層級的 `@SQLDelete` 與 `@SQLRestriction` 自動管理。預設為 `true` (啟用)。 */
    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    /** 最後更新時間: 每次更新時，由 `@UpdateTimestamp` 自動填入當前時間。 */
    @UpdateTimestamp
    @Column(name = "last_updated_at", insertable = false)
    private LocalDateTime lastUpdatedAt; // LocalDateTime 代表日期與時間，適合用於記錄資料的更新時間。

    /** 帳號註冊時間: 首次建立時，由 `@CreationTimestamp` 自動填入，且不可更新。 */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    //================================================================
    //					 建構子 (Constructors)
    //================================================================

    /**
     * JPA 規範要求之無參數建構子。
     * 受保護的存取層級可防止外部直接實例化。
     */
    public MemberEntity() {
    }

    /**
     * 用於建立新會員實體的有參數建構子。
     *
     * @param username     會員真實姓名
     * @param account      登入帳號
     * @param password     登入密碼 (通常為未加密的原始密碼，將在 Service 層加密)
     * @param email        電子郵件
     * @param phone        連絡電話
     * @param birthday     會員生日
     * @param gender       性別 (使用 Gender Enum)
     */
    public MemberEntity(String username, String account, String password, String email, String phone, LocalDate birthday, Gender gender) {
        this.username = username;
        this.account = account;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.birthday = birthday;
        this.gender = gender;
    }

    //================================================================
    //					 Getters and Setters
    //================================================================

    /**
     * @return 會員編號 (主鍵)
     */
    public Long getMemberId() {
        return memberId;
    }

    /**
     * @param memberId 要設定的會員編號
     */
    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    /**
     * @return 會員真實姓名
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username 要設定的會員真實姓名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return 登入帳號
     */
    public String getAccount() {
        return account;
    }

    /**
     * @param account 要設定的登入帳號
     */
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * @return 登入密碼 (加密後的雜湊值)
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password 要設定的登入密碼 (加密後的雜湊值)
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return 電子郵件
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email 要設定的電子郵件
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return 連絡電話
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone 要設定的連絡電話
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * @return 會員生日
     */
    public LocalDate getBirthday() {
        return birthday;
    }

    /**
     * @param birthday 要設定的會員生日
     */
    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }
    
    /**
     * @return 性別 (Gender Enum)
     */
    public Gender getGender() {
        return gender;
    }

    /**
     * @param gender 要設定的性別 (Gender Enum)
     */
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    
    /**
     * @return 帳號是否啟用 (軟刪除標記)
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * @param enabled 要設定的帳號啟用狀態
     */
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    /**
     * @return 資料最後更新時間
     */
    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    /**
     * @param lastUpdatedAt 要設定的資料最後更新時間
     */
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    /**
     * @return 帳號註冊時間
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt 要設定的帳號註冊時間
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    //================================================================
    // 物件核心方法 (equals, hashCode, toString)
    //================================================================

    /**
     * 覆寫 toString() 方法，用於日誌輸出與偵錯。
     * **重要**: 此處已將 `password` 欄位遮蔽為 '[PROTECTED]'，避免在日誌中洩漏敏感資訊。
     * @return 此會員實體的字串表示法。
     */
    @Override
    public String toString() {
        return "MemberEntity{" +
                "memberId=" + memberId +
                ", username='" + username + '\'' +
                ", account='" + account + '\'' +
                ", password='[PROTECTED]'" +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", birthday=" + birthday +
                ", gender=" + gender +
                ", isEnabled=" + isEnabled +
                ", lastUpdatedAt=" + lastUpdatedAt +
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * 覆寫 equals() 方法。
     * 比較邏輯僅基於主鍵 `memberId`，以確保物件在不同持久化狀態（例如，transient, managed, detached）下的一致性。
     * @param o 要比較的物件
     * @return 如果 `memberId` 相同，則返回 `true`，否則返回 `false`。
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberEntity that = (MemberEntity) o;
        return Objects.equals(memberId, that.memberId);
    }

    /**
     * 覆寫 hashCode() 方法。
     * 雜湊碼計算僅基於主鍵 `memberId`，確保與 `equals()` 方法的行為一致。
     * @return 基於 `memberId` 計算的雜湊碼。
     */
    @Override
    public int hashCode() {
        return Objects.hash(memberId);
    }
}
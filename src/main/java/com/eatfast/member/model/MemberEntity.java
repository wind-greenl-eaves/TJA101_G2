package com.eatfast.member.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import com.eatfast.member.validation.CreateValidation;
import com.eatfast.member.validation.UpdateValidation;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

//------------------<li><b>【暫時註解】</b>與訂單、購物車等的關聯，等待其他模組完成後再啟用。</li>----------------
/**
*●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●會員資料實體 (Member Entity)●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●
*  												--- 核心設計 ---
*
* 對應資料庫 `member` 資料表。
* 使用 Validation Groups 區分「新增」與「更新」的驗證邏輯。
* 整合軟刪除 (Soft Delete)，查詢時自動排除 `is_enabled = false` 的資料,
* 安全：避免資料被誤刪，隨時可以「復活」不需要在每個查詢方法上手動加條件，框架自動搞定 減少了出錯的機會。
 */
@Entity
@Table(name = "member",
    uniqueConstraints = {				// 在資料庫層級建立唯一約束，確保帳號和 Email 的唯一性，提供最終的資料保障。
        @UniqueConstraint(name = "uk_account", columnNames = "account"),
        @UniqueConstraint(name = "uk_email", columnNames = "email")
    })	
										// 定義軟刪除邏輯：執行刪除時，實際是更新 is_enabled 旗標。
@SQLDelete(sql = "UPDATE member SET is_enabled = false WHERE member_id = ?")
@SQLRestriction("is_enabled = true")	// Hibernate 6+ 的新特性，會讓所有查詢自動加上 "is_enabled = true" 條件。
public class MemberEntity {

    /** 主鍵 (Primary Key)，使用資料庫自動增長 (AUTO_INCREMENT) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    /** 使用者姓名 */
    @NotBlank(message = "使用者姓名不可為空", groups = {CreateValidation.class, UpdateValidation.class})
    @Size(min = 2, max = 20, message = "使用者姓名長度必須在 2 到 20 個字之間", groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "username", nullable = false, length = 20)
    private String username;

    /** 登入帳號。重點：`updatable = false` 確保帳號一經建立便不可修改。*/
    @NotBlank(message = "登入帳號不可為空", groups = CreateValidation.class)
    @Size(min = 4, max = 50, message = "登入帳號長度必須在 4 到 50 個字之間", groups = CreateValidation.class)
    @Column(name = "account", nullable = false, updatable = false, length = 50)
    private String account;

    /** 密碼。僅在 `CreateValidation` 群組中驗證，更新時不處理。長度 255 是為了儲存加密後的雜湊值。*/
    @NotBlank(message = "密碼不可為空", groups = CreateValidation.class)
    @Size(min = 8, message = "密碼長度至少需要 8 個字元", groups = CreateValidation.class)
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /** 電子郵件 */
    @NotBlank(message = "電子郵件不可為空", groups = {CreateValidation.class, UpdateValidation.class})
    @Email(message = "電子郵件格式不正確", groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    /** 連絡電話 */
    @NotBlank(message = "連絡電話不可為空", groups = {CreateValidation.class, UpdateValidation.class})
    // 使用正則表達式驗證電話號碼格式，符合台灣手機號碼格式 09xx-xxxxxx。
    @Pattern(regexp = "^09\\d{2}-\\d{6}$", message = "電話號碼格式不正確，應為 09xx-xxxxxx", groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    /** 生日 */
    @NotNull(message = "生日不可為空", groups = {CreateValidation.class, UpdateValidation.class})
    // 使用 LocalDate 來儲存日期，避免時區問題。
    @Past(message = "生日必須是過去的日期", groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "birthday", nullable = false)
    private LocalDate birthday;
    
    /** 性別。使用 String 對應資料庫的 CHAR(1)，未來可考慮改為 Enum 以增強型別安全。 */
    @NotBlank(message = "性別不可為空", groups = {CreateValidation.class, UpdateValidation.class})
    @Pattern(regexp = "[MFO]", message = "性別必須為 M, F, 或 O", groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "gender", nullable = false, length = 1)
    private String gender;

    /** 軟刪除旗標。`true` 表示啟用，`false` 表示已刪除。預設為 `true`。 */
    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    /** 最後更新時間。由 Hibernate 自動維護。 */
    @UpdateTimestamp
    @Column(name = "last_updated_at", insertable = false)
    private LocalDateTime lastUpdatedAt;

    /** 建立時間。重點：`updatable = false`，僅在建立時寫入一次，後續不會被更新。 */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
//============================================================================================================
    //																										
    // 							【關聯映射】--- 對應 SQL Foreign Key ---										
    /**																										
     * 【一對多】會員 與 訂單 (OrderList) 的關係 一個會員可以有多筆訂單。										
     * - mappedBy = "member": 指出這段關係是由 `OrderListEntity` 中的 `member` 屬性來維護。					
     * JPA 不會在 `member` 這邊產生額外的關聯欄位。															
     * - fetch = FetchType.LAZY: 延遲載入。只有當程式碼實際存取 `getOrders()` 時，才會去資料庫查詢訂單資料。	
     */																										
////    // 移除 CascadeType.REMOVE，只保留其他必要操作
//    @OneToMany(mappedBy = "member", 
//            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, 
//            fetch = FetchType.LAZY)
// private Set<OrderListEntity> orders = new HashSet<>();												
//    				                                                                                        
//    /**
//     * 【一對多】會員 與 購物車項目 (Cart) 的關係 一個會員可以有多個購物車項目。
//     * - cascade = CascadeType.ALL: 當儲存或刪除會員時，會一併處理其購物車內的項目。
//     * - orphanRemoval = true: 若某個購物車項目從會員的 `cartItems` 集合中被移除，
//     */
//    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    private Set<CartEntity> cartItems = new HashSet<>();
//    
//    /**
//     * 【一對多】會員 與 意見回饋 (Feedback) 的關係。
//     * - 一個會員可以有多筆意見回饋。
//     */
//    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private Set<FeedbackEntity> feedbacks = new HashSet<>();
//    
//    /**
//     * 【一對多】會員 與 我的最愛 (Fav) 的關係。
//     * - 這邊映射的是 `fav` 這張中介表，而非直接對應 `meal`。
//     * 如此一來，`FavEntity` 可以更完整地對應 `fav` 資料表的結構。
//     * - 一個會員可以有多筆收藏紀錄。
//     */
//    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    private Set<FavEntity> favorites = new HashSet<>();

//============================================================================================================
    
    // --- 建構子 (Constructors) ---

    /** JPA 規範要求，必須提供一個無參數的建構子。 */
    public MemberEntity() {
    }

    /** 方便快速建立物件的建構子。 */
    public MemberEntity(String username, String account, String password, String email, String phone, LocalDate birthday, String gender) {
        this.username = username;
        this.account = account;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.birthday = birthday;
        this.gender = gender;
    }

    // --- Getters and Setters (Java Bean 標準方法) ---

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }
    
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // --- 物件核心方法 (Object method overrides) ---

    /** 覆寫 toString()，隱藏 password，避免敏感資訊外洩至日誌。 */
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
                ", gender='" + gender + '\'' +
                ", isEnabled=" + isEnabled +
                ", lastUpdatedAt=" + lastUpdatedAt +
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * 覆寫 equals() 與 hashCode()，僅比對主鍵。
     * 這是 JPA Entity 的最佳實踐，可避免因其他欄位變動導致在 Set 或 Map 中行為異常。
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberEntity that = (MemberEntity) o;
        return Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId);
    }
}
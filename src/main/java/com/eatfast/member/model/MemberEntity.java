package com.eatfast.member.model;

// 引入共享 Enum 與所有關聯實體
import com.eatfast.common.enums.Gender;
import com.eatfast.fav.model.FavEntity;
import com.eatfast.orderlist.model.OrderListEntity;
import com.eatfast.cart.model.CartEntity;
import com.eatfast.feedback.model.FeedbackEntity;

// 引入 Jakarta Validation API 進行資料驗證
import com.eatfast.member.validation.CreateValidation;
import com.eatfast.member.validation.UpdateValidation;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

// 引入 Hibernate 專屬優化與功能註解
import org.hibernate.annotations.*;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;

// 引入 Jackson 註解以處理 JSON 序列化
import com.fasterxml.jackson.annotation.JsonIgnore;

/*
 * ================================================================
 * MemberEntity.java (收藏關聯重構定版)
 * ================================================================
 * - 審查結論:
 * 1. 【關聯重構】: 已移除 @ManyToMany 關聯，並正確建立對 FavEntity 的 @OneToMany
 * 關聯，符合團隊決議。
 * 2. 【邏輯校正】:
 * - `orders` 關聯: 無級聯刪除，符合資料庫 ON DELETE RESTRICT 約束。
 * - `favorites`, `cartItems`, `feedback`: 正確配置級聯與孤兒移除，
 * 確保了依賴物件的生命週期與會員一致。
 * 3. 【結構完整】:
 * - 已修正 Getter/Setter 與成員變數不匹配的問題。
 * - 所有 JPA 與 Hibernate 註解均已正確配置。
 */

/**
 * 會員實體 (Member Entity)
 * <p>
 * 核心功能:
 * - 映射資料庫 `member` 表。
 * - 實現軟刪除機制 (@SQLDelete, @SQLRestriction)。
 * - 定義與訂單、收藏、購物車、意見回饋的一對多關聯。
 */
@Entity
@Table(name = "member")
@SQLDelete(sql = "UPDATE member SET is_enabled = false WHERE member_id = ?")
@SQLRestriction("is_enabled = true")
@DynamicUpdate
public class MemberEntity {

    //================================================================
    // 							欄位定義
    //================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @NotBlank(message = "會員姓名：請勿空白", groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "username", nullable = false, length = 20)
    private String username;

    @NaturalId // 定義為業務上的自然主鍵，提升查詢效能
    @NotBlank(message = "登入帳號：請勿空白", groups = CreateValidation.class)
    @Column(name = "account", nullable = false, updatable = false, length = 50, unique = true)
    private String account;

    @NotBlank(message = "登入密碼：請勿空白", groups = CreateValidation.class)
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @NaturalId
    @NotBlank(message = "電子郵件：請勿空白", groups = {CreateValidation.class, UpdateValidation.class})
    @Email(message = "電子郵件：請填寫有效的格式", groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    @NotBlank(message = "連絡電話：請勿空白", groups = {CreateValidation.class, UpdateValidation.class})
    @Pattern(regexp = "^09\\d{2}-?\\d{3}-?\\d{3}$", message = "連絡電話：請填寫有效的台灣手機號碼格式", groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;
    
    @NotNull(message = "會員生日：請勿空白", groups = {CreateValidation.class, UpdateValidation.class})
    @Past(message = "會員生日：必須為過去的日期", groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "birthday", nullable = false)
    private LocalDate birthday;
    
    @NotNull(message = "性別：請勿空白", groups = {CreateValidation.class, UpdateValidation.class})
    @Enumerated(EnumType.STRING) // 建議使用 STRING 儲存，以增加可讀性與安全性
    @Column(name = "gender", nullable = false, length = 1)
    private Gender gender;
    
    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    @UpdateTimestamp
    @Column(name = "last_updated_at", insertable = false)
    private LocalDateTime lastUpdatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    // ================================================================
    //         反向一對多關聯 (Bidirectional @OneToMany)
    // ================================================================
    
    /**
     * 此會員的所有訂單。
     * 對應資料庫 ON DELETE RESTRICT，不設定級聯刪除。
     */
    @JsonIgnore
    @BatchSize(size = 10) // 優化: 抓取關聯集合時，每次最多抓 10 筆
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private Set<OrderListEntity> orders = new HashSet<>();

    /**
     * 此會員的所有收藏紀錄。
     * 級聯關係設為 ALL，刪除會員時將一併清除其收藏紀錄。
     */
    @JsonIgnore
    @BatchSize(size = 10)
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<FavEntity> favorites = new HashSet<>();

    /**
     * 此會員的購物車項目。
     */
    @JsonIgnore
    @BatchSize(size = 10)
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<CartEntity> cartItems = new HashSet<>();

    /**
     * 此會員的所有意見回饋。
     */
    @JsonIgnore
    @BatchSize(size = 10)
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<FeedbackEntity> feedback = new HashSet<>();

    //================================================================
    //					 建構子、Getters、Setters
    //================================================================
    public MemberEntity() {}
    
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean isEnabled) { this.isEnabled = isEnabled; }
    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Set<OrderListEntity> getOrders() { return orders; }
    public void setOrders(Set<OrderListEntity> orders) { this.orders = orders; }
    
    public Set<FavEntity> getFavorites() { return favorites; }
    public void setFavorites(Set<FavEntity> favorites) { this.favorites = favorites; }

    public Set<CartEntity> getCartItems() { return cartItems; }
    public void setCartItems(Set<CartEntity> cartItems) { this.cartItems = cartItems; }
    public Set<FeedbackEntity> getFeedback() { return feedback; }
    public void setFeedback(Set<FeedbackEntity> feedback) { this.feedback = feedback; }

    //================================================================
    // 物件核心方法 (equals, hashCode, toString)
    //================================================================
    @Override
    public String toString() {
        return "MemberEntity{" +
                "memberId=" + memberId +
                ", username='" + username + '\'' +
                ", account='" + account + '\'' +
                ", isEnabled=" + isEnabled +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberEntity that = (MemberEntity) o;
        if (this.account != null && that.account != null) {
            return Objects.equals(account, that.account);
        }
        if (this.memberId == null || that.memberId == null) {
            return false;
        }
        return Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return account != null ? Objects.hash(account) : getClass().hashCode();
    }
}
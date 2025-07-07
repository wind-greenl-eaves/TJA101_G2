package com.eatfast.member.model;

// ↓↓↓ 基礎資料型別與列舉的引入 ↓↓↓
import com.eatfast.common.enums.Gender;  // 性別列舉，定義在 com.eatfast.common.enums 包下
import java.time.LocalDate;              // 生日使用的日期類型
import java.time.LocalDateTime;          // 時間戳記使用的日期時間類型
import java.util.HashSet;               // 用於存放關聯集合的資料結構
import java.util.Objects;               // 用於 equals 和 hashCode 方法
import java.util.Set;                   // 集合介面

// ↓↓↓ 關聯實體的引入（一對多關係中的「多」方） ↓↓↓
import com.eatfast.fav.model.FavEntity;           // 收藏紀錄實體，路徑: /fav/model/FavEntity
import com.eatfast.orderlist.model.OrderListEntity; // 訂單實體，路徑: /orderlist/model/OrderListEntity
import com.eatfast.cart.model.CartEntity;         // 購物車實體，路徑: /cart/model/CartEntity
import com.eatfast.feedback.model.FeedbackEntity; // 意見回饋實體，路徑: /feedback/model/FeedbackEntity

// ↓↓↓ 驗證相關的引入 ↓↓↓
import com.eatfast.member.validation.CreateValidation;  // 創建驗證群組，路徑: /member/validation/CreateValidation
import com.eatfast.member.validation.UpdateValidation;  // 更新驗證群組，路徑: /member/validation/UpdateValidation
import jakarta.validation.constraints.*;  // 所有驗證註解，如 @NotBlank, @Email 等

// ↓↓↓ Hibernate 和 JPA 相關的引入 ↓↓↓
import org.hibernate.annotations.*;        // Hibernate 特有的註解，如 @DynamicUpdate
import jakarta.persistence.*;             // JPA 標準註解，如 @Entity, @Id 等
import jakarta.persistence.CascadeType;   // 級聯操作類型
import jakarta.persistence.Table;         // 資料表映射註解

// ↓↓↓ JSON 序列化相關的引入 ↓↓↓
import com.fasterxml.jackson.annotation.JsonIgnore;  // 防止 JSON 循環引用的註解

/**
 * 會員實體類別 - 對應資料庫中的 member 表
 * 
 * 【重要路徑說明】
 * 1. 資料庫對應：@Table(name = "member") 
 *    → 對應到資料庫中的 member 表
 * 
 * 2. 關聯路徑：
 *    - 訂單：member(1) → orders(多)，透過 member_id 欄位關聯
 *    - 收藏：member(1) → favorites(多)，透過 member_id 欄位關聯
 *    - 購物車：member(1) → cart_items(多)，透過 member_id 欄位關聯
 *    - 意見回饋：member(1) → feedback(多)，透過 member_id 欄位關聯
 *
 * 3. URL 路徑：
 *    - 查詢：GET /member/api/detail/{memberId}
 *    - 新增：POST /member/insert
 *    - 修改：POST /member/update
 *    - 刪除：POST /member/delete
 */

@Entity  // 標記這是一個實體类別，會對應到資料庫的表
@Table(name = "member")  // 指定對應的資料表名稱
@SQLDelete(sql = "UPDATE member SET is_enabled = false WHERE member_id = ?")  // 軟刪除的SQL語句
@SQLRestriction("is_enabled = true")  // 查詢時只顯示未刪除的記錄
@DynamicUpdate  // 只更新有變更的欄位，提升效能
public class MemberEntity {

    //================================================================
    // 							欄位定義
    //================================================================

    /**
     * 會員ID - 主鍵
     * 1. @Id：標記這是主鍵
     * 2. @GeneratedValue：自動生成值
     * 3. 對應資料表欄位：member_id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    /**
     * 會員姓名
     * 1. @NotBlank：不能為空白
     * 2. 驗證群組：新增和修改時都要檢查
     * 3. 對應資料表欄位：username
     */
    @NotBlank(message = "會員姓名：請勿空白", groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "username", nullable = false, length = 20)
    private String username;

    /**
     * 登入帳號 - 自然鍵
     * 1. @NaturalId：標記為業務邏輯上的唯一識別碼
     * 2. @Column(unique = true)：確保資料庫層級的唯一性
     * 3. updatable = false：建立後不可修改
     */
    @NaturalId
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
    @Pattern(regexp = "^(09\\d{8}|09\\d{2}[\\s-]\\d{3}[\\s-]\\d{3}|09[\\s-]\\d{8}|0[2-8][\\s-]?\\d{7,8}|\\(0[2-8]\\)\\d{7,8})$", 
             message = "連絡電話：請填寫有效的電話號碼格式（如：0912345678、0912-345-678、02-12345678）", 
             groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;
    
    @NotNull(message = "會員生日：請勿空白", groups = {CreateValidation.class, UpdateValidation.class})
    @Past(message = "會員生日：必須為過去的日期", groups = {CreateValidation.class, UpdateValidation.class})
    @Column(name = "birthday", nullable = false)
    private LocalDate birthday;
    
    @NotNull(message = "性別：請勿空白", groups = {CreateValidation.class, UpdateValidation.class})
    @Enumerated(EnumType.STRING) // 修正：使用 STRING 儲存，符合資料庫 CHAR(1) 和 SQL 插入的 'M','F','O' 設計
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
     * 
     * 【路徑說明】
     * 1. 資料庫：member表 ←→ order_list表（透過 member_id 欄位）
     * 2. 程式碼：MemberEntity ←→ OrderListEntity（透過 @OneToMany）
     * 3. @JsonIgnore：防止JSON序列化時的循環引用
     * 4. mappedBy = "member"：對應到 OrderListEntity 中的 member 屬性
     */
    @JsonIgnore
    @BatchSize(size = 10) // 優化: 抓取關聯集合時，每次最多抓 10 筆
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private Set<OrderListEntity> orders = new HashSet<>();

    /**
     * 此會員的所有收藏紀錄。
     * 級聯關係設為 ALL，刪除會員時將一併清除其收藏紀錄。
     * 
     * 【路徑說明】
     * 1. 資料庫：member表 ←→ fav表（透過 member_id 欄位）
     * 2. 程式碼：MemberEntity ←→ FavEntity（透過 @OneToMany）
     * 3. cascade = ALL：刪除會員時，相關收藏也會被刪除
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
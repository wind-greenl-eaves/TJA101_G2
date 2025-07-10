package com.eatfast.employee.model;

// 匯入專案中會用到的類別與註解
import com.eatfast.common.enums.AccountStatus;
import com.eatfast.common.enums.EmployeeRole;
import com.eatfast.common.enums.Gender;
import com.eatfast.announcement.model.AnnouncementEntity;
import com.eatfast.employee.permission.model.EmployeePermissionEntity;
import com.eatfast.news.model.NewsEntity;
import com.eatfast.store.model.StoreEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * EmployeeEntity
 *
 * 這個類別代表「員工」的資料，對應到資料庫中的 employee 資料表。
 * 在 Spring Boot 專案中，這類檔案通常放在 model 或 entity 資料夾下。
 *
 * 主要用途：
 * - 存放員工的基本資料（如姓名、帳號、密碼、信箱等）
 * - 讓程式可以方便地存取、修改、儲存員工資料
 * - 這個類別會被 JPA/Hibernate 用來自動對應到資料庫的 employee 資料表
 *
 * 註解說明：
 * - @Entity：代表這是一個資料庫實體類別，會對應到一張資料表
 * - @Table(name = "employee")：指定對應的資料表名稱
 * - @DynamicUpdate：只會更新有變動的欄位，提升效能
 */
@Entity // 這個類別會對應到資料庫的 employee 資料表
@Table(name = "employee", indexes = {
    @Index(name = "idx_employee_account", columnList = "account"),
    @Index(name = "idx_employee_email", columnList = "email"),
    @Index(name = "idx_employee_status", columnList = "status"),
    @Index(name = "idx_employee_store", columnList = "store_id")
})
@DynamicUpdate
public class EmployeeEntity {

    // ======================== 主要欄位 (Primary Fields) ========================

    /**
     * 員工系統 ID（主鍵）
     * 這是每個員工在資料庫中的唯一編號，系統自動產生。
     * 對應到 employee 資料表的 employee_id 欄位。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;

    /**
     * 員工真實姓名
     * 這是員工的本名，不能為空，最多 20 個字。
     * 對應到 employee 資料表的 username 欄位。
     */
    @NotBlank(message = "員工姓名不可為空")
    @Size(max = 20, message = "員工姓名長度不可超過 20 個字元")
    @Column(name = "username", nullable = false, length = 20)
    private String username;

    /**
     * 員工登入帳號（業務唯一鍵）
     * 這是員工用來登入系統的帳號，不能重複。
     * 對應到 employee 資料表的 account 欄位。
     */
    @NaturalId // 幫助 Hibernate 優化查詢，這個欄位在業務上是唯一的
    @NotBlank(message = "登入帳號不可為空")
    @Column(name = "account", nullable = false, updatable = false, length = 50, unique = true)
    private String account;

    /**
     * 登入密碼（建議儲存加密後的值）
     * 這是員工登入系統時要輸入的密碼，不能為空。
     * 對應到 employee 資料表的 password 欄位。
     */
    @NotBlank(message = "登入密碼不可為空")
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * 員工電子郵件
     * 這是員工的工作郵箱，必須是唯一的且符合標準格式。
     * 對應到 employee 資料表的 email 欄位。
     */
    @NotBlank(message = "電子郵件不可為空")
    @Email(message = "請輸入有效的電子郵件格式")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", 
             message = "電子郵件格式不正確，只能包含英文字母、數字和特殊符號")
    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    /**
     * 員工聯絡電話
     * 這是員工的手機號碼，必須符合台灣手機格式。
     * 對應到 employee 資料表的 phone 欄位。
     */
    @NotBlank(message = "連絡電話不可為空")
    @Pattern(regexp = "^09\\d{2}-?\\d{3}-?\\d{3}$", message = "請輸入有效的台灣手機號碼格式 (例如 0912-345-678)")
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    /**
     * 員工身分證字號（唯一）
     * 這是員工的身分證號碼，必須符合台灣格式，不能重複。
     * 對應到 employee 資料表的 national_id 欄位。
     */
    @NotBlank(message = "身分證字號不可為空")
    @Pattern(regexp = "^[A-Z][1-2]\\d{8}$", message = "請輸入有效的台灣身分證字號格式")
    @Column(name = "national_id", nullable = false, length = 10, unique = true)
    private String nationalId;
    
    // 【說明】
    // 以前會把照片的二進位資料直接存進資料庫，現在改成只存照片的網址，這樣效能更好。

    /**
     * 員工照片的網址
     * 這裡只存放圖片的路徑或 URL，不存二進位檔案。
     * 對應到 employee 資料表的 photo_url 欄位。
     */
    @Column(name = "photo_url")
    private String photoUrl;

    // ======================== 列舉類型欄位 (Enum Fields) ========================

    /**
     * 員工角色
     * 例如：MANAGER（管理者）、STAFF（一般員工）
     * 存進資料庫時會用文字（如 "MANAGER"），不是數字。
     * 對應到 employee 資料表的 role 欄位。
     */
    @NotNull(message = "員工角色不可為空")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private EmployeeRole role;

    /**
     * 帳號狀態
     * 例如：ACTIVE（啟用）、INACTIVE（停用）
     * 存進資料庫時會用文字（如 "ACTIVE"）。
     * 對應到 employee 資料表的 status 欄位。
     */
    @NotNull(message = "帳號狀態不可為空")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE; // 預設為啟用

    /**
     * 員工性別
     * 例如：MALE（男）、FEMALE（女）
     * 對應到 employee 資料表的 gender 欄位。
     */
    @NotNull(message = "性別不可為空")
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 1)
    private Gender gender;

    // ======================== 時間戳記 (Timestamps) ========================

    /**
     * 帳號建立時間
     * 這個欄位會自動記錄員工帳號建立的時間。
     * 對應到 employee 資料表的 create_time 欄位。
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 資料最後更新時間
     * 這個欄位會自動記錄員工資料最後一次被修改的時間。
     * 對應到 employee 資料表的 last_updated_at 欄位。
     */
    @UpdateTimestamp
    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    // ======================== 登入失敗追蹤欄位 (Login Failure Tracking) ========================

    /**
     * 登入失敗次數
     * 記錄連續登入失敗的次數，成功登入後會重置為 0。
     * 當達到 8 次時，帳號將被自動停用。
     * 對應到 employee 資料表的 login_failure_count 欄位。
     */
    @Column(name = "login_failure_count", nullable = false)
    private Integer loginFailureCount = 0;

    /**
     * 最後一次登入失敗時間
     * 記錄最後一次登入失敗的時間，用於計算是否需要重置失敗次數。
     * 對應到 employee 資料表的 last_failure_time 欄位。
     */
    @Column(name = "last_failure_time")
    private LocalDateTime lastFailureTime;

    /**
     * 帳號鎖定時間
     * 當登入失敗次數達到上限時，記錄帳號被鎖定的時間。
     * 對應到 employee 資料表的 account_locked_time 欄位。
     */
    @Column(name = "account_locked_time")
    private LocalDateTime accountLockedTime;

    /**
     * 版本號（樂觀鎖定）
     * 用於處理併發更新，防止資料被意外覆蓋。
     * 每次更新資料時，JPA 會自動檢查版本號是否一致，
     * 如果不一致則拋出 OptimisticLockException。
     * 對應到 employee 資料表的 version 欄位。
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    // ======================== 關聯欄位 (Associations) ========================

    /**
     * 所屬門市（多對一關聯）
     * 每個員工都屬於一間門市。
     * 對應到 employee 資料表的 store_id 欄位。
     *
     * 資料流說明：
     * - 當你查詢員工時，可以透過這個欄位取得他所屬的門市資訊。
     */
    @NotNull(message = "所屬門市不可為空")
    @ManyToOne(fetch = FetchType.LAZY) // 只有用到時才會查詢門市資料，節省效能
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    /**
     * 帳號建立者（多對一，自關聯）
     * 這個欄位記錄是哪位員工建立了這個帳號。
     * 對應到 employee 資料表的 created_by 欄位。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private EmployeeEntity createdBy;

    /**
     * 由此員工建立的其他員工帳號（一對多，自關聯）
     * 這個欄位可以查出這位員工曾經幫哪些人建立過帳號。
     */
    @BatchSize(size = 10) // 查詢時會分批載入，避免效能問題
    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private Set<EmployeeEntity> createdEmployees = new HashSet<>();

    /**
     * 發布的最新消息（一對多）
     * 這位員工曾經發布過的所有最新消息。
     * 對應到 news 資料表。
     */
    @BatchSize(size = 10)
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<NewsEntity> publishedNews = new HashSet<>();

    /**
     * 發布的門市公告（一對多）
     * 這位員工曾經發布過的所有門市公告。
     * 對應到 announcement 資料表。
     */
    @BatchSize(size = 10)
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<AnnouncementEntity> publishedAnnouncements = new HashSet<>();
    
    /**
     * 擁有的權限（一對多，透過中間表 EmployeePermissionEntity）
     * 這個欄位記錄員工擁有哪些權限。
     * 對應到 employee_permission 資料表。
     */
    @BatchSize(size = 10)
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<EmployeePermissionEntity> employeePermissions = new HashSet<>();

    // ======================== 建構子、Getter、Setter ========================

    /**
     * 無參數建構子
     * JPA 需要這個建構子來建立物件。
     */
    public EmployeeEntity() {}

    // 下面這些是屬性的 getter 和 setter 方法，讓其他程式可以安全地存取和修改欄位值。
    // 例如：getUsername() 會回傳員工姓名，setUsername() 可以設定員工姓名。
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
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
    public EmployeeRole getRole() { return role; }
    public void setRole(EmployeeRole role) { this.role = role; }
    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }
    public StoreEntity getStore() { return store; }
    public void setStore(StoreEntity store) { this.store = store; }
    public EmployeeEntity getCreatedBy() { return createdBy; }
    public void setCreatedBy(EmployeeEntity createdBy) { this.createdBy = createdBy; }
    public Set<EmployeeEntity> getCreatedEmployees() { return createdEmployees; }
    public void setCreatedEmployees(Set<EmployeeEntity> createdEmployees) { this.createdEmployees = createdEmployees; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
    public Set<NewsEntity> getPublishedNews() { return publishedNews; }
    public void setPublishedNews(Set<NewsEntity> publishedNews) { this.publishedNews = publishedNews; }
    public Set<AnnouncementEntity> getPublishedAnnouncements() { return publishedAnnouncements; }
    public void setPublishedAnnouncements(Set<AnnouncementEntity> publishedAnnouncements) { this.publishedAnnouncements = publishedAnnouncements; }
    public Set<EmployeePermissionEntity> getEmployeePermissions() { return employeePermissions; }
    public void setEmployeePermissions(Set<EmployeePermissionEntity> employeePermissions) { this.employeePermissions = employeePermissions; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Integer getLoginFailureCount() { return loginFailureCount; }
    public void setLoginFailureCount(Integer loginFailureCount) { this.loginFailureCount = loginFailureCount; }
    public LocalDateTime getLastFailureTime() { return lastFailureTime; }
    public void setLastFailureTime(LocalDateTime lastFailureTime) { this.lastFailureTime = lastFailureTime; }
    public LocalDateTime getAccountLockedTime() { return accountLockedTime; }
    public void setAccountLockedTime(LocalDateTime accountLockedTime) { this.accountLockedTime = accountLockedTime; }

    // ======================== equals、hashCode、toString 方法 ========================

    /**
     * toString 方法
     * 方便在除錯時快速看到員工的主要資訊。
     */
    @Override
    public String toString() {
        return "EmployeeEntity{" + "employeeId=" + employeeId + ", username='" + username + '\'' + ", account='" + account + '\'' + ", role=" + role + ", status=" + status + ", storeId=" + (store != null ? store.getStoreId() : "null") + '}';
    }

    /**
     * equals 方法
     * 用來判斷兩個員工物件是否代表同一個人。
     * 這裡主要用 account（帳號）來比對，因為帳號是唯一的。
     * 如果帳號為 null，則用 employeeId 來比對。
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeEntity that = (EmployeeEntity) o;
        // 先比對帳號（account）
        if (this.account != null && that.account != null) {
            return Objects.equals(this.account, that.account);
        }
        // 如果帳號為 null（理論上不會發生），就比對 ID
        if (this.employeeId == null || that.employeeId == null) {
            return false;
        }
        return Objects.equals(employeeId, that.employeeId);
    }

    /**
     * hashCode 方法
     * 產生物件的雜湊碼，通常會搭配 equals 一起使用。
     * 這裡優先用帳號（account）來產生 hashCode，確保唯一性。
     */
    @Override
    public int hashCode() {
        if (account != null) {
            return Objects.hash(account);
        }
        return getClass().hashCode();
    }
}
// ======================== 注意事項 ========================
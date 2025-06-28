package com.eatfast.employee.model;

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
 * [可自定義的類別名稱]: EmployeeEntity
 * 員工資料庫實體 (Entity)，對應到 'employee' 資料表。
 * - @Entity: 標記此類別為 JPA 實體。
 * - @Table: 指定對應的資料表名稱。
 * - @DynamicUpdate: 最佳化更新操作，只更新有變動的欄位。
 */
@Entity
@Table(name = "employee")
@DynamicUpdate
public class EmployeeEntity {

    // ================================================================
    //                       主要欄位 (Primary Fields)
    // ================================================================

    /** 員工系統 ID (主鍵) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;

    /** 員工真實姓名 */
    @NotBlank(message = "員工姓名不可為空")
    @Size(max = 20, message = "員工姓名長度不可超過 20 個字元")
    @Column(name = "username", nullable = false, length = 20)
    private String username;

    /** 員工登入帳號 (業務唯一鍵) */
    @NaturalId // 標記為業務上的自然主鍵，有助於 Hibernate 效能優化。
    @NotBlank(message = "登入帳號不可為空")
    @Column(name = "account", nullable = false, updatable = false, length = 50, unique = true)
    private String account;

    /** 登入密碼 (應儲存加密後的值) */
    @NotBlank(message = "登入密碼不可為空")
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /** 員工聯絡電子郵件 (業務唯一鍵) */
    @NaturalId
    @NotBlank(message = "電子郵件不可為空")
    @Email(message = "請輸入有效的電子郵件格式")
    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    /** 員工聯絡電話 */
    @NotBlank(message = "連絡電話不可為空")
    @Pattern(regexp = "^09\\d{2}-?\\d{3}-?\\d{3}$", message = "請輸入有效的台灣手機號碼格式 (例如 0912-345-678)")
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    /** 員工身分證字號 (業務唯一鍵) */
    @NotBlank(message = "身分證字號不可為空")
    @Pattern(regexp = "^[A-Z][1-2]\\d{8}$", message = "請輸入有效的台灣身分證字號格式")
    @Column(name = "national_id", nullable = false, length = 10, unique = true)
    private String nationalId;

    /** 員工大頭照 (二進位資料) */
    @Lob // 標記為大物件 (Large Object)。
    @Column(name = "photo")
    private byte[] photo;

    // ================================================================
    //                       列舉類型欄位 (Enum Fields)
    // ================================================================

    /** 員工角色 */
    @NotNull(message = "員工角色不可為空")
    @Enumerated(EnumType.ORDINAL) // 【警告】使用順序儲存(0,1,2)，未來嚴禁調整 Enum 順序。
    @Column(name = "role", nullable = false)
    private EmployeeRole role;

    /** 帳號狀態 */
    @NotNull(message = "帳號狀態不可為空")
    @Enumerated(EnumType.ORDINAL) // 【警告】使用順序儲存(0,1)，未來嚴禁調整 Enum 順序。
    @Column(name = "status", nullable = false)
    private AccountStatus status;

    /** 員工性別 */
    @NotNull(message = "性別不可為空")
    @Enumerated(EnumType.STRING) // 使用字串儲存(M, F, O)，可讀性高且較安全。
    @Column(name = "gender", nullable = false, length = 1)
    private Gender gender;

    // ================================================================
    //                       時間戳記 (Timestamps)
    // ================================================================

    /** 帳號建立時間 (由 Hibernate 自動生成) */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /** 資料最後更新時間 (由 Hibernate 自動生成) */
    @UpdateTimestamp
    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    // ================================================================
    //                     關聯欄位 (Associations)
    // ================================================================

    /** 所屬門市 (多對一) */
    @NotNull(message = "所屬門市不可為空")
    @ManyToOne(fetch = FetchType.LAZY) // 使用懶加載以提升效能
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    /** 帳號建立者 (多對一，自關聯) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private EmployeeEntity createdBy;

    /** 由此員工建立的其他員工帳號 (一對多，自關聯) */
    @BatchSize(size = 10) // 優化 N+1 問題
    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private Set<EmployeeEntity> createdEmployees = new HashSet<>();

    /** 發布的最新消息 (一對多) */
    @BatchSize(size = 10)
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<NewsEntity> publishedNews = new HashSet<>();

    /** 發布的門市公告 (一對多) */
    @BatchSize(size = 10)
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<AnnouncementEntity> publishedAnnouncements = new HashSet<>();

    /** 擁有的權限 (一對多) */
    @BatchSize(size = 10)
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<EmployeePermissionEntity> employeePermissions = new HashSet<>();

    // ================================================================
    //             建構子, Getters, Setters (Boilerplate Code)
    // ================================================================
    public EmployeeEntity() {}

    // Getters and Setters...
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
    public byte[] getPhoto() { return photo; }
    public void setPhoto(byte[] photo) { this.photo = photo; }
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

    // ================================================================
    //          物件核心方法 (equals, hashCode, toString)
    // ================================================================
    @Override
    public String toString() {
        return "EmployeeEntity{" + "employeeId=" + employeeId + ", username='" + username + '\'' + ", account='" + account + '\'' + ", role=" + role + ", status=" + status + ", storeId=" + (store != null ? store.getStoreId() : "null") + '}';
    }

    /**
     * 基於業務唯一鍵 'account' 實作 equals 方法，
     * 確保即使物件為未寫入資料庫的暫時狀態 (transient)，也能正確比較。
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeEntity that = (EmployeeEntity) o;
        // 優先使用業務唯一鍵比較
        if (this.account != null && that.account != null) {
            return Objects.equals(this.account, that.account);
        }
        // 若 account 為 null (理論上不該發生)，則退回比較 ID
        if (this.employeeId == null || that.employeeId == null) {
            return false;
        }
        return Objects.equals(employeeId, that.employeeId);
    }

    /**
     * 對應 equals 方法，優先使用業務唯一鍵 'account' 來產生 hashCode。
     */
    @Override
    public int hashCode() {
        if (account != null) {
            return Objects.hash(account);
        }
        return getClass().hashCode();
    }
}

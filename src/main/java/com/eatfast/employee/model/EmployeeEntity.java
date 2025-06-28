/*
 * ================================================================
 * 檔案: EmployeeEntity.java (★★ 命名與可讀性微調版 ★★)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/employee/model/EmployeeEntity.java
 * - 核心修正 (依指示):
 * 1. 【命名一致性】: 將自關聯 creator 修正為 createdBy，使其與 DDL 和 JPA 慣例保持一致。
 * 2. 【可讀性】: 將 permissions 集合更名為 employeePermissions，使其語意更精確。
 * 3. 【保持原樣】: status 欄位維持使用 EnumType.ORDINAL。
 */
package com.eatfast.employee.model;

// 為了程式碼重用，從共通 package 引入 Enum
import com.eatfast.common.enums.AccountStatus;
import com.eatfast.common.enums.Gender;

// 其他關聯的 import 維持不變
import com.eatfast.store.model.StoreEntity;
import com.eatfast.news.model.NewsEntity;
import com.eatfast.announcement.model.AnnouncementEntity;
import com.eatfast.employee.permission.model.EmployeePermissionEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "employee")
@DynamicUpdate
public class EmployeeEntity {

    //================================================================
    // 							欄位定義 (Fields)
    //================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;

    @NotBlank(message = "員工姓名不可為空")
    @Size(max = 20, message = "員工姓名長度不可超過 20 個字元")
    @Column(name = "username", nullable = false, length = 20)
    private String username;

    @NaturalId
    @NotBlank(message = "登入帳號不可為空")
    @Size(max = 50, message = "登入帳號長度不可超過 50 個字元")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "登入帳號僅能包含英數字、底線、點或連字號")
    @Column(name = "account", nullable = false, updatable = false, length = 50, unique = true)
    private String account;
    
    @NotBlank(message = "登入密碼不可為空")
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    
    @NaturalId
    @NotBlank(message = "電子郵件不可為空")
    @Email(message = "請輸入有效的電子郵件格式")
    @Size(max = 100, message = "電子郵件長度不可超過 100 個字元")
    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;
    
    @NotBlank(message = "連絡電話不可為空")
    @Pattern(regexp = "^09\\d{2}-?\\d{3}-?\\d{3}$", message = "請輸入有效的台灣手機號碼格式 (例如 0912-345-678)")
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;
    
    @NotBlank(message = "員工角色不可為空")
    @Size(max = 10, message = "員工角色長度不可超過 10 個字元")
    @Column(name = "role", nullable = false, length = 10)
    private String role;
    
    // 【保持原樣】: 根據您的指示，此處維持 EnumType.ORDINAL
    @NotNull(message = "帳號狀態不可為空")
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private AccountStatus status;
    
    @NotNull(message = "性別不可為空")
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 1)
    private Gender gender;
    
    @Lob
    @Column(name = "photo")
    private byte[] photo;

    @NotBlank(message = "身分證字號不可為空")
    @Pattern(regexp = "^[A-Z][1-2]\\d{8}$", message = "請輸入有效的台灣身分證字號格式")
    @Size(min = 10, max = 10, message = "身分證字號長度必須為 10 個字元")
    @Column(name = "national_id", nullable = false, length = 10, unique = true)
    private String nationalId;
    
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    //================================================================
    // 					關聯欄位 (Associations)
    //================================================================

    @NotNull(message = "所屬門市不可為空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    // ================================================================
    //         【指示修正 1: 自關聯命名一致性】
    // ================================================================
    /**
     * 此員工帳號的建立者。
     * [可自定義的名稱]: createdBy (原 creator)
     * [說明]: 更名為 createdBy 以符合資料庫欄位 created_by 及 JPA 的命名慣例。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private EmployeeEntity createdBy;

    /**
     * 由此員工所建立的其他員工帳號集合。
     * [不可變動的關鍵字]: mappedBy
     * [說明]: mappedBy 的值已更新為 "createdBy"，與多方的關聯變數名稱保持一致。
     */
    @BatchSize(size = 10)
    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private Set<EmployeeEntity> createdEmployees = new HashSet<>();
    
    @BatchSize(size = 10)
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<NewsEntity> publishedNews = new HashSet<>();
    
    @BatchSize(size = 10)
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<AnnouncementEntity> publishedAnnouncements = new HashSet<>();
    
    // ================================================================
    //         【指示修正 2: 關聯集合命名可讀性】
    // ================================================================
    /**
     * 此員工所擁有的所有權限關聯紀錄。
     * [可自定義的名稱]: employeePermissions (原 permissions)
     * [說明]: 更名為 employeePermissions，使其名稱能更精確地反映集合內容是 EmployeePermissionEntity。
     */
    @BatchSize(size = 10)
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<EmployeePermissionEntity> employeePermissions = new HashSet<>();

    //================================================================
    //					 建構子, Getters, Setters
    //================================================================
    public EmployeeEntity() {}

    // Getters and Setters 已同步更新
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
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
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

    //================================================================
    // 物件核心方法 (equals, hashCode, toString)
    //================================================================
    @Override
    public String toString() {
        return "EmployeeEntity{" +
                "employeeId=" + employeeId +
                ", username='" + username + '\'' +
                ", account='" + account + '\'' +
                ", role='" + role + '\'' +
                ", status=" + status +
                ", storeId=" + (store != null ? store.getStoreId() : "null") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeEntity that = (EmployeeEntity) o;
        if (this.account != null && that.account != null) {
            return Objects.equals(this.account, that.account);
        }
        if (this.employeeId == null || that.employeeId == null) {
            return false;
        }
        return Objects.equals(employeeId, that.employeeId);
    }

    @Override
    public int hashCode() {
        if (account != null) {
            return Objects.hash(account);
        }
        return getClass().hashCode();
    }
}
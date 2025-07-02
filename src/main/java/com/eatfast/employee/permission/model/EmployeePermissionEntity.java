package com.eatfast.employee.permission.model; // 假設的 package 路徑

// 【檔案路徑配對】: 為了建立多對一關聯，需要 import 父實體
import com.eatfast.employee.model.EmployeeEntity;
import com.eatfast.permission.model.PermissionEntity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 員工權限關聯實體 (Employee Permission Entity)
 * <p>
 * 此實體對應資料庫中的 `employee_permission` 中間表，用於建立 `EmployeeEntity` 與 `PermissionEntity`
 * 之間的多對多關聯。每一筆紀錄都代表著一個特定的員工被授予了一項特定的權限。
 * </p>
 */
@Entity
@Table(name = "employee_permission",
    // 【不可變動】: uniqueConstraints 的設定必須與資料庫的 uk_employee_permission 唯一鍵完全對應。
    // 這確保了在業務邏輯上，同一個員工不會被重複授予相同的權限。
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_employee_permission",
            columnNames = {"employee_id", "permission_id"}
        )
    }
)
public class EmployeePermissionEntity {

    //================================================================
    // 							欄位定義 (Fields)
    //================================================================

    /**
     * 代理主鍵 (Surrogate Key)。
     * 對應 `ep_id` 欄位，無實際業務意義，僅作為 JPA 操作的唯一識別。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ep_id")
    private Long epId;

    /**
     * 權限被授予的時間。
     * 1. @CreationTimestamp: (不可變關鍵字) Hibernate 提供的註解。當此實體被新增至資料庫時，
     * 會自動將當前時間戳寫入此欄位。
     * 2. updatable = false: (不可變關鍵字) 確保此欄位在更新時不會被修改。
     */
    @CreationTimestamp
    @Column(name = "granted_at", nullable = false, updatable = false)
    private LocalDateTime grantedAt;


    //================================================================
    // 				關聯的擁有方 (Owning Side of Relationship)
    //================================================================

    /**
     * 被授予權限的員工 (多對一)。
     * 1. @ManyToOne: (不可變關鍵字) 宣告這是一個多對一關聯 (多筆權限紀錄 -> 一位員工)。
     * 2. fetch = FetchType.LAZY: (不可變關鍵字) **效能關鍵**。只有在需要時才載入員工資料。
     * 3. @JoinColumn: (不可變關鍵字) 指定外鍵欄位。
     * 4. name = "employee_id": (不可變動) 必須與資料庫 `employee_permission` 表中的外鍵欄位名完全匹配。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employee;

    /**
     * 被授予的權限 (多對一)。
     * 1. name = "permission_id": (不可變動) 必須與資料庫 `employee_permission` 表中的外鍵欄位名完全匹配。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private PermissionEntity permission;


    //================================================================
    //					 建構子、Getters、Setters
    //================================================================
    public EmployeePermissionEntity() {}

    public Long getEpId() {
        return epId;
    }

    public void setEpId(Long epId) {
        this.epId = epId;
    }

    public LocalDateTime getGrantedAt() {
        return grantedAt;
    }

    public void setGrantedAt(LocalDateTime grantedAt) {
        this.grantedAt = grantedAt;
    }

    public EmployeeEntity getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeEntity employee) {
        this.employee = employee;
    }

    public PermissionEntity getPermission() {
        return permission;
    }

    public void setPermission(PermissionEntity permission) {
        this.permission = permission;
    }

    //================================================================
    // 物件核心方法 (equals, hashCode, toString)
    //================================================================
    @Override
    public String toString() {
        // 為避免 LAZY loading 問題，關聯物件只印出其 ID
        return "EmployeePermissionEntity{" +
                "epId=" + epId +
                ", employeeId=" + (employee != null ? employee.getEmployeeId() : "null") +
                ", permissionId=" + (permission != null ? permission.getPermissionId() : "null") +
                ", grantedAt=" + grantedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeePermissionEntity that = (EmployeePermissionEntity) o;
        if (epId == null || that.epId == null) {
            return false;
        }
        return Objects.equals(epId, that.epId);
    }

    @Override
    public int hashCode() {
        return epId != null ? Objects.hash(epId) : super.hashCode();
    }
}

package com.eatfast.permission.model;

// 【檔案路徑配對】: 為了建立反向關聯，需要 import 子實體 EmployeePermissionEntity
import com.eatfast.employee.permission.model.EmployeePermissionEntity; // 假設中間表的 Entity 名稱

import jakarta.persistence.*; // 【新增】: 為了 @OneToMany 等註解，需要 import 更多 persistence 元件
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 權限實體 (Permission Entity) - 【已補全雙向關聯】
 * <p>
 * 此實體對應 `permission` 資料表，定義系統中的「原子權限」。
 * 透過 @OneToMany，它現在可以直接導航至所有使用此權限的員工權限關聯紀錄。
 * </p>
 */
@Entity
@Table(name = "permission")
public class PermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long permissionId;

    @NotBlank(message = "權限說明不可為空")
    @Size(max = 255, message = "權限說明長度不可超過 255 個字元")
    @Column(name = "description", nullable = false, length = 255)
    private String description;


    // ================================================================
    //         【新增區塊】反向一對多關聯 (Bidirectional @OneToMany)
    // ================================================================

    /**
     * 使用此權限的所有員工權限關聯紀錄。
     * 1. mappedBy = "permission": (不可變動) 關聯的控制權交給 `EmployeePermissionEntity` 中的 `permission` 屬性。
     * 2. cascade = CascadeType.ALL: (不可變關鍵字) 資料庫設為 `ON DELETE CASCADE`，故此處可設定連動。
     * 刪除此權限時，所有賦予員工此權限的紀錄也將一併刪除。
     * 3. orphanRemoval = true: (不可變關鍵字) 當關聯紀錄從 `employeePermissions` 集合中移除時，也會從資料庫刪除。
     */
    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<EmployeePermissionEntity> employeePermissions = new HashSet<>();


    // =================================================================
    //                  建構子 (Constructors)
    // =================================================================
    public PermissionEntity() {}

    public PermissionEntity(String description) {
        this.description = description;
    }


    // =================================================================
    //                  Getter and Setter 方法
    // =================================================================
    public Long getPermissionId() {
        return permissionId;
    }
    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    // 【新增】關聯集合的 Getter / Setter
    public Set<EmployeePermissionEntity> getEmployeePermissions() {
        return employeePermissions;
    }
    public void setEmployeePermissions(Set<EmployeePermissionEntity> employeePermissions) {
        this.employeePermissions = employeePermissions;
    }

    // =================================================================
    //                 equals, hashCode, toString 方法
    // =================================================================
    @Override
    public String toString() {
        return "PermissionEntity{" +
                "permissionId=" + permissionId +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionEntity that = (PermissionEntity) o;
        return Objects.equals(permissionId, that.permissionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(permissionId);
    }
}

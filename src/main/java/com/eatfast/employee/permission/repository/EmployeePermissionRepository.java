package com.eatfast.employee.permission.repository; // 可自定義的 package 路徑

import com.eatfast.employee.permission.model.EmployeePermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * [可自定義的類別名稱]: EmployeePermissionRepository
 * 員工與權限「關聯」的資料存取層。
 * 專門用於操作 `employee_permission` 這張中間表。
 *
 * - @Repository: (不可變動) 標記此介面為 Spring 管理的資料存取層 Bean。
 */
@Repository
public interface EmployeePermissionRepository extends JpaRepository<EmployeePermissionEntity, Long> {

    /**
     * [可自定義的方法名稱]: findByEmployeeEmployeeIdAndPermissionPermissionId
     * 根據員工 ID 和權限 ID，查找特定的關聯紀錄。
     * Spring Data JPA 會自動解析此方法名稱，並產生對應的 SQL 查詢。
     * 這個方法在「撤銷權限」時至關重要。
     *
     * @param employeeId (可自定義的參數名) 員工的主鍵 ID。
     * @param permissionId (可自定義的參數名) 權限的主鍵 ID。
     * @return 返回一個可能包含關聯實體 (EmployeePermissionEntity) 的 Optional。
     */
    Optional<EmployeePermissionEntity> findByEmployeeEmployeeIdAndPermissionPermissionId(Long employeeId, Long permissionId);

    /**
     * [可自定義的方法名稱]: existsByEmployeeEmployeeIdAndPermissionPermissionId
     * 檢查特定員工是否已經擁有特定權限。
     * 這是一個高效能的檢查方法，因為它通常會被轉譯為 `SELECT COUNT(*)` 或 `EXISTS` 查詢，
     * 而不是抓取整個實體。
     *
     * @param employeeId (可自定義的參數名) 員工 ID。
     * @param permissionId (可自定義的參數名) 權限 ID。
     * @return 如果關聯已存在，則返回 true，否則返回 false。
     */
    boolean existsByEmployeeEmployeeIdAndPermissionPermissionId(Long employeeId, Long permissionId);
}

package com.eatfast.permission.repository;

import com.eatfast.common.enums.EmployeeRole;
import com.eatfast.permission.model.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 權限資料存取層 (Repository) - 【已重構】
 * @Repository: (不可變動) 標記此介面為 Spring 管理的資料存取層 Bean。
 */
@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {

    /**
     * 【新增方法】: 根據員工角色 (EmployeeRole) 查詢對應的所有權限。
     * 說明:
     * - 這是一個複雜的 JPQL 查詢，它會遍歷三張表 (`permission`, `employee_permission`, `employee`) 來找出關聯。
     * - `JOIN p.employeePermissions ep`: 從權限(p)連接到它的員工權限關聯(ep)。
     * - `JOIN ep.employee e`: 從員工權限關聯(ep)連接到員工(e)。
     * - `WHERE e.role = :role`: 篩選出符合指定角色的員工。
     * - `SELECT DISTINCT p`: 選擇不重複的權限實體。
     * @param role (可自定義的參數名): 要查詢的員工角色 Enum。
     * @return 返回一個包含該角色所有權限實體的 Set 集合。
     */
    @Query("SELECT DISTINCT p FROM PermissionEntity p JOIN p.employeePermissions ep JOIN ep.employee e WHERE e.role = :role")
    Set<PermissionEntity> findPermissionsByEmployeeRole(@Param("role") EmployeeRole role);
}

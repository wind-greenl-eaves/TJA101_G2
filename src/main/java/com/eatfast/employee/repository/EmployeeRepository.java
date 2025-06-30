/*
 * ================================================================
 * 檔案: EmployeeRepository.java 
 * ================================================================
 * - 說明: 提供員工資料的 CRUD、動態查詢及效能優化查詢。
 */
package com.eatfast.employee.repository;

import com.eatfast.employee.model.EmployeeEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 員工資料存取層 (Repository)。
 * - JpaRepository: 提供標準 CRUD 功能。
 * - JpaSpecificationExecutor: 提供動態條件查詢功能。
 */
@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long>, JpaSpecificationExecutor<EmployeeEntity> {

    //================================================================
    // 				      依特定欄位查詢
    //================================================================

    /** 依登入帳號查詢員工。*/
    Optional<EmployeeEntity> findByAccount(String account);

    /** 依電子郵件查詢員工。*/
    Optional<EmployeeEntity> findByEmail(String email);

    //================================================================
    // 				      存在性檢查 (高效能)
    //================================================================

    /** 檢查登入帳號是否已存在。*/
    boolean existsByAccount(String account);

    /** 檢查電子郵件是否已存在。*/
    boolean existsByEmail(String email);

    /** 檢查身分證字號是否已存在。*/
    boolean existsByNationalId(String nationalId);

    //================================================================
    // 				      效能優化查詢 (解決 N+1 問題)
    //================================================================

    /**
     * 依帳號查詢員工，並同時載入其權限 (Permissions)。
     * @EntityGraph: 使用 JOIN 一次性抓取關聯資料，避免 N+1 查詢。
     */
    @EntityGraph(attributePaths = { "employeePermissions.permission" })
    Optional<EmployeeEntity> findWithPermissionsByAccount(String account);

    /**
     * 依帳號查詢員工，並同時載入其所屬門市 (Store)。
     * @EntityGraph: 使用 JOIN 一次性抓取關聯資料，避免 N+1 查詢。
     */
    @EntityGraph(attributePaths = { "store" })
    Optional<EmployeeEntity> findWithStoreByAccount(String account);
    
    
}

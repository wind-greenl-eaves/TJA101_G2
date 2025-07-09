/*
 * ================================================================
 * 檔案: EmployeeRepository.java 
 * 功能說明: 員工資料存取層 - 提供 CRUD、動態查詢及效能優化查詢
 * 架構層級: 資料存取層 (Repository Layer / Data Access Layer)
 * 配對關係:
 *   - 實體層: EmployeeEntity - 對應的 JPA 實體
 *   - 服務層: EmployeeServiceImpl - 透過此介面存取資料
 *   - 資料庫: employee 資料表 - 實際資料儲存位置
 *   - Spring Data JPA: 自動產生基礎 CRUD 實作
 * 設計模式:
 *   - Repository Pattern (儲存庫模式)
 *   - Specification Pattern (規格模式) - 用於動態查詢
 *   - Entity Graph Pattern - 解決 N+1 查詢問題
 * 查詢優化:
 *   - @EntityGraph: 使用 JOIN 避免 N+1 問題
 *   - 存在性檢查: 使用 exists 方法提升效能
 *   - 自然 ID 查詢: 針對業務唯一鍵的查詢優化
 * ================================================================
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
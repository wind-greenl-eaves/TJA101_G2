package com.eatfast.employee.repository;

import com.eatfast.employee.model.EmployeeApplicationEntity;
import com.eatfast.employee.model.EmployeeApplicationEntity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeApplicationRepository extends JpaRepository<EmployeeApplicationEntity, Long> {

    /**
     * 查詢所有待審核的申請
     */
    List<EmployeeApplicationEntity> findByStatusOrderByCreatedAtDesc(ApplicationStatus status);

    /**
     * 查詢特定申請人的所有申請
     */
    List<EmployeeApplicationEntity> findByApplicantIdOrderByCreatedAtDesc(Long applicantId);

    /**
     * 查詢特定門市的所有申請
     */
    List<EmployeeApplicationEntity> findByStoreIdOrderByCreatedAtDesc(Long storeId);

    /**
     * 查詢特定門市和狀態的申請
     */
    List<EmployeeApplicationEntity> findByStoreIdAndStatusOrderByCreatedAtDesc(Long storeId, ApplicationStatus status);

    /**
     * 檢查帳號是否已在申請中
     */
    boolean existsByAccountAndStatus(String account, ApplicationStatus status);

    /**
     * 檢查Email是否已在申請中
     */
    boolean existsByEmailAndStatus(String email, ApplicationStatus status);

    /**
     * 檢查身分證字號是否已在申請中
     */
    boolean existsByNationalIdAndStatus(String nationalId, ApplicationStatus status);

    /**
     * 查詢所有申請（分頁支援）
     */
    @Query("SELECT a FROM EmployeeApplicationEntity a ORDER BY a.createdAt DESC")
    List<EmployeeApplicationEntity> findAllOrderByCreatedAtDesc();

    /**
     * 統計各狀態的申請數量
     */
    @Query("SELECT a.status, COUNT(a) FROM EmployeeApplicationEntity a GROUP BY a.status")
    List<Object[]> countByStatus();

    /**
     * 統計特定門市各狀態的申請數量
     */
    @Query("SELECT a.status, COUNT(a) FROM EmployeeApplicationEntity a WHERE a.storeId = :storeId GROUP BY a.status")
    List<Object[]> countByStatusAndStoreId(@Param("storeId") Long storeId);
}
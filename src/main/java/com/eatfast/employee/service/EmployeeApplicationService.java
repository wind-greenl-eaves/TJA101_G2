package com.eatfast.employee.service;

import com.eatfast.employee.dto.CreateEmployeeRequest;
import com.eatfast.employee.dto.EmployeeApplicationDTO;
import com.eatfast.employee.dto.ReviewApplicationRequest;
import com.eatfast.employee.model.EmployeeApplicationEntity.ApplicationStatus;

import java.util.List;

public interface EmployeeApplicationService {

    /**
     * 提交新增員工申請（門市經理使用）
     */
    EmployeeApplicationDTO submitApplication(CreateEmployeeRequest request, Long applicantId);

    /**
     * 審核員工申請（總部管理員使用）
     */
    EmployeeApplicationDTO reviewApplication(ReviewApplicationRequest request, Long reviewerId);

    /**
     * 查詢所有待審核申請（總部管理員使用）
     */
    List<EmployeeApplicationDTO> getAllPendingApplications();

    /**
     * 查詢特定申請人的申請列表（門市經理查看自己的申請）
     */
    List<EmployeeApplicationDTO> getApplicationsByApplicant(Long applicantId);

    /**
     * 查詢特定門市的申請列表
     */
    List<EmployeeApplicationDTO> getApplicationsByStore(Long storeId);

    /**
     * 根據狀態查詢申請
     */
    List<EmployeeApplicationDTO> getApplicationsByStatus(ApplicationStatus status);

    /**
     * 根據ID查詢申請詳情
     */
    EmployeeApplicationDTO getApplicationById(Long applicationId);

    /**
     * 查詢所有申請（總部管理員使用）
     */
    List<EmployeeApplicationDTO> getAllApplications();

    /**
     * 檢查申請中的唯一性約束
     */
    boolean isFieldAvailableInApplications(String field, String value);
}
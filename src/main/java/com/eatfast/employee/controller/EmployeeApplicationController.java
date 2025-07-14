package com.eatfast.employee.controller;

import com.eatfast.common.enums.EmployeeRole;
import com.eatfast.employee.dto.EmployeeApplicationDTO;
import com.eatfast.employee.dto.EmployeeDTO;
import com.eatfast.employee.dto.ReviewApplicationRequest;
import com.eatfast.employee.model.EmployeeApplicationEntity.ApplicationStatus;
import com.eatfast.employee.service.EmployeeApplicationService;
import com.eatfast.employee.util.EmployeeLogger;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/employee-applications")
public class EmployeeApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeApplicationController.class);
    
    private final EmployeeApplicationService applicationService;
    private final EmployeeLogger employeeLogger;

    @Autowired
    public EmployeeApplicationController(EmployeeApplicationService applicationService, 
                                       EmployeeLogger employeeLogger) {
        this.applicationService = applicationService;
        this.employeeLogger = employeeLogger;
    }

    /**
     * 查詢所有待審核申請（總部管理員使用）
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingApplications(HttpSession session) {
        employeeLogger.logInfo("收到查詢待審核申請請求");
        
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            employeeLogger.logWarn("未登入用戶嘗試查詢待審核申請");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "請重新登入"));
        }

        // 只有總部管理員可以查看待審核申請
        if (currentEmployee.getRole() != EmployeeRole.HEADQUARTERS_ADMIN) {
            employeeLogger.logWarn("非總部管理員嘗試查詢待審核申請: userId={}", currentEmployee.getEmployeeId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "權限不足：只有總部管理員可以查看待審核申請"));
        }

        try {
            List<EmployeeApplicationDTO> applications = applicationService.getAllPendingApplications();
            employeeLogger.logInfo("成功查詢待審核申請: 共{}筆", applications.size());
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            employeeLogger.logError("查詢待審核申請失敗: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "查詢失敗：" + e.getMessage()));
        }
    }

    /**
     * 查詢所有申請（總部管理員使用）
     */
    @GetMapping
    public ResponseEntity<?> getAllApplications(HttpSession session) {
        employeeLogger.logInfo("收到查詢所有申請請求");
        
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "請重新登入"));
        }

        // 根據角色返回不同的申請列表
        try {
            List<EmployeeApplicationDTO> applications;
            
            if (currentEmployee.getRole() == EmployeeRole.HEADQUARTERS_ADMIN) {
                // 總部管理員：查看所有申請
                applications = applicationService.getAllApplications();
                employeeLogger.logInfo("總部管理員查詢所有申請: 共{}筆", applications.size());
            } else if (currentEmployee.getRole() == EmployeeRole.MANAGER) {
                // 門市經理：只能查看自己提交的申請
                applications = applicationService.getApplicationsByApplicant(currentEmployee.getEmployeeId());
                employeeLogger.logInfo("門市經理查詢自己的申請: userId={}, 共{}筆", 
                        currentEmployee.getEmployeeId(), applications.size());
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "權限不足：您無法查看申請列表"));
            }

            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            employeeLogger.logError("查詢申請列表失敗: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "查詢失敗：" + e.getMessage()));
        }
    }

    /**
     * 查詢特定申請詳情
     */
    @GetMapping("/{applicationId}")
    public ResponseEntity<?> getApplicationById(@PathVariable Long applicationId, HttpSession session) {
        employeeLogger.logInfo("收到查詢申請詳情請求: applicationId={}", applicationId);
        
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "請重新登入"));
        }

        try {
            EmployeeApplicationDTO application = applicationService.getApplicationById(applicationId);
            
            // 權限檢查：總部管理員可以查看所有申請，門市經理只能查看自己的申請
            if (currentEmployee.getRole() == EmployeeRole.HEADQUARTERS_ADMIN ||
                (currentEmployee.getRole() == EmployeeRole.MANAGER && 
                 application.getApplicantId().equals(currentEmployee.getEmployeeId()))) {
                
                employeeLogger.logInfo("成功查詢申請詳情: applicationId={}", applicationId);
                return ResponseEntity.ok(application);
            } else {
                employeeLogger.logWarn("用戶嘗試查看無權限的申請: userId={}, applicationId={}", 
                        currentEmployee.getEmployeeId(), applicationId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "權限不足：您無法查看此申請"));
            }
        } catch (Exception e) {
            employeeLogger.logError("查詢申請詳情失敗: applicationId={}, error={}", applicationId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "查詢失敗：" + e.getMessage()));
        }
    }

    /**
     * 審核申請（總部管理員使用）
     */
    @PostMapping("/review")
    public ResponseEntity<?> reviewApplication(@Valid @RequestBody ReviewApplicationRequest request, 
                                             HttpSession session) {
        employeeLogger.logInfo("收到審核申請請求: applicationId={}, status={}", 
                request.getApplicationId(), request.getStatus());
        
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "請重新登入"));
        }

        // 只有總部管理員可以審核申請
        if (currentEmployee.getRole() != EmployeeRole.HEADQUARTERS_ADMIN) {
            employeeLogger.logWarn("非總部管理員嘗試審核申請: userId={}, applicationId={}", 
                    currentEmployee.getEmployeeId(), request.getApplicationId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "權限不足：只有總部管理員可以審核申請"));
        }

        try {
            EmployeeApplicationDTO reviewedApplication = applicationService.reviewApplication(
                    request, currentEmployee.getEmployeeId());
            
            employeeLogger.logInfo("申請審核成功: applicationId={}, status={}, reviewerId={}", 
                    reviewedApplication.getApplicationId(), reviewedApplication.getStatus(), 
                    currentEmployee.getEmployeeId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "申請審核完成");
            response.put("application", reviewedApplication);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            employeeLogger.logError("申請審核失敗: applicationId={}, error={}", 
                    request.getApplicationId(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "審核失敗：" + e.getMessage()));
        }
    }

    /**
     * 查詢申請統計信息（總部管理員使用）
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getApplicationStatistics(HttpSession session) {
        employeeLogger.logInfo("收到查詢申請統計請求");
        
        EmployeeDTO currentEmployee = (EmployeeDTO) session.getAttribute("loggedInEmployee");
        if (currentEmployee == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "請重新登入"));
        }

        if (currentEmployee.getRole() != EmployeeRole.HEADQUARTERS_ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "權限不足：只有總部管理員可以查看統計信息"));
        }

        try {
            List<EmployeeApplicationDTO> pendingApplications = applicationService.getApplicationsByStatus(ApplicationStatus.PENDING);
            List<EmployeeApplicationDTO> approvedApplications = applicationService.getApplicationsByStatus(ApplicationStatus.APPROVED);
            List<EmployeeApplicationDTO> rejectedApplications = applicationService.getApplicationsByStatus(ApplicationStatus.REJECTED);
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("pending", pendingApplications.size());
            statistics.put("approved", approvedApplications.size());
            statistics.put("rejected", rejectedApplications.size());
            statistics.put("total", pendingApplications.size() + approvedApplications.size() + rejectedApplications.size());
            
            employeeLogger.logInfo("申請統計查詢成功: pending={}, approved={}, rejected={}", 
                    pendingApplications.size(), approvedApplications.size(), rejectedApplications.size());
            
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            employeeLogger.logError("查詢申請統計失敗: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "查詢統計失敗：" + e.getMessage()));
        }
    }
}
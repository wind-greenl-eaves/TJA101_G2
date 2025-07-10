package com.eatfast.employee.controller;

import com.eatfast.employee.util.EmployeeLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class EmployeeLogTestController {
    
    private static final Logger logger = LoggerFactory.getLogger(EmployeeLogTestController.class);
    
    @Autowired
    private EmployeeLogger employeeLogger;
    
    @GetMapping("/employee-log")
    public String testEmployeeLog() {
        logger.info("=== 測試員工日誌記錄 INFO ===");
        logger.debug("=== 測試員工日誌記錄 DEBUG ===");
        logger.warn("=== 測試員工日誌記錄 WARN ===");
        logger.error("=== 測試員工日誌記錄 ERROR ===");
        
        // 使用自定義的員工日誌記錄器
        employeeLogger.logInfo("=== 使用 EmployeeLogger 記錄 INFO 日誌 ===");
        employeeLogger.logDebug("=== 使用 EmployeeLogger 記錄 DEBUG 日誌 ===");
        employeeLogger.logWarn("=== 使用 EmployeeLogger 記錄 WARN 日誌 ===");
        employeeLogger.logError("=== 使用 EmployeeLogger 記錄 ERROR 日誌 ===");
        
        // 記錄多條日誌確保檔案會被創建
        for (int i = 1; i <= 5; i++) {
            employeeLogger.logInfo("員工模組測試日誌 第 %d 條", i);
        }
        
        return "員工日誌測試完成，請檢查 logs/employee.log 文件 (使用自定義 EmployeeLogger)";
    }
}
package com.eatfast.employee.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class EmployeeLogger {
    
    private static final Logger logger = LoggerFactory.getLogger(EmployeeLogger.class);
    private static final String LOG_FILE_PATH = "logs/employee.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    public void logInfo(String message, Object... args) {
        String formattedMessage = String.format(message, args);
        logger.info(formattedMessage);
        writeToFile("INFO", formattedMessage);
    }
    
    public void logDebug(String message, Object... args) {
        String formattedMessage = String.format(message, args);
        logger.debug(formattedMessage);
        writeToFile("DEBUG", formattedMessage);
    }
    
    public void logWarn(String message, Object... args) {
        String formattedMessage = String.format(message, args);
        logger.warn(formattedMessage);
        writeToFile("WARN", formattedMessage);
    }
    
    public void logError(String message, Object... args) {
        String formattedMessage = String.format(message, args);
        logger.error(formattedMessage);
        writeToFile("ERROR", formattedMessage);
    }
    
    private void writeToFile(String level, String message) {
        try {
            File logFile = new File(LOG_FILE_PATH);
            
            // 確保目錄存在
            logFile.getParentFile().mkdirs();
            
            // 創建日誌格式
            String timestamp = LocalDateTime.now().format(formatter);
            String logEntry = String.format("%s [%s] %s - %s%n", 
                timestamp, Thread.currentThread().getName(), level, message);
            
            // 追加寫入文件
            try (FileWriter writer = new FileWriter(logFile, true)) {
                writer.write(logEntry);
                writer.flush();
            }
            
        } catch (IOException e) {
            logger.error("無法寫入員工日誌文件: {}", e.getMessage());
        }
    }
}
package com.eatfast.common.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局例外處理器 - 【已升級】
 * 新增了對 DataIntegrityViolationException 的處理。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "資料驗證失敗，請檢查您的輸入。");
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        response.put("errors", fieldErrors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 【新增例外處理】: 捕捉資料庫完整性相關的例外。
     * 說明: DataIntegrityViolationException 是 Spring 對資料庫層級錯誤
     * (如：資料過長、唯一鍵衝突) 的一層包裝。捕捉這個例外可以避免
     * 將底層的 SQL 錯誤直接暴露給前端。
     *
     * @param ex 資料庫完整性例外。
     * @return 返回一個包含友善錯誤訊息的 ResponseEntity，HTTP 狀態碼為 400 (Bad Request)。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Map<String, Object> response = new HashMap<>();
        String message = "資料庫操作失敗。";
        
        // 檢查根本原因是否為資料過長
        if (ex.getMostSpecificCause().getMessage().contains("Data too long")) {
            message = "上傳的檔案過大，請選擇較小的檔案。";
        } else if (ex.getMostSpecificCause().getMessage().contains("Duplicate entry")) {
            message = "資料重複，可能已有相同的帳號或Email存在。";
        }
        
        response.put("message", message);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}

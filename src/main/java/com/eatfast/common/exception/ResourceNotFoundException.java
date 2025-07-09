// =================================================================================
// 自定義例外 (ResourceNotFoundException)
// 說明: 當查詢不到指定資源時拋出此例外。
// =================================================================================
package com.eatfast.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) // 404 Not Found
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
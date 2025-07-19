// =================================================================================
// 自定義例外 (DuplicateResourceException)
// 說明: 當試圖新增的資料違反唯一約束時拋出。
// =================================================================================
package com.eatfast.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) //
// 當拋出此例外時，HTTP狀態碼將設為409 Conflict
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
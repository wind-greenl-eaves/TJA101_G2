package com.eatfast.store.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 自定義例外：當根據提供的條件找不到門市時拋出。
 * @ResponseStatus(HttpStatus.NOT_FOUND) 這個註解會讓 Spring MVC 
 * 在此例外未被捕捉時，自動回傳 HTTP 404 Not Found 狀態碼給前端，
 * 這是一個非常好的實踐。
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class StoreNotFoundException extends RuntimeException {

    /**
     * 建構子，允許傳入自定義的錯誤訊息。
     * @param message 錯誤訊息，例如 "找不到 ID 為 5 的門市"
     */
    public StoreNotFoundException(String message) {
        super(message);
    }
}
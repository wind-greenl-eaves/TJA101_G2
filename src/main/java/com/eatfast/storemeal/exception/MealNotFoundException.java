package com.eatfast.storemeal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 自定義例外：當根據提供的 ID 找不到餐點時拋出。
 * @ResponseStatus(HttpStatus.NOT_FOUND) 會讓 Spring MVC 自動回傳 HTTP 404 狀態碼。
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class MealNotFoundException extends RuntimeException {

    public MealNotFoundException(String message) {
        super(message);
    }
}

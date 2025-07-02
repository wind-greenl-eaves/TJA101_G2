package com.eatfast.common.response;

import java.util.Map;

public class ValidationErrorResponse {
    private Map<String, String> errors;

    public ValidationErrorResponse(Map<String, String> errors) {
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}
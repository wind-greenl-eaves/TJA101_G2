package com.eatfast.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 會員驗證請求 DTO
 */
public class MemberVerificationRequest {
    
    @NotBlank(message = "電子郵件不能為空")
    @Email(message = "請輸入有效的電子郵件格式")
    private String email;
    
    @NotBlank(message = "驗證碼不能為空")
    @Pattern(regexp = "^\\d{6}$", message = "驗證碼必須為6位數字")
    private String verificationCode;
    
    public MemberVerificationRequest() {}
    
    public MemberVerificationRequest(String email, String verificationCode) {
        this.email = email;
        this.verificationCode = verificationCode;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getVerificationCode() {
        return verificationCode;
    }
    
    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
}
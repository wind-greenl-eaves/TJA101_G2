package com.eatfast.employee.dto;

import com.eatfast.common.enums.AccountStatus;
import com.eatfast.common.enums.EmployeeRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * [可自定義的類別名稱]: UpdateEmployeeRequest
 * 用於接收「更新員工」請求的資料傳輸物件 (DTO)。
 * 欄位加上了驗證註解，以便 Controller 層能透過 @Valid 進行前置校驗。
 */
public class UpdateEmployeeRequest {

    /** 員工姓名 (可選) */
    @Size(max = 20, message = "姓名長度不可超過 20 個字元")
    private String username;

    /** 新密碼 (可選，若提供則會更新並加密) */
    @Size(min = 8, message = "密碼長度至少為 8 個字元")
    private String password;

    /** 員工電子郵件 (可選) */
    @Email(message = "請輸入有效的電子郵件格式")
    private String email;

    /** 員工聯絡電話 (可選) */
    @Pattern(regexp = "^09\\d{2}-?\\d{3}-?\\d{3}$", message = "請輸入有效的台灣手機號碼格式")
    private String phone;
    
    /** 員工角色 (可選) */
    private EmployeeRole role;
    
    /** 帳號狀態 (可選，用於停權或復權) */
    private AccountStatus status;

    /** 所屬門市 ID (可選，用於調店) */
    private Long storeId;

    // ================================================================
    //             標準 Getters and Setters
    // ================================================================

    public String getUsername() { 
        return username; 
    }
    public void setUsername(String username) { 
        this.username = username; 
    }

    public String getPassword() { 
        return password; 
    }
    public void setPassword(String password) { 
        this.password = password; 
    }

    public String getEmail() { 
        return email; 
    }
    public void setEmail(String email) { 
        this.email = email; 
    }

    public String getPhone() { 
        return phone; 
    }
    public void setPhone(String phone) { 
        this.phone = phone; 
    }

    public EmployeeRole getRole() { 
        return role; 
    }
    public void setRole(EmployeeRole role) { 
        this.role = role; 
    }

    public AccountStatus getStatus() { 
        return status; 
    }
    public void setStatus(AccountStatus status) { 
        this.status = status; 
    }

    public Long getStoreId() { 
        return storeId; 
    }
    public void setStoreId(Long storeId) { 
        this.storeId = storeId; 
    }
}

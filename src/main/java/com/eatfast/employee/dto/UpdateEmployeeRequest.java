package com.eatfast.employee.dto;

import com.eatfast.common.enums.AccountStatus;
import com.eatfast.common.enums.EmployeeRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

/**
 * [可自定義的類別名稱]: UpdateEmployeeRequest - 【已重構】
 * 用於接收「更新員工」請求的 DTO。
 * 【本次修改】: 為 username, email, phone 欄位新增 @NotBlank 驗證，
 * 確保這些欄位一旦被提交，其值不能為空字串或僅包含空白字元。
 */
public class UpdateEmployeeRequest {

    /** 員工姓名 (必填) */
    @NotBlank(message = "員工姓名不可為空") // 【已新增】
    @Size(max = 20, message = "姓名長度不可超過 20 個字元")
    private String username;

    /** 新密碼 (可選，若提供則會更新並加密) */
    @Size(min = 8, message = "密碼長度至少為 8 個字元")
    private String password;

    /** 員工電子郵件 (必填) */
    @NotBlank(message = "電子郵件不可為空") // 【已新增】
    @Email(message = "請輸入有效的電子郵件格式")
    private String email;

    /** 員工聯絡電話 (必填) */
    @NotBlank(message = "連絡電話不可為空") // 【已新增】
    @Pattern(regexp = "^09\\d{2}-?\\d{3}-?\\d{3}$", message = "請輸入有效的台灣手機號碼格式")
    private String phone;
    
    /** 員工角色 (必填) */
    @NotNull(message = "員工角色不可為空") // 【已修改】: 從可選改為必填
    private EmployeeRole role;
    
    /** 帳號狀態 (必填) */
    @NotNull(message = "帳號狀態不可為空") // 【已修改】: 從可選改為必填
    private AccountStatus status;

    /** 所屬門市 ID (必填) */
    @NotNull(message = "所屬門市 ID 不可為空") // 【已修改】: 從可選改為必填
    private Long storeId;

    /** 員工照片 (可選) */
    private MultipartFile photo;  // 新增照片欄位

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

    public MultipartFile getPhoto() {
        return photo;
    }

    public void setPhoto(MultipartFile photo) {
        this.photo = photo;
    }
}
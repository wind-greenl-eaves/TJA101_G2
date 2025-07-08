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
 * [可自定義的類別名稱]: UpdateEmployeeRequest
 * 用於接收「更新員工」請求的 DTO。
 */
public class UpdateEmployeeRequest {

    /** 員工姓名 (必填) */
    @NotBlank(message = "員工姓名不可為空")
    @Size(max = 20, message = "姓名長度不可超過 20 個字元")
    private String username;

    /**
     * 新密碼 (可選)
     *  將 @Size(min=8) 修改為 @Pattern。
     * 說明:
     * - 原本的 @Size(min=8) 會錯誤地將「空字串」視為驗證失敗 (因為長度為 0)。
     * - 新的 @Pattern 使用了與前端 HTML 完全相同的正規表示式，其含義是：
     * 此欄位的值必須匹配「空字串(^$)」或「(|)」「符合8位數以上英數混合的字串(...)」。
     * - 這完美地實現了「若不修改則留空，若要修改則必須符合格式」的業務邏輯，
     * 是處理可選欄位驗證的最佳實踐。
     */
    @Pattern(regexp = "^$|^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message = "若填寫新密碼，長度需至少為8個字元，且包含至少一個字母和一個數字")
    private String password;

    /** 員工電子郵件 (必填) */
    @NotBlank(message = "電子郵件不可為空")
    @Email(message = "請輸入有效的電子郵件格式")
    private String email;

    /** 員工聯絡電話 (必填) */
    @NotBlank(message = "連絡電話不可為空")
    @Pattern(regexp = "^09\\d{2}-?\\d{3}-?\\d{3}$", message = "請輸入有效的台灣手機號碼格式")
    private String phone;
    
    /** 員工角色 (必填) */
    @NotNull(message = "員工角色不可為空")
    private EmployeeRole role;
    
    /** 帳號狀態 (必填) */
    @NotNull(message = "帳號狀態不可為空")
    private AccountStatus status;

    /** 所屬門市 ID (必填) */
    @NotNull(message = "所屬門市 ID 不可為空")
    private Long storeId;

    /** 員工照片 (可選) */
    private MultipartFile photo;

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

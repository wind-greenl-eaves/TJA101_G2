// ================================================================
// 檔案名稱: CreateEmployeeRequest.java
// 功能說明: 新增員工請求的資料傳輸物件 (DTO)
// 架構層級: 數據傳輸層 (Data Transfer Layer)
// 配對關係:
//   - 控制器: EmployeeController.createEmployee() 方法接收
//   - 服務層: EmployeeService.createEmployee() 方法處理
//   - 映射器: EmployeeMapper.toEntity() 轉換為實體
//   - 驗證器: Jakarta Bean Validation 註解進行欄位驗證
// 設計模式: DTO Pattern (資料傳輸物件模式)
// 版本資訊: Spring Boot 3.x + Jakarta Validation
// ================================================================
package com.eatfast.employee.dto;

import com.eatfast.common.enums.EmployeeRole;
import com.eatfast.common.enums.Gender;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * [可自定義的類別名稱]: CreateEmployeeRequest
 * 用於接收「新增員工」請求的資料傳輸物件 (DTO)。
 * 定義了建立新員工所需的所有欄位及對應的驗證規則。
 */
public class CreateEmployeeRequest {

    /** 員工真實姓名 (必填) */
    @NotBlank(message = "員工姓名不可為空")
    @Size(max = 20, message = "姓名長度不可超過 20 個字元")
    private String username;

    /** 登入帳號 (必填，限英數字、底線、點、連字號) */
    @NotBlank(message = "登入帳號不可為空")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "帳號格式不符")
    @Size(max = 50, message = "帳號長度不可超過 50 個字元")
    private String account;

    /** 登入密碼 (必填，長度至少 8) */
    @NotBlank(message = "登入密碼不可為空")
    @Size(min = 8, message = "密碼長度至少為 8 個字元")
    private String password;

    /** 電子郵件 (必填，需符合格式) */
    @NotBlank(message = "電子郵件不可為空")
    @Email(message = "請輸入有效的電子郵件格式")
    private String email;

    /** 聯絡電話 (必填，需符合手機格式) */
    @NotBlank(message = "連絡電話不可為空")
    @Pattern(regexp = "^09\\d{2}-?\\d{3}-?\\d{3}$", message = "請輸入有效的台灣手機號碼格式")
    private String phone;
    
    /** 員工角色 (必填) */
    @NotNull(message = "員工角色不可為空")
    private EmployeeRole role;
    
    /** 員工性別 (必填) */
    @NotNull(message = "性別不可為空")
    private Gender gender;

    /** 身分證字號 (必填，需符合格式) */
    @NotBlank(message = "身分證字號不可為空")
    @Pattern(regexp = "^[A-Z][1-2]\\d{8}$", message = "請輸入有效的台灣身分證字號格式")
    private String nationalId;

    /** 所屬門市 ID (必填) */
    @NotNull(message = "所屬門市 ID 不可為空")
    private Long storeId;

    /** 員工照片 (選填) */
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

    public String getAccount() { 
        return account; 
    }
    public void setAccount(String account) { 
        this.account = account; 
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

    public Gender getGender() { 
        return gender; 
    }
    public void setGender(Gender gender) { 
        this.gender = gender; 
    }

    public String getNationalId() { 
        return nationalId; 
    }
    public void setNationalId(String nationalId) { 
        this.nationalId = nationalId; 
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
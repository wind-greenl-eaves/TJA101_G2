package com.tia102g2.member.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.eatfast.member.service.MemberService; // 修正 import 路徑
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/member/validate")
public class MemberValidationController {

    @Autowired
    private MemberService memberService;
    
    // 驗證帳號是否重複
    @GetMapping("/account")
    public ResponseEntity<Map<String, Object>> validateAccount(@RequestParam String account) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 檢查格式：只允許英文、數字，長度4-20
            if (account == null || account.trim().isEmpty()) {
                response.put("valid", false);
                response.put("message", "請輸入帳號");
                return ResponseEntity.ok(response);
            }
            
            if (!Pattern.matches("^[a-zA-Z0-9]{4,20}$", account)) {
                response.put("valid", false);
                response.put("message", "帳號只能包含英文字母和數字，長度4-20字元");
                return ResponseEntity.ok(response);
            }
            
            // 檢查是否已存在
            boolean exists = memberService.isAccountExists(account);
            if (exists) {
                response.put("valid", false);
                response.put("message", "此帳號已被註冊");
            } else {
                response.put("valid", true);
                response.put("message", "帳號可以使用");
            }
            
        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "系統錯誤，請稍後再試");
        }
        
        return ResponseEntity.ok(response);
    }
    
    // 驗證Email格式和重複性
    @GetMapping("/email")
    public ResponseEntity<Map<String, Object>> validateEmail(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 檢查格式
            if (email == null || email.trim().isEmpty()) {
                response.put("valid", false);
                response.put("message", "請輸入電子郵件");
                return ResponseEntity.ok(response);
            }
            
            String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
            if (!Pattern.matches(emailPattern, email)) {
                response.put("valid", false);
                response.put("message", "請輸入正確的電子郵件格式");
                return ResponseEntity.ok(response);
            }
            
            // 檢查是否已存在
            boolean exists = memberService.isEmailExists(email);
            if (exists) {
                response.put("valid", false);
                response.put("message", "此電子郵件已被註冊");
            } else {
                response.put("valid", true);
                response.put("message", "電子郵件可以使用");
            }
            
        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "系統錯誤，請稍後再試");
        }
        
        return ResponseEntity.ok(response);
    }
    
    // 驗證手機號碼格式和重複性
    @GetMapping("/phone")
    public ResponseEntity<Map<String, Object>> validatePhone(@RequestParam String phone) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 檢查格式
            if (phone == null || phone.trim().isEmpty()) {
                response.put("valid", false);
                response.put("message", "請輸入手機號碼");
                return ResponseEntity.ok(response);
            }
            
            // 允許 09xxxxxxxx 或 09xx-xxx-xxx 格式
            String phonePattern = "^09\\d{2}-?\\d{3}-?\\d{3,4}$";
            if (!Pattern.matches(phonePattern, phone)) {
                response.put("valid", false);
                response.put("message", "請輸入正確的手機號碼格式（例：0912345678）");
                return ResponseEntity.ok(response);
            }
            
            // 檢查是否已存在
            boolean exists = memberService.isPhoneExists(phone);
            if (exists) {
                response.put("valid", false);
                response.put("message", "此手機號碼已被註冊");
            } else {
                response.put("valid", true);
                response.put("message", "手機號碼可以使用");
            }
            
        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "系統錯誤，請稍後再試");
        }
        
        return ResponseEntity.ok(response);
    }
    
    // 驗證密碼格式
    @GetMapping("/password")
    public ResponseEntity<Map<String, Object>> validatePassword(@RequestParam String password) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (password == null || password.trim().isEmpty()) {
                response.put("valid", false);
                response.put("message", "請輸入密碼");
                return ResponseEntity.ok(response);
            }
            
            if (password.length() < 8) {
                response.put("valid", false);
                response.put("message", "密碼至少需要8個字元");
                return ResponseEntity.ok(response);
            }
            
            if (password.length() > 50) {
                response.put("valid", false);
                response.put("message", "密碼不能超過50個字元");
                return ResponseEntity.ok(response);
            }
            
            // 可以加入更複雜的密碼規則檢查
            // 例如：必須包含大小寫字母、數字、特殊字元等
            
            response.put("valid", true);
            response.put("message", "密碼格式正確");
            
        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "系統錯誤，請稍後再試");
        }
        
        return ResponseEntity.ok(response);
    }
    
    // 驗證會員姓名
    @GetMapping("/username")
    public ResponseEntity<Map<String, Object>> validateUsername(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (username == null || username.trim().isEmpty()) {
                response.put("valid", false);
                response.put("message", "請輸入會員姓名");
                return ResponseEntity.ok(response);
            }
            
            if (username.trim().length() < 2) {
                response.put("valid", false);
                response.put("message", "姓名至少需要2個字元");
                return ResponseEntity.ok(response);
            }
            
            if (username.trim().length() > 20) {
                response.put("valid", false);
                response.put("message", "姓名不能超過20個字元");
                return ResponseEntity.ok(response);
            }
            
            response.put("valid", true);
            response.put("message", "姓名格式正確");
            
        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "系統錯誤，請稍後再試");
        }
        
        return ResponseEntity.ok(response);
    }
}
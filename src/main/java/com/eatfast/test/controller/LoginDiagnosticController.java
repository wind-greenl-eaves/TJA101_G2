package com.eatfast.test.controller;

import com.eatfast.member.service.MemberService;
import com.eatfast.member.model.MemberEntity;
import com.eatfast.member.dto.MemberCreateRequest;
import com.eatfast.member.dto.MemberUpdateRequest;
import com.eatfast.common.enums.Gender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.*;

/**
 * 登入診斷測試控制器
 * 用於診斷會員登入問題
 */
@Controller
@RequestMapping("/test/auth")
public class LoginDiagnosticController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    public LoginDiagnosticController(MemberService memberService, PasswordEncoder passwordEncoder) {
        this.memberService = memberService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 顯示診斷頁面
     */
    @GetMapping("/diagnostic")
    public String showDiagnosticPage() {
        return "test/login-diagnostic";
    }

    /**
     * 檢查帳號是否存在
     */
    @GetMapping("/check-account")
    @ResponseBody
    public Map<String, Object> checkAccount(@RequestParam String account) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<MemberEntity> memberOpt = memberService.getMemberByAccount(account);
            
            if (memberOpt.isPresent()) {
                MemberEntity member = memberOpt.get();
                result.put("exists", true);
                result.put("memberId", member.getMemberId());
                result.put("username", member.getUsername());
                result.put("email", member.getEmail());
                result.put("enabled", member.isEnabled());
                
                // 檢查密碼格式
                String password = member.getPassword();
                if (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$")) {
                    result.put("passwordFormat", "BCrypt (正確)");
                } else if (password.length() == 32) {
                    result.put("passwordFormat", "MD5 (可能有問題)");
                } else if (password.length() < 20) {
                    result.put("passwordFormat", "明文密碼 (不安全)");
                } else {
                    result.put("passwordFormat", "未知格式 (長度: " + password.length() + ")");
                }
            } else {
                result.put("exists", false);
            }
        } catch (Exception e) {
            result.put("exists", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 測試密碼驗證
     */
    @PostMapping("/test-password")
    @ResponseBody
    public Map<String, Object> testPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String account = request.get("account");
        String password = request.get("password");
        
        try {
            Optional<MemberEntity> memberOpt = memberService.getMemberByAccount(account);
            
            if (memberOpt.isEmpty()) {
                result.put("success", false);
                result.put("reason", "帳號不存在");
                result.put("details", "系統中找不到帳號: " + account);
                return result;
            }
            
            MemberEntity member = memberOpt.get();
            
            // 檢查帳號是否啟用
            if (!member.isEnabled()) {
                result.put("success", false);
                result.put("reason", "帳號已停用");
                result.put("details", "帳號存在但已被停用");
                return result;
            }
            
            // 測試密碼比對
            boolean passwordMatches = passwordEncoder.matches(password, member.getPassword());
            
            if (passwordMatches) {
                result.put("success", true);
                result.put("memberName", member.getUsername());
                
                // 模擬完整登入流程
                try {
                    // 這裡可以測試完整的登入邏輯
                    result.put("loginTestResult", "模擬登入成功");
                } catch (Exception e) {
                    result.put("loginTestResult", "模擬登入失敗: " + e.getMessage());
                }
            } else {
                result.put("success", false);
                result.put("reason", "密碼不正確");
                
                // 提供更詳細的診斷資訊
                String storedPassword = member.getPassword();
                result.put("details", String.format(
                    "密碼比對失敗\n" +
                    "輸入密碼長度: %d\n" +
                    "資料庫密碼長度: %d\n" +
                    "資料庫密碼前綴: %s\n" +
                    "是否為BCrypt格式: %s",
                    password.length(),
                    storedPassword.length(),
                    storedPassword.length() > 10 ? storedPassword.substring(0, 10) + "..." : storedPassword,
                    storedPassword.startsWith("$2") ? "是" : "否"
                ));
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("reason", "系統錯誤");
            result.put("details", e.getMessage());
        }
        
        return result;
    }

    /**
     * 創建測試帳號
     */
    @PostMapping("/create-test-account")
    @ResponseBody
    public Map<String, Object> createTestAccount() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String testAccount = "testuser" + System.currentTimeMillis();
            String testPassword = "test123456";
            
            // 檢查帳號是否已存在
            if (memberService.getMemberByAccount(testAccount).isPresent()) {
                result.put("success", false);
                result.put("message", "測試帳號已存在");
                return result;
            }
            
            // 創建測試會員
            MemberCreateRequest createRequest = new MemberCreateRequest();
            createRequest.setAccount(testAccount);
            createRequest.setPassword(testPassword);
            createRequest.setUsername("測試用戶");
            createRequest.setEmail(testAccount + "@test.com");
            createRequest.setPhone("0912-345-678");
            createRequest.setBirthday(LocalDate.of(1990, 1, 1));
            createRequest.setGender(Gender.M);
            
            MemberEntity savedMember = memberService.registerMember(createRequest);
            
            result.put("success", true);
            result.put("account", testAccount);
            result.put("password", testPassword);
            result.put("memberId", savedMember.getMemberId());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        
        return result;
    }

    /**
     * 查看所有會員
     */
    @GetMapping("/all-members")
    @ResponseBody
    public Map<String, Object> getAllMembers() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<MemberEntity> members = memberService.getAllMembers();
            List<Map<String, Object>> memberList = new ArrayList<>();
            
            for (MemberEntity member : members) {
                Map<String, Object> memberInfo = new HashMap<>();
                memberInfo.put("account", member.getAccount());
                memberInfo.put("username", member.getUsername());
                memberInfo.put("email", member.getEmail());
                memberInfo.put("enabled", member.isEnabled());
                
                // 分析密碼格式
                String password = member.getPassword();
                if (password.startsWith("$2")) {
                    memberInfo.put("passwordFormat", "BCrypt");
                } else if (password.length() == 32) {
                    memberInfo.put("passwordFormat", "MD5");
                } else if (password.length() < 20) {
                    memberInfo.put("passwordFormat", "明文");
                } else {
                    memberInfo.put("passwordFormat", "未知");
                }
                
                memberList.add(memberInfo);
            }
            
            result.put("members", memberList);
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 批量升級明文密碼為BCrypt格式
     */
    @PostMapping("/upgrade-passwords")
    @ResponseBody
    public Map<String, Object> upgradeAllPasswords() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<MemberEntity> allMembers = memberService.getAllMembers();
            int upgradedCount = 0;
            int totalCount = allMembers.size();
            List<String> upgradedAccounts = new ArrayList<>();
            
            for (MemberEntity member : allMembers) {
                String currentPassword = member.getPassword();
                
                // 檢查是否為明文密碼（不是BCrypt格式）
                if (!currentPassword.startsWith("$2a$") && 
                    !currentPassword.startsWith("$2b$") && 
                    !currentPassword.startsWith("$2y$")) {
                    
                    try {
                        // 將明文密碼加密
                        String encryptedPassword = passwordEncoder.encode(currentPassword);
                        
                        // 直接更新密碼欄位
                        member.setPassword(encryptedPassword);
                        memberService.updateMemberDetails(new MemberUpdateRequest() {{
                            setMemberId(member.getMemberId());
                            setUsername(member.getUsername());
                            setEmail(member.getEmail());
                            setPhone(member.getPhone());
                            setBirthday(member.getBirthday());
                            setGender(member.getGender());
                            setIsEnabled(member.isEnabled());
                        }});
                        
                        upgradedCount++;
                        upgradedAccounts.add(member.getAccount());
                        
                    } catch (Exception e) {
                        System.err.println("升級帳號 " + member.getAccount() + " 的密碼失敗: " + e.getMessage());
                    }
                }
            }
            
            result.put("success", true);
            result.put("totalMembers", totalCount);
            result.put("upgradedCount", upgradedCount);
            result.put("upgradedAccounts", upgradedAccounts);
            result.put("message", String.format("成功升級 %d 個帳號的密碼（共 %d 個帳號）", upgradedCount, totalCount));
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}
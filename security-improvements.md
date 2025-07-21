# 員工登入頁面安全改進建議

## 1. 密碼加密改進 

### 當前問題
- 密碼以明文形式儲存在資料庫
- 登入驗證使用明文比對

### 建議改進
```java
// 在 EmployeeServiceImpl 中加入 BCrypt 密碼加密
@Autowired
private PasswordEncoder passwordEncoder;

// 登入驗證改為：
if (!passwordEncoder.matches(password, employee.getPassword())) {
    throw new IllegalArgumentException("帳號或密碼錯誤");
}
```

## 2. 管理員小幫手安全改進

### 當前問題
- 直接在頁面顯示員工密碼
- 可能洩露敏感資訊

### 建議改進
- 移除密碼顯示功能
- 或僅在開發環境中顯示
- 加入環境變數控制

## 3. 登入嘗試限制

### 建議新增
- 失敗次數限制
- 帳號鎖定機制
- 驗證碼功能

## 4. Session 安全

### 建議改進
- Session 超時設定
- HTTPS 強制使用
- CSRF 保護
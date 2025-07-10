-- 測試登入失敗次數限制功能的初始化腳本
-- 這個腳本會確保所有現有員工都有正確的登入失敗追蹤欄位預設值

-- 更新所有現有員工的登入失敗次數為 0（如果是 NULL）
UPDATE employee 
SET login_failure_count = 0 
WHERE login_failure_count IS NULL;

-- 確保帳號狀態正確
UPDATE employee 
SET status = 'ACTIVE' 
WHERE status IS NULL;

-- 顯示更新結果
SELECT 
    employee_id,
    username,
    account,
    status,
    login_failure_count,
    last_failure_time,
    account_locked_time
FROM employee 
ORDER BY employee_id;
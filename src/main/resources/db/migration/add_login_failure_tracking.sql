-- 為員工表添加登入失敗次數追蹤欄位
-- 如果欄位已存在則不會執行，避免重複建立

-- 添加登入失敗次數欄位
ALTER TABLE employee 
ADD COLUMN IF NOT EXISTS login_failure_count INT NOT NULL DEFAULT 0 
COMMENT '登入失敗次數，連續失敗8次將停用帳號';

-- 添加最後失敗時間欄位
ALTER TABLE employee 
ADD COLUMN IF NOT EXISTS last_failure_time DATETIME NULL 
COMMENT '最後一次登入失敗的時間';

-- 添加帳號鎖定時間欄位
ALTER TABLE employee 
ADD COLUMN IF NOT EXISTS account_locked_time DATETIME NULL 
COMMENT '帳號被鎖定的時間';

-- 為新增欄位建立索引以提升查詢效能
CREATE INDEX IF NOT EXISTS idx_employee_login_failure 
ON employee(login_failure_count, last_failure_time);
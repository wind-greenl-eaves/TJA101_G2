-- =======================================================================================
-- 檔案名稱：eatfast_db_initialization_integrated.sql
-- 資料庫：eatfast_db
-- 版本：v1.5.1 - 已整合員工申請功能
-- 功能：此腳本為早餐店系統「eatfast_db」的完整初始化腳本。
--      它會執行以下操作：
--      1. 建立資料庫 (如果不存在)。
--      2. 依賴序刪除舊有的資料表，確保環境乾淨。
--      3. 建立所有需要的資料表結構，包含新增的「employee_applications」。
--      4. 插入用於開發與測試的範例資料。
--      5. 建立用於自動生成訂單編號的觸發器。
-- =======================================================================================

-- -----------------------------------------------------
-- 步驟 1: 資料庫建立與選用
-- -----------------------------------------------------
-- 如果 eatfast_db 資料庫不存在，則建立它。
-- 使用 utf8mb4 字元集以支援更廣泛的字元，包括表情符號。
CREATE DATABASE IF NOT EXISTS eatfast_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
-- 切換到 eatfast_db 資料庫，後續的 SQL 操作都將在此資料庫中執行。
USE eatfast_db;

-- -----------------------------------------------------
-- 步驟 2: 資料表刪除 (確保可重複執行此腳本)
-- -----------------------------------------------------
-- 【修正】為了穩定地處理複雜的外鍵相依性，在刪除操作前後停用/啟用外鍵檢查。
-- 這是重設資料庫時的標準做法，可以避免因刪除順序問題造成的錯誤。
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS announcement;
DROP TABLE IF EXISTS homepage_Info;
DROP TABLE IF EXISTS news;
DROP TABLE IF EXISTS feedback;
DROP TABLE IF EXISTS store_meal_status;
DROP TABLE IF EXISTS cart;
DROP TABLE IF EXISTS fav;
DROP TABLE IF EXISTS order_list_info;
DROP TABLE IF EXISTS order_list;
DROP TABLE IF EXISTS employee_applications; -- ★ 新增：刪除員工申請表
DROP TABLE IF EXISTS employee_permission;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS permission;
DROP TABLE IF EXISTS meal;
DROP TABLE IF EXISTS meal_type;
DROP TABLE IF EXISTS store;
DROP TABLE IF EXISTS member;

SET FOREIGN_KEY_CHECKS = 1;

-- =================================================================
-- 步驟一：建立 member 資料表 (支援軟刪除)
-- =================================================================
-- 這份 SQL 腳本定義了 member 資料表的結構。
-- 關鍵點：
-- 1. `gender` 欄位：使用 CHAR(1) 型別，並搭配 CHECK 約束，限制只能存入 'M', 'F', 'O' 三種值。
-- 2. `is_enabled` 欄位：用於實現軟刪除。TRUE 代表啟用，FALSE 代表已刪除。
-- 3. `INDEX idx_is_enabled`: 為 `is_enabled` 欄位建立索引，以優化只查詢啟用中會員的效能。
-- =================================================================

CREATE TABLE IF NOT EXISTS member (
    member_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '會員編號 (主鍵，自動增長)',
    username VARCHAR(20) NOT NULL COMMENT '會員真實姓名',
    account VARCHAR(50) NOT NULL COMMENT '登入帳號 (使用者自訂，不可重複)',
    password VARCHAR(255) NOT NULL COMMENT '登入密碼 (應儲存經過加密後的雜湊值)',
    email VARCHAR(100) NOT NULL COMMENT '電子郵件 (不可重複)',
    phone VARCHAR(20) NOT NULL COMMENT '連絡電話',
    birthday DATE NOT NULL COMMENT '會員生日',
     gender CHAR(1) NOT NULL COMMENT '性別 (M: 男性, F: 女性, O: 其他)',
    -- 軟刪除標記欄位 --
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '軟刪除標記 (TRUE: 啟用, FALSE: 已刪除/停用)',
        -- 時間戳記欄位 --
    last_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '資料最後更新時間',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '帳號註冊時間',
        -- 主鍵與唯一鍵约束 --
    PRIMARY KEY (member_id),
    UNIQUE KEY uk_account (account) COMMENT '設定 account 為唯一鍵，確保所有會員的登入帳號不重複',
    UNIQUE KEY uk_email (email) COMMENT '設定 email 為唯一鍵，確保所有會員的電子郵件不重複',
        -- 為軟刪除欄位建立索引以優化查詢效能 --
    INDEX idx_is_enabled (is_enabled),
    -- 【新增】CHECK 約束，確保性別欄位的資料正確性
    CONSTRAINT chk_gender CHECK (gender IN ('M', 'F', 'O'))
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='會員資料表，儲存前台使用者的所有資訊 (已支援軟刪除)。';
    
-- 1. 我們不需要手動插入 member_id, is_enabled, created_at, last_updated_at 這些欄位，
--    因為它們都有 AUTO_INCREMENT 或 DEFAULT 預設值，資料庫會自動處理。
--
-- 2. 【開發階段專用 - 安全性警告】
--    為了方便開發初期測試，以下所有會員的密碼均設定為簡單的明文密碼。
--    **嚴禁將此 SQL 腳本用於正式上線的產品環境！**
--    正式環境的密碼必須經過後端 BCrypt 加密處理。
-- =================================================================

INSERT INTO member (username, account, password, email, phone, birthday, gender) VALUES
('陳威宇', 'yijun.chen', 'password001', 'yijun.chen@gmail.com', '0912-345-678', '1990-05-15', 'M'),
('林佳蓉', 'chiming.lin', 'password002', 'chiming.lin@gmail.com', '0928-765-432', '1985-11-22', 'F'),
('黃冠霖', 'meiling.huang', 'password003', 'meiling.huang@gmail.com', '0933-123-789', '1995-02-10', 'M'),
('張雅筑', 'chiahao.chang', 'password004', 'chiahao.chang@gmail.com', '0955-987-654', '1988-08-08', 'F'),
('李俊賢', 'yating.wu', 'password005', 'yating.wu@gmail.com', '0966-555-222', '1992-12-01', 'M'),
('王志明', 'chienhung.liu', 'password006', 'chienhung.liu@gmail.com', '0910-111-333', '1979-07-25', 'M'),
('陳靜香', 'shufen.tsai', 'password007', 'shufen.tsai@gmail.com', '0978-222-444', '1998-03-30', 'F'),
('林承翰', 'wenhsiung.hsu', 'password008', 'wenhsiung.hsu@gmail.com', '0988-333-555', '1982-09-18', 'M'),
('黃詩涵', 'hsiaohan.cheng', 'password009', 'hsiaohan.cheng@gmail.com', '0919-444-666', '2001-01-05', 'F'),
('張志偉', 'chihwei.wang', 'password010', 'chihwei.wang@gmail.com', '0921-888-777', '1993-06-12', 'M'),
('李美芳', 'yachu.fang', 'password011', 'yachu.fang@gmail.com', '0930-121-212', '1996-04-20', 'F'),
('王淑芬', 'minghui.kao', 'password012', 'minghui.kao@gmail.com', '0952-343-434', '1980-10-10', 'F'),
('陳俊傑', 'chialing.sung', 'password013', 'chialing.sung@gmail.com', '0970-565-656', '1991-08-25', 'M'),
('林心怡', 'chunhsien.li', 'password014', 'chunhsien.li@gmail.com', '0911-787-878', '1987-01-15', 'F'),
('黃俊彥', 'hsinyi.chou', 'password015', 'hsinyi.chou@gmail.com', '0939-909-190', '1999-05-05', 'M'),
('張文傑', 'shauman.hsu', 'password016', 'shauman.hsu@gmail.com', '0963-111-999', '2000-02-29', 'M'),
('李宗霖', 'weilun.chao', 'password017', 'weilun.chao@gmail.com', '0925-123-456', '1994-03-18', 'M'),
('王建民', 'hsiaomei.sun', 'password018', 'hsiaomei.sun@gmail.com', '0935-234-567', '1989-07-07', 'M'),
('陳雅涵', 'tayun.chien', 'password019', 'tayun.chien@gmail.com', '0916-345-678', '1997-11-11', 'F'),
('林建宏', 'meifang.li', 'password020', 'meifang.li@gmail.com', '0977-456-789', '1991-09-20', 'M'),
('黃美玲', 'yuyen.peng', 'password021', 'yuyen.peng@test.com', '0911-111-111', '1982-03-24', 'F'),
('張惠婷', 'ning.chang', 'password022', 'ning.chang@test.com', '0922-222-222', '1982-09-04', 'F'),
('李靜怡', 'ethan.juan', 'password023', 'ethan.juan@test.com', '0933-333-333', '1982-11-08', 'F'),
('王心妤', 'lunmei.kwei', 'password024', 'lunmei.kwei@test.com', '0944-444-444', '1983-12-25', 'F'),
('陳宗翰', 'jay.chou', 'password025', 'jay.chou@test.com', '0955-555-555', '1979-01-18', 'M'),
('林曉慧', 'jolin.tsai', 'password026', 'jolin.tsai@test.com', '0966-666-666', '1980-09-15', 'F'),
('黃家豪', 'ashin.chen', 'password027', 'ashin.chen@test.com', '0977-777-777', '1975-12-06', 'M'),
('張博翔', 'hebe.tien', 'password028', 'hebe.tien@test.com', '0988-888-888', '1983-03-30', 'M'),
('李婉婷', 'jj.lin', 'password029', 'jj.lin@test.com', '0918-181-818', '1981-03-27', 'F'),
('王冠廷', 'gem.tang', 'password030', 'gem.tang@test.com', '0928-282-828', '1991-08-16', 'M');


-- =================================================================
-- 範例：如何插入「已被軟刪除」的會員資料
-- =================================================================
-- 有時候可能需要從後台直接新增一筆停用狀態的帳號。
-- 這時我們就需要明確地指定 `is_enabled` 欄位的值為 FALSE。
-- =================================================================

INSERT INTO member (username, account, password, email, phone, birthday, gender, is_enabled) VALUES
('黃明志', 'disabled.user1', 'password999', 'disabled.user1@example.com', '0900-000-000', '2000-01-01', 'O', FALSE),
('王力宏', 'disabled.user2', 'password998', 'disabled.user2@example.com', '0900-000-001', '2001-02-02', 'O', FALSE);
-- =======================================================================================
-- 資料表: store (門市資料表)
-- 功能: 儲存所有分店的基本資訊，如地址、電話、營業時間等。此為員工和訂單的關聯對象。
-- =======================================================================================
CREATE TABLE store (
  store_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '門市編號 (主鍵，自動增長)',
  store_name VARCHAR(10) NOT NULL COMMENT '門市名稱 (例如: 中山南京店)',
  store_type VARCHAR(30) NOT NULL COMMENT '門市類型(如：總部、一般門市)',
  store_loc VARCHAR(50) NOT NULL COMMENT '門市詳細地址',
  store_phone VARCHAR(10) NOT NULL COMMENT '門市聯絡電話 (區碼+號碼)',
  store_time VARCHAR(50) NOT NULL COMMENT '營業時間描述 (純文字，方便彈性顯示)',
  store_status VARCHAR(15) NOT NULL COMMENT '營業狀態 (由後台控制，例如: 營業中, 休息中, 裝修中)',
  google_map_url VARCHAR(512) NULL COMMENT 'Google Maps 嵌入式網址 (由後端自動生成)',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '此門市資料的建立時間',
  PRIMARY KEY (store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='門市基本資料表。';


/*
 * -----------------------------------------------------
 * 範例資料: store
 * 說明: 新增 2 間分店+總部作為系統的主要營運據點。
 * -----------------------------------------------------
 */
INSERT INTO store (store_name,store_type, store_loc, store_phone, store_time, store_status, google_map_url) VALUES
('中山南京店','BRANCH','台北市中山區南京東路三段219號4F','0227120589','05:00~14:00 周三公休','OPERATING','https://www.google.com/maps/embed/v1/place?key=YOUR_API_KEY&q=%E5%8F%B0%E5%8C%97%E5%B8%82%E4%B8%AD%E5%B1%B1%E5%8D%80%E5%8D%97%E4%BA%AC%E6%9D%B1%E8%B7%AF%E4%B8%89%E6%AE%B5219%E8%99%9F4F'), -- '一般門市營業中' -> 'BRANCH,OPERATING'
('中壢復興店','BRANCH','桃園市中壢區復興路46號9樓','034251108','05:00~14:00 周一公休','OPERATING','https://www.google.com/maps/embed/v1/place?key=YOUR_API_KEY&q=%E6%A1%83%E5%9C%92%E5%B8%82%E4%B8%AD%E5%A3%A2%E5%8D%80%E5%BE%A9%E8%88%88%E8%B7%AF46%E8%99%9F9%E6%A8%93'),    -- '一般門市營業中' -> 'BRANCH,OPERATING'
('總部', 'HEADQUARTERS','台北市中山區南京東路三段219號5F', '0281017777', '不適用', 'HEADQUARTERS','https://www.google.com/maps/embed/v1/place?key=YOUR_API_KEY&q=%E5%8F%B0%E5%8C%97%E5%B8%82%E4%B8%AD%E5%B1%B1%E5%8D%80%E5%8D%97%E4%BA%AC%E6%9D%B1%E8%B7%AF%E4%B8%89%E6%AE%B5219%E8%99%9F5F'); -- '總部營運' -> 'HEADQUARTERS''HEADQUARTERS'


-- =======================================================================================
-- 資料表: employee (員工資料表)
-- 功能: 儲存所有員工（包含總部及各分店）的詳細資料、帳號、角色及狀態。此為後台系統的核心使用者。
-- =======================================================================================
-- =======================================================================================
-- 檔案: employee 資料表結構修正
-- 說明: 根據架構審查報告，修正 employee 資料表的欄位定義，以符合 Java Entity 的設計。
-- 核心變更:
-- 1. 移除 `photo LONGBLOB` 欄位。
-- 2. 新增 `photo_url VARCHAR(255)` 欄位，用於儲存員工照片的相對路徑或 URL。
--    這種「存路徑，不存本體」的做法是業界標準，能大幅提升資料庫效能。
-- =======================================================================================

-- 修正後的 employee 資料表結構
-- =======================================================================================
-- 資料表: employee (員工資料表) - 【已整合帳號鎖定機制】
-- =======================================================================================
CREATE TABLE employee (
    employee_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '員工編號 (主鍵，自動增長)',
    store_id BIGINT NOT NULL COMMENT '所屬門市編號 (外鍵)',
    username VARCHAR(20) NOT NULL COMMENT '員工真實姓名',
    account VARCHAR(50) NOT NULL COMMENT '員工登入後台用的帳號 (不可重複)',
    created_by BIGINT NULL COMMENT '此員工帳號的建檔者 (外鍵)',
    password VARCHAR(255) NOT NULL COMMENT '登入密碼 (應儲存加密後的雜湊值)',
    phone VARCHAR(20) NOT NULL COMMENT '員工連絡電話',
    email VARCHAR(100) NOT NULL COMMENT '員工的電子郵件 (不可重複)',
    role VARCHAR(50) NOT NULL COMMENT '員工角色 (儲存 Enum 名稱，例如: HEADQUARTERS_ADMIN)',
    status VARCHAR(50) NOT NULL COMMENT '帳號狀態 (儲存 Enum 名稱，例如: ACTIVE, INACTIVE)',
    login_failure_count INT NOT NULL DEFAULT 0 COMMENT '登入失敗次數',
    last_failure_time DATETIME NULL COMMENT '最後一次登入失敗時間',
    account_locked_time DATETIME NULL COMMENT '帳號被鎖定時間',
    gender CHAR(1) NOT NULL COMMENT '員工性別 (M=男性, F=女性, O=其他)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '此員工資料的建立時間',
    last_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '此員工資料的最後更新時間',
    photo_url VARCHAR(255) NULL COMMENT '員工大頭照的路徑或URL',
    national_id VARCHAR(10) NOT NULL COMMENT '身分證字號 (不可重複)',
    PRIMARY KEY (employee_id),
    UNIQUE KEY uk_employee_account (account),
    UNIQUE KEY uk_employee_email (email),
    UNIQUE KEY uk_employee_national_id (national_id),
    CONSTRAINT fk_employee_store FOREIGN KEY (store_id) REFERENCES store(store_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_employee_created_by_ref FOREIGN KEY (created_by) REFERENCES employee(employee_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='員工資料表，後台系統的使用者 (已包含帳號鎖定機制)。';

/*
 * -----------------------------------------------------
 * 範例資料: employee (對應修正後的結構)
 * 說明: INSERT 陳述式本身無需變更，因為原先就未包含 `photo` 欄位。
 * 新欄位 `photo_url` 為 NULLable，預設為 NULL 即可，
 * 其值將由後端應用程式在上傳照片後更新。
 * -----------------------------------------------------
 */
INSERT INTO employee (store_id, username, account, created_by, password, email, phone, role, status, gender, national_id) VALUES
(3, '金泰亨V.', 'manager-gui', NULL, 'manager-gui', 'manager-gui@gmail.com', '0911-000-111', 'HEADQUARTERS_ADMIN', 'ACTIVE', 'M', 'A121976751'),
(1, '李準基', 'david.wang', 1, 'david.wang', 'david.wang@gmail.com', '0910-111-222', 'MANAGER', 'ACTIVE', 'M', 'B120267183'),
(1, '蔡秀彬', 'mary.chen', 2, 'mary.chen', 'mary.chen@gmail.com', '0920-222-333', 'STAFF', 'ACTIVE', 'F', 'A220464692'),
(2, '玄彬', 'ken.lin', 1, 'ken.lin', 'ken.lin@gmail.com', '0930-333-444', 'MANAGER', 'ACTIVE', 'M', 'J166864809'),
(1, '朴寶英', 'tina.chang', 2, 'tina.chang', 'tina.chang@gmail.com', '0940-444-555', 'STAFF', 'ACTIVE', 'F', 'J259707846'),
(2, '杉野遙亮', 'chihung.huang', 1, 'chihung.huang', 'chihung.huang@gmail.com', '0950-555-666', 'STAFF', 'ACTIVE', 'M', 'A172965971'),
(3, '新垣結衣', 'sylvia.wu', NULL, 'sylvia.wu', 'sylvia.wu@gmail.com', '0960-666-777', 'HEADQUARTERS_ADMIN', 'ACTIVE', 'F', 'A237730085'),
(1, '赤楚衛二', 'jason.liu', 2, 'jason.liu', 'jason.liu@gmail.com', '0970-777-888', 'STAFF', 'ACTIVE', 'M', 'A161978784'),
(2, '今田美櫻', 'joyce.tsai', 1, 'joyce.tsai', 'joyce.tsai@gmail.com', '0980-888-999', 'STAFF', 'ACTIVE', 'F', 'C213892240'),
(1, '高橋文哉', 'minghan.hsu', 2, 'minghan.hsu', 'minghan.hsu@gmail.com', '0911-123-456', 'STAFF', 'ACTIVE', 'M', 'E196350396'),
(1, '張婷婷', 'cindy.cheng', 1, 'cindy.cheng', 'cindy.cheng@gmail.com', '0922-234-567', 'STAFF', 'ACTIVE', 'F', 'E283693295'),
(2, '鄧佳華', 'maggie.fang', 2, 'maggie.fang', 'maggie.fang@gmail.com', '0955-567-890', 'STAFF', 'INACTIVE', 'O', 'E263360866');

-- =======================================================================================
-- ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
-- ★ 新增區塊：建立 employee_applications 資料表 (員工申請表)
-- ★ 說明：此表用於記錄新增員工的申請流程。
-- ★ 關聯：它透過外鍵關聯到 `store` 表和 `employee` 表。
-- =======================================================================================
CREATE TABLE `employee_applications` (
    -- 申請單 ID (主鍵, 自動增長)
    `application_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申請單的主鍵 ID',

    -- 申請人資訊
    `applicant_id` BIGINT NOT NULL COMMENT '提交申請的管理員或員工 ID',
    `applicant_name` VARCHAR(50) NOT NULL COMMENT '提交申請者的姓名',

    -- 預計新增的員工資料
    `employee_username` VARCHAR(50) NOT NULL COMMENT '新員工的姓名',
    `employee_account` VARCHAR(50) NOT NULL COMMENT '新員工的登入帳號 (唯一)',
    `employee_email` VARCHAR(100) NOT NULL COMMENT '新員工的電子郵件 (唯一)',
    `employee_phone` VARCHAR(20) NULL COMMENT '新員工的電話號碼',
    `employee_national_id` VARCHAR(10) NOT NULL COMMENT '新員工的身分證號碼 (唯一)',
    `employee_password` VARCHAR(255) NOT NULL COMMENT '新員工的加密後密碼',
    `employee_role` VARCHAR(50) NOT NULL COMMENT '新員工的角色 (例如: STAFF, MANAGER)',
    `employee_gender` VARCHAR(20) NULL COMMENT '新員工的性別 (例如: MALE, FEMALE)',

    -- 關聯的店家資訊
    `store_id` BIGINT NOT NULL COMMENT '員工所屬的店家 ID',
    `store_name` VARCHAR(100) NULL COMMENT '員工所屬的店家名稱',
    `photo_url` VARCHAR(255) NULL COMMENT '員工大頭照的 URL',

    -- 申請狀態
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '申請狀態 (PENDING, APPROVED, REJECTED)',

    -- 審核相關資訊
    `reviewer_id` BIGINT NULL COMMENT '審核人員的 ID',
    `reviewer_name` VARCHAR(50) NULL COMMENT '審核人員的姓名',
    `review_comment` VARCHAR(500) NULL COMMENT '審核意見或備註',
    `reviewed_at` DATETIME NULL COMMENT '審核完成的時間',

    -- 時間戳記
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '資料建立時間',
    `updated_at` DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '資料最後更新時間',

    -- 約束定義
    PRIMARY KEY (`application_id`),
    UNIQUE KEY `uk_app_employee_account` (`employee_account`),
    UNIQUE KEY `uk_app_employee_email` (`employee_email`),
    UNIQUE KEY `uk_app_employee_national_id` (`employee_national_id`),

    -- ★ 新增外鍵約束
    CONSTRAINT `fk_app_applicant` FOREIGN KEY (`applicant_id`) REFERENCES `employee` (`employee_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_app_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `employee` (`employee_id`) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_app_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`store_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='儲存新增員工的申請記錄';
-- ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★

-- =======================================================================================
-- 資料表: permission (權限資料表)
-- 功能: 儲存系統中所有可用的「原子權限」項目，作為權限管理的基礎定義。
-- =======================================================================================
CREATE TABLE permission (
    permission_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '權限編號 (主鍵，自動增長)',
    description VARCHAR(255) NOT NULL COMMENT '此權限的具體功能說明 (例如: 管理員工資料)',
    PRIMARY KEY (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='權限定義表，列出所有系統可分配的功能權限。';

/*
 * -----------------------------------------------------
 * 範例資料: permission
 * 說明: 定義系統中所有可分配的權限。ID 將從 1 開始自動增長。
 * -----------------------------------------------------
 */
-- 1. 後台管理相關權限
INSERT INTO permission (description) VALUES ('管理員工資料');       -- (ID: 1) 總部級權限，可新增、修改、查詢所有員工資料
INSERT INTO permission (description) VALUES ('管理會員資料');       -- (ID: 2) 總部級權限，可查詢、停權會員
INSERT INTO permission (description) VALUES ('查看後台訂單');       -- (ID: 3) 通用權限，總部可看所有訂單，店長及職員僅可看所屬分店訂單
INSERT INTO permission (description) VALUES ('管理商品/菜單');     -- (ID: 4) 店長級權限，可編輯所屬分店的菜單 (meal)
INSERT INTO permission (description) VALUES ('管理門市資訊');       -- (ID: 5) 總部級權限，可新增、修改門市資料

-- 2. 總部特定權限
INSERT INTO permission (description) VALUES ('發布總部最新消息');   -- (ID: 6) 總部級權限，可發布 news

-- 3. 分店特定權限
INSERT INTO permission (description) VALUES ('發布分店公告');       -- (ID: 7) 店長級權限，可發布所屬分店的 announcement
INSERT INTO permission (description) VALUES ('更新分店餐點狀態 (如：售完)');   -- (ID: 8) 店長及職員權限，可更新所屬分店的 store_meal_status
INSERT INTO permission (description) VALUES ('處理顧客意見回饋');   -- (ID: 9) 店長級權限，可查看、回覆所屬分店的 feedback

-- 4. 通用操作權限
INSERT INTO permission (description) VALUES ('登入後台系統');       -- (ID: 10) 所有後台員工的基本權限
INSERT INTO permission (description) VALUES ('修改個人密碼');       -- (ID: 11) 所有後台員工的基本權限
INSERT INTO permission (description) VALUES ('管理權限設定');       -- (ID: 12) 超級管理員權限(總部)，可管理 employee_permission 對應表


-- =======================================================================================
-- 資料表: employee_permission (員工權限對應表)
-- 功能: 建立員工與權限之間的多對多關係。此為權限系統的核心，記錄哪個員工擁有哪個權限。
-- =======================================================================================
CREATE TABLE employee_permission (
    ep_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '員工權限對應編號 (主鍵，無實質業務意義，僅為唯一識別)',
    employee_id BIGINT NOT NULL COMMENT '員工編號 (外鍵，關聯至 employee 表的 employee_id)',
    permission_id BIGINT NOT NULL COMMENT '權限編號 (外鍵，關聯至 permission 表的 permission_id)',
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '此權限被授予給員工的時間',
    PRIMARY KEY (ep_id),
    UNIQUE KEY uk_employee_permission (employee_id, permission_id) COMMENT '唯一鍵约束：確保同一員工不會被重複授予相同的權限',
    CONSTRAINT fk_ep_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_ep_permission FOREIGN KEY (permission_id) REFERENCES permission(permission_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='員工與權限的關聯表 (多對多)。';

/*
 * -----------------------------------------------------
 * 範例資料: employee_permission
 * 說明: 根據員工角色，指派對應的權限，使權限分配更加直觀。
 *
 * --- 權限分配策略 ---
 * [總部人員]: 擁有最大管理範圍，可管理員工、會員、門市資訊，並發布全站消息。
 * - 總部管理員: 擁有系統所有權限，包含權限設定。
 *
 * [分店店長]: 擁有該分店的管理權限，包含管理該店員工、菜單、公告及處理客訴。
 *
 * [分店職員]: 擁有基本操作權限，如查看訂單、更新餐點售完狀態。
 *
 * [通用權限]: 所有後台人員皆有登入、修改個人密碼的權限。
 * -----------------------------------------------------
 */
INSERT INTO employee_permission (employee_id, permission_id) VALUES
-- 總部人員 (林一郎, 吳淑芬): 擁有系統最高權限
-- 林一郎 (總部)
(1, 1),  -- 管理員工資料
(1, 2),  -- 管理會員資料
(1, 4),  -- 管理商品/菜單
(1, 5),  -- 管理門市資訊
(1, 6),  -- 發布總部最新消息
(1, 10), -- 登入後台系統
(1, 11), -- 修改個人密碼
(1, 12), -- 管理權限設定
-- 吳淑芬 (總部)
(7, 1),  -- 管理員工資料
(7, 2),  -- 管理會員資料
(7, 4),  -- 管理商品/菜單
(7, 5),  -- 管理門市資訊
(7, 6),  -- 發布總部最新消息
(7, 10), -- 登入後台系統
(7, 11), -- 修改個人密碼
(7, 12), -- 管理權限設定

-- 員工 2 (王大明 - 一號店店長): 擁有完整的店務管理權限
(2, 1),  -- 管理員工資料 (同店)
(2, 3),  -- 查看後台訂單 (同店)
(2, 7),  -- 發布分店公告
(2, 8),  -- 更新分店餐點狀態
(2, 9),  -- 處理顧客意見回饋
(2, 10), -- 登入後台系統
(2, 11), -- 修改個人密碼

-- 員工 4 (林建良 - 二號店店長): 擁有完整的店務管理權限
(4, 1),  -- 管理員工資料 (同店)
(4, 3),  -- 查看後台訂單 (同店)
(4, 7),  -- 發布分店公告
(4, 8),  -- 更新分店餐點狀態
(4, 9),  -- 處理顧客意見回饋
(4, 10), -- 登入後台系統
(4, 11), -- 修改個人密碼

-- 職員: 擁有相同的基本操作權限
-- 陳美麗 (一號店職員)
(3, 3),  -- 查看後台訂單 (同店)
(3, 8),  -- 更新分店餐點狀態
(3, 10), -- 登入後台系統
(3, 11), -- 修改個人密碼
-- 張雅婷 (一號店職員)
(5, 3),  -- 查看後台訂單 (同店)
(5, 8),  -- 更新分店餐點狀態
(5, 10), -- 登入後台系統
(5, 11), -- 修改個人密碼
-- 黃啟宏 (二號店職員)
(6, 3),  -- 查看後台訂單 (同店)
(6, 8),  -- 更新分店餐點狀態
(6, 10), -- 登入後台系統
(6, 11), -- 修改個人密碼
-- 蔡怡靜 (二號店職員)
(9, 3),  -- 查看後台訂單 (同店)
(9, 8),  -- 更新分店餐點狀態
(9, 10), -- 登入後台系統
(9, 11), -- 修改個人密碼
-- 許明翰 (一號店職員)
(10, 3), -- 查看後台訂單 (同店)
(10, 8), -- 更新分店餐點狀態
(10, 10),-- 登入後台系統
(10, 11),-- 修改個人密碼
-- 鄭心儀 (一號店職員)
(11, 3), -- 查看後台訂單 (同店)
(11, 8), -- 更新分店餐點狀態
(11, 10),-- 登入後台系統
(11, 11),-- 修改個人密碼
-- 方美琪 (二號店職員)
(12, 3), -- 查看後台訂單 (同店)
(12, 8), -- 更新分店餐點狀態
(12, 10),-- 登入後台系統
(12, 11),-- 修改個人密碼

-- 員工 8 (劉俊傑 - 已停用職員): 保留權限紀錄，但帳號狀態為停用
(8, 10), -- 登入後台系統
(8, 11); -- 修改個人密碼


-- =======================================================================================
-- 資料表: meal_type (餐點種類表)
-- 功能: 定義餐點的分類，例如：吐司、漢堡、飲品等，方便前台進行菜單分類顯示。
-- =======================================================================================
CREATE TABLE meal_type(
    meal_type_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '餐點種類編號 (主鍵，自動增長)',
    meal_name VARCHAR(50) NOT NULL COMMENT '餐點種類名稱',
    PRIMARY KEY (meal_type_id)
) COMMENT='餐點種類定義表。';

/*
 * -----------------------------------------------------
 * 範例資料: meal_type
 * 說明: 定義五大餐點類別。
 * -----------------------------------------------------
 */
INSERT INTO meal_type (meal_type_id, meal_name) VALUES
(1, '吐司'), (2, '漢堡'), (3, '蛋餅'), (4, '麵食'), (5, '飲品');

-- =======================================================================================
-- 資料表: meal (餐點資料表)
-- 功能: 儲存所有可販售餐點的詳細資訊。
-- 備註: 此表的 status 欄位可作為「總開關」，若設為0，則無論分店狀態為何，該商品都無法販售。
-- =======================================================================================
/*
===========================================================================================
【重點說明】關於 reviewTotalStars 欄位的設計與移除
-------------------------------------------------------------------------------------------
1. 本系統的餐點評價星數顯示，統一採用「即時統計」方式，
   前端若需顯示總星數、平均星數等資料，應由 API 即時查詢 order_list_info 表，
   由後端 SQL 聚合計算後回傳最新值，資料來源單一、乾淨、不易混淆。

2. reviewTotalStars 欄位已移除，不再於 meal 表中冗餘存放評價資料，
   可避免資料同步不一致與管理複雜化問題。

3. 除非未來有「高流量、批次快照」等效能需求，才建議額外設計快取欄位，
   並須明確規劃定時同步機制，且於前端顯示「資料非即時」等提示。
   目前本系統規模與查詢需求，皆以即時計算即可。

—— 設計原則：資料來源單一、避免冗餘、查詢維護最簡單化。
===========================================================================================
*/

CREATE TABLE meal(
    meal_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '餐點編號 (主鍵，自動增長)',
    meal_type_id BIGINT NOT NULL COMMENT '餐點種類編號 (外鍵，關聯至 meal_type 表)',
    meal_name VARCHAR(50) NOT NULL COMMENT '餐點的完整名稱 (例如: 起司火腿蛋吐司)',
    meal_pic VARCHAR(255) DEFAULT NULL COMMENT '餐點圖片(路徑或URL)',
    meal_price BIGINT NOT NULL COMMENT '餐點單價',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '狀態 (1:上架, 0:下架)',
    PRIMARY KEY (meal_id),
    FOREIGN KEY (meal_type_id) REFERENCES meal_type(meal_type_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='所有餐點的詳細資料表。';

ALTER TABLE meal AUTO_INCREMENT = 101;

/*
 * -----------------------------------------------------
 * 範例資料: meal - [已根據建議修正]
 * 說明: status 的值已由字串 '1'/'0' 改為數字 1/0。
 * -----------------------------------------------------
 */
-- 吐司類（meal_type_id = 1）
INSERT INTO meal (meal_type_id, meal_name, meal_pic, meal_price, status) VALUES
(1, '花生厚片吐司', NULL, 35, 1),
(1, '奶酥吐司', NULL, 35, 1),
(1, '草莓吐司', NULL, 30, 1),
(1, '起司火腿吐司', NULL, 40, 1);

-- 漢堡類（meal_type_id = 2）
INSERT INTO meal (meal_type_id, meal_name, meal_pic, meal_price, status) VALUES
(2, '豬排漢堡', NULL, 60, 1),
(2, '牛肉漢堡', NULL, 70, 1),
(2, '雞腿漢堡', NULL, 65, 1),
(2, '培根蛋堡', NULL, 70, 1);

-- 蛋餅類（meal_type_id = 3）
INSERT INTO meal (meal_type_id, meal_name, meal_pic, meal_price, status) VALUES
(3, '玉米蛋餅', NULL, 35, 1),
(3, '起司蛋餅', NULL, 40, 1),
(3, '泡菜蛋餅', NULL, 50, 1),
(3, '鮪魚蛋餅', NULL, 50, 1);

-- 麵食（meal_type_id = 4）
INSERT INTO meal (meal_type_id, meal_name, meal_pic, meal_price, status) VALUES
(4, '黑胡椒鐵板麵', NULL, 55, 1),
(4, '肉醬義大利麵', NULL, 65, 1),
(4, '青醬義大利麵', NULL, 60, 1),
(4, '培根義大利麵', NULL, 60, 0); -- 下架

-- 飲品類（meal_type_id = 5）
INSERT INTO meal (meal_type_id, meal_name, meal_pic, meal_price, status) VALUES
(5, '紅茶', NULL, 25, 1),
(5, '美式咖啡', NULL, 35, 1),
(5, '鮮奶茶', NULL, 40, 1),
(5, '拿鐵', NULL, 45, 1);


-- =======================================================================================
-- 資料表: store_meal_status (門市餐點狀態表) - [已修改為代理主鍵版本]
-- 功能: 建立特定門市與特定餐點的供應狀態，實現分店層級的即時庫存管理 (例如：今日售完)。
-- 備註: 此表的紀錄會覆蓋 meal 表的總狀態。若此表無紀錄，則依 meal 表的 status 為準。
--
-- [修改說明]:
-- 1. 新增 `sms_id` 欄位作為此表的代理主鍵 (Surrogate Key)，並設定為自動增長。
-- 2. 將原本的複合主鍵 (store_id, meal_id) 改為唯一鍵約束 (UNIQUE KEY)，
--    以確保同一間門市的同一個餐點不會有重複的狀態紀錄，維持業務邏輯的正確性。
-- =======================================================================================
CREATE TABLE store_meal_status (
    sms_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '門市餐點狀態編號 (主鍵，自動增長)',
    store_id BIGINT NOT NULL COMMENT '門市編號 (外鍵)',
    meal_id BIGINT NOT NULL COMMENT '餐點編號 (外鍵)',
    status TINYINT(1) NOT NULL COMMENT '該門市的此餐點狀態 (1=供應中, 0=已售完)',
    last_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '狀態最後更新時間',
    PRIMARY KEY (sms_id),
    -- 唯一鍵約束，確保資料的唯一性
    UNIQUE KEY uk_store_meal (store_id, meal_id),
    -- 外鍵約束維持不變
    CONSTRAINT fk_sms_store FOREIGN KEY (store_id) REFERENCES store(store_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_sms_meal FOREIGN KEY (meal_id) REFERENCES meal(meal_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='門市對應餐點的即時狀態表 (多對多，使用代理主鍵)。';

/*
 * -----------------------------------------------------
 * 範例資料: store_meal_status
 * 說明: INSERT 陳述式與原版相同，因為我們不需要手動插入主鍵 `sms_id`。
 * -----------------------------------------------------
 */
INSERT INTO store_meal_status (store_id, meal_id, status) VALUES
(1, 101, 0), -- 一號店的起司火腿吐司售完
(2, 103, 1), -- 二號店的玉米蛋餅正常供應
(2, 104, 0), -- 二號店的黑胡椒鐵板麵售完
(1, 106, 0), -- 一號店的花生厚片吐司售完
(1, 102, 1), -- 一號店的豬排漢堡正常供應
(1, 108, 1), -- 一號店的起司蛋餅正常供應
(2, 102, 1), -- 二號店的豬排漢堡正常供應
(1, 103, 0); -- 一號店的玉米蛋餅售完

-- =======================================================================================
-- 資料表: cart (購物車紀錄表)
-- 功能: 暫存會員選擇的餐點，直到結帳為止。結帳後此處的資料應被清除。
-- =======================================================================================
CREATE TABLE cart (
    cart_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '購物車項目編號 (主鍵)',
    member_id BIGINT NOT NULL COMMENT '所屬會員編號 (外鍵)',
    meal_id BIGINT NOT NULL COMMENT '選擇的餐點編號 (外鍵)',
    store_id BIGINT NOT NULL COMMENT '選擇的門市編號 (外鍵)',
    quantity BIGINT NOT NULL COMMENT '餐點數量',
    meal_customization VARCHAR(255) COMMENT '餐點客製化備註',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入購物車的時間',
    PRIMARY KEY (cart_id),
    FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (meal_id) REFERENCES meal(meal_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (store_id) REFERENCES store(store_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='購物車暫存資料表。';

/*
 * -----------------------------------------------------
 * 範例資料: cart
 * 說明: 模擬多位會員將不同商品加入購物車的情境。
 * -----------------------------------------------------
 */
INSERT INTO cart (member_id, meal_id, store_id, quantity, meal_customization) VALUES
(5, 105, 1, 5, '去冰'),      -- 會員 5 (吳雅婷) 在一號店將 5 份 鮮奶茶 加入購物車，備註：去冰
(8, 101, 1, 2, '加蛋'),          -- 會員 8 (許文雄) 在一號店將 2 份 起司火腿吐司 加入購物車，備註：加蛋
(9, 104, 2, 1, NULL),           -- 會員 9 (鄭曉涵) 在二號店將 1 份 黑胡椒鐵板麵 加入購物車
(10, 101, 2, 2, NULL),          -- 會員 10 (王志偉) 在二號店將 2 份 起司火腿吐司 加入購物車
(5, 105, 1, 3, '去冰'),          -- 會員 5 (吳雅婷) 在一號店將 3 份 鮮奶茶 加入購物車，備註：去冰
(1, 108, 2, 1, '加火腿'),        -- 會員 1 (陳怡君) 在二號店將 1 份 起司蛋餅 加入購物車，備註：加火腿
(4, 110, 1, 2, '少冰'),          -- 會員 4 (張家豪) 在一號店將 2 份 冰美式咖啡 加入購物車，備註：少冰
(8, 106, 2, 5, '多醬'),          -- 會員 8 (許文雄) 在二號店將 5 份 花生厚片吐司 加入購物車，備註：多醬
(10, 102, 2, 1, '加起司'),       -- 會員 10 (王志偉) 在二號店將 1 份 豬排漢堡 加入購物車，備註：加起司
(1, 103, 1, 2, NULL),           -- 會員 1 (陳怡君) 在一號店將 2 份 玉米蛋餅 加入購物車
(17, 102, 1, 1, '不要洋蔥'),     -- 會員 17 (趙偉倫) 在一號店將 1 份 豬排漢堡 加入購物車，備註：不要洋蔥
(20, 107, 2, 2, '加辣');         -- 會員 20 (李美芳) 在二號店將 2 份 雞腿漢堡 加入購物車，備註：加辣

-- =======================================================================================
-- 資料表: order_list (訂單主表)
-- 功能: 儲存所有會員訂單的摘要資訊，每一筆記錄代表一整張訂單。
-- =======================================================================================
CREATE TABLE order_list (
    order_list_id VARCHAR(20) NOT NULL COMMENT '訂單編號 (主鍵，格式:YYYYMMDDXXXX)',
    member_id BIGINT NOT NULL COMMENT '下訂單的會員編號 (外鍵，關聯至 member 表)',
    store_id BIGINT NOT NULL COMMENT '接收此訂單的門市編號 (外鍵，關聯至 store 表)',
    order_amount BIGINT NOT NULL COMMENT '此訂單的總金額',
    order_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '訂單成立時間 (自動生成)',
    order_status BIGINT NOT NULL COMMENT '訂單狀態 (0=處理中, 1=已確認, 2=待取餐, 3=已完成, 4=已取消)',
    pickup_time DATETIME NOT NULL COMMENT '取餐時間',
    meal_pickup_number BIGINT NOT NULL COMMENT '取餐編號 (用於現場叫號)',
    card_number VARCHAR(20) NOT NULL COMMENT '信用卡卡號 (【警告】正式環境不應儲存完整卡號，此處僅為範例。應串接金流並儲存交易ID)',
	meal_customization VARCHAR(255) COMMENT '餐點客製化備註 (例如:鮮奶茶:微糖少冰, 牛肉漢堡:不要生菜)',
    PRIMARY KEY (order_list_id),
    FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (store_id) REFERENCES store(store_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='訂單主資料表。';

/*
 * -----------------------------------------------------
 * 範例資料: order_list (修正後)
 * 說明: order_amount 的值已根據 order_list_info 中的項目重新計算，確保資料一致性。
 * -----------------------------------------------------
 */
INSERT INTO order_list (order_list_id, member_id, store_id, order_amount, order_date, order_status, pickup_time, meal_pickup_number, card_number, meal_customization) VALUES
('202506010001', 18, 2, 115, '2025-06-01 07:12:00', 2, '2025-06-01 07:30:00', 567, '8814723690154321', NULL),
('202506010002', 4, 1, 105, '2025-06-01 11:49:00', 3, '2025-06-01 12:15:00', 654, '5210987654321098', '拿鐵:少冰'),
('202506020001', 29, 1, 95, '2025-06-02 08:02:00', 1, '2025-06-02 08:30:00', 818, '9988776655443322', NULL),
('202506020002', 12, 2, 65, '2025-06-02 12:33:00', 0, '2025-06-02 13:00:00', 434, '1122334455667788', '雞腿漢堡:不要加醬'),
('202506030001', 7, 1, 135, '2025-06-03 09:18:00', 3, '2025-06-03 09:45:00', 444, '6543210987654321', '起司火腿吐司:不要加醬'),
('202506030002', 25, 2, 75, '2025-06-03 13:04:00', 2, '2025-06-03 13:30:00', 555, '1234567890123456', '起司蛋餅:醬油膏多'),
('202506040001', 1, 1, 65, '2025-06-04 06:55:00', 1, '2025-06-04 07:15:00', 678, '1111222233334444', NULL),
('202506040002', 22, 1, 140, '2025-06-04 10:21:00', 3, '2025-06-04 10:45:00', 222, '7778889990001112', '牛肉漢堡:不要生菜'),
('202506050001', 16, 2, 100, '2025-06-05 08:44:00', 1, '2025-06-05 09:00:00', 999, '2233445566778899', NULL),
('202506050002', 19, 1, 105, '2025-06-05 13:38:00', 2, '2025-06-05 14:00:00', 678, '4455667788990011', '鮮奶茶:溫的'),
('202506060001', 10, 2, 120, '2025-06-06 09:03:00', 0, '2025-06-06 09:30:00', 777, '8901234567890123', '鮪魚蛋餅:不加胡椒'),
('202506060002', 30, 2, 130, '2025-06-06 12:11:00', 3, '2025-06-06 12:30:00', 828, '3456789012345678', NULL),
('202506070001', 5, 1, 125, '2025-06-07 07:48:00', 2, '2025-06-07 08:15:00', 222, '9012345678901234', '青醬義大利麵:醬多'),
('202506070002', 11, 1, 85, '2025-06-07 11:26:00', 1, '2025-06-07 11:45:00', 212, '5678901234567890', NULL),
('202506080001', 26, 2, 60, '2025-06-08 10:09:00', 3, '2025-06-08 10:30:00', 666, '2345678901234567', NULL),
('202506080002', 2, 1, 95, '2025-06-08 13:51:00', 2, '2025-06-08 14:15:00', 432, '6789012345678901', '豬排漢堡:加蛋'),
('202506090001', 23, 1, 105, '2025-06-09 08:23:00', 0, '2025-06-09 08:45:00', 333, '1213141516171819', NULL),
('202506090002', 8, 2, 95, '2025-06-09 12:57:00', 3, '2025-06-09 13:15:00', 555, '2021222324252627', '玉米蛋餅:加番茄醬'),
('202506100001', 14, 2, 135, '2025-06-10 09:33:00', 2, '2025-06-10 10:00:00', 878, '2829303132333435', NULL),
('202506100002', 28, 1, 70, '2025-06-10 13:16:00', 1, '2025-06-10 13:45:00', 888, '3637383940414243', NULL),
('202506110001', 3, 1, 125, '2025-06-11 07:37:00', 3, '2025-06-11 08:00:00', 789, '4445464748495051', NULL),
('202506110002', 20, 2, 85, '2025-06-11 11:08:00', 2, '2025-06-11 11:30:00', 789, '5253545556575859', '泡菜蛋餅:加辣,奶酥吐司:烤酥一點'),
('202506120001', 27, 2, 130, '2025-06-12 08:58:00', 2, '2025-06-12 09:15:00', 777, '6061626364656667', NULL),
('202506120002', 6, 1, 75, '2025-06-12 12:46:00', 3, '2025-06-12 13:00:00', 333, '6869707172737475', '花生厚片吐司:醬多'),
('202506130001', 17, 1, 140, '2025-06-13 09:42:00', 1, '2025-06-13 10:00:00', 456, '7677787980818283', '牛肉漢堡:不要生菜'),
('202506130002', 13, 2, 100, '2025-06-13 13:28:00', 2, '2025-06-13 13:45:00', 656, '8485868788899091', NULL),
('202506140001', 24, 1, 80, '2025-06-14 07:06:00', 0, '2025-06-14 07:30:00', 444, '9293949596979899', NULL),
('202506140002', 9, 2, 115, '2025-06-14 10:34:00', 3, '2025-06-14 11:00:00', 666, '1011121314151617', NULL),
('202506150001', 15, 2, 95, '2025-06-15 08:14:00', 2, '2025-06-15 08:30:00', 190, '1819202122232425', NULL),
('202506150002', 21, 1, 125, '2025-06-15 11:41:00', 1, '2025-06-15 12:00:00', 111, '2627282930313233', NULL),
('202506160001', 4, 2, 60, '2025-06-16 09:27:00', 3, '2025-06-16 09:45:00', 654, '5210987654321098', '紅茶:半糖'),
('202506160002', 18, 1, 100, '2025-06-16 13:09:00', 2, '2025-06-16 13:30:00', 567, '8814723690154321', NULL),
('202506170001', 29, 2, 105, '2025-06-17 07:51:00', 0, '2025-06-17 08:15:00', 818, '9988776655443322', '拿鐵:熱的'),
('202506170002', 12, 1, 135, '2025-06-17 12:13:00', 3, '2025-06-17 12:30:00', 434, '1122334455667788', NULL),
('202506180001', 7, 2, 100, '2025-06-18 08:36:00', 2, '2025-06-18 09:00:00', 444, '6543210987654321', '鮮奶茶:微糖,豬排漢堡:不要番茄'),
('202506180002', 25, 1, 95, '2025-06-18 13:43:00', 1, '2025-06-18 14:00:00', 555, '1234567890123456', NULL),
('202506190001', 1, 2, 130, '2025-06-19 09:56:00', 3, '2025-06-19 10:15:00', 678, '1111222233334444', '培根蛋堡:蛋要全熟'),
('202506190002', 22, 2, 120, '2025-06-19 12:24:00', 2, '2025-06-19 12:45:00', 222, '7778889990001112', NULL),
('202506200001', 16, 1, 70, '2025-06-20 07:29:00', 0, '2025-06-20 07:45:00', 999, '2233445566778899', NULL),
('202506200002', 19, 2, 75, '2025-06-20 11:58:00', 3, '2025-06-20 12:15:00', 678, '4455667788990011', NULL),
('202506210001', 10, 1, 105, '2025-06-21 08:08:00', 2, '2025-06-21 08:30:00', 777, '8901234567890123', NULL),
('202506210002', 30, 1, 135, '2025-06-21 13:14:00', 1, '2025-06-21 13:30:00', 828, '3456789012345678', NULL),
('202506220001', 5, 2, 60, '2025-06-22 09:11:00', 3, '2025-06-22 09:30:00', 222, '9012345678901234', '美式咖啡:少冰'),
('202506220002', 11, 2, 115, '2025-06-22 12:48:00', 2, '2025-06-22 13:15:00', 212, '5678901234567890', NULL),
('202506230001', 26, 1, 100, '2025-06-23 07:56:00', 3, '2025-06-23 08:15:00', 666, '2345678901234567', NULL),
('202506230002', 2, 2, 120, '2025-06-23 11:17:00', 3, '2025-06-23 11:45:00', 432, '6789012345678901', NULL),
('202506240001', 23, 2, 105, '2025-06-24 08:25:00', 1, '2025-06-24 08:45:00', 333, '1213141516171819', NULL),
('202506240002', 8, 1, 95, '2025-06-24 13:55:00', 3, '2025-06-24 14:15:00', 555, '2021222324252627', '玉米蛋餅:加辣'),
('202506250001', 14, 1, 135, '2025-06-25 09:05:00', 2, '2025-06-25 09:30:00', 878, '2829303132333435', NULL),
('202506250002', 28, 2, 50, '2025-06-25 12:01:00', 0, '2025-06-25 12:30:00', 888, '3637383940414243', '紅茶:少冰'),
('202506260001', 3, 2, 145, '2025-06-26 07:46:00', 3, '2025-06-26 08:15:00', 789, '4445464748495051', NULL),
('202506260002', 20, 1, 85, '2025-06-26 11:32:00', 2, '2025-06-26 12:00:00', 789, '5253545556575859', NULL),
('202506270001', 27, 1, 130, '2025-06-27 08:53:00', 1, '2025-06-27 09:15:00', 777, '6061626364656667', '培根蛋堡:不加番茄醬'),
('202506270002', 6, 2, 75, '2025-06-27 13:22:00', 3, '2025-06-27 13:45:00', 333, '6869707172737475', NULL),
('202506280001', 17, 2, 140, '2025-06-28 09:16:00', 2, '2025-06-28 09:45:00', 456, '7677787980818283', NULL),
('202506280002', 13, 1, 100, '2025-06-28 12:54:00', 0, '2025-06-28 13:15:00', 656, '8485868788899091', '泡菜蛋餅:不要醬油膏'),
('202506290001', 24, 2, 80, '2025-06-29 07:40:00', 3, '2025-06-29 08:00:00', 444, '9293949596979899', NULL),
('202506290002', 9, 1, 115, '2025-06-29 11:06:00', 2, '2025-06-29 11:30:00', 666, '1011121314151617', '青醬義大利麵:加辣'),
('202506300001', 15, 1, 95, '2025-06-30 08:19:00', 1, '2025-06-30 08:45:00', 190, '1819202122232425', NULL),
('202506300002', 21, 2, 125, '2025-06-30 13:48:00', 3, '2025-06-30 14:00:00', 111, '2627282930313233', NULL);


-- =======================================================================================
-- 資料表: order_list_info (訂單明細資料表)
-- 功能: 儲存每一筆訂單中包含的各個餐點詳細資訊，例如數量、單價、客製化選項等。
-- =======================================================================================
CREATE TABLE order_list_info (
    order_list_info_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '訂單明細編號 (主鍵，自動增長)',
    order_list_id VARCHAR(20) NOT NULL COMMENT '此明細所屬的訂單編號 (外鍵，關聯至 order_list 表)',
    meal_id BIGINT NOT NULL COMMENT '訂購的餐點編號 (外鍵，關聯至 meal 表)',
    meal_price BIGINT NOT NULL COMMENT '下訂當下的餐點單價 (價格快照，避免因餐點價格變動影響歷史訂單金額)',
    review_stars BIGINT NOT NULL COMMENT '此餐點在此次消費的評論星數 (預設可為0，待使用者評論後更新)',
    quantity BIGINT NOT NULL COMMENT '訂購数量',
    PRIMARY KEY (order_list_info_id),
    FOREIGN KEY (order_list_id) REFERENCES order_list(order_list_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (meal_id) REFERENCES meal(meal_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='訂單明細資料表，與訂單主表為一對多關係。';

/*
 * -----------------------------------------------------
 * 範例資料: order_list_info
 * 說明: 對應 `order_list` 中的每一筆訂單，提供詳細的購買品項。meal_price 已修正為下訂時的「單價」。
 * -----------------------------------------------------
 */
INSERT INTO order_list_info (order_list_id, meal_id, meal_price, review_stars, quantity) VALUES
('202506010001', 113, 55, 4, 1), ('202506010001', 105, 60, 5, 1),
('202506010002', 120, 45, 5, 1), ('202506010002', 105, 60, 5, 1),
('202506020001', 104, 40, 4, 1), ('202506020001', 113, 55, 4, 1),
('202506020002', 107, 65, 5, 1),
('202506030001', 104, 40, 4, 1), ('202506030001', 106, 70, 4, 1), ('202506030001', 117, 25, 5, 1),
('202506030002', 110, 40, 5, 1), ('202506030002', 118, 35, 4, 1),
('202506040001', 101, 35, 5, 1), ('202506040001', 103, 30, 5, 1),
('202506040002', 106, 70, 5, 2),
('202506050001', 111, 50, 4, 2),
('202506050002', 119, 40, 5, 1), ('202506050002', 107, 65, 4, 1),
('202506060001', 112, 50, 5, 1), ('202506060001', 106, 70, 4, 1),
('202506060002', 108, 70, 5, 1), ('202506060002', 105, 60, 5, 1),
('202506070001', 115, 60, 4, 1), ('202506070001', 107, 65, 3, 1),
('202506070002', 102, 35, 5, 1), ('202506070002', 111, 50, 4, 1),
('202506080001', 103, 30, 4, 2),
('202506080002', 105, 60, 5, 1), ('202506080002', 102, 35, 3, 1),
('202506090001', 107, 65, 4, 1), ('202506090001', 110, 40, 5, 1),
('202506090002', 109, 35, 5, 1), ('202506090002', 105, 60, 4, 1),
('202506100001', 108, 70, 5, 1), ('202506100001', 107, 65, 4, 1),
('202506100002', 118, 35, 4, 2),
('202506110001', 114, 65, 4, 1), ('202506110001', 105, 60, 4, 1),
('202506110002', 111, 50, 5, 1), ('202506110002', 102, 35, 5, 1),
('202506120001', 106, 70, 5, 1), ('202506120001', 105, 60, 4, 1),
('202506120002', 101, 35, 5, 1), ('202506120002', 110, 40, 4, 1),
('202506130001', 106, 70, 5, 2),
('202506130002', 112, 50, 4, 2),
('202506140001', 110, 40, 3, 2),
('202506140002', 114, 65, 5, 1), ('202506140002', 111, 50, 4, 1),
('202506150001', 104, 40, 5, 1), ('202506150001', 113, 55, 5, 1),
('202506150002', 107, 65, 5, 1), ('202506150002', 105, 60, 4, 1),
('202506160001', 117, 25, 5, 1), ('202506160001', 102, 35, 4, 1),
('202506160002', 111, 50, 5, 2),
('202506170001', 120, 45, 4, 1), ('202506170001', 105, 60, 5, 1),
('202506170002', 104, 40, 4, 1), ('202506170002', 106, 70, 4, 1), ('202506170002', 117, 25, 5, 1),
('202506180001', 119, 40, 5, 1), ('202506180001', 105, 60, 4, 1),
('202506180002', 104, 40, 5, 1), ('202506180002', 113, 55, 4, 1),
('202506190001', 108, 70, 5, 1), ('202506190001', 105, 60, 5, 1),
('202506190002', 112, 50, 4, 1), ('202506190002', 106, 70, 3, 1),
('202506200001', 102, 35, 4, 2),
('202506200002', 110, 40, 5, 1), ('202506200002', 118, 35, 4, 1),
('202506210001', 113, 55, 3, 1), ('202506210001', 111, 50, 5, 1),
('202506210002', 108, 70, 5, 1), ('202506210002', 107, 65, 4, 1),
('202506220001', 118, 35, 5, 1), ('202506220001', 117, 25, 4, 1),
('202506220002', 114, 65, 5, 1), ('202506220002', 111, 50, 4, 1),
('202506230001', 111, 50, 3, 2),
('202506230002', 112, 50, 5, 1), ('202506230002', 106, 70, 4, 1),
('202506240001', 107, 65, 5, 1), ('202506240001', 110, 40, 4, 1),
('202506240002', 109, 35, 5, 1), ('202506240002', 105, 60, 5, 1),
('202506250001', 108, 70, 5, 1), ('202506250001', 107, 65, 4, 1),
('202506250002', 117, 25, 3, 2),
('202506260001', 104, 40, 4, 1), ('202506260001', 106, 70, 5, 1), ('202506260001', 118, 35, 4, 1),
('202506260002', 111, 50, 5, 1), ('202506260002', 102, 35, 3, 1),
('202506270001', 108, 70, 5, 1), ('202506270001', 105, 60, 4, 1),
('202506270002', 101, 35, 3, 1), ('202506270002', 110, 40, 5, 1),
('202506280001', 106, 70, 5, 2),
('202506280002', 111, 50, 4, 2),
('202506290001', 110, 40, 5, 2),
('202506290002', 115, 60, 5, 1), ('202506290002', 113, 55, 4, 1),
('202506300001', 104, 40, 5, 1), ('202506300001', 113, 55, 5, 1),
('202506300002', 107, 65, 5, 1), ('202506300002', 105, 60, 4, 1);


-- =======================================================================================
-- 資料表: fav (我的最愛表)
-- 功能: 儲存會員收藏的餐點，建立會員與餐點之間的多對多收藏關係。
-- =======================================================================================
CREATE TABLE fav(
    fav_meal_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '我的最愛編號 (主鍵，自動增長)',
    member_id BIGINT NOT NULL COMMENT '會員編號 (外鍵，關聯至 member 表)',
    meal_id BIGINT NOT NULL COMMENT '被收藏的餐點編號 (外鍵，關聯至 meal 表)',
    FOREIGN KEY (member_id) REFERENCES member (member_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (meal_id) REFERENCES meal (meal_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='會員收藏餐點的關聯表。';

/*
 * -----------------------------------------------------
 * 範例資料: fav
 * 說明: 模擬不同會員將特定餐點加入我的最愛。
 * -----------------------------------------------------
 */
INSERT INTO fav (member_id, meal_id) VALUES
(1, 102), -- 會員 1 (陳怡君) 收藏了 豬排漢堡
(1, 103), -- 會員 1 (陳怡君) 收藏了 玉米蛋餅
(2, 101), -- 會員 2 (林志明) 收藏了 起司火腿吐司
(3, 106), -- 會員 3 (黃美玲) 收藏了 花生厚片吐司
(4, 105), -- 會員 4 (張家豪) 收藏了 鮮奶茶
(5, 108), -- 會員 5 (吳雅婷) 收藏了 起司蛋餅
(6, 107), -- 會員 6 (劉建宏) 收藏了 雞腿漢堡
(7, 104), -- 會員 7 (蔡淑芬) 收藏了 黑胡椒鐵板麵
(8, 109), -- 會員 8 (許文雄) 收藏了 培根鐵板麵 (已下架)
(9, 110), -- 會員 9 (鄭曉涵) 收藏了 冰美式咖啡
(17, 101),-- 會員 17 (趙偉倫) 收藏了 起司火腿吐司
(18, 105),-- 會員 18 (孫小美) 收藏了 鮮奶茶
(19, 104),-- 會員 19 (錢大宇) 收藏了 黑胡椒鐵板麵
(20, 107);-- 會員 20 (李美芳) 收藏了 雞腿漢堡


-- 更新後的「顧客意見回饋表」
-- 功能: 儲存顧客意見，並增加了處理狀態與回覆內容的欄位。
-- 版本: 已移除 Foreign Key (外鍵) 限制，以簡化開發。
-- =======================================================================================
CREATE TABLE feedback (
  feedback_id BIGINT NOT NULL AUTO_INCREMENT,
  member_id BIGINT NOT NULL COMMENT '會員編號',
  store_id BIGINT NULL COMMENT '門市編號 (允許為空)',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建檔時間',
  phone VARCHAR(20) NOT NULL COMMENT '連絡電話',
  content VARCHAR(5000) NOT NULL COMMENT '內文',
  dining_time VARCHAR(255) NULL COMMENT '顧客填寫的用餐時間',
  dining_store VARCHAR(255) NULL COMMENT '顧客填寫的用餐門市',



  status VARCHAR(20) NOT NULL DEFAULT '待處理' COMMENT '狀態 (例如: 待處理, 已處理)',
  reply_content TEXT NULL COMMENT '管理員回覆的內容',
  replied_at TIMESTAMP NULL COMMENT '回覆送出的時間',
  
  PRIMARY KEY (feedback_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='顧客意見回饋紀錄表。';
/*
 * -----------------------------------------------------
 * 範例資料: feedback
 * 說明: 模擬 20 位會員提出的各式意見回饋。
 * -----------------------------------------------------
 */
INSERT INTO feedback (member_id, store_id, phone, content) VALUES
(1, 1, '0912-345-678', '餐點很美味，服務也很好。'),
(2, 2, '0928-765-432', '出餐速度稍慢，希望能改善。'),
(3, 1, '0933-123-789', '飲料太甜了，下次希望能調整。'),
(4, 2, '0955-987-654', '門市環境整潔舒適，讚！'),
(5, 1, '0966-555-222', '服務人員態度不佳。'),
(6, 2, '0910-111-333', '希望增加更多素食選項。'),
(7, 1, '0978-222-444', '第一次來吃，體驗很不錯！'),
(8, 2, '0988-333-555', '訂單內容有誤，少了一項餐點。'),
(9, 1, '0919-444-666', '希望可以有更多優惠活動。'),
(10, 2, '0921-888-777', '結帳時等太久，希望改善流程。'),
(11, 1, '0930-121-212', '飲料好喝!!!'),
(12, 2, '0952-343-434', '吐司超好吃，下次還會再來！'),
(13, 1, '0970-565-656', '希望可以開中午時段。'),
(14, 2, '0911-787-878', '價格合理，CP 值高。'),
(15, 1, '0939-909-090', '服務人員態度非常親切！'),
(16, 2, '0963-111-999', '漢堡好吃下次還點!'),
(17, 1, '0925-123-456', '網站的介面設計很漂亮，點餐很方便。'),
(18, 2, '0935-234-567', '雞腿漢堡的肉很多汁，非常好吃。'),
(19, 1, '0916-345-678', '希望可以支援更多的付款方式。'),
(20, 2, '0977-456-789', '二號店的店員服務很熱情，值得推薦。');

-- =======================================================================================
-- 資料表: news (最新消息表) - 【已修正，加入 update_time】
-- 功能: 儲存由後台員工發布的最新消息或公告。
-- =======================================================================================
CREATE TABLE news (
    news_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '最新消息編號 (主鍵)',
    employee_id BIGINT NOT NULL COMMENT '發布此消息的員工編號 (外鍵)',
    title VARCHAR(50) NOT NULL COMMENT '消息標題',
    content VARCHAR(5000) NOT NULL COMMENT '消息詳細內容',
    image_url VARCHAR(255) NULL COMMENT '最新消息的圖片路徑',
    start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息開始顯示時間',
    end_time TIMESTAMP NULL COMMENT '消息結束顯示時間 (允許NULL代表永久顯示)',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '資料最後更新時間',
    
    status TINYINT NOT NULL COMMENT '消息狀態 (1=已發布, 0=草稿)',
    PRIMARY KEY (news_id),
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='最新消息與公告表。';

/*
 * -----------------------------------------------------
 * 範例資料: news
 * 說明: 新增 3 筆最新消息，包含已發布、草稿、永久顯示等不同狀態。
 * -----------------------------------------------------
 */
INSERT INTO news (employee_id, title, content, start_time, end_time, status) VALUES
(2, '會員專屬優惠開跑', '即日起加入會員即可享有消費滿 $300 折 $50 的專屬優惠，只到 2025-08-30，別錯過！', '2025-06-01 10:00:00', '2025-08-30 23:59:59', 1),
(7, '中壢復興店內部整修公告', '為提供更舒適的用餐環境，二號店將於 2025-07-01 至 2025-08-15 進行內部整修，暫停營業，造成不便敬請見諒。', '2025-08-15 09:00:00', NULL, 1),
(1, '新品上市：草莓戀人吐司 (草稿)', '夢幻新品「草莓戀人吐司」即將上市，敬請期待！', '2025-06-20 00:00:00', '2025-09-20 00:00:00', 0);

-- =======================================================================================
-- 資料表: announcement (門市公告表)
-- 功能: 儲存由「各分店」獨立發布的專屬公告。
-- =======================================================================================
CREATE TABLE announcement (
    announcement_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '門市公告編號 (主鍵)',
    employee_id BIGINT NOT NULL COMMENT '發布公告的員工編號 (外鍵)',
    store_id BIGINT NOT NULL COMMENT '此公告所屬的門市編號 (外鍵)',
    title VARCHAR(50) NOT NULL COMMENT '公告標題',
    content VARCHAR(5000) NOT NULL COMMENT '公告詳細內容',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建檔時間',
    start_time TIMESTAMP NOT NULL COMMENT '公告上架時間',
    end_time TIMESTAMP NOT NULL COMMENT '公告下架時間',
    status TINYINT NOT NULL COMMENT '狀態 (1=顯示, 0=隱藏)',
    PRIMARY KEY (announcement_id),
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (store_id) REFERENCES store(store_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='各分店專屬的公告表。';

/*
 * -----------------------------------------------------
 * 範例資料: announcement
 * 說明: 新增 2 筆由不同分店店長發布的公告。
 * -----------------------------------------------------
 */
INSERT INTO announcement (employee_id, store_id, title, content, start_time, end_time, status) VALUES
(2, 1, '中山南京店公休時間調整', '親愛的顧客您好，本店因應端午節連假，6/22(六)公休一日，祝您佳節愉快！', '2025-06-10 00:00:00', '2025-06-22 23:59:59', 1),
(4, 2, '中壢復興號店限定！鐵板麵套餐優惠', '即日起至月底，凡點購任一鐵板麵，即可以 $10 加購一杯中杯紅茶！', '2025-06-01 08:00:00', '2025-06-30 23:59:59', 1);


-- =======================================================================================
-- 資料表: homepage_Info (首頁資訊表)
-- 功能: 儲存網站首頁需要顯示的內容，例如 Banner 圖片、介紹文字等。
-- =======================================================================================
CREATE TABLE homepage_Info(
    homepage_Info_id BIGINT NOT NULL AUTO_INCREMENT,
    homepage_banner LONGBLOB NULL COMMENT '首頁主視覺 Banner 圖片',
    content VARCHAR(5000) NOT NULL COMMENT '品牌或網站介紹文字',
    picture LONGBLOB NULL COMMENT '介紹區塊搭配的圖片',
    PRIMARY KEY (homepage_Info_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='首頁內容管理表。';

/*
 * -----------------------------------------------------
 * 範例資料: homepage_Info
 * 說明: 新增一筆網站的預設介紹文字。
 * -----------------------------------------------------
 */
INSERT INTO homepage_Info (homepage_banner, content, picture) VALUES
(NULL, '歡迎來到 EatFast！我們致力於提供最新鮮、最美味的早餐，讓您用滿滿的活力開啟每一天。', NULL);

-- =======================================================================================
-- 觸發器: generate_order_no
-- 功能: 在新增訂單到 `order_list` 表之前，自動生成格式為 'YYYYMMDD' + 當日流水號 的訂單編號。
-- =======================================================================================
-- 更改結束符，因為觸發器內部會使用分號(;)
DELIMITER $$
-- 建立觸發器
CREATE TRIGGER generate_order_no 
-- 設定觸發時機為"在新增資料之前"
BEFORE INSERT ON `order_list`
-- 設定每一筆新增的資料都會觸發
FOR EACH ROW
BEGIN
    -- 宣告一個整數變數，用來儲存下一個流水號
    DECLARE next_seq INT;
    -- 宣告一個字串變數，用來儲存當前的年月日(YYYYMMDD)
    DECLARE current_day VARCHAR(8);
    
    -- 將當前日期格式化為 'YYYYMMDD' 並存入變數
    SET current_day = DATE_FORMAT(NOW(), '%Y%m%d');
    
    -- 查詢當天已存在的最大流水號，並加 1
    SELECT 
        -- COALESCE函數：如果找不到當天的訂單(結果為NULL)，則回傳0，避免計算錯誤
        -- MAX(...): 找出當天訂單中最大的流水號
        -- CAST(... AS UNSIGNED): 將文字格式的流水號轉為數字才能比大小
        -- RIGHT(order_list_id, 4): 從訂單編號右邊取出4位數的流水號
        COALESCE(MAX(CAST(RIGHT(order_list_id, 4) AS UNSIGNED)), 0) + 1 
    -- 將查詢結果存入 next_seq 變數
    INTO next_seq
    -- 從 order_list 表中查詢
    FROM `order_list` 
    -- 條件：只查詢今天建立的訂單
    WHERE LEFT(order_list_id, 8) = current_day;

    -- 組合新的訂單編號，並設定給即將新增的資料
    -- CONCAT: 將日期和流水號字串組合起來
    -- LPAD: 將流水號(next_seq)補成4位數，不足的在左邊補0 (例如: 1 -> 0001)
    SET NEW.order_list_id = CONCAT(current_day, LPAD(next_seq, 4, '0'));
END$$
-- 將結束符改回預設的分號(;)
DELIMITER ;


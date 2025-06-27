-- =======================================================================================
-- 檔案名稱：eatfast_db_initialization.sql
-- 資料庫：eatfast_db
-- 版本：v1.2 - 正式初版
-- 功能：此腳本為早餐店系統「eatfast_db」的完整初始化腳本。
--      它會執行以下操作：
--      1. 建立資料庫 (如果不存在)。
--      2. 依賴序刪除舊有的資料表，確保環境乾淨。
--      3. 建立所有需要的資料表結構。
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
-- 為了避免重複建立資料表造成錯誤，在建立前先嘗試刪除已存在的資料表。
-- 刪除順序遵循「由子到父」的原則，先刪除有外鍵依賴的資料表，再刪除被其他表參考的父表。
DROP TABLE IF EXISTS announcement;          -- 門市公告 (依賴 employee, store)
DROP TABLE IF EXISTS homepage_Info;         -- 首頁資訊 (獨立父表)
DROP TABLE IF EXISTS news;                  -- 最新消息 (依賴 employee)
DROP TABLE IF EXISTS feedback;              -- 顧客意見 (依賴 member, store)
DROP TABLE IF EXISTS store_meal_status;     -- 門市餐點狀態 (依賴 store, meal)
DROP TABLE IF EXISTS cart;                  -- 購物車 (依賴 member, meal, store)
DROP TABLE IF EXISTS fav;                   -- 我的最愛 (依賴 member 和 meal)
DROP TABLE IF EXISTS order_list_info;       -- 訂單明細 (依賴 order_list 和 meal)
DROP TABLE IF EXISTS order_list;            -- 訂單主表 (依賴 member 和 store)
DROP TABLE IF EXISTS employee_permission;   -- 員工權限 (依賴 employee 和 permission)
DROP TABLE IF EXISTS employee;              -- 員工資料 (依賴 store)
DROP TABLE IF EXISTS permission;            -- 權限定義 (獨立父表)
DROP TABLE IF EXISTS meal;                  -- 餐點資料 (依賴 meal_type)
DROP TABLE IF EXISTS meal_type;             -- 餐點種類 (獨立父表)
DROP TABLE IF EXISTS store;                 -- 門市資料 (獨立父表)
DROP TABLE IF EXISTS member;                -- 會員資料 (獨立父表)


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
('黃俊彥', 'hsinyi.chou', 'password015', 'hsinyi.chou@gmail.com', '0939-909-090', '1999-05-05', 'M'),
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
    store_name VARCHAR(10) NOT NULL COMMENT '門市名稱 (例如: 台北南西店)',
    store_loc VARCHAR(50) NOT NULL COMMENT '門市詳細地址',
    store_phone VARCHAR(10) NOT NULL COMMENT '門市聯絡電話 (區碼+號碼)',
    store_time VARCHAR(50) NOT NULL COMMENT '營業時間描述 (純文字，方便彈性顯示)',
    store_status VARCHAR(10) NOT NULL COMMENT '營業狀態 (由後台控制，例如: 營業中, 休息中, 裝修中)',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '此門市資料的建立時間',
    PRIMARY KEY (store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='門市基本資料表。';

/*
 * -----------------------------------------------------
 * 範例資料: store
 * 說明: 新增 2 間分店作為系統的主要營運據點。
 * -----------------------------------------------------
 */
INSERT INTO store (store_name, store_loc, store_phone, store_time, store_status) VALUES
('一號店','台北市中山區南京東路三段219號4F','0227120589','平日05:30~14:00 / 假日 07:00~16:00 周三公休','營業中'),
('二號店','桃園市中壢區復興路46號9樓','034251108','平日05:30~13:00 / 假日 07:00~18:00 周一公休','休息中'),
('總部', '台北市中山區南京東路三段219號5F', '0281017777', '不適用', '總部營運');

-- =======================================================================================
-- 資料表: employee (員工資料表)
-- 功能: 儲存所有員工（包含總部及各分店）的詳細資料、帳號、角色及狀態。此為後台系統的核心使用者。
-- =======================================================================================
CREATE TABLE employee (
    employee_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '員工編號 (主鍵，自動增長)',
    store_id BIGINT NOT NULL COMMENT '所屬門市編號 (外鍵，關聯至 store 表的 store_id)',
    username VARCHAR(20) NOT NULL COMMENT '員工真實姓名',
    account VARCHAR(50) NOT NULL COMMENT '員工登入後台用的帳號 (不可重複)',
    created_by BIGINT NULL COMMENT '此員工帳號的建檔者 (外鍵，關聯至本表 employee_id，允許NULL代表系統管理員或初始員工)',
    password VARCHAR(255) NOT NULL COMMENT '登入密碼 (同樣應儲存加密後的雜湊值)',
    email VARCHAR(100) NOT NULL COMMENT '員工的電子郵件 (不可重複)',
    phone VARCHAR(20) NOT NULL COMMENT '員工的聯絡電話',
    role VARCHAR(10) NOT NULL COMMENT '員工角色 (例如: 總部, 店長, 職員)，用於快速區分職責',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '帳號狀態 (1=啟用，0=停用)，可用來停權員工帳號',
    gender CHAR(1) NOT NULL COMMENT '員工性別 (M=男性, F=女性, O=其他)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '此員工資料的建立時間',
    last_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '此員工資料的最後更新時間',
    photo LONGBLOB NULL COMMENT '員工大頭照 (使用二進制格式儲存圖片資料)',
    national_id VARCHAR(10) NOT NULL COMMENT '身分證字號 (用於人事管理，不可重複)',
    PRIMARY KEY (employee_id),
    UNIQUE KEY uk_employee_account (account) COMMENT '設定 account (員工帳號) 為唯一鍵',
    UNIQUE KEY uk_employee_email (email) COMMENT '設定 email (員工電子郵件) 為唯一鍵',
    UNIQUE KEY uk_employee_national_id (national_id) COMMENT '設定 national_id (身分證字號) 為唯一鍵',
    CONSTRAINT fk_employee_store FOREIGN KEY (store_id) REFERENCES store(store_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_employee_created_by_ref FOREIGN KEY (created_by) REFERENCES employee(employee_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='員工資料表，後台系統的使用者。';

/*
 * -----------------------------------------------------
 * 範例資料: employee
 * 說明: 新增 12 筆員工假資料，涵蓋總部、店長、職員等不同角色與門市。
 * -----------------------------------------------------
 */
INSERT INTO employee (store_id, username, account, created_by, password, email, phone, role, status, gender, national_id) VALUES
(3, '林一郎', 'manager-gui', NULL, 'manager-gui', 'manager-gui@gmail.com', '0911-000-111', '總部管理員', 1, 'M', 'A121976751'), -- 總部最高管理員
(1, '王大明', 'david.wang', 1, '$2y$10$empHashPass002', 'david.wang@gmail.com', '0910-111-222', '店長', 1, 'M', 'B120267183'), -- 一號店店長
(1, '陳美麗', 'mary.chen', 2, '$2y$10$empHashPass003', 'mary.chen@gmail.com', '0920-222-333', '職員', 1, 'F', 'A220464692'), -- 一號店職員
(2, '林建良', 'ken.lin', 1, '$2y$10$empHashPass004', 'ken.lin@gmail.com', '0930-333-444', '店長', 1, 'M', 'J166864809'), -- 二號店店長
(1, '張雅婷', 'tina.chang', 2, '$2y$10$empHashPass005', 'tina.chang@gmail.com', '0940-444-555', '職員', 1, 'F', 'J259707846'), -- 一號店職員
(2, '黃啟宏', 'chihung.huang', 1, '$2y$10$empHashPass006', 'chihung.huang@gmail.com', '0950-555-666', '職員', 1, 'M', 'A172965971'), -- 二號店職員
(3, '吳淑芬', 'sylvia.wu', NULL, '$2y$10$empHashPass007', 'sylvia.wu@gmail.com', '0960-666-777', '總部管理員', 1, 'F', 'A237730085'), -- 總部管理員
(1, '劉俊傑', 'jason.liu', 2, '$2y$10$empHashPass008', 'jason.liu@gmail.com', '0970-777-888', '職員', 0, 'M', 'A161978784'), -- 已停用帳號的職員
(2, '蔡怡靜', 'joyce.tsai', 1, '$2y$10$empHashPass009', 'joyce.tsai@gmail.com', '0980-888-999', '職員', 1, 'F', 'C213892240'), -- 二號店職員
(1, '許明翰', 'minghan.hsu', 2, '$2y$10$empHashPass010', 'minghan.hsu@gmail.com', '0911-123-456', '職員', 1, 'M', 'E196350396'), -- 一號店職員 (原二號店店長)
(1, '鄭心儀', 'cindy.cheng', 1, '$2y$10$empHashPass011', 'cindy.cheng@gmail.com', '0922-234-567', '職員', 1, 'F', 'E283693295'), -- 一號店職員
(2, '方美琪', 'maggie.fang', 2, '$2y$10$empHashPass012', 'maggie.fang@gmail.com', '0955-567-890', '職員', 1, 'F', 'E263360866'); -- 二號店職員

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
(1, '吐司'), (2, '漢堡'), (3, '蛋餅'), (4, '鐵板麵'), (5, '飲品');

-- =======================================================================================
-- 資料表: meal (餐點資料表)
-- 功能: 儲存所有可販售餐點的詳細資訊。
-- 備註: 此表的 status 欄位可作為「總開關」，若設為0，則無論分店狀態為何，該商品都無法販售。
-- =======================================================================================
CREATE TABLE meal(
    meal_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '餐點編號 (主鍵，自動增長)',
    meal_type_id BIGINT NOT NULL COMMENT '餐點種類編號 (外鍵，關聯至 meal_type 表)',
    meal_name VARCHAR(50) NOT NULL COMMENT '餐點的完整名稱 (例如: 起司火腿蛋吐司)',
    meal_pic LONGBLOB DEFAULT NULL COMMENT '餐點圖片',
    meal_price BIGINT NOT NULL COMMENT '餐點單價',
    review_total_stars BIGINT NOT NULL DEFAULT 0 CHECK (review_total_stars BETWEEN 0 AND 5) COMMENT '評價總星數',
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
INSERT INTO meal (meal_type_id, meal_name, meal_pic, meal_price, review_total_stars, status) VALUES
(1, '起司火腿吐司', NULL, 45, 4, 1),    -- 種類:吐司, 名稱:起司火腿吐司, 價格:45, 評價:4星, 狀態:上架
(2, '豬排漢堡', NULL, 60, 5, 1),      -- 種類:漢堡, 名稱:豬排漢堡, 價格:60, 評價:5星, 狀態:上架
(3, '玉米蛋餅', NULL, 35, 4, 1),      -- 種類:蛋餅, 名稱:玉米蛋餅, 價格:35, 評價:4星, 狀態:上架
(4, '黑胡椒鐵板麵', NULL, 55, 5, 1),   -- 種類:鐵板麵, 名稱:黑胡椒鐵板麵, 價格:55, 評價:5星, 狀態:上架
(5, '鮮奶茶', NULL, 40, 3, 1),        -- 種類:飲品, 名稱:鮮奶茶, 價格:40, 評價:3星, 狀態:上架
(1, '花生厚片吐司', NULL, 30, 4, 1),   -- 種類:吐司, 名稱:花生厚片吐司, 價格:30, 評價:4星, 狀態:上架
(2, '雞腿漢堡', NULL, 65, 5, 1),      -- 種類:漢堡, 名稱:雞腿漢堡, 價格:65, 評價:5星, 狀態:上架
(3, '起司蛋餅', NULL, 40, 5, 1),      -- 種類:蛋餅, 名稱:起司蛋餅, 價格:40, 評價:5星, 狀態:上架
(4, '培根鐵板麵', NULL, 60, 4, 0),    -- 種類:鐵板麵, 名稱:培根鐵板麵, 價格:60, 評價:4星, 狀態:下架
(5, '冰美式咖啡', NULL, 35, 5, 1);      -- 種類:飲品, 名稱:冰美式咖啡, 價格:35, 評價:5星, 狀態:上架
-- =======================================================================================
-- 資料表: store_meal_status (門市餐點狀態表) - [已根據建議修正]
-- 功能: 建立特定門市與特定餐點的供應狀態，實現分店層級的即時庫存管理 (例如：今日售完)。
-- 備註: 此表的紀錄會覆蓋 meal 表的總狀態。若此表無紀錄，則依 meal 表的 status 為準。
-- [修正說明]: status 欄位型態由 VARCHAR 改為 TINYINT(1)，更符合布林狀態的語意且效能更佳。
-- =======================================================================================
CREATE TABLE store_meal_status (
    store_id BIGINT NOT NULL COMMENT '門市編號 (複合主鍵之一，外鍵)',
    meal_id BIGINT NOT NULL COMMENT '餐點編號 (複合主鍵之一，外鍵)',
    status TINYINT(1) NOT NULL COMMENT '該門市的此餐點狀態 (1=供應中, 0=已售完)',
    last_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '狀態最後更新時間',
    PRIMARY KEY (store_id, meal_id),
    CONSTRAINT fk_sms_store FOREIGN KEY (store_id) REFERENCES store(store_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_sms_meal FOREIGN KEY (meal_id) REFERENCES meal(meal_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='門市對應餐點的即時狀態表 (多對多)。';

/*
 * -----------------------------------------------------
 * 範例資料: store_meal_status - [已根據建議修正]
 * 說明: status 的值已由字串 '1'/'0' 改為數字 1/0。
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
    order_status BIGINT NOT NULL COMMENT '訂單狀態 (0=待處理, 1=準備中, 2=已完成, 3=已取消)',
    meal_pickup_number BIGINT NOT NULL COMMENT '取餐編號 (用於現場叫號)',
    card_number VARCHAR(20) NOT NULL COMMENT '信用卡卡號 (【警告】正式環境不應儲存完整卡號，此處僅為範例。應串接金流並儲存交易ID)',
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
INSERT INTO order_list (order_list_id, member_id, store_id, order_amount, order_status, meal_pickup_number, card_number) VALUES
('202409050001', 1, 1, 130, 2, 101, '4539123412341234'),   -- (45*2)+(40*1)=130
('202409050002', 1, 2, 100, 1, 102, '4539123412341234'),   -- (60*1)+(40*1)=100
('202409050003', 2, 1, 105, 2, 103, '3540123456789012'),   -- (35*3)=105
('202409050004', 2, 2, 55, 1, 104, '3540123456789012'),    -- (55*1)=55
('202409050005', 3, 1, 60, 0, 105, '5500123412345678'),    -- (30*2)=60
('202409050006', 3, 2, 65, 2, 106, '5500123412345678'),    -- (65*1)=65
('202409050007', 4, 1, 155, 1, 107, '4026123412345678'),   -- (60*2)+(35*1)=155
('202409050008', 4, 2, 135, 2, 108, '4026123412345678'),   -- (45*3)=135
('202409050009', 5, 1, 120, 0, 109, '3569123412345678'),   -- (60*2)=120
('202409050010', 5, 2, 35, 1, 110, '3569123412345678'),    -- (35*1)=35
('202409050011', 6, 1, 110, 2, 111, '5244123412345678'),   -- (55*2)=110
('202409050012', 6, 2, 40, 0, 112, '5244123412345678'),    -- (40*1)=40
('202409050013', 7, 1, 60, 1, 113, '4556123412345678'),    -- (30*2)=60
('202409050014', 7, 2, 65, 2, 114, '4556123412345678'),    -- (65*1)=65
('202409050015', 8, 1, 80, 0, 115, '3537123412341234'),    -- (40*2)=80
('202409050016', 8, 2, 60, 1, 116, '3537123412341234'),    -- (60*1)=60
('202409050017', 9, 1, 115, 2, 117, '5512123412345678'),   -- (35*2)+(45*1)=115
('202409050018', 9, 2, 120, 0, 118, '5512123412345678'),   -- (60*2)=120
('202409050019', 10, 1, 35, 1, 119, '4929123412345678'),   -- (35*1)=35
('202409050020', 10, 2, 165, 2, 120, '4929123412345678'),  -- (55*3)=165
('202409050021', 11, 1, 80, 0, 121, '4000123412341234'),   -- (40*2)=80
('202409050022', 11, 2, 30, 1, 122, '4000123412341234'),   -- (30*1)=30
('202409050023', 12, 1, 130, 2, 123, '3528123412341234'),  -- (65*2)=130
('202409050024', 12, 2, 40, 0, 124, '3528123412341234'),   -- (40*1)=40
('202409050025', 13, 1, 120, 1, 125, '5400123412345678'),  -- (60*2)=120
('202409050026', 13, 2, 105, 2, 126, '5400123412345678'),  -- (35*3)=105
('202409050027', 14, 1, 45, 0, 127, '4333123412345678'),   -- (45*1)=45
('202409050028', 14, 2, 120, 1, 128, '4333123412345678'),  -- (60*2)=120
('202409050029', 15, 1, 35, 2, 129, '3577123412341234'),   -- (35*1)=35
('202409050030', 15, 2, 110, 0, 130, '3577123412341234'),  -- (55*2)=110
('202409050031', 16, 1, 40, 1, 131, '5700123412345678'),   -- (40*1)=40
('202409050032', 16, 2, 60, 2, 132, '5700123412345678'),   -- (30*2)=60
('202409050033', 17, 1, 160, 2, 133, '4111222233334444'),  -- (60*1)+(55*1)+(45*1)=160
('202409050034', 18, 2, 210, 1, 134, '5222333344445555'),  -- (65*2)+(40*2)=210
('202409050035', 19, 1, 100, 0, 135, '3444555566667777'),  -- (30*1)+(35*1)+(35*1)=100
('202409050036', 20, 2, 360, 2, 136, '4999888877776666');  -- (40*3)+(60*2)+(40*3)=360


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
    meal_customization VARCHAR(255) COMMENT '餐點客製化備註 (例如: 少冰、不加蔥)',
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
INSERT INTO order_list_info (order_list_id, meal_id, meal_price, review_stars, quantity, meal_customization) VALUES
('202409050001', 101, 45, 4, 2, '去冰'), ('202409050001', 105, 40, 5, 1, '加蛋'),
('202409050002', 102, 60, 3, 1, '少糖'), ('202409050002', 108, 40, 4, 1, '多醬'),
('202409050003', 103, 35, 5, 3, '不辣'),
('202409050004', 104, 55, 2, 1, '加起司'),
('202409050005', 106, 30, 4, 2, '無蔥'),
('202409050006', 107, 65, 3, 1, '辣度中等'),
('202409050007', 109, 60, 5, 2, ''), ('202409050007', 110, 35, 4, 1, '去蛋'),
('202409050008', 101, 45, 3, 3, '加飯'),
('202409050009', 102, 60, 5, 2, '不加蒜'),
('202409050010', 103, 35, 4, 1, '多起司'),
('202409050011', 104, 55, 3, 2, '全糖'),
('202409050012', 105, 40, 5, 1, '不要蔥花'),
('202409050013', 106, 30, 4, 2, ''),
('202409050014', 107, 65, 3, 1, '無調味'),
('202409050015', 108, 40, 5, 2, '去皮'),
('202409050016', 109, 60, 4, 1, '半糖少冰'),
('202409050017', 110, 35, 3, 2, '正常冰'), ('202409050017', 101, 45, 4, 1, '加辣'),
('202409050018', 102, 60, 5, 2, '不要洋蔥'),
('202409050019', 103, 35, 3, 1, '多蛋'),
('202409050020', 104, 55, 4, 3, '加蔥'),
('202409050021', 105, 40, 5, 2, '微冰'),
('202409050022', 106, 30, 3, 1, '無糖'),
('202409050023', 107, 65, 4, 2, '不加醬'),
('202409050024', 108, 40, 5, 1, '全糖'),
('202409050025', 109, 60, 3, 2, '正常辣'),
('202409050026', 110, 35, 4, 3, '少醬'),
('202409050027', 101, 45, 5, 1, '去起司'),
('202409050028', 102, 60, 3, 2, '多冰'),
('202409050029', 103, 35, 4, 1, '加飯'),
('202409050030', 104, 55, 5, 2, '不加蒜'),
('202409050031', 105, 40, 3, 1, '多起司'),
('202409050032', 106, 30, 4, 2, '去冰'),
('202409050033', 102, 60, 5, 1, ''), ('202409050033', 104, 55, 4, 1, ''), ('202409050033', 101, 45, 5, 1, '去邊'),
('202409050034', 107, 65, 5, 2, '辣'), ('202409050034', 105, 40, 4, 2, '半糖'),
('202409050035', 106, 30, 4, 1, ''), ('202409050035', 110, 35, 5, 1, ''), ('202409050035', 103, 35, 3, 1, ''),
('202409050036', 108, 40, 5, 3, '醬多'), ('202409050036', 102, 60, 5, 2, ''), ('202409050036', 105, 40, 5, 3, '溫');


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


-- =======================================================================================
-- 資料表: feedback (顧客意見回饋表)
-- 功能: 儲存來自顧客的意見或問題回饋。
-- =======================================================================================
CREATE TABLE feedback (
    feedback_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL COMMENT '會員編號',
    store_id BIGINT NOT NULL COMMENT '門市編號',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建檔時間',
    phone VARCHAR(20) NOT NULL COMMENT '連絡電話',
    content VARCHAR(5000) NOT NULL COMMENT '內文',
    PRIMARY KEY (feedback_id),
    FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (store_id) REFERENCES store(store_id) ON DELETE CASCADE ON UPDATE CASCADE
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
-- 資料表: news (最新消息表)
-- 功能: 儲存由後台員工發布的最新消息或公告。
-- =======================================================================================
CREATE TABLE news (
    news_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '最新消息編號 (主鍵)',
    employee_id BIGINT NOT NULL COMMENT '發布此消息的員工編號 (外鍵)',
    title VARCHAR(50) NOT NULL COMMENT '消息標題',
    content VARCHAR(5000) NOT NULL COMMENT '消息詳細內容',
    start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息開始顯示時間',
    end_time TIMESTAMP NULL COMMENT '消息結束顯示時間 (允許NULL代表永久顯示)',
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
(2, '會員專屬優惠開跑', '即日起加入會員即可享有消費滿 $300 折 $50 的專屬優惠，只到 2025-06-30，別錯過！', '2025-06-01 10:00:00', '2025-06-30 23:59:59', 1),
(7, '二號店內部整修公告', '為提供更舒適的用餐環境，二號店將於 2025-07-01 至 2025-07-05 進行內部整修，暫停營業五天，造成不便敬請見諒。', '2025-06-15 09:00:00', NULL, 1),
(1, '新品上市：草莓戀人吐司 (草稿)', '夢幻新品「草莓戀人吐司」即將上市，敬請期待！', '2025-06-20 00:00:00', '2025-07-20 00:00:00', 0);

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
(2, 1, '一號店公休時間調整', '親愛的顧客您好，本店因應端午節連假，6/22(六)公休一日，祝您佳節愉快！', '2025-06-10 00:00:00', '2025-06-22 23:59:59', 1),
(4, 2, '二號店限定！鐵板麵套餐優惠', '即日起至月底，凡點購任一鐵板麵，即可以 $10 加購一杯中杯紅茶！', '2025-06-01 08:00:00', '2025-06-30 23:59:59', 1);


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


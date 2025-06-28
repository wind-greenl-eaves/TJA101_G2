package com.eatfast.common.enums;

/**
 * [可自定義的名稱]: EmployeeRole
 * * 用於定義員工的職位角色。
 * 每個枚舉常數對應資料庫中的一個角色。
 * * 【風險警告 - EnumType.ORDINAL】
 * 當使用 EnumType.ORDINAL 時，JPA 會將枚舉的「順序」(從 0 開始) 存入資料庫。
 * 例如：
 * - HEADQUARTERS_ADMIN -> 0
 * - STORE_MANAGER -> 1
 * - STAFF -> 2
 * * **嚴禁修改或重新排序此檔案中已存在的枚舉常數順序！**
 * 任何順序上的變動都將導致資料庫中的既有資料與程式邏輯無法對應，造成嚴重的資料錯亂。
 * 新增角色應永遠加在檔案的最末端。
 */
public enum EmployeeRole {
    
    HEADQUARTERS_ADMIN("總部管理員"), // 順序 0
    STORE_MANAGER("店長"),         // 順序 1
    STAFF("職員");                 // 順序 2

    private final String description;

    EmployeeRole(String description) {
        this.description = description;
    }

    // [可自定義的方法]: getDescription
    // 這個方法可以在需要顯示中文名稱時呼叫，與資料庫儲存無關。
    public String getDescription() {
        return description;
    }
}

// [檔案路徑]: src/main/java/com/eatfast/common/enums/EmployeeRole.java
package com.eatfast.common.enums;

/**
 * 員工角色列舉。
 * 【核心升級】: 為每個列舉常數增加一個 'displayName' 屬性，專門用於前端 UI 顯示。
 * 這是將「內部邏輯值」與「外部顯示值」分離的最佳實踐。
 */
public enum EmployeeRole {
    
    // [可自定義的常數]: 定義角色，並在建構子中傳入其對應的顯示名稱。
    STAFF("一般員工"),
    MANAGER("門市經理"),
    HEADQUARTERS_ADMIN("總部管理員");

    // [不可變動的關鍵字]: final
    // 說明: 顯示名稱在實例化後就不應再被改變。
    private final String displayName;

    /**
     * [不可變動的關鍵字]: private
     * 說明: Enum 的建構子必須是 private，由 Java 內部呼叫。
     */
    EmployeeRole(String displayName) {
        this.displayName = displayName;
    }

    /**
     * [不可變動的關鍵字]: public
     * 說明: 提供一個公開的 getter 方法，讓外部 (例如 Thymeleaf) 可以讀取到 displayName 的值。
     * @return 此角色的中文顯示名稱。
     */
    public String getDisplayName() {
        return displayName;
    }
}

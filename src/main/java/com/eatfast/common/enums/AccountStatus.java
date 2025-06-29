// [檔案路徑]: src/main/java/com/eatfast/common/enums/AccountStatus.java
package com.eatfast.common.enums;

/**
 * 帳號狀態列舉。
 * 【核心升級】: 同樣為其增加 'displayName' 屬性。
 */
public enum AccountStatus {
	 INACTIVE("停用"),
    ACTIVE("啟用");
   

    private final String displayName;

    AccountStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

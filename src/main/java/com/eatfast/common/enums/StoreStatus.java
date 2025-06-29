// =================================================================================
// 檔案 1/8: StoreStatus.java (★★ 標準化重構 ★★)
// 路徑: src/main/java/com/eatfast/common/enums/StoreStatus.java
// 說明: 移至 common.enums 共用包，並使用完整英文單字提升可讀性。
// =================================================================================
package com.eatfast.common.enums;

public enum StoreStatus {
    OPERATING("營業中"),
    RESTING("休息中"),
    ENDED("已歇業"),
	HEADQUARTERS("總部營運");

    private final String description;

    StoreStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
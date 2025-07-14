package com.eatfast.common.enums;

public enum StoreType {
    BRANCH("一般分店"),
    HEADQUARTERS("總部");
	
    // 新增一個 final 欄位來儲存這個中文名稱
    private final String displayName;

    // 3. 建立建構子，讓枚舉在建立時能把中文名稱存到欄位裡
    StoreType(String displayName) {
        this.displayName = displayName;
    }

    // 4. 提供一個 public 的 getter 方法，讓 Thymeleaf 可以透過這個方法取得中文名稱
    public String getdisplayName() {
    	return displayName;
    }
    
}

package com.eatfast.orderlist.model; 

public enum OrderStatus {
    
    // 為每個枚舉常數加上對應的中文顯示名稱
    PENDING("處理中"),
    CONFIRMED("已確認"),
    SHIPPED("已出餐"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    // 1. 新增一個 final 的私有屬性來儲存顯示名稱
    private final String displayName;

    // 2. 新增一個建構子 (Constructor) 來接收顯示名稱
    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    // 3. 新增一個 public 的 getter 方法，讓 Thymeleaf 可以讀取到這個值
    public String getDisplayName() {
        return displayName;
    }
}
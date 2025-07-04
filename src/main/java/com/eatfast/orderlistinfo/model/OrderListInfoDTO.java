package com.eatfast.orderlistinfo.model;

// 🔹【可自定義】類別名稱，DTO 通常代表 Data Transfer Object
public class OrderListInfoDTO {

    // 這些是我們希望在彈出視窗中顯示的欄位
    private String mealName; // 餐點名稱
    private Long quantity;   // 數量
    private Long mealPrice;  // 當時的單價
    private String mealCustomization; // 客製化選項

    // --- 建構子、Getters、Setters ---
    
    public OrderListInfoDTO(String mealName, Long quantity, Long mealPrice, String mealCustomization) {
        this.mealName = mealName;
        this.quantity = quantity;
        this.mealPrice = mealPrice;
        this.mealCustomization = mealCustomization;
    }

    public String getMealName() {
        return mealName;
    }

    public void setMealName(String mealName) {
        this.mealName = mealName;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Long getMealPrice() {
        return mealPrice;
    }

    public void setMealPrice(Long mealPrice) {
        this.mealPrice = mealPrice;
    }

    public String getMealCustomization() {
        return mealCustomization;
    }

    public void setMealCustomization(String mealCustomization) {
        this.mealCustomization = mealCustomization;
    }
}
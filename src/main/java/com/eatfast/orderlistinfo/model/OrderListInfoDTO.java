package com.eatfast.orderlistinfo.model;

public class OrderListInfoDTO {

    private String mealName;
    private Long quantity;
    private Long mealPrice;
    private Long reviewStars; // ⭐【修改】評論星等欄位（移除 mealCustomization）
    private String mealCustomization; // ⭐【新增】客製化備註欄位

    // --- 建構子、Getters、Setters ---
    
    // ⭐【修改】更新建構子，添加 mealCustomization 參數
    public OrderListInfoDTO(String mealName, Long quantity, Long mealPrice, Long reviewStars, String mealCustomization) {
        this.mealName = mealName;
        this.quantity = quantity;
        this.mealPrice = mealPrice;
        this.reviewStars = reviewStars;
        this.mealCustomization = mealCustomization;
    }

    // ... (保留原本的 Getters/Setters) ...
    public String getMealName() { return mealName; }
    public void setMealName(String mealName) { this.mealName = mealName; }
    public Long getQuantity() { return quantity; }
    public void setQuantity(Long quantity) { this.quantity = quantity; }
    public Long getMealPrice() { return mealPrice; }
    public void setMealPrice(Long mealPrice) { this.mealPrice = mealPrice; }

    // ⭐【修改】reviewStars 的 Getter 和 Setter
    public Long getReviewStars() {
        return reviewStars;
    }

    public void setReviewStars(Long reviewStars) {
        this.reviewStars = reviewStars;
    }

    // ⭐【新增】客製化備註的 Getter 和 Setter
    public String getMealCustomization() {
        return mealCustomization;
    }

    public void setMealCustomization(String mealCustomization) {
        this.mealCustomization = mealCustomization;
    }
}
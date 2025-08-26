package com.eatfast.orderlistinfo.model;

public class OrderListInfoDTO {

    private String mealName;
    private Long quantity;
    private Long mealPrice;
    private Long reviewStars; 
    private String mealCustomization; 

    // --- 建構子、Getters、Setters ---
    
    public OrderListInfoDTO(String mealName, Long quantity, Long mealPrice, Long reviewStars, String mealCustomization) {
        this.mealName = mealName;
        this.quantity = quantity;
        this.mealPrice = mealPrice;
        this.reviewStars = reviewStars;
        this.mealCustomization = mealCustomization;
    }

    public String getMealName() { return mealName; }
    public void setMealName(String mealName) { this.mealName = mealName; }
    public Long getQuantity() { return quantity; }
    public void setQuantity(Long quantity) { this.quantity = quantity; }
    public Long getMealPrice() { return mealPrice; }
    public void setMealPrice(Long mealPrice) { this.mealPrice = mealPrice; }

    public Long getReviewStars() {
        return reviewStars;
    }

    public void setReviewStars(Long reviewStars) {
        this.reviewStars = reviewStars;
    }

    public String getMealCustomization() {
        return mealCustomization;
    }

    public void setMealCustomization(String mealCustomization) {
        this.mealCustomization = mealCustomization;
    }
}
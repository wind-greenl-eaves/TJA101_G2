package com.eatfast.meal.dto;

public class MealDTO {
    private Long mealId;
    private String mealName;
    private Long mealPrice;
    private String mealTypeName;        // 餐點種類名稱（MealTypeEntity.name）
    private String mealPicUrl;          // 餐點圖片網址
    private Long reviewTotalStars;      // 評價總星數
    private Boolean favored;            // 會員是否已收藏
    private Long favMealId;             // 收藏餐點ID（如果已收藏，則有值）

    public MealDTO() {}

    public MealDTO(Long mealId, String mealName, Long mealPrice, String mealTypeName,
    		String mealPicUrl, Long reviewTotalStars, Boolean favored, Long favMealId) {
        this.mealId = mealId;
        this.mealName = mealName;
        this.mealPrice = mealPrice;
        this.mealTypeName = mealTypeName;
        this.mealPicUrl = mealPicUrl;
        this.reviewTotalStars = reviewTotalStars;
        this.favored = favored;
        this.favMealId = favMealId;
        
        
    }

    // Getter/Setter

    public Long getMealId() {
        return mealId;
    }
    public void setMealId(Long mealId) {
        this.mealId = mealId;
    }
    public String getMealName() {
        return mealName;
    }
    public void setMealName(String mealName) {
        this.mealName = mealName;
    }
    public Long getMealPrice() {
        return mealPrice;
    }
    public void setMealPrice(Long mealPrice) {
        this.mealPrice = mealPrice;
    }
    public String getMealTypeName() {
        return mealTypeName;
    }
    public void setMealTypeName(String mealTypeName) {
        this.mealTypeName = mealTypeName;
    }
    public String getMealPicUrl() {
        return mealPicUrl;
    }
    public void setMealPicUrl(String mealPicUrl) {
        this.mealPicUrl = mealPicUrl;
    }
    public Long getReviewTotalStars() {
        return reviewTotalStars;
    }
    public void setReviewTotalStars(Long reviewTotalStars) {
        this.reviewTotalStars = reviewTotalStars;
    }
    public Boolean getFavored() {
        return favored;
    }
    public void setFavored(Boolean favored) {
        this.favored = favored;
    }
    
    public void setFavMealId(Long favMealId) {
		this.favMealId = favMealId;
	}
    public Long getFavMealId() {
		return favMealId;
	}
}

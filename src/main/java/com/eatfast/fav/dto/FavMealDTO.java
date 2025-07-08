package com.eatfast.fav.dto;

public class FavMealDTO {
    private Long favMealId;
    private Long mealId;
    private String mealName;
    private Long mealPrice;
    private String mealPicUrl;
    private Long mealTypeId;

    public Long getFavMealId() {
        return favMealId;
    }
    public void setFavMealId(Long favMealId) {
        this.favMealId = favMealId;
    }
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
    public String getMealPicUrl() {
        return mealPicUrl;
    }
    public void setMealPicUrl(String mealPicUrl) {
        this.mealPicUrl = mealPicUrl;
    }
	public Long getMealTypeId() {
		return mealTypeId;
	}
	public void setMealTypeId(Long mealTypeId) {
		this.mealTypeId = mealTypeId;
	}
}

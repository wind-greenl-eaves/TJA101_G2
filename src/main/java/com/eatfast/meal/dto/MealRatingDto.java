package com.eatfast.meal.dto;

public class MealRatingDto {

	private Long mealId;
    private Double avgStars;
    
    // 無參數建構子
    public MealRatingDto() {
	}
    
    // 計算平均星級的建構子
    public MealRatingDto(Long mealId, Double avgStars) {
		this.mealId = mealId;
		this.avgStars = avgStars;
	}

	public Long getMealId() {
		return mealId;
	}

	public void setMealId(Long mealId) {
		this.mealId = mealId;
	}

	public Double getAvgStars() {
		return avgStars;
	}

	public void setAvgStars(Double avgStars) {
		this.avgStars = (avgStars != null) ? avgStars : 0.0;
	}
	
	@Override
    public String toString() {
        return "MealRatingDto{" +
               "mealId=" + mealId +
               ", avgStars=" + avgStars +
               '}';
    }
}

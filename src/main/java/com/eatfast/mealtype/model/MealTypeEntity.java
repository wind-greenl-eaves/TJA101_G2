package com.eatfast.mealtype.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name="meal_type")
public class MealTypeEntity {
	
	@Id
	@Column(name="meal_type_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer mealTypeId;

	@NotBlank(message = "餐點種類名稱: 請勿空白")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{1,10}$", message = "餐點種類名稱須為中文字，長度1~10字")
    @Column(name = "meal_name", nullable = false, length = 50)
    private String mealName;
    

    
    public MealTypeEntity() {
    }

    public Integer getMealTypeId() {
        return mealTypeId;
    }

    public void setMealTypeId(Integer mealTypeId) {
        this.mealTypeId = mealTypeId;
    }

    public String getMealName() {
        return mealName;
    }

    public void setMealName(String mealName) {
        this.mealName = mealName;
    }

    
}

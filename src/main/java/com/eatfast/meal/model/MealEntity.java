package com.eatfast.meal.model;

import java.io.Serializable;

import com.eatfast.mealtype.model.MealTypeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name="meal")
public class MealEntity implements Serializable {
		private static final long serialVersionUID = 1L;
		
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		@Column(name = "meal_id", nullable = false)
		private Integer mealId;
		
		@ManyToOne
		@JoinColumn(name = "meal_type_id", nullable = false)
		@NotNull(message = "餐點種類不可為空")
		private MealTypeEntity mealType;
		
		@Column(name = "meal_name", nullable = false)
		@Pattern(regexp = "^[\u4e00-\u9fa5]{2,10}$", message = "餐點名稱需為中文,且長度必需在2到10之間")
		@NotEmpty(message="餐點名稱不可為空")
		private String mealName;
		
		@Column(name = "meal_pic", columnDefinition = "longblob")
		private byte[] mealPic; 
		
		@Column(name = "meal_price", nullable = false)
		@Min(value = 1, message = "餐點價格不可小於{value}")
		@NotNull(message="餐點價格不可為空")
		private Integer mealPrice;
		
		@Column(name = "review_total_stars", nullable = false)
		@Min(0)
	    @Max(5)
		@NotNull(message = "總星數不可為空")
		private Integer reviewTotalStars;
		
		@Column(name = "status", nullable = false)
		@Min(0)
	    @Max(1)
		@NotNull(message = "餐點狀態不可為空")
		private Integer status = 1;  // 預設為上架
		
		@Transient
		private String mealTypeName;
		
		public MealEntity() {
			this.status = 1; // 預設為上架
		}

		// 餐點編號
		public Integer getMealId() {
			return mealId;
		}
		
		public void setMealId(Integer mealId) {
			this.mealId = mealId;
		}
		
		// 餐點種類編號
		public MealTypeEntity getMealType() {
			return mealType;
		}

		public void setMealType(MealTypeEntity mealType) {
			this.mealType = mealType;
		}

		// 餐點名稱
		public String getMealName() {
			return mealName;
		}

		public void setMealName(String mealName) {
			this.mealName = mealName;
		}

		// 餐點圖片
		public byte[] getMealPic() {
			return mealPic;
		}

		public void setMealPic(byte[] mealPic) {
			this.mealPic = mealPic;
		}

		// 餐點單價
		public Integer getMealPrice() {
			return mealPrice;
		}

		public void setMealPrice(Integer mealPrice) {
			this.mealPrice = mealPrice;
		}

		// 評論總星數
		public Integer getReviewTotalStars() {
			return reviewTotalStars;
		}

		public void setReviewTotalStars(Integer reviewTotalStars) {
			this.reviewTotalStars = reviewTotalStars;
		}

		// 餐點狀態(上下架)
		public Integer getStatus() {
			return status;
		}

		public void setStatus(Integer status) {
			this.status = status;
		}
		
		// 可用餐點種類來查詢餐點
		public String getMealTypeName() {
		    return mealTypeName;
		}

		public void setMealTypeName(String mealTypeName) {
		    this.mealTypeName = mealTypeName;
		}

		@Override
		public String toString() {
			return "MealEntity [mealId=" + mealId +
	                ", mealTypeEntity=" + (mealType != null ? mealType.getMealTypeId() : "null") +
	                ", mealName=" + mealName +
	                ", mealPrice=" + mealPrice +
	                ", reviewTotalStars=" + reviewTotalStars +
	                ", status=" + status + "]";
		}
		
	}
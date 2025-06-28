package com.eatfast.storemealstatus.model;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id; // 引入 @Id
import jakarta.persistence.IdClass; // 引入 @IdClass
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "store_meal_status") // 移除了重複的 UniqueConstraint
// 使用 @IdClass 指定複合主鍵類別
@IdClass(StoreMealStatusId.class)
public class StoreMealStatusEntity {

	// 標記為複合主鍵的一部分
	@Id
	@Column(name = "store_id", nullable = false)
	private Integer storeId;

	// 標記為複合主鍵的一部分
	@Id
	@Column(name = "meal_id", nullable = false) // 將 column name 改為 meal_id
	private Integer mealId;

	@Column(name = "status", nullable = false)
	private String status;

	@Column(name = "create_time", nullable = false)
	private LocalDateTime createTime;

	// 建立空的建構子(JPA)
	public StoreMealStatusEntity() {
	}

	public StoreMealStatusEntity(Integer storeId, Integer mealId, String status, LocalDateTime createTime) {
		super();
		this.storeId = storeId;
		this.mealId = mealId;
		this.status = status;
		this.createTime = createTime;
	}

	public Integer getStoreId() {
		return storeId;
	}

	public void setStoreId(Integer storeId) {
		this.storeId = storeId;
	}

	public Integer getMealId() {
		return mealId;
	}

	public void setMealId(Integer mealId) {
		this.mealId = mealId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return "StoreMealStatusEntity [storeId=" + storeId + ", mealId=" + mealId + ", status=" + status
				+ ", createTime=" + createTime + "]";
	}

	// hashCode 和 equals 方法的實現應該基於複合主鍵
	// 在 @IdClass 模式下，JPA 會使用 StoreMealStatusId 的 equals/hashCode，但Entity本身也建議正確實作
	@Override
	public int hashCode() {
		return Objects.hash(storeId, mealId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StoreMealStatusEntity other = (StoreMealStatusEntity) obj;
		return Objects.equals(storeId, other.storeId) && Objects.equals(mealId, other.mealId);
	}
}
//購物車VO
package com.eatfast.cart.model;


import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import oracle.sql.TIMESTAMP;

@Entity
@Table(name = "cart", uniqueConstraints = 
	{ @UniqueConstraint(name = "uk_cartId", columnNames = "cart_id")})

public class CartEntity {
	
	//購物車編號
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)  //主鍵自動生成
	@Column(name = "cart_id")
	private Integer cartId;
	
	//會員編號  (之後再關聯)
//	@ManyToOne(fetch=FetchType.EAGER) //多對一
//	@JoinColumn(name = "member_id")
	@Column(name = "member_Id", nullable = false)
	private Integer memberId;
	
	//餐點編號  (之後再關聯)
//	@ManyToOne(fetch=FetchType.EAGER) //多對一
//	@JoinColumn(name = "meal_id")
	@Column(name = "meal_Id", nullable = false)
	private Integer mealId;
	
	//門市編號  (之後再關聯)
//	@ManyToOne(fetch=FetchType.EAGER) //多對一
//	@JoinColumn(name = "store_id")	
	@Column(name = "store_Id", nullable = false)
	private Integer storeId;
	
	//餐點數量
	@Column(name = "amount", nullable = false)
	private Integer amount;
	
	//建立時間
	@CreationTimestamp // Hibernate 會自動填入當下時間
	@Column(name = "create_time", nullable = false, updatable = false)
	private LocalDateTime createTime;
	
	
	//餐點客製化備註
	@Column(name = "meal_customization")
	@Size(max = 255, message = "餐點客製化備註長度最多在255個字以內")  //後端錯誤驗證
	private String mealCustomization;
	
	
	public Integer getCartId() {
		return cartId;
	}

	public void setCartId(Integer cartId) {
		this.cartId = cartId;
	}

	public Integer getMemberId() {
		return memberId;
	}

	public void setMemberId(Integer memberId) {
		this.memberId = memberId;
	}

	public Integer getMealId() {
		return mealId;
	}

	public void setMealId(Integer mealId) {
		this.mealId = mealId;
	}

	public Integer getStoreId() {
		return storeId;
	}

	public void setStoreId(Integer storeId) {
		this.storeId = storeId;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public LocalDateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}

	public String getMealCustomization() {
		return mealCustomization;
	}

	public void setMealCustomization(String mealCustomization) {
		this.mealCustomization = mealCustomization;
	}

	//JPA 規範要求必須提供一個無參數的建構子， 以便框架透過反射機制建立物件實例
	public CartEntity() {
	}

	//提供一個有參數的建構子
	public CartEntity(Integer cartId, Integer memberId, Integer mealId, Integer storeId, Integer amount,
			LocalDateTime createTime, @Size(max = 25520, message = "餐點客製化備註長度最多在255個字以內") String mealCustomization) {
		super();
		this.cartId = cartId;
		this.memberId = memberId;
		this.mealId = mealId;
		this.storeId = storeId;
		this.amount = amount;
		this.createTime = createTime;
		this.mealCustomization = mealCustomization;
	}
	
	
	@Override
	public String toString() {
	    return "CartEntity{" +
	            "cartId=" + cartId +
	            ", memberId='" + memberId + '\'' +
	            ", mealId='" + mealId + '\'' +
	            ", storeId='" + storeId + '\'' +
	            ", amount='" + amount + '\'' +
	            ", createTime=" + createTime +
	            ", mealCustomization=" + mealCustomization +
	            '}';
	}
	
}



package com.eatfast.orderlist.model;

import java.time.LocalDateTime;

import com.eatfast.member.model.MemberEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 訂單主資料表 Entity。 對應到資料庫中的 'order_list' 表。
 */
@Entity // 📌【不可變】聲明這是一個 JPA Entity 類別。
@Table(name = "order_list") // 📌【不可變】指定這個 Entity 對應的資料庫表名稱是 "order_list"。
public class OrderListEntity { // 🔹【已修改】類別名稱已根據你的要求修改。

	/**
	 * 訂單編號 (主鍵，格式:YYYYMMDDXXXX)
	 */
	@Id // 📌【不可變】標示這個屬性是資料表的主鍵 (Primary Key)。
	@Column(name = "order_list_id", nullable = false, length = 20) // 📌【不可變】將屬性對應到 "order_list_id" 欄位。
	private String orderListId; // 🔹【可自定義】

	/**
	 * 此訂單的總金額
	 */
	@Column(name = "order_amount", nullable = false) // 📌【不可變】對應到 "order_amount" 欄位。
	private Long orderAmount; // 🔹【可自定義】

	/**
	 * 訂單成立時間 (由資料庫自動生成)
	 */
	@Column(name = "order_date", nullable = false, updatable = false) // 📌【不可變】對應 "order_date" 欄位。
	private LocalDateTime orderDate; // 🔹【可自定義】

	/**
	 * 訂單狀態 (0=待處理, 1=準備中, 2=已完成, 3=已取消)
	 */
	@Column(name = "order_status", nullable = false) // 📌【不可變】對應 "order_status" 欄位。
	private Long orderStatus; // 🔹【可自定義】

	/**
	 * 取餐編號 (用於現場叫號)
	 */
	@Column(name = "meal_pickup_number", nullable = false) // 📌【不可變】對應 "meal_pickup_number" 欄位。
	private Long mealPickupNumber; // 🔹【可自定義】

	/**
	 * 【警告】信用卡卡號。
	 */
	@Column(name = "card_number", nullable = false, length = 20) // 📌【不可變】對應 "card_number" 欄位。
	private String cardNumber; // 🔹【可自定義】

	// --- 以下為外鍵關聯 ---

	/**
	 * 下訂單的會員。
	 */
	@ManyToOne(fetch = FetchType.LAZY) // 📌【不可變】定義多對一關聯。
	@JoinColumn(name = "member_id", nullable = false) // 📌【不可變】指定用來關聯的欄位是 "member_id"。
	private MemberEntity member;

	/**
	 * 接收此訂單的門市。
	 */
	@ManyToOne(fetch = FetchType.LAZY) // 📌【不可變】定義多對一關聯。
	@JoinColumn(name = "store_id", nullable = false) // 📌【不可變】指定用來關聯的欄位是 "store_id"。
	private StoreEntity store;

	// --- Getter 和 Setter 方法 ---

	public String getOrderListId() {
		return orderListId;
	}

	public void setOrderListId(String orderListId) {
		this.orderListId = orderListId;
	}

	public Long getOrderAmount() {
		return orderAmount;
	}

	public void setOrderAmount(Long orderAmount) {
		this.orderAmount = orderAmount;
	}

	public LocalDateTime getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(LocalDateTime orderDate) {
		this.orderDate = orderDate;
	}

	public Long getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(Long orderStatus) {
		this.orderStatus = orderStatus;
	}

	public Long getMealPickupNumber() {
		return mealPickupNumber;
	}

	public void setMealPickupNumber(Long mealPickupNumber) {
		this.mealPickupNumber = mealPickupNumber;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public MemberEntity getMember() {
		return member;
	}

	public void setMember(MemberEntity member) {
		this.member = member;
	}

	public StoreEntity getStore() {
		return store;
	}

	public void setStore(StoreEntity store) {
		this.store = store;
	}

	public OrderListEntity() {
		super();
	}	

	public OrderListEntity(String orderListId, Long orderAmount, LocalDateTime orderDate, Long orderStatus,
			Long mealPickupNumber, String cardNumber, MemberEntity member, StoreEntity store) {
		super();
		this.orderListId = orderListId;
		this.orderAmount = orderAmount;
		this.orderDate = orderDate;
		this.orderStatus = orderStatus;
		this.mealPickupNumber = mealPickupNumber;
		this.cardNumber = cardNumber;
		this.member = member;
		this.store = store;
	}
}
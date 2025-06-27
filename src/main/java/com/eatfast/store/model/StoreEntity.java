package com.eatfast.store.model;

import java.util.Objects;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;


@Entity
@Table(name = "store", uniqueConstraints = 
{ @UniqueConstraint(name = "uk_storeId", columnNames = "store_id")})
public class StoreEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)  //主鍵自動生成
	@Column(name = "store_id")
	private Integer storeId;
	
	@NotBlank(message = "門市名稱不可為空")  //後端驗證空值錯誤
	@Column(name = "store_name", nullable = false)
	private String storeName;
	
	@NotBlank(message = "地點不可為空")
	@Column(name = "store_loc", nullable = false)
	private String storeLoc;
	
	@NotBlank(message = "門市電話不可為空")
	@Column(name = "store_phone", nullable = false)
	private String storePhone;
	
	@NotBlank(message = "門市營業時間不可為空")
	@Column(name = "store_time", nullable = false)
	private String storeTime;
	
	//營業狀態(營業中、休息中、停業)
	@Column(name = "store_status", nullable = false)
	private StoreStatus storeStatus;
	
	
	@Column(name = "create_time", nullable = false)
	private LocalDateTime createTime;
	
	
	// 新增無參數建構子，JPA 規範要求
	public StoreEntity() {
	}


	public StoreEntity(Integer storeId, @NotBlank(message = "門市名稱不可為空") String storeName,
			@NotBlank(message = "地點不可為空") String storeLoc, @NotBlank(message = "門市電話不可為空") String storePhone,
			@NotBlank(message = "門市營業時間不可為空") String storeTime, StoreStatus storeStatus, LocalDateTime createTime) {
		super();
		this.storeId = storeId;
		this.storeName = storeName;
		this.storeLoc = storeLoc;
		this.storePhone = storePhone;
		this.storeTime = storeTime;
		this.storeStatus = storeStatus;
		this.createTime = createTime;
	}


	public Integer getStoreId() {
		return storeId;
	}


	public void setStoreId(Integer storeId) {
		this.storeId = storeId;
	}


	public String getStoreName() {
		return storeName;
	}


	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}


	public String getStoreLoc() {
		return storeLoc;
	}


	public void setStoreLoc(String storeLoc) {
		this.storeLoc = storeLoc;
	}


	public String getStorePhone() {
		return storePhone;
	}


	public void setStorePhone(String storePhone) {
		this.storePhone = storePhone;
	}


	public String getStoreTime() {
		return storeTime;
	}


	public void setStoreTime(String storeTime) {
		this.storeTime = storeTime;
	}

	//門市狀態枚舉Enum
	public StoreStatus getStoreStatus() {
		return storeStatus;
	}


	public void setStoreStatus(StoreStatus storeStatus) {
		this.storeStatus = storeStatus;
	}


	public LocalDateTime getCreateTime() {
		return createTime;
	}


	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}
	


	@Override
	public String toString() {
		return "StoreEntity [storeId=" + storeId + ", storeName=" + storeName + ", storeLoc=" + storeLoc
				+ ", storePhone=" + storePhone + ", storeTime=" + storeTime + ", storeStatus=" + storeStatus
				+ ", createTime=" + createTime + "]";
	}


	@Override
	public int hashCode() {
		return Objects.hash(storeId);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StoreEntity other = (StoreEntity) obj;
		return Objects.equals(storeId, other.storeId);
	}


	
}

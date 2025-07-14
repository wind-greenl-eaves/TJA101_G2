// =================================================================================
// 檔案 2/8: StoreDto.java (★★★ 全新建立 ★★★)
// 路徑: src/main/java/com/eatfast/store/dto/StoreDto.java
// 說明: Service 回傳給外部的標準資料結構，隱藏了不必要的內部細節。
// =================================================================================
package com.eatfast.store.dto;

import com.eatfast.common.enums.StoreStatus;
import java.time.LocalDateTime;

public class StoreDto {
    private Long storeId;
    private String storeName;
    private String storeLoc;
    private String storePhone;
    private String storeTime;
    private StoreStatus storeStatus;
    private LocalDateTime createTime;
    
    //新增googleMap欄位
    private String googleMapUrl; 
    
    public StoreDto(Long storeId, String storeName) {
        this.storeId = storeId;
        this.storeName = storeName;
    }
    
    //建立空的建構子
    public StoreDto() {
    }

    public StoreDto(Long storeId, String storeName, String storeLoc, String storePhone, String storeTime,
			StoreStatus storeStatus, LocalDateTime createTime, String googleMapUrl) {
		super();
		this.storeId = storeId;
		this.storeName = storeName;
		this.storeLoc = storeLoc;
		this.storePhone = storePhone;
		this.storeTime = storeTime;
		this.storeStatus = storeStatus;
		this.createTime = createTime;
		this.googleMapUrl = googleMapUrl;
	}
	// Getters and Setters...
    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getStoreLoc() { return storeLoc; }
    public void setStoreLoc(String storeLoc) { this.storeLoc = storeLoc; }
    public String getStorePhone() { return storePhone; }
    public void setStorePhone(String storePhone) { this.storePhone = storePhone; }
    public String getStoreTime() { return storeTime; }
    public void setStoreTime(String storeTime) { this.storeTime = storeTime; }
    public StoreStatus getStoreStatus() { return storeStatus; }
    public void setStoreStatus(StoreStatus storeStatus) { this.storeStatus = storeStatus; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getGoogleMapUrl() { return googleMapUrl; }
    public void setGoogleMapUrl(String googleMapUrl) { this.googleMapUrl = googleMapUrl; }
}
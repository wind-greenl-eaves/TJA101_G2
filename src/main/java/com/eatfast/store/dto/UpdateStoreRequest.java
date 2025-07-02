// =================================================================================
// 檔案 1/5: UpdateStoreRequest.java (★★★ 全新建立 ★★★)
// 路徑: src/main/java/com/eatfast/store/dto/UpdateStoreRequest.java
// 說明: 專門用於接收「更新門市」請求的 DTO。所有欄位皆為可選。
// =================================================================================
package com.eatfast.store.dto;

import com.eatfast.common.enums.StoreStatus;
import jakarta.validation.constraints.Size;

public class UpdateStoreRequest {

    // ID 是必要的，用來在 Controller 中確認操作對象
    private Long storeId;

    @Size(max = 10, message = "門市名稱長度不可超過 10 個字元")
    private String storeName;

    @Size(max = 50, message = "地點長度不可超過 50 個字元")
    private String storeLoc;

    @Size(max = 10, message = "門市電話長度不可超過 10 個字元")
    private String storePhone;

    @Size(max = 50, message = "營業時間長度不可超過 50 個字元")
    private String storeTime;

    // 更新時，狀態也是可選的
    private StoreStatus storeStatus;

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
}
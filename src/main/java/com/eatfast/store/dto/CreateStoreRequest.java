package com.eatfast.store.dto;

import com.eatfast.common.enums.StoreStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateStoreRequest {
    
    // 【核心修正】: 新增 storeId 欄位。
    // 說明: 雖然「新增」時不需要此欄位，但在「更新」驗證失敗返回表單時，
    // 需要用它來攜帶 storeId，以便前端能正確重新渲染。
    private Long storeId;

    @NotBlank(message = "門市名稱不可為空")
    @Size(max = 10)
    private String storeName;

    @NotBlank(message = "地點不可為空")
    @Size(max = 50)
    private String storeLoc;

    @NotBlank(message = "門市電話不可為空")
    @Size(max = 10)
    private String storePhone;

    @NotBlank(message = "門市營業時間不可為空")
    @Size(max = 50)
    private String storeTime;

    @NotNull(message = "門市狀態不可為空")
    private StoreStatus storeStatus;

    // Getters and Setters...
    
    // 【核心修正】: 新增 storeId 的 Getter 和 Setter
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
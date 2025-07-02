package com.eatfast.storemeal.dto;

import com.eatfast.common.enums.MealSupplyStatus;
import jakarta.validation.constraints.NotNull;

/**
 * 用於接收更新門市餐點狀態請求的資料傳輸物件 (DTO)。
 * 只包含客戶端需要提供的最少資訊。
 */
public class UpdateStatusRequest {

    // 【檔案路徑配對】: 引入共享的 Enum
    // 使用 @NotNull 進行後端驗證，確保前端請求的 body 中必須包含 status 欄位。
    @NotNull(message = "供應狀態不可為空")
    private MealSupplyStatus status;

    // Getters and Setters for Spring to map JSON data
    public MealSupplyStatus getStatus() {
        return status;
    }

    public void setStatus(MealSupplyStatus status) {
        this.status = status;
    }
}
package com.eatfast.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 顧客意見回饋表單的資料傳輸物件 (DTO) 
 * 使用 Lombok 的 @Data 註解自動產生 getter, setter, toString 等方法
 */
@Data
public class FeedbackDTO {

    /**
     * 會員姓名 (由 Controller 從 Session 填入，唯讀)
     */
    private String memberName;

    /**
     * 聯絡電話 (由 Controller 從 Session 填入，唯讀)
     */
    private String contactPhone;

    /**
     * 用餐時間 (由使用者填寫，需要驗證)
     */
    @NotNull(message = "用餐時間不能為空")
    @PastOrPresent(message = "用餐時間不能是未來時間")
    private LocalDateTime diningTime;

    /**
     * 用餐門市的 ID (由使用者從下拉選單選擇，需要驗證)
     */
    @NotNull(message = "請選擇用餐門市")
    private Long storeId;

    /**
     * 意見內容 (由使用者填寫，需要驗證)
     */
    @NotBlank(message = "意見內容不能為空")
    @Size(max = 500, message = "意見內容最多不能超過 500 字")
    private String feedbackContent;

    // 手動添加 getter 和 setter 方法以解決編譯錯誤
    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public LocalDateTime getDiningTime() {
        return diningTime;
    }

    public void setDiningTime(LocalDateTime diningTime) {
        this.diningTime = diningTime;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public String getFeedbackContent() {
        return feedbackContent;
    }

    public void setFeedbackContent(String feedbackContent) {
        this.feedbackContent = feedbackContent;
    }
}
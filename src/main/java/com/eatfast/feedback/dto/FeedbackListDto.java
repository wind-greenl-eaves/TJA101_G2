package com.eatfast.feedback.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用於後台意見回饋列表的資料傳輸物件 (DTO)
 * 使用 Lombok 的 @Data 註解，自動產生所有 getter, setter 等方法。
 */
@Data
public class FeedbackListDto {

    // == 欄位定義 ==

    private Long feedbackId;
    private Long memberId;
    private String memberName;
    private String phone;
    private String content;
    private LocalDateTime createTime;
    private String status;
    private String diningStore;

    // ✅ 只保留一個 LocalDateTime 型別的 diningTime
    private LocalDateTime diningTime;

    // 手動添加 getter 和 setter 方法以解決編譯錯誤
    public Long getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(Long feedbackId) {
        this.feedbackId = feedbackId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDiningStore() {
        return diningStore;
    }

    public void setDiningStore(String diningStore) {
        this.diningStore = diningStore;
    }

    public LocalDateTime getDiningTime() {
        return diningTime;
    }

    public void setDiningTime(LocalDateTime diningTime) {
        this.diningTime = diningTime;
    }
}
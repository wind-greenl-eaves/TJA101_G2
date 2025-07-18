package com.eatfast.feedback.dto;

import java.time.LocalDateTime;

public class FeedbackListDto {

    // 原有欄位
    private Long feedbackId;
    private Long memberId;
    private String phone;
    private String content;
    private LocalDateTime createTime;
    private String status;
    private String memberName;

    // ★★★ 新增的欄位 ★★★
    private String diningTime;
    private String diningStore;

    // Getters and Setters for all fields...
    public Long getFeedbackId() { return feedbackId; }
    public void setFeedbackId(Long feedbackId) { this.feedbackId = feedbackId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    // ★★★ 新增欄位的 Getters/Setters ★★★
    public String getDiningTime() { return diningTime; }
    public void setDiningTime(String diningTime) { this.diningTime = diningTime; }
    public String getDiningStore() { return diningStore; }
    public void setDiningStore(String diningStore) { this.diningStore = diningStore; }
}
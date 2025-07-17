package com.eatfast.feedback.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 顧客意見回饋實體 (Feedback Entity)
 * <p>
 * 此實體對應資料庫中的 `feedback` 表，用於儲存來自顧客的意見或問題。
 * (版本: 已移除 Foreign Key 關聯，由程式邏輯確保資料完整性)
 * </p>
 */
@Entity
@Table(name = "feedback")
public class FeedbackEntity {

    //================================================================
    //                    欄位定義 (Fields)
    //================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    // --- 原有欄位 ---

    @Column(name = "member_id", nullable = false)
    private Long memberId; // FK 已移除，改為 Long 型別

    @Column(name = "store_id")
    private Long storeId; // FK 已移除，改為 Long 型別

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @NotBlank(message = "連絡電話不可為空")
    @Size(max = 20, message = "連絡電話長度不可超過 20 字元")
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @NotBlank(message = "意見內容不可為空")
    @Column(name = "content", nullable = false, length = 5000)
    private String content;

    @Column(name = "dining_time", length = 255) // 根據 DDL 調整長度
    private String diningTime;

    @Column(name = "dining_store", length = 255) // 根據 DDL 調整長度
    private String diningStore;

    // --- ★★★ 緊急應對新增欄位 (由此開始) ★★★ ---

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "reply_content", columnDefinition = "TEXT")
    private String replyContent;

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    @Column(name = "processed_by_admin_id")
    private Long processedByAdminId;

    // --- ★★★ 緊急應對新增欄位 (到此結束) ★★★ ---


    //================================================================
    //               建構子、Getters、Setters
    //================================================================

    public FeedbackEntity() {
        // 預設建構子
    }

    // --- Getters and Setters ---

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

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
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

    public String getDiningTime() {
        return diningTime;
    }

    public void setDiningTime(String diningTime) {
        this.diningTime = diningTime;
    }

    public String getDiningStore() {
        return diningStore;
    }

    public void setDiningStore(String diningStore) {
        this.diningStore = diningStore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public LocalDateTime getRepliedAt() {
        return repliedAt;
    }

    public void setRepliedAt(LocalDateTime repliedAt) {
        this.repliedAt = repliedAt;
    }

    public Long getProcessedByAdminId() {
        return processedByAdminId;
    }

    public void setProcessedByAdminId(Long processedByAdminId) {
        this.processedByAdminId = processedByAdminId;
    }

    //================================================================
    // 物件核心方法 (equals, hashCode, toString)
    //================================================================

    @Override
    public String toString() {
        return "FeedbackEntity{" +
                "feedbackId=" + feedbackId +
                ", memberId=" + memberId +
                ", storeId=" + storeId +
                ", createTime=" + createTime +
                ", status='" + status + '\'' +
                ", repliedAt=" + repliedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeedbackEntity that = (FeedbackEntity) o;
        if (feedbackId == null || that.feedbackId == null) {
            return false;
        }
        return Objects.equals(feedbackId, that.feedbackId);
    }

    @Override
    public int hashCode() {
        return feedbackId != null ? Objects.hash(feedbackId) : super.hashCode();
    }
}

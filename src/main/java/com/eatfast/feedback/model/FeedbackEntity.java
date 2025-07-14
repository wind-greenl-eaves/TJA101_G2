package com.eatfast.feedback.model    ; // 假設的 package 路徑

// 【檔案路徑配對】: 為了建立多對一關聯，需要 import 父實體
import com.eatfast.member.model.MemberEntity;
import com.eatfast.store.model.StoreEntity;

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
 * 每一筆紀錄都必須關聯到一位提交意見的會員和其所針對的門市。
 * </p>
 */
@Entity
@Table(name = "feedback")
public class FeedbackEntity {

    //================================================================
    // 							欄位定義 (Fields)
    //================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    /**
     * 此意見回饋的提交時間。
     * 1. @CreationTimestamp: (不可變關鍵字) Hibernate 註解，在實體新增時自動填入當前時間。
     * 2. updatable = false: (不可變關鍵字) 確保此時間戳在後續更新中不會被改變。
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @NotBlank(message = "連絡電話不可為空")
    @Size(max = 20, message = "連絡電話長度不可超過 20 字元")
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    /**
     * 意見回饋的詳細內文。
     * length = 5000: (不可變動) 此設定需與資料庫 DDL 的 VARCHAR(5000) 完全匹配，
     * 以避免因內容過長導致的資料截斷錯誤。
     */
    @NotBlank(message = "意見內容不可為空")
    @Column(name = "content", nullable = false, length = 5000)
    private String content;


    //================================================================
    // 				關聯的擁有方 (Owning Side of Relationship)
    //================================================================

    /**
     * 提交此意見的會員 (多對一)。
     * 1. @ManyToOne: (不可變關鍵字) 宣告這是一個多對一關聯 (多筆意見 -> 一位會員)。
     * 2. fetch = FetchType.LAZY: (不可變關鍵字) **效能關鍵**。
     * 3. @JoinColumn: (不可變關鍵字) 指定外鍵欄位。
     * 4. name = "member_id": (不可變動) 必須與資料庫中的外鍵欄位名完全匹配。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    /**
     * 意見所針對的門市 (多對一)。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;


    //================================================================
    //					 建構子、Getters、Setters
    //================================================================
    public FeedbackEntity() {}

    public Long getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(Long feedbackId) {
        this.feedbackId = feedbackId;
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

    //================================================================
    // 物件核心方法 (equals, hashCode, toString)
    //================================================================
    @Override
    public String toString() {
        // 為避免 LAZY loading 問題，關聯物件只印出其 ID
        return "FeedbackEntity{" +
                "feedbackId=" + feedbackId +
                ", memberId=" + (member != null ? member.getMemberId() : "null") +
                ", storeId=" + (store != null ? store.getStoreId() : "null") +
                ", createTime=" + createTime +
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

    public void setSubmissionDate(LocalDateTime now) {
    }

    public void setStatus(String 待處理) {
    }
}

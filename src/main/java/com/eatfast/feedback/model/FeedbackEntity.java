package com.eatfast.feedback.model;

import java.time.LocalDateTime;

import com.eatfast.member.model.MemberEntity;
import com.eatfast.store.model.StoreEntity;
//本版本時分店資料 StoreEntity尚未上傳完成
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "feedback")
public class FeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;
  //本版本時分店資料 StoreEntity尚未上傳完成
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "content", nullable = false, length = 5000)
    private String content;

    // ===== Getters and Setters =====

    public Long getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(Long feedbackId) {
        this.feedbackId = feedbackId;
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
  //本版本時分店資料 StoreEntity尚未上傳完成
    public void setStore(StoreEntity store) {
        this.store = store;
    }
  //本版本時分店資料 StoreEntity尚未上傳完成
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
}

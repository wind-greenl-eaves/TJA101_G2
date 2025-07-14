package com.eatfast.employee.model;

import com.eatfast.common.enums.EmployeeRole;
import com.eatfast.common.enums.Gender;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_applications")
public class EmployeeApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long applicationId;

    // 申請人資訊
    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    @Column(name = "applicant_name", nullable = false, length = 50)
    private String applicantName;

    // 要新增的員工資訊
    @Column(name = "employee_username", nullable = false, length = 50)
    private String username;

    @Column(name = "employee_account", nullable = false, length = 50, unique = true)
    private String account;

    @Column(name = "employee_email", nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "employee_phone", length = 20)
    private String phone;

    @Column(name = "employee_national_id", nullable = false, length = 10, unique = true)
    private String nationalId;

    @Column(name = "employee_password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_role", nullable = false)
    private EmployeeRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_gender")
    private Gender gender;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "store_name", length = 100)
    private String storeName;

    @Column(name = "photo_url", length = 255)
    private String photoUrl;

    // 申請狀態
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    // 審核相關
    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "reviewer_name", length = 50)
    private String reviewerName;

    @Column(name = "review_comment", length = 500)
    private String reviewComment;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    // 時間戳記
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 申請狀態列舉
    public enum ApplicationStatus {
        PENDING("待審核"),
        APPROVED("已核准"),
        REJECTED("已拒絕");

        private final String displayName;

        ApplicationStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 無參建構函式
    public EmployeeApplicationEntity() {
    }

    // 有參建構函式，用於創建新申請
    public EmployeeApplicationEntity(Long applicantId, String applicantName, String username, 
                                   String account, String email, String phone, String nationalId,
                                   String password, EmployeeRole role, Gender gender, 
                                   Long storeId, String storeName, String photoUrl) {
        this.applicantId = applicantId;
        this.applicantName = applicantName;
        this.username = username;
        this.account = account;
        this.email = email;
        this.phone = phone;
        this.nationalId = nationalId;
        this.password = password;
        this.role = role;
        this.gender = gender;
        this.storeId = storeId;
        this.storeName = storeName;
        this.photoUrl = photoUrl;
        this.status = ApplicationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Getter 和 Setter 方法
    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public EmployeeRole getRole() {
        return role;
    }

    public void setRole(EmployeeRole role) {
        this.role = role;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
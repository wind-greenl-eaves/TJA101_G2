package com.eatfast.employee.dto;

import com.eatfast.common.enums.EmployeeRole;
import com.eatfast.common.enums.Gender;
import com.eatfast.employee.model.EmployeeApplicationEntity.ApplicationStatus;

import java.time.LocalDateTime;

public class EmployeeApplicationDTO {

    private Long applicationId;
    
    // 申請人資訊
    private Long applicantId;
    private String applicantName;
    
    // 要新增的員工資訊
    private String username;
    private String account;
    private String email;
    private String phone;
    private String nationalId;
    private EmployeeRole role;
    private Gender gender;
    private Long storeId;
    private String storeName;
    private String photoUrl;
    
    // 申請狀態
    private ApplicationStatus status;
    private String statusDisplayName;
    
    // 審核相關
    private Long reviewerId;
    private String reviewerName;
    private String reviewComment;
    private LocalDateTime reviewedAt;
    
    // 時間戳記
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 格式化的時間顯示
    private String createdAtFormatted;
    private String reviewedAtFormatted;

    // 無參建構函式
    public EmployeeApplicationDTO() {
    }

    // 有參建構函式
    public EmployeeApplicationDTO(Long applicationId, Long applicantId, String applicantName,
                                 String username, String account, String email, String phone,
                                 String nationalId, EmployeeRole role, Gender gender, Long storeId,
                                 String storeName, String photoUrl, ApplicationStatus status,
                                 String statusDisplayName, Long reviewerId, String reviewerName,
                                 String reviewComment, LocalDateTime reviewedAt, LocalDateTime createdAt,
                                 LocalDateTime updatedAt, String createdAtFormatted, String reviewedAtFormatted) {
        this.applicationId = applicationId;
        this.applicantId = applicantId;
        this.applicantName = applicantName;
        this.username = username;
        this.account = account;
        this.email = email;
        this.phone = phone;
        this.nationalId = nationalId;
        this.role = role;
        this.gender = gender;
        this.storeId = storeId;
        this.storeName = storeName;
        this.photoUrl = photoUrl;
        this.status = status;
        this.statusDisplayName = statusDisplayName;
        this.reviewerId = reviewerId;
        this.reviewerName = reviewerName;
        this.reviewComment = reviewComment;
        this.reviewedAt = reviewedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdAtFormatted = createdAtFormatted;
        this.reviewedAtFormatted = reviewedAtFormatted;
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

    public String getStatusDisplayName() {
        return statusDisplayName;
    }

    public void setStatusDisplayName(String statusDisplayName) {
        this.statusDisplayName = statusDisplayName;
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

    public String getCreatedAtFormatted() {
        return createdAtFormatted;
    }

    public void setCreatedAtFormatted(String createdAtFormatted) {
        this.createdAtFormatted = createdAtFormatted;
    }

    public String getReviewedAtFormatted() {
        return reviewedAtFormatted;
    }

    public void setReviewedAtFormatted(String reviewedAtFormatted) {
        this.reviewedAtFormatted = reviewedAtFormatted;
    }
}
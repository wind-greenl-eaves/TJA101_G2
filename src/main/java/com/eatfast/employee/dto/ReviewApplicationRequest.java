package com.eatfast.employee.dto;

import com.eatfast.employee.model.EmployeeApplicationEntity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReviewApplicationRequest {

    @NotNull(message = "申請ID不能為空")
    private Long applicationId;

    @NotNull(message = "審核結果不能為空")
    private ApplicationStatus status;

    @Size(max = 500, message = "審核意見不能超過500字")
    private String reviewComment;

    // 無參建構函式
    public ReviewApplicationRequest() {
    }

    // 有參建構函式
    public ReviewApplicationRequest(Long applicationId, ApplicationStatus status, String reviewComment) {
        this.applicationId = applicationId;
        this.status = status;
        this.reviewComment = reviewComment;
    }

    // Getter 和 Setter 方法
    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }
}
/*
 * ================================================================
 * 檔案: NewsEntity.java (Enum 重構定版)
 * ================================================================
 * - 審查結論:
 * 1. 【結構重構】: 已移除內部的 `NewsStatus` Enum 定義，並改為
 * 引用共享的 `com.eatfast.common.enums.NewsStatus`，
 * 符合團隊決議，提升了程式碼的重用性與維護性。
 * 2. 【邏輯正確】:
 * - @ManyToOne: 與 Employee 的多對一關聯已正確配置。
 * - @Enumerated(EnumType.ORDINAL): 遵照團隊決議，維持使用 ORDINAL 儲存。
 * 3. 【關聯完整】: 已在 EmployeeEntity 中建立對應的 @OneToMany
 * 反向關聯，形成完整的雙向關係。
 *
 * >> 此版本已是可交付的最終版本。
 */
package com.eatfast.news.model;

// 引入關聯的父實體
import com.eatfast.employee.model.EmployeeEntity;
// 引入共享的 Enum
import com.eatfast.common.enums.NewsStatus;

// 引入 Jakarta Persistence 與 Validation API
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 最新消息實體 (News Entity)
 * <p>
 * 核心功能:
 * - 映射資料庫 `news` 表。
 * - 儲存由員工發布的全站最新消息或公告。
 */
@Entity
@Table(name = "news")
public class NewsEntity {

    //================================================================
    // 							欄位定義
    //================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_id")
    private Long newsId;

    @NotBlank(message = "消息標題不可為空")
    @Size(max = 50, message = "消息標題長度不可超過 50 字元")
    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @NotBlank(message = "消息內容不可為空")
    @Column(name = "content", nullable = false, length = 5000)
    private String content;

    @NotNull(message = "消息開始時間不可為空")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /**
     * 消息結束顯示的時間。
     * 此欄位可為 null，代表永久顯示。
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    @NotNull(message = "消息狀態不可為空")
    @Enumerated(EnumType.ORDINAL) // 遵照團隊決議，使用 ORDINAL
    @Column(name = "status", nullable = false)
    private NewsStatus status;


    //================================================================
    // 				關聯的擁有方 (Owning Side)
    //================================================================

    /**
     * 發布此消息的員工 (多對一)。
     * LAZY Fetching: 預設不載入關聯的 Employee 物件，提升查詢效能。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employee;


    //================================================================
    //					 建構子、Getters、Setters
    //================================================================
    public NewsEntity() {}

    public Long getNewsId() { return newsId; }
    public void setNewsId(Long newsId) { this.newsId = newsId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public NewsStatus getStatus() { return status; }
    public void setStatus(NewsStatus status) { this.status = status; }
    public EmployeeEntity getEmployee() { return employee; }
    public void setEmployee(EmployeeEntity employee) { this.employee = employee; }

    //================================================================
    // 物件核心方法 (equals, hashCode, toString)
    //================================================================
    @Override
    public String toString() {
        return "NewsEntity{" +
                "newsId=" + newsId +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", employeeId=" + (employee != null ? employee.getEmployeeId() : "null") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewsEntity that = (NewsEntity) o;
        if (newsId == null || that.newsId == null) {
            return false;
        }
        return Objects.equals(newsId, that.newsId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

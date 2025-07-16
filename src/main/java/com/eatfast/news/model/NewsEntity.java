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

// 引入需要的 Annotation 和時間類別
import org.hibernate.annotations.UpdateTimestamp;
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
    //                    欄位定義
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


    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private NewsStatus status;

    // ★ 1. 在這裡新增 updateTime 欄位
    /**
     * 資料最後更新時間。
     * @UpdateTimestamp 會讓 Hibernate 在每次更新這筆資料時，
     * 自動填入當前的系統時間。
     */
    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    /**
     * 消息圖片的網址
     */
    @Column(name = "image_url")
    private String imageUrl;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employee;


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

    // ★ 2. 在這裡新增 updateTime 的 getter/setter
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    // ★ 3. 補上 imageUrl 的 getter/setter
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }


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
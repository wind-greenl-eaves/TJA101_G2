
package com.eatfast.announcement.model;

// 引入關聯的父實體
import com.eatfast.employee.model.EmployeeEntity;
import com.eatfast.store.model.StoreEntity;
// 引入共享的 Enum
import com.eatfast.common.enums.AnnouncementStatus;

// 引入 Jakarta Persistence 與 Validation API
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 引入 Hibernate 專屬功能註解


import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;

/*
 * ================================================================
 *  AnnouncementEntity.java (Enum 重構定版)
 * ================================================================
 * - 審查結論:
 * 1. 【結構重構】: 已移除內部的 `AnnouncementStatus` Enum 定義，並改為
 * 引用共享的 `com.eatfast.common.enums.AnnouncementStatus`，
 * 符合團隊決議，提升了程式碼的重用性與維護性。
 * 2. 【邏輯正確】:
 * - @ManyToOne: 與 Employee 和 Store 的多對一關聯均已正確配置。
 * - @CreationTimestamp: `createTime` 欄位正確使用此註解，確保僅在
 * 新增時寫入時間戳。
 * - @Enumerated(EnumType.ORDINAL): 遵照團隊決議，維持使用 ORDINAL 儲存。
 * 3. 【關聯完整】: 已在 EmployeeEntity 與 StoreEntity 中建立對應的 @OneToMany
 * 反向關聯，形成完整的雙向關係。
 */

/**
 * 門市公告實體 (Announcement Entity)
 * <p>
 * 核心功能:
 * - 映射資料庫 `announcement` 表。
 * - 儲存由各分店獨立發布的專屬公告。
 */
@Entity
@Table(name = "announcement")
public class AnnouncementEntity {

    //================================================================
    // 							欄位定義
    //================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "announcement_id")
    private Long announcementId;

    @NotBlank(message = "公告標題不可為空")
    @Size(max = 50, message = "公告標題長度不可超過 50 字元")
    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @NotBlank(message = "公告內容不可為空")
    @Column(name = "content", nullable = false, length = 5000)
    private String content;

    @CreationTimestamp // 重點註記: 僅在新建時，由 Hibernate 自動寫入當前時間。
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @NotNull(message = "公告上架時間不可為空")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @NotNull(message = "公告下架時間不可為空")
    @Future(message = "下架時間必須在未來") // 重點註記: Jakarta Validation 約束，確保日期合理性。
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @NotNull(message = "狀態不可為空")
    @Enumerated(EnumType.ORDINAL) // 遵照團隊決議，使用 ORDINAL
    @Column(name = "status", nullable = false)
    private AnnouncementStatus status;


    //================================================================
    // 				關聯的擁有方 (Owning Side)
    //================================================================

    /**
     * 發布此公告的員工 (多對一)。
     * LAZY Fetching: 預設不載入關聯的 Employee 物件，提升查詢效能。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employee;

    /**
     * 公告所屬的門市 (多對一)。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;


    //================================================================
    //					 建構子、Getters、Setters
    //================================================================
    public AnnouncementEntity() {}

    public Long getAnnouncementId() { return announcementId; }
    public void setAnnouncementId(Long announcementId) { this.announcementId = announcementId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public AnnouncementStatus getStatus() { return status; }
    public void setStatus(AnnouncementStatus status) { this.status = status; }
    public EmployeeEntity getEmployee() { return employee; }
    public void setEmployee(EmployeeEntity employee) { this.employee = employee; }
    public StoreEntity getStore() { return store; }
    public void setStore(StoreEntity store) { this.store = store; }

    //================================================================
    // 物件核心方法 (equals, hashCode, toString)
    //================================================================
    @Override
    public String toString() {
        return "AnnouncementEntity{" +
                "announcementId=" + announcementId +
                ", title='" + title + '\'' +
                ", storeId=" + (store != null ? store.getStoreId() : "null") +
                ", status=" + status +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnouncementEntity that = (AnnouncementEntity) o;
        if (announcementId == null || that.announcementId == null) {
            return false;
        }
        return Objects.equals(announcementId, that.announcementId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

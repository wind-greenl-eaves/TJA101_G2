/*
 * ================================================================
 *  門市餐點狀態實體 (StoreMealStatusEntity) - 【修正版】
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/storemeal/model/StoreMealStatusEntity.java
 * - 核心改動: status 欄位改用 SupplyStatus Enum。
 */
package com.eatfast.storemeal.model;

// 【檔案路徑配對】: 引入新建立的共享 Enum
import com.eatfast.common.enums.SupplyStatus;
import com.eatfast.store.model.StoreEntity;
import com.eatfast.meal.model.MealEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "store_meal_status",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_store_meal", columnNames = {"store_id", "meal_id"})
    }
)
public class StoreMealStatusEntity {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sms_id")
    private Long smsId;

    // 【架構修正】: status 欄位改用 SupplyStatus Enum
    // [不可變動的關鍵字/語法]: @NotNull, @Enumerated, EnumType.ORDINAL, @Column
    // 說明: @Enumerated(EnumType.ORDINAL) 會將 Enum 的順序(0, 1)存入資料庫，對應 TINYINT，效能最佳。
    @NotNull(message = "供應狀態不可為空")
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false)
    private SupplyStatus status;

    @UpdateTimestamp
    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = false)
    private MealEntity meal;

    // Constructors, Getters/Setters, equals/hashCode ... (部分 Getters/Setters 型別已更新)
    public StoreMealStatusEntity() {}
    public Long getSmsId() { return smsId; }
    public void setSmsId(Long smsId) { this.smsId = smsId; }
    public SupplyStatus getStatus() { return status; } // 型別已更新
    public void setStatus(SupplyStatus status) { this.status = status; } // 型別已更新
    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
    public StoreEntity getStore() { return store; }
    public void setStore(StoreEntity store) { this.store = store; }
    public MealEntity getMeal() { return meal; }
    public void setMeal(MealEntity meal) { this.meal = meal; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoreMealStatusEntity that = (StoreMealStatusEntity) o;
        return Objects.equals(smsId, that.smsId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(smsId);
    }
}
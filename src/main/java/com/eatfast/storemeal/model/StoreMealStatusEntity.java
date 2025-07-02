package com.eatfast.storemeal.model;

import com.eatfast.common.enums.MealSupplyStatus;
import com.eatfast.store.model.StoreEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 門市餐點供應狀態實體類
 * 用於記錄每個門市中各餐點的供應狀態
 */
@Entity
@Table(name = "store_meal_status")
public class StoreMealStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    private Long statusId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @Column(name = "meal_id", nullable = false)
    private Long mealId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MealSupplyStatus status;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // 建構子
    public StoreMealStatusEntity() {
    }

    public StoreMealStatusEntity(StoreEntity store, Long mealId, MealSupplyStatus status) {
        this.store = store;
        this.mealId = mealId;
        this.status = status;
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getStatusId() {
        return statusId;
    }

    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }

    public StoreEntity getStore() {
        return store;
    }

    public void setStore(StoreEntity store) {
        this.store = store;
    }

    public Long getMealId() {
        return mealId;
    }

    public void setMealId(Long mealId) {
        this.mealId = mealId;
    }

    public MealSupplyStatus getStatus() {
        return status;
    }

    public void setStatus(MealSupplyStatus status) {
        this.status = status;
        this.lastUpdated = LocalDateTime.now(); // 更新狀態時自動更新時間戳
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "StoreMealStatusEntity{" +
                "statusId=" + statusId +
                ", storeId=" + (store != null ? store.getStoreId() : null) +
                ", mealId=" + mealId +
                ", status=" + status +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
/*
 * ================================================================
 * 門市實體 (StoreEntity) - 【架構修正版】
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/store/model/StoreEntity.java
 * - 作用: 門市資料表的核心物件映射。
 */
package com.eatfast.store.model;

// 【檔案路徑配對 - 新增】: 引入新建立的共享 Enum
import com.eatfast.common.enums.StoreStatus;
import com.eatfast.common.enums.StoreType;
// 【檔案路徑配對】: 引入所有相關的子實體
import com.eatfast.employee.model.EmployeeEntity;
import com.eatfast.orderlist.model.OrderListEntity;
import com.eatfast.announcement.model.AnnouncementEntity;
import com.eatfast.cart.model.CartEntity;
import com.eatfast.feedback.model.FeedbackEntity;
import com.eatfast.storemeal.model.StoreMealEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 門市實體 (Store Entity) - 【架構修正版】
 * <p>
 * 核心修正:
 * 1.  **狀態 Enum 化**: `storeStatus` 欄位改用 `StoreStatus` Enum，提升型別安全。
 * 2.  **命名一致性**: `orders` 集合變數更名為 `orderLists`，使其與 `OrderListEntity` 型別對應。
 * </p>
 */
 
@Entity
@Table(name = "store")
public class StoreEntity {

    // ================================================================
    // 欄位定義 (Fields)
    // ================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long storeId;
    
    @Enumerated(EnumType.STRING) // 建議將 Enum 以字串形式存入資料庫，可讀性較高
    @Column(name = "store_type", nullable = false)
    private StoreType storeType = StoreType.BRANCH;

    @NotBlank(message = "門市名稱不可為空")
    @Size(max = 10, message = "門市名稱長度不可超過 10 個字元")
    @Column(name = "store_name", nullable = false, length = 10)
    private String storeName;

    @NotBlank(message = "門市地址不可為空")
    @Size(max = 50, message = "門市地址長度不可超過 50 個字元")
    @Column(name = "store_loc", nullable = false, length = 50)
    private String storeLoc;

    @NotBlank(message = "門市電話不可為空")
    @Size(max = 10, message = "門市電話長度不可超過 10 個字元")
    @Column(name = "store_phone", nullable = false, length = 10)
    private String storePhone;

    @NotBlank(message = "門市營業時間不可為空")
    @Size(max = 50, message = "營業時間描述長度不可超過 50 個字元")
    @Column(name = "store_time", nullable = false, length = 50)
    private String storeTime;

    // 【架構修正 1】: storeStatus 欄位改用 Enum
    // [不可變動的關鍵字/語法]: @NotNull, @Enumerated, EnumType.STRING, @Column
    // 說明: @Enumerated(EnumType.STRING) 會讓 JPA 將 Enum 的名稱 (如 "OPERATING") 直接存成字串到資料庫，可讀性最高。
    @NotNull(message = "門市狀態不可為空")
    @Enumerated(EnumType.STRING)
    @Column(name = "store_status", nullable = false, length = 15)
    private StoreStatus storeStatus;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    // ================================================================
    // 反向一對多關聯 (Bidirectional @OneToMany)
    // ================================================================
    
    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    private Set<EmployeeEntity> employees = new HashSet<>();
    
    // 【命名一致性修正】: `orders` -> `orderLists`
    // [可自定義名稱]: orderLists
    // 說明: 將變數名稱修正為 orderLists，使其與集合內的 OrderListEntity 型別在語意上更為匹配。
    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    private Set<OrderListEntity> orderLists = new HashSet<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<AnnouncementEntity> announcements = new HashSet<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<FeedbackEntity> feedback = new HashSet<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<CartEntity> cartItems = new HashSet<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<StoreMealEntity> mealStatuses = new HashSet<>();

    // ================================================================
    // 建構子、Getters、Setters
    // ================================================================
    public StoreEntity() {}

    // ... 原有 Getter / Setter ...
    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getStoreLoc() { return storeLoc; }
    public void setStoreLoc(String storeLoc) { this.storeLoc = storeLoc; }
    public String getStorePhone() { return storePhone; }
    public void setStorePhone(String storePhone) { this.storePhone = storePhone; }
    public String getStoreTime() { return storeTime; }
    public void setStoreTime(String storeTime) { this.storeTime = storeTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    // Getter/Setter 型別更新為 StoreStatus
    public StoreStatus getStoreStatus() { return storeStatus; }
    public void setStoreStatus(StoreStatus storeStatus) { this.storeStatus = storeStatus; }

    // 【命名一致性修正】: `getOrders` -> `getOrderLists` & `setOrders` -> `setOrderLists`
    public Set<OrderListEntity> getOrderLists() { return orderLists; }
    public void setOrderLists(Set<OrderListEntity> orderLists) { this.orderLists = orderLists; }
    
    public Set<EmployeeEntity> getEmployees() { return employees; }
    public void setEmployees(Set<EmployeeEntity> employees) { this.employees = employees; }
    public Set<AnnouncementEntity> getAnnouncements() { return announcements; }
    public void setAnnouncements(Set<AnnouncementEntity> announcements) { this.announcements = announcements; }
    public Set<FeedbackEntity> getFeedback() { return feedback; }
    public void setFeedback(Set<FeedbackEntity> feedback) { this.feedback = feedback; }
    public Set<CartEntity> getCartItems() { return cartItems; }
    public void setCartItems(Set<CartEntity> cartItems) { this.cartItems = cartItems; }
    public Set<StoreMealEntity> getMealStatuses() { return mealStatuses; }
    public void setMealStatuses(Set<StoreMealEntity> mealStatuses) { this.mealStatuses = mealStatuses; }

    // ================================================================
    // 物件核心方法 (equals, hashCode, toString)
    // ================================================================
    @Override
    public String toString() {
        return "StoreEntity{" +
                "storeId=" + storeId +
                ", storeName='" + storeName + '\'' +
                ", employeeCount=" + (employees != null ? employees.size() : 0) +
                ", orderCount=" + (orderLists != null ? orderLists.size() : 0) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoreEntity that = (StoreEntity) o;
        return Objects.equals(storeId, that.storeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId);
    }
}
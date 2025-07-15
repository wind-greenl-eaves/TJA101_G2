package com.eatfast.orderlistinfo.model; // 【修正】: 依照指示更新 package 路徑

// 【檔案路徑配對】: 為了建立多對一關聯，需要 import 父實體
import com.eatfast.orderlist.model.OrderListEntity;
import com.eatfast.meal.model.MealEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * 訂單明細實體 (Order List Info Entity) - 【路徑已修正】
 * <p>
 * 此實體對應資料庫中的 `order_list_info` 表，詳細記錄了每一筆訂單中購買的各個餐點資訊。
 * 它是構成一張完整訂單的原子單位。
 * </p>
 */
@Entity
@Table(name = "order_list_info")
public class OrderListInfoEntity {

    //================================================================
    // 							欄位定義 (Fields)
    //================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_list_info_id")
    private Long orderListInfoId;

    /**
     * 價格快照 (Price Snapshot)。
     * 此欄位儲存的是下訂單「當下」的餐點單價，而非參考 MealEntity 的當前價格。
     * 這是確保歷史訂單金額正確性的關鍵設計。
     */
    @NotNull(message = "下訂單價不可為空")
    @Min(value = 0, message = "單價不可為負數")
    @Column(name = "meal_price", nullable = false)
    private Long mealPrice;

    @NotNull(message = "評論星數不可為空")
    @Column(name = "review_stars", nullable = false)
    private Long reviewStars;

    @NotNull(message = "訂購數量不可為空")
    @Min(value = 1, message = "數量至少為 1")
    @Column(name = "quantity", nullable = false)
    private Long quantity;


    //================================================================
    // 				關聯的擁有方 (Owning Side of Relationship)
    //================================================================

    /**
     * 此明細所屬的訂單主表 (多對一)。
     * 1. @ManyToOne: (不可變關鍵字) 宣告這是一個多對一關聯 (多筆明細 -> 一張訂單)。
     * 2. fetch = FetchType.LAZY: (不可變關鍵字) **效能關鍵**。
     * 3. @JoinColumn: (不可變關鍵字) 指定外鍵欄位。
     * 4. name = "order_list_id": (不可變動) 必須與資料庫中的外鍵欄位名完全匹配。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_list_id", nullable = false)
    private OrderListEntity orderList;

    /**
     * 此明細訂購的餐點 (多對一)。
     * 注意: 此處的關聯在資料庫層級為 `ON DELETE RESTRICT`，
     * 因此絕不能設定 `CascadeType.REMOVE`，以保護歷史訂單的完整性。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = false)
    private MealEntity meal;


    //================================================================
    //					 建構子、Getters、Setters
    //================================================================
    public OrderListInfoEntity() {}

    public Long getOrderListInfoId() {
        return orderListInfoId;
    }

    public void setOrderListInfoId(Long orderListInfoId) {
        this.orderListInfoId = orderListInfoId;
    }

    public Long getMealPrice() {
        return mealPrice;
    }

    public void setMealPrice(Long mealPrice) {
        this.mealPrice = mealPrice;
    }

    public Long getReviewStars() {
        return reviewStars;
    }

    public void setReviewStars(Long reviewStars) {
        this.reviewStars = reviewStars;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public OrderListEntity getOrderList() {
        return orderList;
    }

    public void setOrderList(OrderListEntity orderList) {
        this.orderList = orderList;
    }

    public MealEntity getMeal() {
        return meal;
    }

    public void setMeal(MealEntity meal) {
        this.meal = meal;
    }

    //================================================================
    // 物件核心方法 (equals, hashCode, toString)
    //================================================================
    @Override
    public String toString() {
        return "OrderListInfoEntity{" +
                "orderListInfoId=" + orderListInfoId +
                ", orderListId='" + (orderList != null ? orderList.getOrderListId() : "null") + '\'' +
                ", mealId=" + (meal != null ? meal.getMealId() : "null") +
                ", quantity=" + quantity +
                ", mealPrice=" + mealPrice +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderListInfoEntity that = (OrderListInfoEntity) o;
        if (orderListInfoId == null || that.orderListInfoId == null) {
            return false;
        }
        return Objects.equals(orderListInfoId, that.orderListInfoId);
    }

    @Override
    public int hashCode() {
        return orderListInfoId != null ? Objects.hash(orderListInfoId) : super.hashCode();
    }
}
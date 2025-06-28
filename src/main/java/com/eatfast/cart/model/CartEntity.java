package com.eatfast.cart.model; // 假設的 package 路徑

// 【檔案路徑配對】: 為了建立多對一關聯，需要 import 父實體
import com.eatfast.member.model.MemberEntity;
import com.eatfast.meal.model.MealEntity;
import com.eatfast.store.model.StoreEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 購物車實體 (Cart Entity)
 * <p>
 * 此實體對應資料庫中的 `cart` 暫存表。每一筆紀錄都代表一個使用者在特定門市
 * 將特定餐點加入購物車的行為。
 * </p>
 */
@Entity
@Table(name = "cart")
public class CartEntity {

    //================================================================
    // 							欄位定義 (Fields)
    //================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;

    /**
     * 訂購數量。
     * 使用 Long 型別以精準對應資料庫的 BIGINT。
     */
    @NotNull(message = "數量不可為空")
    @Min(value = 1, message = "數量至少為 1")
    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Size(max = 255, message = "客製化備註長度不可超過 255 字元")
    @Column(name = "meal_customization", length = 255)
    private String mealCustomization;

    /**
     * 加入購物車的時間。
     * 1. @CreationTimestamp: (不可變關鍵字) Hibernate 提供的註解。當此實體被新增至資料庫時，
     * 會自動將當前時間戳寫入此欄位。
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    //================================================================
    // 				關聯的擁有方 (Owning Side of Relationship)
    //================================================================

    /**
     * 所屬的會員 (多對一)。
     * 1. @ManyToOne: (不可變關鍵字) 宣告這是一個多對一關聯 (多筆購物車紀錄 -> 一位會員)。
     * 2. fetch = FetchType.LAZY: (不可變關鍵字) **效能關鍵**。
     * 3. @JoinColumn: (不可變關鍵字) 指定外鍵欄位。
     * 4. name = "member_id": (不可變動) 必須與資料庫中的外鍵欄位名完全匹配。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    /**
     * 選擇的餐點 (多對一)。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = false)
    private MealEntity meal;

    /**
     * 選擇的門市 (多對一)。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;


    //================================================================
    //					 建構子、Getters、Setters
    //================================================================
    public CartEntity() {}

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public String getMealCustomization() {
        return mealCustomization;
    }

    public void setMealCustomization(String mealCustomization) {
        this.mealCustomization = mealCustomization;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public MemberEntity getMember() {
        return member;
    }

    public void setMember(MemberEntity member) {
        this.member = member;
    }

    public MealEntity getMeal() {
        return meal;
    }

    public void setMeal(MealEntity meal) {
        this.meal = meal;
    }

    public StoreEntity getStore() {
        return store;
    }

    public void setStore(StoreEntity store) {
        this.store = store;
    }

    //================================================================
    // 物件核心方法 (equals, hashCode, toString)
    //================================================================
    @Override
    public String toString() {
        // 為避免 LAZY loading 問題，關聯物件只印出其 ID
        return "CartEntity{" +
                "cartId=" + cartId +
                ", memberId=" + (member != null ? member.getMemberId() : "null") +
                ", mealId=" + (meal != null ? meal.getMealId() : "null") +
                ", storeId=" + (store != null ? store.getStoreId() : "null") +
                ", quantity=" + quantity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartEntity that = (CartEntity) o;
        if (cartId == null || that.cartId == null) {
            return false;
        }
        return Objects.equals(cartId, that.cartId);
    }

    @Override
    public int hashCode() {
        return cartId != null ? Objects.hash(cartId) : super.hashCode();
    }
}

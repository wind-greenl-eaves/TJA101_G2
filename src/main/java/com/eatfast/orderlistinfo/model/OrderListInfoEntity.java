package com.eatfast.orderlistinfo.model;

import com.eatfast.mealtype.model.MealTypeEntity; // 假設 MealEntity 的路徑
import com.eatfast.orderlist.model.OrderListEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 訂單明細資料表 Entity。
 * 與訂單主表為一對多關係。
 */
@Entity
@Table(name = "order_list_info")
public class OrderListInfoEntity {

    /**
     * 訂單明細編號 (主鍵，自動增長)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 📌【新重點】對應 MySQL 的 AUTO_INCREMENT
    @Column(name = "order_list_info_id")
    private Long orderListInfoId;

    /**
     * 下訂當下的餐點單價 (價格快照)
     */
    @Column(name = "meal_price", nullable = false)
    private Long mealPrice;

    /**
     * 此餐點在此次消費的評論星數 (預設0)
     */
    @Column(name = "review_stars", nullable = false)
    private Long reviewStars;

    /**
     * 訂購数量
     */
    @Column(name = "quantity", nullable = false)
    private Long quantity;

    /**
     * 餐點客製化備註 (例如: 少冰、不加蔥)
     */
    @Column(name = "meal_customization", length = 255)
    private String mealCustomization;

    // --- 以下為外鍵關聯 ---

    /**
     * 此明細所屬的訂單。
     * 這是多對一 (Many-to-One) 的關聯，多筆訂單明細會對應到一張訂單主表。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_list_id", nullable = false)
    private OrderListEntity orderList; // 直接關聯到 OrderListEntity

    /**
     * 訂購的餐點。
     * 這是多對一 (Many-to-One) 的關聯，多筆訂單明細可能都訂了同一種餐點。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = false)
    private MealTypeEntity meal; // 假設您有 MealEntity

    // --- Constructors ---
    public OrderListInfoEntity() {
    }

    // --- Getters and Setters ---
    // (此處省略所有欄位的 Getter/Setter，您可以用 IDE 自動生成)

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

    public String getMealCustomization() {
        return mealCustomization;
    }

    public void setMealCustomization(String mealCustomization) {
        this.mealCustomization = mealCustomization;
    }

    public OrderListEntity getOrderList() {
        return orderList;
    }

    public void setOrderList(OrderListEntity orderList) {
        this.orderList = orderList;
    }

    public MealTypeEntity getMeal() {
        return meal;
    }

    public void setMeal(MealTypeEntity meal) {
        this.meal = meal;
    }
}
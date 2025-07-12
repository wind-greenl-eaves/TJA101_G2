/*
 * ================================================================
 * 檔案 2: OrderListEntity.java (★★ 核心修正 ★★)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/orderlist/model/OrderListEntity.java
 * - 核心改動:
 * 1. 移除內部的 Enum 定義。
 * 2. 欄位 orderStatus 的型別直接引用公開的 OrderStatus Enum。
 */
package com.eatfast.orderlist.model;

import com.eatfast.member.model.MemberEntity;
import com.eatfast.orderlistinfo.model.OrderListInfoEntity;
import com.eatfast.store.model.StoreEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "order_list")
public class OrderListEntity {

    @Id
    @Column(name = "order_list_id", length = 20)
    private String orderListId;

    @Column(name = "order_amount", nullable = false)
    private Long orderAmount;

    @CreationTimestamp
    @Column(name = "order_date", nullable = false, updatable = false)
    private LocalDateTime orderDate;

    //【關鍵修正】: 此處的 OrderStatus 現在引用的是公開的 public enum OrderStatus。
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "meal_pickup_number", nullable = false)
    private Long mealPickupNumber;

    @Column(name = "card_number", nullable = false, length = 20)
    private String cardNumber;

    // 【新增】取餐時間欄位
    @Column(name = "pickup_time")
    private LocalDateTime pickupTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @OneToMany(mappedBy = "orderList", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<OrderListInfoEntity> orderListInfos = new HashSet<>();

    // Constructors, Getters, Setters, etc. (維持不變)
    public OrderListEntity() {}
    public String getOrderListId() { return orderListId; }
    public void setOrderListId(String orderListId) { this.orderListId = orderListId; }
    public Long getOrderAmount() { return orderAmount; }
    public void setOrderAmount(Long orderAmount) { this.orderAmount = orderAmount; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }
    public Long getMealPickupNumber() { return mealPickupNumber; }
    public void setMealPickupNumber(Long mealPickupNumber) { this.mealPickupNumber = mealPickupNumber; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public MemberEntity getMember() { return member; }
    public void setMember(MemberEntity member) { this.member = member; }
    public StoreEntity getStore() { return store; }
    public void setStore(StoreEntity store) { this.store = store; }
    public Set<OrderListInfoEntity> getOrderListInfos() { return orderListInfos; }
    public void setOrderListInfos(Set<OrderListInfoEntity> orderListInfos) { this.orderListInfos = orderListInfos; }
    // 【新增】pickupTime 的 getter 和 setter 方法
    public LocalDateTime getPickupTime() { 
        return pickupTime; 
    }
    
    public void setPickupTime(LocalDateTime pickupTime) { 
        this.pickupTime = pickupTime; 
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderListEntity that = (OrderListEntity) o;
        return Objects.equals(orderListId, that.orderListId);
    }
    @Override
    public int hashCode() { return Objects.hash(orderListId); }
}
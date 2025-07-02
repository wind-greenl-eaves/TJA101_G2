package com.eatfast.meal.model;

// 引入共享 Enum 與所有關聯實體
import com.eatfast.common.enums.MealStatus;
import com.eatfast.mealtype.model.MealTypeEntity;
import com.eatfast.fav.model.FavEntity;
import com.eatfast.cart.model.CartEntity;
import com.eatfast.orderlistinfo.model.OrderListInfoEntity;
import com.eatfast.storemeal.model.StoreMealEntity;

// 引入 Jakarta Validation API 進行資料驗證
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/*
 * ================================================================
 * MealEntity.java (收藏關聯重構定版)
 * ================================================================
 * - 審查結論:
 * 1. 【關聯重構】: 已移除與 MemberEntity 的 @ManyToMany 關聯，並正確建立對
 * FavEntity 的 @OneToMany 反向關聯，符合團隊決議。
 * 2. 【邏輯校正】:
 * - `storeStatuses`, `orderListInfos`, `cartItems`: 正確設置為反向關聯的
 * 被控方(mappedBy)，不負責管理關聯關係。
 * - `favorites`: 正確配置級聯刪除，當餐點被(硬)刪除時，相關的收藏紀錄應一併清除。
 * 3. 【結構完整】:
 * - 已加入 Jakarta Validation API 進行欄位約束，強化資料完整性。
 * - 所有 JPA 與 Hibernate 註解均已正確配置。
 */
/**
 * 餐點實體 (Meal Entity)
 * <p>
 * 核心功能:
 * - 映射資料庫 `meal` 表。
 * - 定義與餐點種類的多對一關聯。
 * - 定義與收藏、購物車、訂單明細等的一對多反向關聯。
 */
@Entity
@Table(name = "meal")
public class MealEntity {

    //================================================================
    // 							欄位定義
    //================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meal_id")
    private Long mealId;

    @NotBlank(message = "餐點名稱不可為空")
    @Size(max = 50, message = "餐點名稱長度不可超過 50 個字元")
    @Column(name = "meal_name", nullable = false, length = 50)
    private String mealName;
    
    @Lob // 標示為大型物件欄位
    @Column(name = "meal_pic")
    private byte[] mealPic;

    @NotNull(message = "餐點單價不可為空")
    @Min(value = 0, message = "餐點單價不可為負數")
    @Column(name = "meal_price", nullable = false)
    private Long mealPrice;
    
    @NotNull(message = "評價總星數不可為空")
    @Min(value = 0, message = "評價星數不可低於 0")
    @Max(value = 5, message = "評價星數不可高於 5")
    @Column(name = "review_total_stars", nullable = false)
    private Long reviewTotalStars;

    @NotNull(message = "餐點狀態不可為空")
    @Enumerated(EnumType.ORDINAL) // 遵照團隊決議，使用 ORDINAL
    @Column(name = "status", nullable = false)
    private MealStatus status;

    //================================================================
    // 						  主要關聯 (擁有方)
    //================================================================

    /**
     * 此餐點所屬的種類 (多對一)。
     */
    @NotNull(message = "餐點種類不可為空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_type_id", nullable = false)
    private MealTypeEntity mealType;
    
    // ================================================================
    //         反向一對多關聯 (Bidirectional @OneToMany)
    // ================================================================

    /**
     * 此餐點在各門市的供應狀態。
     */
    @OneToMany(mappedBy = "meal", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<StoreMealEntity> storeStatuses = new HashSet<>();

    /**
     * 包含此餐點的所有訂單明細。
     * 不設定級聯刪除，因為訂單明細的生命週期由訂單主表(OrderList)控制。
     */
    @OneToMany(mappedBy = "meal", fetch = FetchType.LAZY)
    private Set<OrderListInfoEntity> orderListInfos = new HashSet<>();
    
    /**
     * 包含此餐點的所有購物車項目。
     */
    @OneToMany(mappedBy = "meal", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<CartEntity> cartItems = new HashSet<>();

    /**
     * 收藏此餐點的所有紀錄。
     * 級聯關係設為 ALL，刪除餐點時將一併清除其被收藏的紀錄。
     */
    @OneToMany(mappedBy = "meal", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<FavEntity> favorites = new HashSet<>();

    //================================================================
    //					 建構子、Getters、Setters
    //================================================================
    public MealEntity() {}
    
    public Long getMealId() { return mealId; }
    public void setMealId(Long mealId) { this.mealId = mealId; }
    public String getMealName() { return mealName; }
    public void setMealName(String mealName) { this.mealName = mealName; }
    public byte[] getMealPic() { return mealPic; }
    public void setMealPic(byte[] mealPic) { this.mealPic = mealPic; }
    public Long getMealPrice() { return mealPrice; }
    public void setMealPrice(Long mealPrice) { this.mealPrice = mealPrice; }
    public Long getReviewTotalStars() { return reviewTotalStars; }
    public void setReviewTotalStars(Long reviewTotalStars) { this.reviewTotalStars = reviewTotalStars; }
    public MealStatus getStatus() { return status; }
    public void setStatus(MealStatus status) { this.status = status; }
    public MealTypeEntity getMealType() { return mealType; }
    public void setMealType(MealTypeEntity mealType) { this.mealType = mealType; }
    
    public Set<StoreMealEntity> getStoreStatuses() { return storeStatuses; }
    public void setStoreStatuses(Set<StoreMealEntity> storeStatuses) { this.storeStatuses = storeStatuses; }
    public Set<OrderListInfoEntity> getOrderListInfos() { return orderListInfos; }
    public void setOrderListInfos(Set<OrderListInfoEntity> orderListInfos) { this.orderListInfos = orderListInfos; }
    public Set<CartEntity> getCartItems() { return cartItems; }
    public void setCartItems(Set<CartEntity> cartItems) { this.cartItems = cartItems; }
    
    // 【審查修正】: 修正 Getter/Setter，使其與 `favorites` 屬性 (Set<FavEntity>) 完全匹配
    public Set<FavEntity> getFavorites() { return favorites; }
    public void setFavorites(Set<FavEntity> favorites) { this.favorites = favorites; }
    
    //================================================================
    // 物件核心方法 (equals, hashCode, toString)
    //================================================================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MealEntity that = (MealEntity) o;
        if (this.mealId == null || that.mealId == null) {
            return false;
        }
        return Objects.equals(mealId, that.mealId);
    }

    @Override
    public int hashCode() {
        return mealId != null ? Objects.hash(mealId) : getClass().hashCode();
    }
}

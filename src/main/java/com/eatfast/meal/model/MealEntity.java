package com.eatfast.meal.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.eatfast.cart.model.CartEntity;
// 引入共享 Enum 與所有關聯實體
import com.eatfast.common.enums.MealStatus;
import com.eatfast.fav.model.FavEntity;
import com.eatfast.mealtype.model.MealTypeEntity;
import com.eatfast.orderlistinfo.model.OrderListInfoEntity;
import com.eatfast.storemeal.model.StoreMealEntity;

// 引入 Jakarta Validation API 進行資料驗證
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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

    // ================================================================
    //                              欄位定義
    // ================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meal_id")
    private Long mealId; // 餐點唯一識別碼

    @NotBlank(message = "餐點名稱不可為空")
    @Size(max = 50, message = "餐點名稱長度不可超過 50 個字元")
    @Column(name = "meal_name", nullable = false, length = 50)
    private String mealName; // 餐點名稱

    
    @Lob // 標示為大型物件欄位，適用於儲存圖片等二進位資料
    @Column(name = "meal_pic")
    private byte[] mealPic; // 餐點圖片，以位元組陣列儲存

    @NotNull(message = "餐點單價不可為空")
    @Min(value = 1, message = "餐點價格不可小於{value}")
    @Column(name = "meal_price", nullable = false)
    private Long mealPrice; // 餐點單價

    @NotNull(message = "評價總星數不可為空")
    @Min(value = 0, message = "評價星數不可低於 0")
    @Max(value = 5, message = "評價星數不可高於 5")
    @Column(name = "review_total_stars", nullable = false)
    private Long reviewTotalStars; // 顧客評價總星數

    @NotNull(message = "餐點狀態不可為空")
    @Enumerated(EnumType.ORDINAL) // 遵照團隊決議，將 Enum 以其序號存儲 (0, 1, ...)
    @Column(name = "status", nullable = false)
    private MealStatus status; // 餐點狀態 (例如：上架、下架)，使用 Enum 提升可讀性與型別安全
    
    @Transient
    private MultipartFile mealPicFile;

    // ================================================================
    //                          主要關聯 (擁有方)
    // ================================================================

    /**
     * 此餐點所屬的種類 (多對一關聯)。
     * MealEntity 是關係的擁有方，通過 meal_type_id 欄位連接 MealTypeEntity。
     */
    @NotNull(message = "餐點種類不可為空")
    @ManyToOne(fetch = FetchType.LAZY) // 延遲加載，僅在需要時才加載餐點種類資訊
    @JoinColumn(name = "meal_type_id", nullable = false)
    private MealTypeEntity mealType; // 餐點所屬的種類實體

    // ================================================================
    //                  反向一對多關聯 (Bidirectional @OneToMany)
    // ================================================================

    /**
     * 此餐點在各門市的供應狀態集合。
     * mappedBy 指向 StoreMealEntity 中維護關聯的屬性 "meal"。
     * CascadeType.ALL 表示對 MealEntity 的操作會級聯到 StoreMealEntity。
     * orphanRemoval = true 表示如果一個 StoreMealEntity 從此集合中移除，它將從資料庫中刪除。
     */
    @OneToMany(mappedBy = "meal", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<StoreMealEntity> storeStatuses = new HashSet<>(); // 門市餐點供應狀態集合

    /**
     * 包含此餐點的所有訂單明細集合。
     * 不設定級聯刪除，因為訂單明細的生命週期由訂單主表(OrderList)控制。
     */
    @OneToMany(mappedBy = "meal", fetch = FetchType.LAZY)
    private Set<OrderListInfoEntity> orderListInfos = new HashSet<>(); // 訂單明細集合

    /**
     * 包含此餐點的所有購物車項目集合。
     * CascadeType.ALL 和 orphanRemoval = true 確保購物車項目隨餐點的刪除而刪除。
     */
    @OneToMany(mappedBy = "meal", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<CartEntity> cartItems = new HashSet<>(); // 購物車項目集合

    /**
     * 收藏此餐點的所有紀錄集合。
     * 級聯關係設為 ALL，當餐點被(硬)刪除時，相關的收藏紀錄應一併清除。
     */
    @OneToMany(mappedBy = "meal", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<FavEntity> favorites = new HashSet<>(); // 收藏紀錄集合

    // ================================================================
    //                       建構子、Getters、Setters
    // ================================================================

    /**
     * 無參數建構子，JPA 規範要求必須提供。
     */
    public MealEntity() {}

    // Meal ID 的 Getter 和 Setter
    public Long getMealId() {
        return mealId;
    }

    public void setMealId(Long mealId) {
        this.mealId = mealId;
    }

    // Meal Name 的 Getter 和 Setter
    public String getMealName() {
        return mealName;
    }

    public void setMealName(String mealName) {
        this.mealName = mealName;
    }

    // Meal Picture 的 Getter 和 Setter
    public byte[] getMealPic() {
        return mealPic;
    }

    public void setMealPic(byte[] mealPic) {
        this.mealPic = mealPic;
    }
    
    // Meal Picture File 的 Getter 和 Setter
    public MultipartFile getMealPicFile() {
        return mealPicFile;
    }

    public void setMealPicFile(MultipartFile mealPicFile) {
        this.mealPicFile = mealPicFile;
    }

    // Meal Price 的 Getter 和 Setter
    public Long getMealPrice() {
        return mealPrice;
    }

    public void setMealPrice(Long mealPrice) {
        this.mealPrice = mealPrice;
    }

    // Review Total Stars 的 Getter 和 Setter
    public Long getReviewTotalStars() {
        return reviewTotalStars;
    }

    public void setReviewTotalStars(Long reviewTotalStars) {
        this.reviewTotalStars = reviewTotalStars;
    }

    // Status 的 Getter 和 Setter
    public MealStatus getStatus() {
        return status;
    }

    public void setStatus(MealStatus status) {
        this.status = status;
    }

    // Meal Type (關聯實體) 的 Getter 和 Setter
    public MealTypeEntity getMealType() {
        return mealType;
    }

    public void setMealType(MealTypeEntity mealType) {
        this.mealType = mealType;
    }

    // Store Statuses (反向關聯集合) 的 Getter 和 Setter
    public Set<StoreMealEntity> getStoreStatuses() {
        return storeStatuses;
    }

    public void setStoreStatuses(Set<StoreMealEntity> storeStatuses) {
        this.storeStatuses = storeStatuses;
    }

    // Order List Infos (反向關聯集合) 的 Getter 和 Setter
    public Set<OrderListInfoEntity> getOrderListInfos() {
        return orderListInfos;
    }

    public void setOrderListInfos(Set<OrderListInfoEntity> orderListInfos) {
        this.orderListInfos = orderListInfos;
    }

    // Cart Items (反向關聯集合) 的 Getter 和 Setter
    public Set<CartEntity> getCartItems() {
        return cartItems;
    }

    public void setCartItems(Set<CartEntity> cartItems) {
        this.cartItems = cartItems;
    }

    // Favorites (反向關聯集合) 的 Getter 和 Setter
    public Set<FavEntity> getFavorites() {
        return favorites;
    }

    public void setFavorites(Set<FavEntity> favorites) {
        this.favorites = favorites;
    }

    // ================================================================
    //                       物件核心方法 (equals, hashCode)
    // ================================================================

    /**
     * 重寫 equals 方法，用於比較兩個 MealEntity 物件是否相等。
     * 基於 mealId 判斷，對於 JPA 實體而言，使用主鍵進行相等性判斷是最佳實踐。
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MealEntity that = (MealEntity) o;
        // 如果 mealId 為 null (表示尚未持久化)，則認為不相等或需要更精確的業務邏輯判斷
        if (this.mealId == null || that.mealId == null) {
            return false;
        }
        return Objects.equals(mealId, that.mealId);
    }
 
    /**
     * 重寫 hashCode 方法，與 equals 方法保持一致性。
     * 返回基於 mealId 的哈希碼，以便在集合 (如 HashSet, HashMap) 中正確儲存和檢索物件。
     */
    @Override
    public int hashCode() {
        // 如果 mealId 不為 null，則使用其哈希碼；否則使用類別的哈希碼 (在尚未持久化時)
        return mealId != null ? Objects.hash(mealId) : getClass().hashCode();
    }
}
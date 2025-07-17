package com.eatfast.mealtype.model;

// 【檔案路徑配對】: 為了建立反向關聯，需要 import 子實體 MealEntity
import com.eatfast.meal.model.MealEntity;

import jakarta.persistence.*; // 【新增】: 為了 @OneToMany 等註解，需要 import 更多 persistence 元件
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.HashSet; // 建議使用 Set 避免重複元素
import java.util.Objects;
import java.util.Set;

/**
 * 餐點種類實體 (Meal Type Entity) - 【已補全雙向關聯】
 * <p>
 * 此實體對應資料庫中的 `meal_type` 資料表，用於定義餐點的分類。
 * 透過 @OneToMany，它現在可以直接導航至其分類下的所有餐點。
 * </p>
 */
@Entity
@Table(name = "meal_type")
public class MealTypeEntity {

    //================================================================
    // 							欄位定義 (Fields)
    //================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meal_type_id")
    private Long mealTypeId;

    @NotBlank(message = "餐點種類名稱不可為空")
    @Pattern(regexp = "^[\u4e00-\u9fa5]+$", message = "餐點種類名稱僅限輸入中文字")
    @Size(min = 1, max = 50, message = "餐點種類名稱長度須為 1 到 50 個字") // 與 DB 同步
    @Column(name = "meal_name", nullable = false, length = 50)
    private String mealName;


    // ================================================================
    //         【新增區塊】反向一對多關聯 (Bidirectional @OneToMany)
    // ================================================================

    /**
     * 此餐點種類下的所有餐點集合。
     * 1. mappedBy = "mealType": (不可變動) 關聯的控制權交給 `MealEntity` 中的 `mealType` 屬性。
     * 2. fetch = FetchType.LAZY: (不可變關鍵字) **效能關鍵**。只有在需要時才載入餐點資料。
     * 3. 注意: 此處**沒有**設定 `cascade` 的移除選項，因為資料庫外鍵 `meal.meal_type_id`
     * 設定為 `ON DELETE RESTRICT`，意即不允許刪除一個底下還有餐點的分類。
     * 相關刪除操作應由業務邏輯層手動管理。
     */
    @OneToMany(mappedBy = "mealType", fetch = FetchType.LAZY)
    private Set<MealEntity> meals = new HashSet<>();


    //================================================================
    //					 建構子 (Constructors)
    //================================================================
    public MealTypeEntity() {}


    //================================================================
    //					 Getters and Setters
    //================================================================

    public Long getMealTypeId() {
        return mealTypeId;
    }
    public void setMealTypeId(Long mealTypeId) {
        this.mealTypeId = mealTypeId;
    }
    public String getMealName() {
        return mealName;
    }
    public void setMealName(String mealName) {
        if (mealName != null) {
            this.mealName = mealName.trim(); // 去頭尾空白
        } else {
            this.mealName = null;
        }
    }


    // 【新增】關聯集合的 Getter / Setter
    public Set<MealEntity> getMeals() {
        return meals;
    }
    public void setMeals(Set<MealEntity> meals) {
        this.meals = meals;
    }


    //================================================================
    // 物件核心方法 (equals, hashCode, toString)
    //================================================================

    @Override
    public String toString() {
        return "MealTypeEntity{" +
                "mealTypeId=" + mealTypeId +
                ", mealName='" + mealName + '\'' +
                ", mealCount=" + (meals != null ? meals.size() : 0) + // 印出數量而非內容
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MealTypeEntity that = (MealTypeEntity) o;
        return Objects.equals(mealTypeId, that.mealTypeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mealTypeId);
    }
}


package com.eatfast.fav.model;

// 引入關聯的父實體
import com.eatfast.member.model.MemberEntity;
import com.eatfast.meal.model.MealEntity;

import jakarta.persistence.*;
import java.util.Objects;


/*
 * ================================================================
 * FavEntity.java (收藏關聯重構定版)
 * ================================================================
 * - 審查結論:
 * 1. 【結構正確】: 此實體作為 Member 與 Meal 之間的獨立中間表，設計健全，
 * 提供了未來擴充(如增加收藏時間)的高度靈活性。
 * 2. 【邏輯嚴密】:
 * - @UniqueConstraint: 正確地防止了重複收藏的髒資料產生。
 * - @ManyToOne: 與 Member 和 Meal 的多對一關聯均已正確配置。
 * 3. 【關聯完整】: 已在 MemberEntity 與 MealEntity 中建立對應的 @OneToMany
 * 反向關聯，形成完整的雙向關係。

/**
 * 我的最愛實體 (Favorite Entity)
 * <p>
 * 核心功能:
 * - 映射資料庫 `fav` 中間表。
 * - 作為 Member 與 Meal 之間多對多關聯的橋樑。
 */
@Entity
@Table(name = "fav",
    uniqueConstraints = {
        // 重點註記: 確保「會員」與「餐點」的組合是唯一的，防止重複收藏。
        @UniqueConstraint(
            name = "uk_member_meal",
            columnNames = {"member_id", "meal_id"}
        )
    }
)
public class FavEntity {

    //================================================================
    // 							欄位定義
    //================================================================

    /**
     * 代理主鍵 (Surrogate Key)，無業務意義，僅供 JPA 使用。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fav_meal_id")
    private Long favMealId;


    //================================================================
    // 				關聯的擁有方 (Owning Side)
    //================================================================

    /**
     * 執行收藏動作的會員 (多對一)。
     * LAZY Fetching: 預設不載入關聯的 Member 物件，提升查詢效能。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    /**
     * 被收藏的餐點 (多對一)。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = false)
    private MealEntity meal;


    //================================================================
    //					 建構子、Getters、Setters
    //================================================================
    public FavEntity() {}

    public Long getFavMealId() {
        return favMealId;
    }

    public void setFavMealId(Long favMealId) {
        this.favMealId = favMealId;
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

    //================================================================
    // 物件核心方法 (equals, hashCode, toString)
    //================================================================
    @Override
    public String toString() {
        return "FavEntity{" +
                "favMealId=" + favMealId +
                ", memberId=" + (member != null ? member.getMemberId() : "null") +
                ", mealId=" + (meal != null ? meal.getMealId() : "null") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FavEntity that = (FavEntity) o;
        // 已持久化的物件，使用ID判斷相等性
        if (favMealId != null && that.favMealId != null) {
            return Objects.equals(favMealId, that.favMealId);
        }
        // 未持久化的物件，它們不相等
        return false;
    }

    @Override
    public int hashCode() {
        // 確保持久化前後的 hashCode 一致
        return getClass().hashCode();
    }
}

/*
 * ================================================================
 * 檔案: CartRepository.java (最終修正版)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/cart/repository/CartRepository.java
 * - 修正總結:
 * 1. 【命名統一】: 所有方法均採用底線 "_" 導航命名法，確保
 * JPA 能正確解析屬性路徑，預防 PropertyReferenceException。
 * 2. 【代碼整理】: 移除了會導致錯誤的舊命名方法，整合了功能
 * 重複的查詢。
 */
package com.eatfast.cart.repository;

import com.eatfast.cart.model.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 購物車資料存取庫 (Repository)
 * <p>
 * 負責處理與 `cart` 資料表相關的所有資料庫操作。
 */
@Repository
public interface CartRepository extends JpaRepository<CartEntity, Long> {

    /**
     * 根據會員主鍵 ID 查找其所有購物車項目。
     * @param memberId 會員的主鍵 ID (Long)。
     * @return 包含該會員所有購物車項目的 List，若無則返回空 List。
     */
    List<CartEntity> findByMember_MemberId(Long memberId);
    
    /**
     * 根據複合鍵（會員ID、門市ID、餐點ID）查找唯一的購物車項目。
     * @param memberId 會員的主鍵 ID (Long)。
     * @param storeId 門市的主鍵 ID (Long)。
     * @param mealId 餐點的主鍵 ID (Long)。
     * @return 一個包含唯一購物車項目的 Optional，若不存在則為 Optional.empty()。
     */
    Optional<CartEntity> findByMember_MemberIdAndStore_StoreIdAndMeal_MealId(Long memberId, Long storeId, Long mealId);
    
    /**
     * 根據會員主鍵 ID 和餐點主鍵 ID 查找唯一的購物車項目。
     * 【注意】: 這個方法假設一個會員對同一個餐點在所有分店中只會有一條購物車紀錄。
     * 如果業務邏輯允許在不同分店加入相同餐點，請使用上面的 findByMember_MemberIdAndStore_StoreIdAndMeal_MealId 方法。
     * @param memberId 會員的主鍵 ID (Long)。
     * @param mealId 餐點的主鍵 ID (Long)。
     * @return 一個包含唯一購物車項目的 Optional，若不存在則為 Optional.empty()。
     */
    Optional<CartEntity> findByMember_MemberIdAndMeal_MealId(Long memberId, Long mealId);
    
    /**
     * 根據會員主鍵 ID 刪除其所有購物車項目。
     * @param memberId 會員的主鍵 ID (Long)。
     */
    @Transactional
    void deleteByMember_MemberId(Long memberId);
    
    /**
     * 根據會員主鍵 ID 和餐點主鍵 ID 刪除購物車項目。
     * @param memberId 會員的主鍵 ID (Long)。
     * @param mealId 餐點的主鍵 ID (Long)。
     */
    @Transactional
    void deleteByMember_MemberIdAndMeal_MealId(Long memberId, Long mealId);
    
    
    /* 【新增方法】
    * 根據會員 ID (Member ID) 計算其在購物車中的商品項目總數。
    * @param memberId 要查詢的會員 ID
    * @return 該會員的購物車商品數量
    */
    //從 Cart 表中計算，當 cart 的 member 欄位的 id 等於我們傳入的 memberId 參數時，總共有幾筆資料
    Long countByMember_MemberId(Long memberId);
}
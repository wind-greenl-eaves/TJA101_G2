/*
 * ================================================================
 * 檔案: CartRepository.java (經嚴格審查與重構)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/cart/repository/CartRepository.java
 * - 重構總結:
 * 1. 【命名修正】: 全面修正所有自定義查詢方法，採用底線 "_" 導航至
 * 關聯實體 (member, meal) 的主鍵屬性 (memberId, mealId)，
 * 以解決 PropertyReferenceException。
 * 2. 【結構強化】: 為所有公開方法添加標準 Javadoc 註解，明確闡述
 * 其用途、參數和回傳值，提升程式碼可維護性。
 * 3. 【邏輯確認】: 保留了 @Transactional 註解於刪除操作，確保資料
 * 一致性。主鍵型別 Long 的使用是正確的。
 */
package com.eatfast.cart.repository;

import com.eatfast.cart.model.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
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
     * <p>
     * 命名慣例: `findByMember_MemberId` 會被 JPA 解析為 JPQL:
     * {@code "SELECT c FROM CartEntity c WHERE c.member.memberId = :memberId"}
     *
     * @param memberId 會員的主鍵 ID (Long)。
     * @return 包含該會員所有購物車項目的 List，若無則返回空 List。
     */
    List<CartEntity> findByMember_MemberId(Long memberId);
    
    /**
     * 根據會員主鍵 ID 和餐點主鍵 ID 查找唯一的購物車項目。
     * <p>
     * 命名慣例: `findByMember_MemberIdAndMeal_MealId` 會被 JPA 解析為
     * 兩個查詢條件的組合。
     *
     * @param memberId 會員的主鍵 ID (Long)。
     * @param mealId 餐點的主鍵 ID (Long)。
     * @return 一個包含唯一購物車項目的 Optional，若不存在則為 Optional.empty()。
     */
    Optional<CartEntity> findByMember_MemberIdAndStore_StoreIdAndMeal_MealId(Long memberId, Long storeId, Long mealId);
    
    /**
     * 根據會員主鍵 ID 刪除其所有購物車項目。
     * <p>
     * 命名慣例: `deleteByMember_MemberId` 同樣會根據正確的屬性路徑生成刪除語句。
     *
     * @param memberId 會員的主鍵 ID (Long)。
     */
    @Transactional
    void deleteByMember_MemberId(Long memberId);
}
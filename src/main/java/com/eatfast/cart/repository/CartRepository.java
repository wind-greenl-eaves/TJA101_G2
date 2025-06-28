/*
 * ================================================================
 * 檔案 1: CartRepository.java (型別已修正)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/cart/repository/CartRepository.java
 */
package com.eatfast.cart.repository;

import com.eatfast.cart.model.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
// 【修正】: 主鍵型別必須為 Long，以匹配 CartEntity 的 @Id 欄位型別。
public interface CartRepository extends JpaRepository<CartEntity, Long> {

    /**
     * 【修正】: 根據會員ID查找其所有購物車項目。
     * - memberId 的型別從 Integer 改為 Long，以匹配 MemberEntity 的主鍵型別。
     */
    List<CartEntity> findByMemberId(Long memberId);
    
    /**
     * 【修正】: 根據會員ID和餐點ID查找唯一的購物車項目。
     * - memberId 和 mealId 的型別都從 Integer 改為 Long。
     */
    Optional<CartEntity> findByMemberIdAndMealId(Long memberId, Long mealId);
    
    /**
     * 【修正】: 根據會員ID刪除其所有購物車項目。
     * - memberId 的型別從 Integer 改為 Long。
     */
    @Transactional
    void deleteByMemberId(Long memberId);
}
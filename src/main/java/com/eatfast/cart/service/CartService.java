/*
 * ================================================================
 * 檔案 2: CartService.java (介面型別已修正)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/cart/service/CartService.java
 */
package com.eatfast.cart.service;

import com.eatfast.cart.model.CartEntity;
import com.eatfast.meal.model.MealEntity; // 為了 DTO 而引入
import com.eatfast.member.model.MemberEntity; // 為了 DTO 而引入

import java.util.List;

public interface CartService {
	
    // 【修正】: 所有與 ID 相關的參數型別都統一為 Long。
    List<CartEntity> getCartByMemberId(Long memberId);

    // 【優化建議】: 未來可以改用 DTO 模式。
    // void addOrUpdateCartItem(CartRequestDTO request);
    void addOrUpdateCartItem(CartEntity cart);

    void clearCart(Long memberId);
    
    Integer calculateTotalAmount(Long memberId);
}

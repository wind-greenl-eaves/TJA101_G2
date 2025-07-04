// src/main/java/com/eatfast/cart/service/CartService.java
package com.eatfast.cart.service;

import com.eatfast.cart.dto.CartDTO.AddToCartRequest;
import com.eatfast.cart.dto.CartDTO.CartItemDto;
import com.eatfast.cart.dto.CartDTO.UpdateCartItemRequest;

import java.util.List;
import java.util.Optional;

public interface CartService {

    CartItemDto addOrUpdateCartItem(AddToCartRequest request);

    List<CartItemDto> getCartItemsByMember(Long memberId);

    // 【新增】基於 keys 更新購物車項目
    CartItemDto updateCartItemByKeys(Long memberId, Long storeId, Long mealId, UpdateCartItemRequest request);

    // 【新增】基於 keys 移除購物車項目
    void removeCartItemByKeys(Long memberId, Long storeId, Long mealId);

    void clearCartByMember(Long memberId);
}
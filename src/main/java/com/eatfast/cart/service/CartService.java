// src/main/java/com/eatfast/cart/service/CartService.java
package com.eatfast.cart.service;

import com.eatfast.cart.dto.CartDTO.AddToCartRequest;
import com.eatfast.cart.dto.CartDTO.CartItemDto;
import com.eatfast.cart.dto.CartDTO.UpdateCartItemRequest;

import java.util.List;

public interface CartService {

    CartItemDto addOrUpdateCartItem(AddToCartRequest request);

    List<CartItemDto> getCartItemsByMember(Long memberId);

    //基於 keys 更新購物車項目
    CartItemDto updateCartItemByKeys(Long memberId, Long storeId, Long mealId, UpdateCartItemRequest request);

    //基於 keys 移除購物車項目
    void removeCartItemByKeys(Long memberId, Long storeId, Long mealId);

    void clearCartByMember(Long memberId);
    
    //基於會員更新購物車客製化備註欄位
    void updateAllCartItemsCustomization(Long memberId, String mealCustomization);
}
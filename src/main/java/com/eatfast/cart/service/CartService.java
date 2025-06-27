package com.eatfast.cart.service;

import java.util.List;

import com.eatfast.cart.model.CartEntity;

public interface CartService {
    List<CartEntity> getCartByMemberId(Integer memberId);
    void addOrUpdateCartItem(CartEntity cart);
    void clearCart(Integer memberId);
    Integer calculateTotalAmount(Integer memberId);
    
}

package com.eatfast.cart.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.eatfast.cart.model.CartEntity;
import com.eatfast.cart.service.CartService;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // 取得購物車清單
    @GetMapping("/get")
    public List<CartEntity> getCart(@RequestParam Integer memberId) {
        return cartService.getCartByMemberId(memberId);
    }

    // 加入或更新購物車項目
    @PostMapping("/add")
    public void addToCart(@RequestBody CartEntity cartItem) {
        cartService.addOrUpdateCartItem(cartItem);
    }

    // 清空購物車
    @DeleteMapping("/clear")
    public void clearCart(@RequestParam Integer memberId) {
    	cartService.clearCart(memberId);
    }
    
    // 計算總金額
    @GetMapping("/total")
    public Integer getTotalAmount(@RequestParam Integer memberId) {
        return cartService.calculateTotalAmount(memberId);
    }

}
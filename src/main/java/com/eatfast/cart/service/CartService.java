// src/main/java/com/eatfast/cart/service/CartService.java
package com.eatfast.cart.service;

import com.eatfast.cart.dto.CartDTO.AddToCartRequest;
import com.eatfast.cart.dto.CartDTO.CartItemDto;
import com.eatfast.cart.dto.CartDTO.UpdateCartItemRequest;

import java.util.List;

public interface CartService {

	CartItemDto addOrUpdateCartItem(AddToCartRequest request);

	List<CartItemDto> getCartItemsByMember(Long memberId);
	
	/* 【新增方法】根據會員ID和餐點ID更新購物車項目的數量。
    * @param memberId 會員 ID
    * @param mealId 餐點 ID
    * @param quantity 新的數量
    * @return 更新後的購物車項目 DTO
    * @throws IllegalArgumentException 如果找不到對應的購物車項目
    */
   CartItemDto updateCartItemQuantity(Long memberId, Long mealId, Long quantity);
   
   /**
    * 【新增方法】根據會員ID和餐點ID移除購物車項目。
    * @param memberId 會員 ID
    * @param mealId 餐點 ID
    */
   void removeCartItemByKeys(Long memberId, Long mealId);
   
	// 基於 keys 更新購物車項目
	CartItemDto updateCartItemByKeys(Long memberId, Long storeId, Long mealId, UpdateCartItemRequest request);

	// 基於 keys 移除購物車項目
	void removeCartItemByKeys(Long memberId, Long storeId, Long mealId);

	void clearCartByMember(Long memberId);

	// 基於會員更新購物車客製化備註欄位
	void updateAllCartItemsCustomization(Long memberId, String mealCustomization);
	

	/**
	 * 根據餐點 ID 獲取餐點資訊，用於在前端組裝購物車項目。 這是一個唯讀操作，不會修改購物車內容。
	 * 
	 * @param mealId 餐點 ID
	 * @return 包含餐點價格與圖片資訊的 DTO
	 */
	CartItemDto prepareItemForCart(Long mealId);

}
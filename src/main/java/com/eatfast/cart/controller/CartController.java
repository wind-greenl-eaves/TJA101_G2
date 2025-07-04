package com.eatfast.cart.controller;

// ... (其他 import) ...
import com.eatfast.cart.dto.CartDTO.AddToCartRequest; // 確保已引入
import com.eatfast.cart.dto.CartDTO.CartItemDto;       // 確保已引入
import com.eatfast.cart.dto.CartDTO.UpdateCartItemRequest; // 確保已引入

import com.eatfast.cart.service.CartService; // 確保已引入

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // ... (其他 API 方法，如 addOrUpdateCartItem, getCartItemsByMember) ...

    /**
     * 更新購物車中特定會員、門市、餐點的數量或備註。
     * PUT /api/cart/member/{memberId}/store/{storeId}/meal/{mealId}
     * 【關鍵修正】: 移除了舊的 /{cartId} 路徑，改為接收複合鍵
     *
     * @param memberId 會員 ID。
     * @param storeId 門市 ID。
     * @param mealId 餐點 ID。
     * @param request 包含更新數量和備註的請求體。
     * @return 更新後的購物車項目 DTO。
     */
    @PutMapping("/member/{memberId}/store/{storeId}/meal/{mealId}") // 【關鍵修正】路徑和參數
    public ResponseEntity<CartItemDto> updateCartItem(
            @PathVariable Long memberId, // 【新增參數】
            @PathVariable Long storeId,  // 【新增參數】
            @PathVariable Long mealId,   // 【新增參數】
            @Valid @RequestBody UpdateCartItemRequest request) {
        try {
            // 【關鍵修正】調用 CartService 中正確的方法，並傳遞所有參數
            CartItemDto updatedCartItem = cartService.updateCartItemByKeys(memberId, storeId, mealId, request);
            if (updatedCartItem == null) { // 當數量更新為 0 時，Service 返回 null
                return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 表示項目被移除
            }
            return new ResponseEntity<>(updatedCartItem, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // 例如：購物車項目不存在，或 memberId/storeId/mealId 無效
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); // 返回 404 Not Found
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 【注意】如果您的 Controller 中還有 removeCartItem(Long cartId) 的方法，也需要進行類似的修改，
    // 以匹配 CartService 中的 removeCartItemByKeys(Long memberId, Long storeId, Long mealId)。

    /**
     * 從購物車中移除特定會員、門市、餐點的項目。
     * DELETE /api/cart/member/{memberId}/store/{storeId}/meal/{mealId}
     *
     * @param memberId 會員 ID。
     * @param storeId 門市 ID。
     * @param mealId 餐點 ID。
     * @return 無內容的 204 No Content 響應。
     */
    @DeleteMapping("/member/{memberId}/store/{storeId}/meal/{mealId}") // 【關鍵修正】路徑和參數
    public ResponseEntity<Void> removeCartItem(
            @PathVariable Long memberId,
            @PathVariable Long storeId,
            @PathVariable Long mealId) {
        try {
            // 【關鍵修正】調用 CartService 中正確的方法，並傳遞所有參數
            cartService.removeCartItemByKeys(memberId, storeId, mealId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
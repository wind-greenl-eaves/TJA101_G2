// 【最終修正與精簡版】CartController.java
package com.eatfast.cart.controller;

import com.eatfast.cart.dto.CartDTO.AddToCartRequest;
import com.eatfast.cart.dto.CartDTO.CartItemDto;
import com.eatfast.cart.dto.CartDTO.UpdateCartItemRequest;
import com.eatfast.cart.service.CartService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 為了安全，關閉 CSRF 後，手動注入 CSRF Token 的相關 import 已不再需要
// import org.springframework.security.web.csrf.CsrfToken;
// import jakarta.servlet.http.HttpServletRequest; 

@Controller
@RequestMapping("/cart") // 所有購物車功能的基礎路徑
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * 顯示購物車頁面
     */
    @GetMapping
    public String showCartPage() {
        return "front-end/cart/cart";
    }

    /**
     * 【API】根據會員 ID 獲取其購物車內容。
     * 對應前端 loadCart() 函式。
     * 路徑: GET /cart/member/{memberId}
     */
    @GetMapping("/member/{memberId}")
    @ResponseBody
    public ResponseEntity<List<CartItemDto>> getCartItemsByMemberId(@PathVariable Long memberId) {
        List<CartItemDto> cartItems = cartService.getCartItemsByMember(memberId);
        if (cartItems == null || cartItems.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }
        return ResponseEntity.ok(cartItems); // 200
    }

    /**
     * 【API】更新購物車中特定項目的數量。
     * 對應前端 updateQuantity() 函式。
     * 路徑: PUT /cart/member/{memberId}/store/{storeId}/meal/{mealId}
     */
    @PutMapping("/member/{memberId}/store/{storeId}/meal/{mealId}")
    @ResponseBody
    public ResponseEntity<CartItemDto> updateCartItemQuantity(
            @PathVariable Long memberId,
            @PathVariable Long storeId,
            @PathVariable Long mealId,
            @Valid @RequestBody UpdateCartItemRequest request) { // 前端送來的 body 只有 quantity
        try {
            // 由於 body 中只有 quantity，我們將 mealCustomization 設為 null，讓 Service 層不去更新它
            request.setMealCustomization(null); 
            CartItemDto updatedCartItem = cartService.updateCartItemByKeys(memberId, storeId, mealId, request);
            if (updatedCartItem == null) {
                return ResponseEntity.noContent().build(); // 204 (數量變為0，項目被刪除)
            }
            return ResponseEntity.ok(updatedCartItem); // 200
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build(); // 404
        }
    }

    /**
     * 【API】從購物車中移除特定項目。
     * 對應前端 deleteCartItem() 函式。
     * 路徑: DELETE /cart/member/{memberId}/store/{storeId}/meal/{mealId}
     */
    @DeleteMapping("/member/{memberId}/store/{storeId}/meal/{mealId}")
    @ResponseBody
    public ResponseEntity<Void> removeCartItem(
            @PathVariable Long memberId,
            @PathVariable Long storeId,
            @PathVariable Long mealId) {
        try {
            cartService.removeCartItemByKeys(memberId, storeId, mealId);
            return ResponseEntity.noContent().build(); // 204
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build(); // 404
        }
    }

    /**
     * 【API】清空指定會員的整個購物車。
     * 對應前端 clearCart() 函式。
     * 路徑: DELETE /cart/member/{memberId}
     */
    @DeleteMapping("/member/{memberId}")
    @ResponseBody
    public ResponseEntity<Void> clearCart(@PathVariable Long memberId) {
        cartService.clearCartByMember(memberId);
        return ResponseEntity.noContent().build(); // 204
    }

    /**
     * 處理從餐點頁面「加入購物車」的傳統表單提交。
     * 路徑: POST /cart
     */
    @PostMapping("/add")
    public String addToCart(HttpSession session,
                            @RequestParam("mealId") Long mealId,
                            @RequestParam("storeId") Long storeId,
                            @RequestParam("quantity") Long quantity,
                            RedirectAttributes redirectAttributes) {
        
        Long memberId = (Long) session.getAttribute("loggedInMemberId");
        if (memberId == null) {
            return "redirect:/api/v1/auth/member-login";
        }

        try {
            AddToCartRequest request = new AddToCartRequest();
            request.setMemberId(memberId);
            request.setMealId(mealId);
            request.setStoreId(storeId);
            request.setQuantity(quantity);
            
            cartService.addOrUpdateCartItem(request);
            redirectAttributes.addFlashAttribute("successMessage", "已成功加入購物車！");

            return "redirect:/menu"; 
        
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/menu";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "加入購物車失敗：" + e.getMessage());
            return "redirect:/menu";
        } catch (Exception e) {
            e.printStackTrace(); // 在伺服器後台印出詳細錯誤，方便追蹤
            redirectAttributes.addFlashAttribute("errorMessage", "發生未知錯誤，加入購物車失敗，請稍後再試");
            return "redirect:/menu";
        }
    }
}
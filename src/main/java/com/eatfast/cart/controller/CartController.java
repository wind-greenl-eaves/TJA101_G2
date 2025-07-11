package com.eatfast.cart.controller;

import com.eatfast.cart.dto.CartDTO;
import com.eatfast.cart.dto.CartDTO.CartItemDto;       // 確保已引入
import com.eatfast.cart.dto.CartDTO.UpdateCartItemRequest; // 確保已引入
import com.eatfast.cart.service.CartService; // 確保已引入

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

	@GetMapping 
	public String showCartPage() {
		return "front-end/cart/cart"; // 返回 Thymeleaf 模板的路徑
	}

//	@GetMapping // 購物車測試，加入假資料
//	public String showCartPage(Model model) {
//		boolean isLoggedIn = false;
//		Long memberId = null;
//
//		// 【關鍵實作】: 使用 Spring Security 判斷登入狀態和獲取用戶ID
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		if (authentication != null && authentication.isAuthenticated()
//				&& !(authentication.getPrincipal() instanceof String)) {
//			// authentication.isAuthenticated() 檢查是否已驗證 (排除匿名用戶)
//			// authentication.getPrincipal() instanceof String 排除 "anonymousUser" 字串 (代表未登入)
//			isLoggedIn = true;
//
//			Object principal = authentication.getPrincipal();
//			if (principal instanceof UserDetails) { 
//				// 如果您的 Principal 是 Spring Security 的 UserDetails
//				// 通常 UserDetails 會有 username 或其他標識符
//				// 您需要將 UserDetails 轉換為您實際的會員物件，或從中獲取會員 ID
//				// 假設您的 UserDetails 實作中包含了 memberId
//				// 例如: MyUserDetails userDetails = (MyUserDetails) principal;
//				// memberId = userDetails.getMemberId();
//
//				// 這裡為簡化，假設您的 username 就是會員 ID 的字串形式，或者您需要從數據庫獲取
//				// 在實際應用中，您應該從自定義的 UserDetails 實現中獲取真正的 memberId
//				try {
//					// 假設 principal.getName() (也就是 username) 可以直接轉換為 Long 的 memberId
//					// 或者您有自定義的 UserDetails 類別，其中有 getMemberId() 方法
//					memberId = Long.parseLong(((UserDetails) principal).getUsername());
//				} catch (NumberFormatException e) {
//					System.err.println("警告: Spring Security 的 username 無法轉換為 Long 類型的會員ID。" + e.getMessage());
//					memberId = null; // 無法解析則設為 null
//				}
//			} else if (principal instanceof Long) { 
//				memberId = (Long) principal;
//			} else {
//				// 如果是其他類型，可能需要更複雜的邏輯來獲取會員ID
//				System.err.println("警告: Spring Security principal 類型未知，無法直接獲取會員ID。");
//				memberId = null;
//			}
//		}
//
//		model.addAttribute("isLoggedIn", isLoggedIn);
//		model.addAttribute("memberId", memberId);
//		return "front-end/cart/carttest"; // 假設您的檔案是 templates/front-end/cart/carttest.html
//	}

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
	//	從表單接收mealId、quantity
	@PostMapping
	public String addToCart(@RequestParam("mealId") Long mealId,
	                        @RequestParam("quantity") Long quantity,
	                        HttpSession session,
	                        RedirectAttributes redirectAttributes) {

		Long memberId = (Long) session.getAttribute("loggedInMemberId");

	    if (memberId == null) {
	        redirectAttributes.addFlashAttribute("errorMessage", "請先登入會員");
	        return "redirect:/api/v1/auth/member-login";
	    }

	    CartDTO.AddToCartRequest dto = new CartDTO.AddToCartRequest();
	    dto.setMemberId(memberId);
	    dto.setMealId(mealId);
	    dto.setQuantity(quantity);
	    dto.setMealCustomization(""); // 若前端未提供可先給空字串

	    cartService.addOrUpdateCartItem(dto); // ✅ 傳入正確的 DTO


	    redirectAttributes.addFlashAttribute("successMessage", "已加入購物車");
	    return "redirect:/menu"; // 加入成功後回到菜單頁
	}
	
	@PutMapping("/member/{memberId}/customization")
	public ResponseEntity<Void> updateMealCustomizationForAll(
	        @PathVariable Long memberId,
	        @RequestBody Map<Long, String> request) {
	    
	    String customization = request.get("mealCustomization");
	    cartService.updateAllCartItemsCustomization(memberId, customization);
	    return ResponseEntity.ok().build();
	}

	
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
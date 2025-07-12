package com.eatfast.cart.controller;

import com.eatfast.cart.dto.CartDTO.AddToCartRequest;
import com.eatfast.cart.dto.CartDTO.CartItemDto;       // 確保已引入
import com.eatfast.cart.dto.CartDTO.UpdateCartItemRequest; // 確保已引入
import com.eatfast.cart.service.CartService; // 確保已引入

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
	public String addToCart(HttpSession session,
	                        @RequestParam(value = "mealId", required = false) String mealIdStr,
	                        @RequestParam(value = "storeId", required = false) String storeIdStr,
	                        @RequestParam(value = "quantity", required = false) String quantityStr,
	                        @RequestParam(value = "mealCustomization", required = false) String customization,
	                        RedirectAttributes redirectAttributes) {
	    
	    try {
	        // 手動解析和驗證參數
	        Long mealId = parseAndValidate(mealIdStr, "餐點ID");
	        Long storeId = parseAndValidate(storeIdStr, "門市ID");
	        Long quantity = parseAndValidate(quantityStr, "數量");
	        
	        if (quantity <= 0) {
	            redirectAttributes.addFlashAttribute("errorMessage", "數量必須大於0");
	            return "redirect:/menu";
	        }
	        
	        Long memberId = (Long) session.getAttribute("loggedInMemberId");
	        if (memberId == null) {
	            return "redirect:/api/v1/auth/member-login";
	        }

	        AddToCartRequest request = new AddToCartRequest();
	        request.setMemberId(memberId);
	        request.setMealId(mealId);
	        request.setStoreId(storeId);
	        request.setQuantity(quantity);
	        request.setMealCustomization(customization);

	        cartService.addOrUpdateCartItem(request);

	        redirectAttributes.addFlashAttribute("successMessage", "已成功加入購物車！");
	        return "redirect:/menu";
	        
	    } catch (IllegalArgumentException e) {
	        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
	        return "redirect:/menu";
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("errorMessage", "加入購物車失敗，請稍後再試");
	        return "redirect:/menu";
	    }
	}

	// 輔助方法
	private Long parseAndValidate(String value, String fieldName) {
	    if (value == null || value.trim().isEmpty()) {
	        throw new IllegalArgumentException(fieldName + "不能為空");
	    }
	    try {
	        return Long.parseLong(value.trim());
	    } catch (NumberFormatException e) {
	        throw new IllegalArgumentException(fieldName + "格式不正確");
	    }
	}

	/**
	 * 根據會員 ID 取得購物車內容
	 * GET /cart/api/member/{memberId}
	 */
	@GetMapping("/api/member/{memberId}")
	@ResponseBody
	public ResponseEntity<?> getCartItemsByMemberId(@PathVariable Long memberId) {
	    try {
	        // 這邊你應該會有一個 service 方法，如：
	        List<CartItemDto> cartItems = cartService.getCartItemsByMember(memberId);
	        if (cartItems == null || cartItems.isEmpty()) {
	            return ResponseEntity.noContent().build(); // 204 無內容
	        }
	        return ResponseEntity.ok(cartItems); // 200 OK
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("取得購物車失敗");
	    }
	}
	
	
    // 【新增】提供給前端，用來獲取餐點資訊以準備加入購物車的 API
    // ================================================================
    @GetMapping("/prepare-item/{mealId}")
    public ResponseEntity<?> prepareItemForCart(@PathVariable Long mealId) {
        try {
            // 呼叫我們在 Service 新增的方法
            CartItemDto preparedItem = cartService.prepareItemForCart(mealId);
            // 成功，回傳 200 OK 與 DTO 物件
            return ResponseEntity.ok(preparedItem);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            // 若 Service 找不到餐點拋出例外，則回傳 404 Not Found
            return ResponseEntity.status(404).body(e.getMessage());
        }
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
    
    
   //更新購物車項目數量 (對應前端 updateQuantity 函式)
    
   @PutMapping("/api/member/{memberId}/meal/{mealId}")
   @ResponseBody
   public ResponseEntity<CartItemDto> updateItemQuantity(
           @PathVariable Long memberId,
           @PathVariable Long mealId,
           @RequestBody UpdateCartItemRequest request) { // ★ request body 只需要傳 quantity 即可
       
       try {
           // 【注意】同樣假設 storeId 不是更新的必要條件
           CartItemDto updatedItem = cartService.updateCartItemQuantity(memberId, mealId, request.getQuantity());
           return ResponseEntity.ok(updatedItem);
       } catch (IllegalArgumentException e) {
           return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
       }
   }

   /**
    * 【新增API】刪除整個購物車的項目 
    */
   @DeleteMapping("/api/member/{memberId}") // ★ 修正路徑，移除 "/clear" 以匹配前端
   @ResponseBody
   public ResponseEntity<Void> clearCart(@PathVariable Long memberId) { // 這裡本來就正確
       try {
           cartService.clearCartByMember(memberId);
           return ResponseEntity.noContent().build(); // 204 No Content
       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
       }
   }

   /**
    * 【新增API】清空指定會員購物車的一個項目 
    */
   @DeleteMapping("/api/member/{memberId}/meal/{mealId}")
   @ResponseBody
   public ResponseEntity<Void> removeCartItem(
           @PathVariable Long memberId, // ★ 改用 @PathVariable 從路徑接收 memberId
           @PathVariable Long mealId) { // ★ 改用 @PathVariable 從路徑接收 mealId
       
       try {
           // 【注意】假設您的 Service 層有能力僅透過 memberId 和 mealId 刪除。
           // 如果 Service 層強烈需要 storeId，則前端也需要相應修改以傳遞該參數。
           // 這裡我們先假設 storeId 不是刪除的必要條件。
           cartService.removeCartItemByKeys(memberId, mealId);
           return ResponseEntity.noContent().build(); // 204 No Content
       } catch (IllegalArgumentException e) {
           // 例如，找不到該項目時 Service 可能會拋出此例外
           return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
       } catch (Exception e) {
           // 處理其他潛在的伺服器錯誤
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
       }
   }
   
   /**
    * 根據會員 ID 獲取其購物車內的所有項目。
    * 這個方法將會處理前端 loadCart() 函式的請求。
    *
    * @param memberId 會員 ID，從 URL 路徑中獲取。
    * @return 一個包含購物車項目 DTO 的 List，以 JSON 格式回傳。
    */
   @GetMapping("/member/{memberId}")
   @ResponseBody // <-- 關鍵#1: 因為您的類別是 @Controller，需要此註解才能直接返回 JSON 資料，而不是尋找樣板頁面。
   public ResponseEntity<List<CartItemDto>> getCartByMemberId(@PathVariable Long memberId) {
       // 假設您的 CartService 有一個 getCartItemsByMemberId 方法
       List<CartItemDto> cartItems = cartService.getCartItemsByMember(memberId); 
       
       // 關鍵#2: 您的前端JS有處理 204 的邏輯，所以這裡要對應實作
       if (cartItems == null || cartItems.isEmpty()) {
           // 如果購物車是空的，回傳 204 No Content 狀態碼
           return new ResponseEntity<>(HttpStatus.NO_CONTENT);
       }
       
       // 如果有資料，回傳 200 OK 和 JSON 資料
       return ResponseEntity.ok(cartItems);
   }

}
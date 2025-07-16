// 【最終修正與精簡版】CartController.java
package com.eatfast.cart.controller;

import com.eatfast.cart.dto.CartDTO.AddToCartRequest;
import com.eatfast.cart.dto.CartDTO.CartItemDto;
import com.eatfast.cart.dto.CartDTO.UpdateCartItemRequest;
import com.eatfast.cart.service.CartService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	 * 【API】根據會員 ID 獲取其購物車內容。 對應前端 loadCart() 函式。 路徑: GET /cart/member/{memberId}
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
	 * 【API】更新購物車中特定項目的數量。 對應前端 updateQuantity() 函式。 路徑: PUT
	 * /cart/member/{memberId}/store/{storeId}/meal/{mealId}
	 */
	@PutMapping("/member/{memberId}/store/{storeId}/meal/{mealId}")
	@ResponseBody
	public ResponseEntity<CartItemDto> updateCartItemQuantity(@PathVariable Long memberId, @PathVariable Long storeId,
			@PathVariable Long mealId, @Valid @RequestBody UpdateCartItemRequest request) { // 前端送來的 body 只有 quantity
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
	 * 【API】從購物車中移除特定項目。 對應前端 deleteCartItem() 函式。 路徑: DELETE
	 * /cart/member/{memberId}/store/{storeId}/meal/{mealId}
	 */
	@DeleteMapping("/member/{memberId}/store/{storeId}/meal/{mealId}")
	@ResponseBody
	public ResponseEntity<Void> removeCartItem(@PathVariable Long memberId, @PathVariable Long storeId,
			@PathVariable Long mealId) {
		try {
			cartService.removeCartItemByKeys(memberId, storeId, mealId);
			return ResponseEntity.noContent().build(); // 204
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build(); // 404
		}
	}

	/**
	 * 【API】清空指定會員的整個購物車。 對應前端 clearCart() 函式。 路徑: DELETE /cart/member/{memberId}
	 */
	@DeleteMapping("/member/{memberId}")
	@ResponseBody
	public ResponseEntity<Void> clearCart(@PathVariable Long memberId) {
		cartService.clearCartByMember(memberId);

		// 回傳一個 HTTP 204 No Content，這是 RESTful API 中代表「成功處理但無內容返回」的標準回應
		return ResponseEntity.noContent().build();
	}

	/**
	 * 切換門市根據會員 ID 清空其所有購物車項目。 當使用者同意後，前端會呼叫這個 API。
	 * 
	 * @param memberId 從 Session 中獲取的會員 ID
	 * @return 一個代表成功的回應 (HTTP 204 No Content)
	 */
	@DeleteMapping("/clear")
	public ResponseEntity<Void> clearCart(HttpSession session) {
		Object memberIdObj = session.getAttribute("loggedInMemberId");
		if (memberIdObj == null) {
			// 未登入，無需操作，直接回傳成功
			return ResponseEntity.noContent().build();
		}

		Long memberId = (Long) memberIdObj;
		cartService.clearCartByMember(memberId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 處理從餐點頁面「加入購物車」的傳統表單提交。 路徑: POST /cart
	 */
	@PostMapping("/add")
	@ResponseBody
	public ResponseEntity<?> addToCartAjax(HttpSession session, @RequestParam("mealId") Long mealId,
			@RequestParam("storeId") Long storeId, @RequestParam("quantity") Long quantity) {

		Long memberId = (Long) session.getAttribute("loggedInMemberId");
		if (memberId == null) {
// 401 Unauthorized: 未經授權 (未登入)
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("請先登入會員");
		}

		try {
			AddToCartRequest request = new AddToCartRequest();
			request.setMemberId(memberId);
			request.setMealId(mealId);
			request.setStoreId(storeId);
			request.setQuantity(quantity);

			cartService.addOrUpdateCartItem(request);

// 200 OK: 請求成功
			return ResponseEntity.ok(Map.of("message", "已成功加入購物車！"));

		} catch (IllegalStateException e) {
// 409 Conflict: 請求衝突 (例如：加入不同門市的商品)
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		} catch (EntityNotFoundException e) {
// 404 Not Found: 請求的資源不存在 (例如：餐點或門市 ID 錯誤)
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("加入購物車失敗：" + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace(); // 在後台印出詳細錯誤，方便追蹤
// 500 Internal Server Error: 伺服器內部錯誤
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("發生未知錯誤，加入購物車失敗");
		}
	}

	/**
	 * 查詢當前登入會員的購物車商品總數
	 * 
	 * @param session HttpSession 用於獲取會員ID
	 * @return 一個包含 item_count 的 Map，如果未登入則回傳 0
	 */
	@GetMapping("/count")
	public ResponseEntity<Map<String, Long>> getCartItemCount(HttpSession session) {
		// 從 Session 中安全地獲取會員 ID
		Object memberIdObj = session.getAttribute("loggedInMemberId");
		if (memberIdObj == null) {
			// 如果未登入，直接回傳數量為 0
			return ResponseEntity.ok(Map.of("item_count", 0L));
		}

		Long memberId = (Long) memberIdObj;
		long count = cartService.getItemCountByMemberId(memberId);
		return ResponseEntity.ok(Map.of("item_count", count));
	}

	/**
	 * 儲存取餐時間到session中 路徑: POST /cart/set-pickup-time
	 */
	@PostMapping("/set-pickup-time")
	@ResponseBody
	public ResponseEntity<Map<String, String>> setPickupTime(@RequestBody Map<String, String> requestBody,
			HttpSession session) {

		Long memberId = (Long) session.getAttribute("loggedInMemberId");
		if (memberId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		String pickupTime = requestBody.get("pickupTime");
		String notes = requestBody.get("notes");

		// 將取餐時間和備註儲存到session中
		session.setAttribute("pickupTime", pickupTime);
		session.setAttribute("orderNotes", notes);

		Map<String, String> response = new HashMap<>();
		response.put("status", "success");
		response.put("message", "取餐時間已儲存");

		return ResponseEntity.ok(response);
	}
}
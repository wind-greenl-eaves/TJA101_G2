// 範例：在一個新的 CartViewController.java
package com.eatfast.cart.controller; // 或者放在其他適合的包，例如 com.eatfast.frontend.controller

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller // 這是處理頁面視圖的 Controller
@RequestMapping("/cart") // 購物車頁面的基礎路徑
public class CartViewController {

	@GetMapping // GET /cart
	public String showCartPage() {
		return "front-end/cart/cart"; // 返回 Thymeleaf 模板的路徑
	}

	@GetMapping("/test") // 購物車測試，加入假資料
	public String showCartPage(Model model) {
		boolean isLoggedIn = false;
		Long memberId = null;

		// 【關鍵實作】: 使用 Spring Security 判斷登入狀態和獲取用戶ID
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()
				&& !(authentication.getPrincipal() instanceof String)) {
			// authentication.isAuthenticated() 檢查是否已驗證 (排除匿名用戶)
			// authentication.getPrincipal() instanceof String 排除 "anonymousUser" 字串 (代表未登入)
			isLoggedIn = true;

			Object principal = authentication.getPrincipal();
			if (principal instanceof UserDetails) { // 如果您的 Principal 是 Spring Security 的 UserDetails
				// 通常 UserDetails 會有 username 或其他標識符
				// 您需要將 UserDetails 轉換為您實際的會員物件，或從中獲取會員 ID
				// 假設您的 UserDetails 實作中包含了 memberId
				// 例如: MyUserDetails userDetails = (MyUserDetails) principal;
				// memberId = userDetails.getMemberId();

				// 這裡為簡化，假設您的 username 就是會員 ID 的字串形式，或者您需要從數據庫獲取
				// 在實際應用中，您應該從自定義的 UserDetails 實現中獲取真正的 memberId
				try {
					// 假設 principal.getName() (也就是 username) 可以直接轉換為 Long 的 memberId
					// 或者您有自定義的 UserDetails 類別，其中有 getMemberId() 方法
					memberId = Long.parseLong(((UserDetails) principal).getUsername());
				} catch (NumberFormatException e) {
					System.err.println("警告: Spring Security 的 username 無法轉換為 Long 類型的會員ID。" + e.getMessage());
					memberId = null; // 無法解析則設為 null
				}
			} else if (principal instanceof Long) { // 如果您的 Principal 直接就是 Long 類型的 memberId
				memberId = (Long) principal;
			} else {
				// 如果是其他類型，可能需要更複雜的邏輯來獲取會員ID
				System.err.println("警告: Spring Security principal 類型未知，無法直接獲取會員ID。");
				memberId = null;
			}
		}

		model.addAttribute("isLoggedIn", isLoggedIn);
		model.addAttribute("memberId", memberId);
		return "front-end/cart/carttest"; // 假設您的檔案是 templates/front-end/cart/carttest.html
	}
}
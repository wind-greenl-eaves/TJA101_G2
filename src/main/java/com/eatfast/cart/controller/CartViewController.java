// 範例：在一個新的 CartViewController.java
package com.eatfast.cart.controller; // 或者放在其他適合的包，例如 com.eatfast.frontend.controller

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller // 這是處理頁面視圖的 Controller
@RequestMapping("/cart") // 購物車頁面的基礎路徑
public class CartViewController {

    @GetMapping // GET /cart
    public String showCartPage() {
        return "front-end/cart/cart"; // 返回 Thymeleaf 模板的路徑
    }
}
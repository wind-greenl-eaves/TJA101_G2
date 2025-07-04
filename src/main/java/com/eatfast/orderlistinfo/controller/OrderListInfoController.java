/*
 * ================================================================
 * 檔案 4: OrderListInfoController.java (★★ 核心重構 ★★)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/orderlistinfo/controller/OrderListInfoController.java
 * - 核心改動:
 * 1. 【移除 DTO】: 遵循您的指示，將 `submitReview` 方法的參數從 DTO 改為 @RequestParam，
 * 使其與 Feedback 模組的風格保持一致。
 * 2. 【錯誤處理強化】: 捕獲更精準的 EntityNotFoundException。
 */
package com.eatfast.orderlistinfo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.eatfast.orderlistinfo.model.OrderListInfoEntity;
import com.eatfast.orderlistinfo.service.OrderListInfoService;
import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/order-details")
public class OrderListInfoController {

    private final OrderListInfoService orderListInfoService;

    @Autowired
    public OrderListInfoController(OrderListInfoService orderListInfoService) {
        this.orderListInfoService = orderListInfoService;
    }

    /**
     * API: 為指定的訂單明細項目新增評論。
     * - HTTP 方法: PUT 或 POST 均可，此處使用 PUT 來表示對資源的「更新」。
     * - URL: /api/order-details/{id}/review
     * @param id 訂單明細的 ID (路徑變數)
     * @param stars 評論星等 (請求參數)
     * @return 更新後的訂單明細
     */
    @PutMapping("/{id}/review")
    public ResponseEntity<?> submitReview(
            @PathVariable Long id, 
            @RequestParam Long stars) { //【修正】: 不再使用 DTO，直接接收請求參數。
        
        try {
            // 直接將從請求中獲取的參數傳遞給 Service 層。
            OrderListInfoEntity updatedInfo = orderListInfoService.addReview(id, stars);
            // 返回 200 OK 及更新後的資料。
            return ResponseEntity.ok(updatedInfo);
            
        } catch (IllegalArgumentException e) {
            // 如果 Service 拋出參數錯誤的例外 (例如星等不符規定)。
            return ResponseEntity.badRequest().body(e.getMessage()); // 返回 400 並附上錯誤訊息。
            
        } catch (EntityNotFoundException e) {
            // 如果 Service 拋出找不到的例外。
            return ResponseEntity.notFound().build(); // 返回 404 Not Found。
        }
    }
    
}
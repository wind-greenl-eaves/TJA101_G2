/*
 * ================================================================
 * 檔案 4: OrderListController.java (★★ 核心重構 ★★)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/orderlist/controller/OrderListController.java
 * - 核心改動:
 * 1. 改為 @Controller: 為了與您的專案風格保持一致，改為傳統控制器。
 * 2. 錯誤處理: 寫入操作加入 try-catch 區塊，處理 Service 層可能拋出的業務例外。
 * 3. 實作查詢: 完成 `getOrdersByMemberId` 的前端調用邏輯。
 * 4. 參數傳遞: 更新訂單狀態時，直接傳遞 Enum，而不是數字。
 */
package com.eatfast.orderlist.controller;

import com.eatfast.orderlist.model.OrderListEntity;
import com.eatfast.orderlist.model.OrderStatus;
import com.eatfast.orderlist.service.OrderListService;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/orders") // 改為非 API 基礎路徑
public class OrderListController {

    private final OrderListService orderListService;

    public OrderListController(OrderListService orderListService) {
        this.orderListService = orderListService;
    }
    
    // 假設有一個後台頁面可以查看所有訂單
    @GetMapping("/listAll")
    public String listAllOrders(Model model) {
        // 這是一個簡化版，真實後台應有分頁和查詢功能
        // List<OrderListEntity> allOrders = orderListService.findAll(); 
        // model.addAttribute("orderList", allOrders);
        return "back-end/order/listAllOrders"; // 假設的視圖路徑
    }

    // --- 以下為 API 端點，保留 @ResponseBody ---
    
    // API: 根據會員ID查詢其所有訂單
    @GetMapping("/api/byMember/{memberId}")
    @ResponseBody
    public ResponseEntity<List<OrderListEntity>> getOrdersByMemberId(@PathVariable Long memberId) {
        List<OrderListEntity> orders = orderListService.getOrdersByMemberId(memberId);
        return ResponseEntity.ok(orders);
    }

    // API: 根據訂單ID查詢單筆訂單詳情
    @GetMapping("/api/detail/{orderId}")
    @ResponseBody
    public ResponseEntity<OrderListEntity> getOrderById(@PathVariable String orderId) {
        return orderListService.getOrderById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // API: 更新訂單狀態
    @PutMapping("/api/status/{orderId}")
    @ResponseBody
    public ResponseEntity<?> updateOrderStatus(@PathVariable String orderId, 
                                               @RequestParam("status") OrderStatus newStatus) {
        try {
            OrderListEntity updatedOrder = orderListService.updateOrderStatus(orderId, newStatus);
            return ResponseEntity.ok(updatedOrder);
        } catch (EntityNotFoundException | IllegalStateException e) {
            // 返回包含錯誤訊息的 400 Bad Request 或 404 Not Found
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
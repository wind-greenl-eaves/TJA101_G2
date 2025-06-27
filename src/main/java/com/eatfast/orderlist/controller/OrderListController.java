package com.eatfast.orderlist.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eatfast.orderlist.dto.UpdateStatusRequest;
import com.eatfast.orderlist.model.OrderListEntity;
import com.eatfast.orderlist.service.OrderListService;

/**
 * 處理訂單相關 API 請求的 Controller。
 * 這就是應用程式的對外窗口。
 */
@RestController // 📌【不可變】聲明這是一個 RESTful 風格的控制器，回傳值會自動轉為 JSON。
@RequestMapping("/api/orders") // 📌【不可變】為此 Controller 下的所有 API 設定一個統一的基礎路徑。
public class OrderListController {

    private final OrderListService orderListService; // 依賴注入 Service 層

    @Autowired
    public OrderListController(OrderListService orderListService) {
        this.orderListService = orderListService;
    }

    /**
     * API: 建立新訂單
     * HTTP 方法: POST
     * URL: /api/orders
     * @param order 從請求的 Body 中傳入的訂單 JSON 資料
     * @return 建立成功後的訂單資料及 HTTP 狀態碼 201 (Created)
     */
    @PostMapping
    public ResponseEntity<OrderListEntity> createOrder(@RequestBody OrderListEntity order) {
        OrderListEntity createdOrder = orderListService.createOrder(order);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    /**
     * API: 根據 ID 查詢訂單
     * HTTP 方法: GET
     * URL: /api/orders/{id}  (例如: /api/orders/202506270001)
     * @param id 從 URL 路徑中獲取的訂單 ID
     * @return 找到的訂單資料或 HTTP 狀態碼 404 (Not Found)
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderListEntity> getOrderById(@PathVariable String id) {
        return orderListService.getOrderById(id)
                .map(order -> ResponseEntity.ok(order)) // 如果找得到，回傳 200 OK 及訂單資料
                .orElse(ResponseEntity.notFound().build()); // 如果找不到，回傳 404 Not Found
    }

    /**
     * API: 根據會員ID查詢所有訂單
     * HTTP 方法: GET
     * URL: /api/orders?memberId=123
     * @param memberId 從 URL 查詢參數中獲取的會員 ID
     * @return 該會員的訂單列表
     */
    @GetMapping
    public ResponseEntity<List<OrderListEntity>> getOrdersByMemberId(@RequestParam Long memberId) {
        // 假設 OrderListService 中有一個 getOrdersByMemberId 方法
        // 這需要您在 Service 層中新增一個方法來處理，這是常見的重構
        // List<OrderListEntity> orders = orderListService.getOrdersByMemberId(memberId);
        // return ResponseEntity.ok(orders);
        // 由於我們之前的 Service 是用 MemberEntity 物件查詢，這裡先註解起來，作為您下一步的練習
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build(); // 暫時回傳 501 Not Implemented
    }


    /**
     * API: 更新訂單狀態
     * HTTP 方法: PUT
     * URL: /api/orders/{id}/status
     * @param id 要更新的訂單 ID
     * @param request 包含新狀態的 DTO 物件
     * @return 更新後的訂單資料
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderListEntity> updateOrderStatus(@PathVariable String id, @RequestBody UpdateStatusRequest request) {
        try {
            OrderListEntity updatedOrder = orderListService.updateOrderStatus(id, request.newStatus());
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            // 這是簡易的錯誤處理，如果 Service 拋出例外（例如找不到訂單），就回傳 404
            return ResponseEntity.notFound().build();
        }
    }
}
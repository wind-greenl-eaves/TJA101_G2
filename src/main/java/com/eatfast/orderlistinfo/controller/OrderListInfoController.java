package com.eatfast.orderlistinfo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eatfast.orderlistinfo.dto.ReviewRequestDTO;
import com.eatfast.orderlistinfo.model.OrderListInfoEntity;
import com.eatfast.orderlistinfo.service.OrderListInfoService;

/**
 * 處理單筆訂單明細相關 API 請求的 Controller。
 */
@RestController
@RequestMapping("/api/order-details") // 基礎路徑設為 order-details
public class OrderListInfoController {

    private final OrderListInfoService orderListInfoService;

    @Autowired
    public OrderListInfoController(OrderListInfoService orderListInfoService) {
        this.orderListInfoService = orderListInfoService;
    }

    /**
     * API: 為指定的訂單明細項目新增評論
     * HTTP 方法: PUT
     * URL: /api/order-details/{id}/review
     * @param id 訂單明細的 ID (主鍵)
     * @param request 包含評論星等的 DTO
     * @return 更新後的訂單明細 (實務上應回傳 DTO)
     */
    @PutMapping("/{id}/review")
    public ResponseEntity<OrderListInfoEntity> submitReview(
            @PathVariable Long id, 
            @RequestBody ReviewRequestDTO request) {
        
        try {
            OrderListInfoEntity updatedInfo = orderListInfoService.addReview(id, request.stars());
            return ResponseEntity.ok(updatedInfo);
        } catch (IllegalArgumentException e) {
            // 如果 Service 拋出參數錯誤的例外 (例如星等不符規定)
            return ResponseEntity.badRequest().build(); // 回傳 400 Bad Request
        } catch (RuntimeException e) {
            // 如果 Service 拋出找不到的例外
            return ResponseEntity.notFound().build(); // 回傳 404 Not Found
        }
    }
}
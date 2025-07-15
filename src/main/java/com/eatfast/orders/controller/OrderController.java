package com.eatfast.orders.controller;

import com.eatfast.cart.service.CartService;
import com.eatfast.cart.dto.CartDTO.CartItemDto;
import com.eatfast.orderlist.service.OrderListService;
import com.eatfast.orderlist.model.OrderListEntity;
import com.eatfast.store.service.StoreService;
import com.eatfast.store.model.StoreEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private CartService cartService;
    
    @Autowired 
    private OrderListService orderListService;
    
    @Autowired
    private StoreService storeService;

    @GetMapping("/pay")
    public String showPaymentPage(Model model, HttpSession session) {
        // 檢查用戶是否已登入
        Long memberId = (Long) session.getAttribute("loggedInMemberId");
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        
        if (memberId == null || isLoggedIn == null || !isLoggedIn) {
            return "redirect:/api/v1/auth/member-login";
        }
        
        // 獲取購物車資訊
        List<CartItemDto> cartItems = cartService.getCartItemsByMember(memberId);
        
        if (cartItems == null || cartItems.isEmpty()) {
            return "redirect:/cart?error=empty";
        }
        
        // 從購物車獲取取餐門市資訊
        Long storeId = cartItems.get(0).getStoreId();
        String storeName = cartItems.get(0).getStoreName();
        
        // 計算總金額
        Long totalAmount = cartItems.stream()
            .mapToLong(item -> item.getMealPrice() * item.getQuantity())
            .sum();
        
        // 從session獲取取餐時間
        String pickupTime = (String) session.getAttribute("pickupTime");
        
        // 創建訂單DTO
        OrderPaymentDTO orderToPay = new OrderPaymentDTO();
        orderToPay.setOrderListId(generateOrderId());
        orderToPay.setOrderDate(LocalDateTime.now());
        orderToPay.setPickupTime(pickupTime);
        orderToPay.setStoreName(storeName);
        orderToPay.setTotalAmount(totalAmount);
        
        model.addAttribute("orderToPay", orderToPay);
        model.addAttribute("orderAmount", totalAmount);
        
        return "front-end/orders/pay";
    }
    
    @PostMapping("/process-payment")
    public String processPayment(
            @RequestParam String orderId,
            @RequestParam String cardNumber,
            @RequestParam String expiryMonth,
            @RequestParam String expiryYear,
            @RequestParam String cvv,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        // 檢查用戶是否已登入
        Long memberId = (Long) session.getAttribute("loggedInMemberId");
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        
        if (memberId == null || isLoggedIn == null || !isLoggedIn) {
            return "redirect:/api/v1/auth/member-login";
        }
        
        try {
            // 這裡處理付款邏輯和創建訂單
            // 模擬付款處理
            Thread.sleep(1000);
            
            // 付款成功後清空購物車
            cartService.clearCartByMember(memberId);
            
            // 清除session中的取餐時間
            session.removeAttribute("pickupTime");
            
            redirectAttributes.addFlashAttribute("paymentSuccess", true);
            redirectAttributes.addFlashAttribute("orderId", orderId);
            
            return "redirect:/orders/payment-success";
        } catch (Exception e) {
            model.addAttribute("paymentError", "付款處理失敗，請稍後再試");
            return showPaymentPage(model, session);
        }
    }
    
    @GetMapping("/payment-success")
    public String showPaymentSuccess(Model model) {
        return "front-end/orders/payment-success";
    }
    
    // 生成訂單編號 (YYYYMMDDXXXX格式)
    private String generateOrderId() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 查詢當日最大的訂單編號
        String maxOrderId = orderListService.findMaxOrderIdByDatePrefix(dateStr);
        
        int nextSequence = 1; // 從1開始
        
        if (maxOrderId != null && maxOrderId.length() >= 12) {
            // 提取序號部分 (後4位)
            String sequenceStr = maxOrderId.substring(8);
            try {
                int currentSequence = Integer.parseInt(sequenceStr);
                nextSequence = currentSequence + 1;
            } catch (NumberFormatException e) {
                // 如果解析失敗，使用預設值1
                nextSequence = 1;
            }
        }
        
        // 格式化序號為4位數字，不足前面補0
        String sequenceStr = String.format("%04d", nextSequence);
        String orderId = dateStr + sequenceStr;
        
        // 確保訂單編號不重複（防止併發問題）
        while (orderListService.existsByOrderListId(orderId)) {
            nextSequence++;
            sequenceStr = String.format("%04d", nextSequence);
            orderId = dateStr + sequenceStr;
        }
        
        return orderId;
    }
    
    // 訂單付款DTO
    public static class OrderPaymentDTO {
        private String orderListId;
        private LocalDateTime orderDate;
        private String pickupTime;
        private String storeName;
        private Long totalAmount;
        
        // Getters and Setters
        public String getOrderListId() { return orderListId; }
        public void setOrderListId(String orderListId) { this.orderListId = orderListId; }
        public LocalDateTime getOrderDate() { return orderDate; }
        public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
        public String getPickupTime() { return pickupTime; }
        public void setPickupTime(String pickupTime) { this.pickupTime = pickupTime; }
        public String getStoreName() { return storeName; }
        public void setStoreName(String storeName) { this.storeName = storeName; }
        public Long getTotalAmount() { return totalAmount; }
        public void setTotalAmount(Long totalAmount) { this.totalAmount = totalAmount; }
        
        // 為了兼容現有的模板，添加一個store對象
        public StoreDTO getStore() {
            return new StoreDTO(storeName);
        }
    }
    
    // 門市DTO
    public static class StoreDTO {
        private String storeName;
        
        public StoreDTO(String storeName) {
            this.storeName = storeName;
        }
        
        public String getStoreName() { return storeName; }
        public void setStoreName(String storeName) { this.storeName = storeName; }
    }
}
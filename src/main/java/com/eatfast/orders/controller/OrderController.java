package com.eatfast.orders.controller;

import com.eatfast.cart.service.CartService;
import com.eatfast.cart.dto.CartDTO.CartItemDto;
import com.eatfast.orderlist.service.OrderListService;
import com.eatfast.orderlist.model.OrderListEntity;
import com.eatfast.orderlist.model.OrderStatus;
import com.eatfast.orderlistinfo.service.OrderListInfoService;
import com.eatfast.orderlistinfo.model.OrderListInfoEntity;
import com.eatfast.member.repository.MemberRepository;
import com.eatfast.member.model.MemberEntity;
import com.eatfast.store.repository.StoreRepository;
import com.eatfast.store.model.StoreEntity;
import com.eatfast.meal.model.MealEntity;

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
    private OrderListInfoService orderListInfoService;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private StoreRepository storeRepository;

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
            // 獲取購物車資訊
            List<CartItemDto> cartItems = cartService.getCartItemsByMember(memberId);
            
            if (cartItems == null || cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("paymentError", "購物車是空的，無法結帳");
                return "redirect:/cart";
            }
            
            // 模擬付款處理
            Thread.sleep(1000);
            
            // 付款成功後創建訂單
            createOrderFromCart(memberId, orderId, cartItems, session, cardNumber);
            
            // 付款成功後清空購物車
            cartService.clearCartByMember(memberId);
            
            // 清除session中的取餐時間
            session.removeAttribute("pickupTime");
            session.removeAttribute("orderNotes");
            
            redirectAttributes.addFlashAttribute("paymentSuccess", true);
            redirectAttributes.addFlashAttribute("orderId", orderId);
            
            return "redirect:/orders/payment-success";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("paymentError", "付款處理失敗，請稍後再試");
            return "redirect:/orders/pay";
        }
    }
    
    // 根據購物車資訊創建訂單
    private void createOrderFromCart(Long memberId, String orderId, List<CartItemDto> cartItems, HttpSession session, String cardNumber) {
        try {
            // 計算總金額
            Long totalAmount = cartItems.stream()
                .mapToLong(item -> item.getMealPrice() * item.getQuantity())
                .sum();
            
            // 獲取會員資訊 - 直接使用Repository
            MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("找不到會員資訊"));
            
            // 獲取門市資訊 - 直接使用Repository
            Long storeId = cartItems.get(0).getStoreId();
            StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("找不到門市資訊"));
            
            // 獲取取餐時間和備註
            String pickupTimeStr = (String) session.getAttribute("pickupTime");
            String orderNotes = (String) session.getAttribute("orderNotes");
            
            // 創建訂單主體
            OrderListEntity orderList = new OrderListEntity();
            orderList.setOrderListId(orderId);
            orderList.setOrderAmount(totalAmount);
            orderList.setOrderDate(LocalDateTime.now());
            orderList.setOrderStatus(OrderStatus.PENDING); // 預設為處理中
            orderList.setMember(member);
            orderList.setStore(store);
            orderList.setCardNumber(maskCardNumber(cardNumber));
            orderList.setMealCustomization(orderNotes);
            
            // 設定取餐號碼為會員電話末三碼
            Long pickupNumber = generatePickupNumberFromPhone(member.getPhone());
            orderList.setMealPickupNumber(pickupNumber);
            
            // 設定取餐時間
            if (pickupTimeStr != null && !pickupTimeStr.trim().isEmpty()) {
                try {
                    LocalDateTime pickupTime = LocalDateTime.parse(
                        LocalDateTime.now().toLocalDate() + "T" + pickupTimeStr + ":00"
                    );
                    orderList.setPickupTime(pickupTime);
                } catch (Exception e) {
                    // 如果解析失敗，設定為預設時間（當前時間+30分鐘）
                    orderList.setPickupTime(LocalDateTime.now().plusMinutes(30));
                }
            }
            
            // 儲存訂單
            OrderListEntity savedOrder = orderListService.createOrder(orderList);
            
            // 創建訂單明細
            for (CartItemDto cartItem : cartItems) {
                OrderListInfoEntity orderInfo = new OrderListInfoEntity();
                orderInfo.setOrderList(savedOrder);
                
                // 設定餐點資訊
                MealEntity meal = new MealEntity();
                meal.setMealId(cartItem.getMealId());
                orderInfo.setMeal(meal);
                
                orderInfo.setMealPrice(cartItem.getMealPrice());
                orderInfo.setQuantity(cartItem.getQuantity());
                orderInfo.setReviewStars(0L); // 初始評論星數為0
                
                // 儲存訂單明細
                orderListInfoService.createOrderListInfo(orderInfo);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("創建訂單失敗: " + e.getMessage());
        }
    }
    
    // 遮罩信用卡號碼
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
    
    // 根據會員電話號碼生成取餐號碼（取末三碼）
    private Long generatePickupNumberFromPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            // 如果電話號碼為空，產生隨機3位數
            return (long) (100 + (Math.random() * 900));
        }
        
        // 移除電話號碼中的非數字字元
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        
        if (cleanPhone.length() >= 3) {
            // 取末三碼
            String lastThree = cleanPhone.substring(cleanPhone.length() - 3);
            return Long.parseLong(lastThree);
        } else {
            // 如果電話號碼不足3位，產生隨機3位數
            return (long) (100 + (Math.random() * 900));
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
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            @RequestParam(required = false) String cardNumber,
            @RequestParam(required = false) String expiryMonth,
            @RequestParam(required = false) String expiryYear,
            @RequestParam(required = false) String cvv,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        // 檢查用戶是否已登入
        Long memberId = (Long) session.getAttribute("loggedInMemberId");
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        
        if (memberId == null || isLoggedIn == null || !isLoggedIn) {
            return "redirect:/api/v1/auth/member-login";
        }
        
        // 後端驗證信用卡資料
        List<String> validationErrors = validateCreditCardData(cardNumber, expiryMonth, expiryYear, cvv);
        
        if (!validationErrors.isEmpty()) {
            // 將驗證錯誤傳回前端
            redirectAttributes.addFlashAttribute("validationErrors", validationErrors);
            redirectAttributes.addFlashAttribute("cardNumber", cardNumber);
            redirectAttributes.addFlashAttribute("expiryMonth", expiryMonth);
            redirectAttributes.addFlashAttribute("expiryYear", expiryYear);
            redirectAttributes.addFlashAttribute("cvv", cvv);
            return "redirect:/orders/pay";
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
            
            // 【修正】將訂單ID保存到 Session 中，供付款成功頁面使用
            session.setAttribute("currentOrderId", orderId);
            
            return "redirect:/orders/payment-success";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("paymentError", "付款處理失敗，請稍後再試");
            return "redirect:/orders/pay";
        }
    }
    
    // 信用卡資料驗證方法
    private List<String> validateCreditCardData(String cardNumber, String expiryMonth, String expiryYear, String cvv) {
        List<String> errors = new ArrayList<>();
        
        // 驗證信用卡號碼 - 必須是16位數字
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            errors.add("請輸入信用卡號碼");
        } else {
            // 移除空格和非數字字符
            String cleanCardNumber = cardNumber.replaceAll("\\D", "");
            if (cleanCardNumber.length() != 16) {
                errors.add("信用卡號碼必須為16位數字");
            }
            // 不使用Luhn算法，允許任何16位數字組合
        }
        
        // 驗證有效期限 - 不能過期
        if (expiryMonth == null || expiryMonth.trim().isEmpty()) {
            errors.add("請選擇有效期限月份");
        } else {
            try {
                int month = Integer.parseInt(expiryMonth);
                if (month < 1 || month > 12) {
                    errors.add("有效期限月份必須在1-12之間");
                }
            } catch (NumberFormatException e) {
                errors.add("有效期限月份格式不正確");
            }
        }
        
        if (expiryYear == null || expiryYear.trim().isEmpty()) {
            errors.add("請選擇有效期限年份");
        } else {
            try {
                int year = Integer.parseInt(expiryYear);
                int currentYear = java.time.Year.now().getValue();
                if (year < currentYear) {
                    errors.add("信用卡已過期");
                } else if (year == currentYear && expiryMonth != null && !expiryMonth.trim().isEmpty()) {
                    try {
                        int month = Integer.parseInt(expiryMonth);
                        int currentMonth = java.time.LocalDate.now().getMonthValue();
                        if (month < currentMonth) {
                            errors.add("信用卡已過期");
                        }
                    } catch (NumberFormatException e) {
                        // 月份格式錯誤，已在上面處理
                    }
                }
            } catch (NumberFormatException e) {
                errors.add("有效期限年份格式不正確");
            }
        }
        
        // 驗證CVV - 必須是3位數字
        if (cvv == null || cvv.trim().isEmpty()) {
            errors.add("請輸入安全碼(CVV)");
        } else {
            String cleanCvv = cvv.replaceAll("\\D", "");
            if (cleanCvv.length() != 3) {
                errors.add("安全碼必須為3位數字");
            }
        }
        
        return errors;
    }
    
    @GetMapping("/payment-success")
    public String showPaymentSuccess(Model model, HttpSession session) {
        // 【修正】從 URL 參數中獲取訂單ID，或者使用 Session 備份
        String orderId = (String) session.getAttribute("currentOrderId");
        
        // 如果 Session 中沒有訂單ID，重定向到菜單頁面
        if (orderId == null) {
            return "redirect:/menu";
        }
        
        try {
            // 獲取訂單資訊
            OrderListEntity order = orderListService.getOrderById(orderId).orElse(null);
            if (order != null) {
                // 使用正確的方法名稱獲取訂單明細
                List<OrderListInfoEntity> orderItems = orderListInfoService.getDetailsForOrder(orderId);
                
                model.addAttribute("orderId", orderId);
                model.addAttribute("order", order);
                model.addAttribute("orderItems", orderItems);
                
                // 清除session中的訂單ID（防止重複訪問）
                session.removeAttribute("currentOrderId");
            } else {
                // 如果找不到訂單，重定向到菜單頁面
                return "redirect:/menu";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("orderId", orderId);
        }
        
        return "front-end/orders/payment-success";
    }
    
    /**
     * 保存餐點評分
     */
    @PostMapping("/save-rating")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveRating(
            @RequestParam("orderInfoId") Long orderInfoId,
            @RequestParam("rating") Integer rating,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 檢查用戶是否已登入
            Long memberId = (Long) session.getAttribute("loggedInMemberId");
            Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
            
            if (memberId == null || isLoggedIn == null || !isLoggedIn) {
                response.put("success", false);
                response.put("message", "請先登入");
                return ResponseEntity.status(401).body(response);
            }
            
            // 驗證評分範圍
            if (rating < 1 || rating > 5) {
                response.put("success", false);
                response.put("message", "評分必須在1-5之間");
                return ResponseEntity.status(400).body(response);
            }
            
            // 【修正】使用 addReview 方法來更新評分
            try {
                OrderListInfoEntity updatedOrderInfo = orderListInfoService.addReview(orderInfoId, rating.longValue());
                
                // 檢查訂單是否屬於當前會員
                if (!updatedOrderInfo.getOrderList().getMember().getMemberId().equals(memberId)) {
                    response.put("success", false);
                    response.put("message", "無權限操作此訂單");
                    return ResponseEntity.status(403).body(response);
                }
                
                response.put("success", true);
                response.put("message", "評分已保存");
                return ResponseEntity.ok(response);
                
            } catch (IllegalArgumentException e) {
                response.put("success", false);
                response.put("message", e.getMessage());
                return ResponseEntity.status(400).body(response);
            } catch (IllegalStateException e) {
                response.put("success", false);
                response.put("message", e.getMessage());
                return ResponseEntity.status(400).body(response);
            } catch (Exception e) {
                response.put("success", false);
                response.put("message", "找不到訂單明細");
                return ResponseEntity.status(404).body(response);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "保存評分時發生錯誤");
            return ResponseEntity.status(500).body(response);
        }
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
    
    // 遮罩信用卡號碼 (只顯示後4位)
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        String cleanCardNumber = cardNumber.replaceAll("\\D", "");
        if (cleanCardNumber.length() >= 4) {
            return "************" + cleanCardNumber.substring(cleanCardNumber.length() - 4);
        }
        return "****";
    }
    
    // 從電話號碼生成取餐號碼 (取末三位數字)
    private Long generatePickupNumberFromPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return 999L; // 預設值
        }
        
        // 移除非數字字符
        String cleanPhone = phone.replaceAll("\\D", "");
        
        if (cleanPhone.length() >= 3) {
            String lastThreeDigits = cleanPhone.substring(cleanPhone.length() - 3);
            try {
                return Long.parseLong(lastThreeDigits);
            } catch (NumberFormatException e) {
                return 999L; // 預設值
            }
        } else {
            return 999L; // 預設值
        }
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

package com.eatfast.cart.dto;

import com.eatfast.common.enums.StoreStatus; // 確保引入 StoreStatus，即使目前 CartDTOs 內部的 DTO 未直接使用
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 購物車相關的所有數據傳輸物件 (DTOs) 集合。
 * 包含請求 (Request) 和響應 (Response) 相關的 DTO。
 */
public class CartDTO { // 主類別名稱

    /**
     * 用於將餐點加入購物車的請求 DTO。
     */
    public static class AddToCartRequest {
        @NotNull(message = "會員ID不可為空")
        private Long memberId;

        @NotNull(message = "餐點ID不可為空")
        private Long mealId;

        @NotNull(message = "門市ID不可為空")
        private Long storeId;

        @NotNull(message = "數量不可為空")
        @Min(value = 1, message = "數量至少為 1")
        private Long quantity;

        @Size(max = 255, message = "客製化備註長度不可超過 255 字元")
        private String mealCustomization;

        public AddToCartRequest() {}

        public Long getMemberId() { return memberId; }
        public void setMemberId(Long memberId) { this.memberId = memberId; }
        public Long getMealId() { return mealId; }
        public void setMealId(Long mealId) { this.mealId = mealId; }
        public Long getStoreId() { return storeId; }
        public void setStoreId(Long storeId) { this.storeId = storeId; }
        public Long getQuantity() { return quantity; }
        public void setQuantity(Long quantity) { this.quantity = quantity; }
        public String getMealCustomization() { return mealCustomization; }
        public void setMealCustomization(String mealCustomization) { this.mealCustomization = mealCustomization; }
    }

    /**
     * 用於更新購物車項目（數量或備註）的請求 DTO。
     */
    public static class UpdateCartItemRequest {
        @NotNull(message = "數量不可為空")
        @Min(value = 0, message = "數量不可小於 0")
        private Long quantity;

        @Size(max = 255, message = "客製化備註長度不可超過 255 字元")
        private String mealCustomization;

        public UpdateCartItemRequest() {}

        public Long getQuantity() { return quantity; }
        public void setQuantity(Long quantity) { this.quantity = quantity; }
        public String getMealCustomization() { return mealCustomization; }
        public void setMealCustomization(String mealCustomization) { this.mealCustomization = mealCustomization; }
    }

    /**
     * 用於表示購物車項目的響應 DTO。
     */
    public static class CartItemDto {
        private String cartId;
        private Long memberId;
        private String memberUsername;
        private Long mealId;
        private String mealName;
        private Long mealPrice;
        private Long storeId;
        private String storeName;
        private Long quantity;
        private String mealCustomization;
        private LocalDateTime createdAt;
        private String mealPicUrl;
        
        
        
        //空的建構子
        public CartItemDto() {}

        public String getCartId() { return cartId; }
        public void setCartId(String cartId) { this.cartId = cartId; }
        public Long getMemberId() { return memberId; }
        public void setMemberId(Long memberId) { this.memberId = memberId; }
        public String getMemberUsername() { return memberUsername; }
        public void setMemberUsername(String memberUsername) { this.memberUsername = memberUsername; }
        public Long getMealId() { return mealId; }
        public void setMealId(Long mealId) { this.mealId = mealId; }
        public String getMealName() { return mealName; }
        public void setMealName(String mealName) { this.mealName = mealName; }
        public Long getMealPrice() { return mealPrice; }
        public void setMealPrice(Long mealPrice) { this.mealPrice = mealPrice; }
        public Long getStoreId() { return storeId; }
        public void setStoreId(Long storeId) { this.storeId = storeId; }
        public String getStoreName() { return storeName; }
        public void setStoreName(String storeName) { this.storeName = storeName; }
        public Long getQuantity() { return quantity; }
        public void setQuantity(Long quantity) { this.quantity = quantity; }
        public String getMealCustomization() { return mealCustomization; }
        public void setMealCustomization(String mealCustomization) { this.mealCustomization = mealCustomization; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public String getMealPicUrl() { return mealPicUrl; } // 【新增】
        public void setMealPicUrl(String mealPicUrl) { this.mealPicUrl = mealPicUrl; } // 【新增】
    }

    /**
     * Redis 中購物車 Hash 的 Value 結構。
     * 【關鍵修正】: 此類別已移至 CartDTO 的第一層靜態內部類，與其他 DTOs 平級。
     */
    public static class CartItemRedisData {
        private Long quantity;
        private String mealCustomization;

        public CartItemRedisData() {}
        public CartItemRedisData(Long quantity, String mealCustomization) {
            this.quantity = quantity;
            this.mealCustomization = mealCustomization;
        }

        public Long getQuantity() { return quantity; }
        public void setQuantity(Long quantity) { this.quantity = quantity; }
        public String getMealCustomization() { return mealCustomization; }
        public void setMealCustomization(String mealCustomization) { this.mealCustomization = mealCustomization; }
    }
    
    
 // 【新增這個類別】用於接收前端「更新數量」的請求
    public static class CartUpdateRequest {
        private Long memberId;
        private Long mealId;
        private Long storeId;
        private Long quantity;
        
        // Getters and Setters...
        public Long getMemberId() { return memberId; }
        public void setMemberId(Long memberId) { this.memberId = memberId; }
        public Long getMealId() { return mealId; }
        public void setMealId(Long mealId) { this.mealId = mealId; }
        public Long getStoreId() { return storeId; }
        public void setStoreId(Long storeId) { this.storeId = storeId; }
        public Long getQuantity() { return quantity; }
        public void setQuantity(Long quantity) { this.quantity = quantity; }
    }

    // 【新增這個類別】用於接收前端「刪除項目」的請求
    public static class CartRemoveRequest {
        private Long memberId;
        private Long mealId;
        private Long storeId;

        // Getters and Setters...
        public Long getMemberId() { return memberId; }
        public void setMemberId(Long memberId) { this.memberId = memberId; }
        public Long getMealId() { return mealId; }
        public void setMealId(Long mealId) { this.mealId = mealId; }
        public Long getStoreId() { return storeId; }
        public void setStoreId(Long storeId) { this.storeId = storeId; }
    }
}
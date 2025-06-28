/*
 * ================================================================
 * 檔案: CartService.java (介面同步修正)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/cart/service/CartService.java
 * - 修正總結:
 * 1. 【合約同步】: 將 calculateTotalAmount 方法的回傳型別從 Integer
 * 修改為 Long，以匹配實作類別 CartServiceImpl 的變更，解決
 * "incompatible return type" 編譯錯誤。
 */
package com.eatfast.cart.service;

import com.eatfast.cart.model.CartEntity;
import java.util.List;

/**
 * 購物車核心業務的合約介面。
 * <p>
 * 定義了所有購物車服務必須提供的標準功能。
 */
public interface CartService {

    /**
     * 根據會員 ID 獲取其完整的購物車內容。
     *
     * @param memberId 會員的主鍵 ID。
     * @return 包含該會員所有購物車項目的 List。
     */
    List<CartEntity> getCartByMemberId(Long memberId);
    
    /**
     * 新增一個商品至購物車，或更新已存在商品的數量。
     *
     * @param newItem 包含會員、餐點與數量資訊的 CartEntity 物件。
     */
    void addOrUpdateCartItem(CartEntity newItem);
    
    /**
     * 清空指定會員的所有購物車項目。
     *
     * @param memberId 欲清空購物車的會員主鍵 ID。
     */
    void clearCart(Long memberId);
    
    /**
     * 計算指定會員購物車內所有項目的總金額。
     *
     * @param memberId 會員的主鍵 ID。
     * @return 購物車的總金額，型別為 Long 以避免資料溢位。
     */
    Long calculateTotalAmount(Long memberId); // 【修正】: 回傳型別已改為 Long
}
/*
 * ================================================================
 * 檔案: CartServiceImpl.java (經 final 審查與重構)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/cart/service/CartServiceImpl.java
 * - 重構總結:
 * 1. 【型別修正】: 將 calculateTotalAmount 方法的回傳型別從 Integer
 * 修改為 Long，以解決 long to int 的型別不符錯誤，並預防資料溢位。
 * 2. 【寫法重構】: 遵照使用者要求，將 calculateTotalAmount 方法的
 * 實作從 Stream API 改為傳統的 for-each 迴圈，提升特定情境下的
 * 直觀性與可讀性。
 * 3. 【結構確認】: 其餘方法的結構與邏輯維持不變，持續遵循依賴注入
 * 與交易管理的最佳實踐。
 */
package com.eatfast.cart.service;

import com.eatfast.cart.model.CartEntity;
import com.eatfast.cart.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 購物車核心業務邏輯的實作。
 * <p>
 * 負責處理購物車的所有業務操作，如新增、查詢、刪除及計算。
 */
@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }
    
    @Override
    public List<CartEntity> getCartByMemberId(Long memberId) {
        return cartRepository.findByMember_MemberId(memberId);
    }
    
    @Override
    @Transactional
    public void addOrUpdateCartItem(CartEntity newItem) {
        Optional<CartEntity> existingItemOpt = cartRepository.findByMember_MemberIdAndMeal_MealId(
            newItem.getMember().getMemberId(),
            newItem.getMeal().getMealId()
        );

        if (existingItemOpt.isPresent()) {
            CartEntity itemToUpdate = existingItemOpt.get();
            itemToUpdate.setQuantity(itemToUpdate.getQuantity() + newItem.getQuantity());
            itemToUpdate.setMealCustomization(newItem.getMealCustomization());
            cartRepository.save(itemToUpdate);
        } else {
            cartRepository.save(newItem);
        }
    }
    
    @Override
    @Transactional
    public void clearCart(Long memberId) {
        cartRepository.deleteByMember_MemberId(memberId);
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * <b>【重構說明】</b>:
     * <ul>
     * <li>回傳型別已改為 <b>Long</b>，以避免金額加總後超過 Integer 上限。</li>
     * <li>已改為傳統 <b>for-each 迴圈</b>寫法。</li>
     * </ul>
     */
    @Override
    public Long calculateTotalAmount(Long memberId) {
        // 步驟 1: 透過 Repository 取得該會員的所有購物車項目。
        List<CartEntity> cartList = cartRepository.findByMember_MemberId(memberId);
        
        // 步驟 2: 初始化一個 long 型別的變數來儲存總金額，並以 "L" 字尾表示為 long。
        long totalAmount = 0L; 

        // 步驟 3: 使用傳統的 for-each 迴圈迭代購物車中的每一個項目。
        for (CartEntity item : cartList) {
            // 步驟 4: 累加每個品項的金額 (單價 * 數量)。
            // 為確保相乘結果不溢位，可將其中一值先轉為 long。
            totalAmount += (long) item.getMeal().getMealPrice() * item.getQuantity();
        }
        
        // 步驟 5: 回傳計算後的總金額。
        return totalAmount;
    }
}
/*
 * ================================================================
 * 檔案 3: CartServiceImpl.java (★★ 編譯錯誤已修正 ★★)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/cart/service/CartServiceImpl.java
 */
package com.eatfast.cart.service;

import com.eatfast.cart.model.CartEntity;
import com.eatfast.cart.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    // 依賴注入的最佳實踐：使用 final 和建構子注入。
    private final CartRepository cartRepository;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }
    
    @Override
    public List<CartEntity> getCartByMemberId(Long memberId) {
        return cartRepository.findByMemberId(memberId);
    }
    
    @Override
    @Transactional // 確保「讀取-判斷-寫入」在同一個交易中。
    public void addOrUpdateCartItem(CartEntity newItem) {
        
        // 1. 使用精準查詢，直接定位目標項目。
        // 【修正】: 呼叫 findByMemberIdAndMealId 時，傳入的參數型別 (Long, Long)
        //          現在與 Repository 中定義的方法簽名完全匹配。
        Optional<CartEntity> existingItemOpt = cartRepository.findByMemberIdAndMealId(
            newItem.getMember().getMemberId(),
            newItem.getMeal().getMealId()
        );

        // 2. 根據是否存在來決定是更新數量還是新增項目。
        if (existingItemOpt.isPresent()) {
            CartEntity itemToUpdate = existingItemOpt.get();
            itemToUpdate.setQuantity(itemToUpdate.getQuantity() + newItem.getQuantity());
            itemToUpdate.setMealCustomization(newItem.getMealCustomization());
            cartRepository.save(itemToUpdate);
        } else {
            // @CreationTimestamp 會在此時自動設定 createdAt。
            cartRepository.save(newItem);
        }
    }
    
    @Override
    @Transactional
    public void clearCart(Long memberId) {
        cartRepository.deleteByMemberId(memberId);
    }
    
    @Override
    public Integer calculateTotalAmount(Long memberId) {
        List<CartEntity> cartList = cartRepository.findByMemberId(memberId);
        
        // 使用 Java Stream API 進行計算總金額。
        return cartList.stream()
                     // 【修正】: 將 getMyMealPrice() 改為 getMealPrice()。
                     // 這假設 MealEntity 中的 getter 方法遵循標準的 JavaBeans 命名慣例。
                     // (即 private Long mealPrice; -> public Long getMealPrice())
                     .mapToInt(item -> item.getMeal().getMealPrice().intValue() * item.getQuantity().intValue())
                     .sum();
    }
}

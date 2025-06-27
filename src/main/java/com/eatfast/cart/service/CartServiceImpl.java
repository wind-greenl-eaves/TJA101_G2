package com.eatfast.cart.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eatfast.cart.model.CartEntity;
import com.eatfast.cart.repository.CartRepository;

import jakarta.transaction.Transactional;

@Service
public class CartServiceImpl implements CartService {

    private String getCartKey(Integer memberId) {
        return "cart:" + memberId;
    }

    //從資料庫取得會員的購物車清單（目前模擬資料，實際應改為呼叫 repository）
    @Autowired
    private CartRepository cartRepository;

    @Override
    public List<CartEntity> getCartByMemberId(Integer memberId) {
        return cartRepository.findByMemberId(memberId);
    }

    //新增或更新購物車商品：若商品已存在，更新數量，否則新增
    @Override
    @Transactional
    public void addOrUpdateCartItem(CartEntity newItem) {
        List<CartEntity> cartList = cartRepository.findByMemberId(newItem.getMemberId());
        
        for (CartEntity item : cartList) {
            if (item.getMealId().equals(newItem.getMealId())) {
                item.setAmount(item.getAmount() + newItem.getAmount());
                item.setMealCustomization(newItem.getMealCustomization());
                item.setCreateTime(new Timestamp(System.currentTimeMillis()));
                cartRepository.save(item);
                return;
            }
        }

        newItem.setCreateTime(new Timestamp(System.currentTimeMillis()));
        cartRepository.save(newItem);
    }


    //清除整個會員購物車
    @Override
    public void clearCart(Integer memberId) {
        cartRepository.deleteByMemberId(memberId);
    }

    //計算總金額（每項假設價格為 100 元）
    @Override
    public Integer calculateTotalAmount(Integer memberId) {
        List<CartEntity> cartList = cartRepository.findByMemberId(memberId);
        int pricePerMeal = 100; // TODO: 可改為查 Meal 資料表
        return cartList.stream().mapToInt(item -> item.getAmount() * pricePerMeal).sum();
    }

} 













//package com.eatfast.cart.service;
//
//import java.sql.Timestamp;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.concurrent.TimeUnit;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//
//import com.eatfast.cart.service.CartService;
//import com.eatfast.cart.model.CartEntity;
//
//@Service
//public class CartServiceImpl implements CartService {
//    @Autowired
//    private RedisTemplate<String, List<CartEntity>> redisTemplate;
//
//    private String getCartKey(Integer memberId) {
//        return "cart:" + memberId;
//    }
//
//
//    //從 Redis 取得會員的購物車清單
//
//    @Override
//    public List<CartEntity> getCartByMemberId(Integer memberId) {
//        String key = getCartKey(memberId);
//        List<CartEntity> cartList = redisTemplate.opsForValue().get(key);
//        return cartList != null ? cartList : new ArrayList<>();
//    }
//
//
//    //新增或更新購物車商品：若商品已存在，更新數量，否則新增
//
//    @Override
//    public void addOrUpdateCartItem(CartEntity cart) {
//        String key = getCartKey(cart.getMemberId());
//        List<CartEntity> cartList = getCartByMemberId(cart.getMemberId());
//
//        boolean updated = false;
//        for (CartEntity item : cartList) {
//            if (item.getMealId().equals(cart.getMealId())) {
//                item.setAmount(item.getAmount() + cart.getAmount());
//                item.setMealCustomization(cart.getMealCustomization());
//                item.setCreateTime(new Timestamp(System.currentTimeMillis()));
//                updated = true;
//                break;
//            }
//        }
//
//        if (!updated) {
//            cart.setCreateTime(new Timestamp(System.currentTimeMillis()));
//            cartList.add(cart);
//        }
//
//        redisTemplate.opsForValue().set(key, cartList, 7, TimeUnit.DAYS);
//    }
//
//    //清除整個會員購物車
//  
//    @Override
//    public void clearCart(Integer memberId) {
//        redisTemplate.delete(getCartKey(memberId));
//    }
//
//    //計算總金額（每項假設價格為 100 元）
//    @Override
//    public Integer calculateTotalAmount(Integer memberId) {
//        List<CartEntity> cartList = getCartByMemberId(memberId);
//        int price = 100;   //實際可從 Meal 資料表取得
//        return cartList.stream().mapToInt(item -> item.getAmount() * price).sum();
//    }
//}

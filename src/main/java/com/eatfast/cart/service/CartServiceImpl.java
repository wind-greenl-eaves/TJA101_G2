package com.eatfast.cart.service;

import com.eatfast.cart.dto.CartDTO;
import com.eatfast.cart.dto.CartDTO.AddToCartRequest;
import com.eatfast.cart.dto.CartDTO.CartItemDto;
import com.eatfast.cart.dto.CartDTO.UpdateCartItemRequest;
import com.eatfast.cart.dto.CartDTO.CartItemRedisData;
import com.eatfast.cart.model.CartEntity;
import com.eatfast.cart.repository.CartRepository;

import com.eatfast.member.model.MemberEntity;
import com.eatfast.meal.model.MealEntity;
import com.eatfast.store.model.StoreEntity;

import com.eatfast.member.repository.MemberRepository;
import com.eatfast.meal.model.MealRepository;
import com.eatfast.store.repository.StoreRepository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private final MemberRepository memberRepository;
    private final MealRepository mealRepository;
    private final StoreRepository storeRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CART_REDIS_KEY_PREFIX = "cart:";
    private static final long CART_TTL_SECONDS = TimeUnit.DAYS.toSeconds(7); 

    public CartServiceImpl(CartRepository cartRepository,
                           MemberRepository memberRepository,
                           MealRepository mealRepository,
                           StoreRepository storeRepository,
                           RedisTemplate<String, Object> redisTemplate) {
        this.memberRepository = memberRepository;
        this.mealRepository = mealRepository;
        this.storeRepository = storeRepository;
        this.redisTemplate = redisTemplate;
    }

    private String getCartRedisKey(Long memberId) {
        return CART_REDIS_KEY_PREFIX + memberId;
    }

    private String getCartItemHashField(Long mealId, Long storeId) {
        return mealId + ":" + storeId;
    }

    @Override
    @Transactional
    public CartItemDto addOrUpdateCartItem(AddToCartRequest request) {
    	System.out.println("Received request: " + request);
    	System.out.println("mealId: " + request.getMealId());
    	System.out.println("memberId: " + request.getMemberId());
    	System.out.println("quantity: " + request.getQuantity());

    	
    	
    	
    	MemberEntity member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("會員不存在: " + request.getMemberId()));
        MealEntity meal = mealRepository.findById(request.getMealId())
                .orElseThrow(() -> new IllegalArgumentException("餐點不存在: " + request.getMealId()));
        StoreEntity store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("門市不存在: " + request.getStoreId()));

        String cartKey = getCartRedisKey(request.getMemberId());
        String hashField = getCartItemHashField(request.getMealId(), request.getStoreId());

        CartItemRedisData existingData = (CartItemRedisData) redisTemplate.opsForHash().get(cartKey, hashField);

        Long newQuantity = request.getQuantity();
        String customization = request.getMealCustomization();

        if (existingData != null) {
            newQuantity += existingData.getQuantity();
            if (customization == null || customization.isEmpty()) {
                customization = existingData.getMealCustomization();
            }
        }
        
        CartItemRedisData newData = new CartItemRedisData(newQuantity, customization);
        
        redisTemplate.opsForHash().put(cartKey, hashField, newData);
        redisTemplate.expire(cartKey, CART_TTL_SECONDS, TimeUnit.SECONDS);

        CartItemDto dto = new CartItemDto();
        dto.setMemberId(member.getMemberId());
        dto.setMemberUsername(member.getUsername());
        dto.setMealId(meal.getMealId());
        dto.setMealName(meal.getMealName());
        dto.setStoreId(store.getStoreId());
        dto.setStoreName(store.getStoreName());
        dto.setQuantity(newData.getQuantity());
        dto.setMealCustomization(newData.getMealCustomization());
        dto.setCreatedAt(LocalDateTime.now()); 
        dto.setCartId(null); 
        dto.setMealPicUrl(getMealImageUrl(meal.getMealId())); 

        return dto;
    }

    @Override
    public List<CartItemDto> getCartItemsByMember(Long memberId) {
        String cartKey = getCartRedisKey(memberId);
        Map<Object, Object> cartHash = redisTemplate.opsForHash().entries(cartKey);

        List<CartItemDto> cartItems = new ArrayList<>();
        if (cartHash.isEmpty()) {
            return cartItems;
        }

        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("會員不存在: " + memberId));

        for (Map.Entry<Object, Object> entry : cartHash.entrySet()) {
            String hashField = (String) entry.getKey();
            CartItemRedisData data = (CartItemRedisData) entry.getValue();

            String[] ids = hashField.split(":");
            if (ids.length != 2) continue;

            Long mealId = Long.parseLong(ids[0]);
            Long storeId = Long.parseLong(ids[1]);

            Optional<MealEntity> mealOpt = mealRepository.findById(mealId);
            Optional<StoreEntity> storeOpt = storeRepository.findById(storeId);

            if (mealOpt.isPresent() && storeOpt.isPresent()) {
                MealEntity meal = mealOpt.get();
                StoreEntity store = storeOpt.get();

                CartItemDto dto = new CartItemDto();
                dto.setMemberId(member.getMemberId());
                dto.setMemberUsername(member.getUsername());
                dto.setMealId(meal.getMealId());
                dto.setMealName(meal.getMealName());
                dto.setStoreId(store.getStoreId());
                dto.setStoreName(store.getStoreName());
                dto.setQuantity(data.getQuantity());
                dto.setMealCustomization(data.getMealCustomization());
                dto.setCreatedAt(null);
                dto.setCartId(null);
                dto.setMealPicUrl(getMealImageUrl(meal.getMealId()));

                cartItems.add(dto);
            }
        }
        redisTemplate.expire(cartKey, CART_TTL_SECONDS, TimeUnit.SECONDS);
        return cartItems;
    }

    // 【關鍵修正】: 實作 updateCartItemByKeys 方法
    @Override // 必須有 @Override 註解
    @Transactional
    public CartItemDto updateCartItemByKeys(Long memberId, Long storeId, Long mealId, UpdateCartItemRequest request) {
        // 1. 驗證關聯實體是否存在（從資料庫獲取，確保 ID 有效）
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("會員不存在: " + memberId));
        MealEntity meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new IllegalArgumentException("餐點不存在: " + mealId));
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("門市不存在: " + storeId));

        String cartKey = getCartRedisKey(memberId);
        String hashField = getCartItemHashField(mealId, storeId);

        CartItemRedisData existingData = (CartItemRedisData) redisTemplate.opsForHash().get(cartKey, hashField);

        if (existingData == null) {
            throw new IllegalArgumentException("購物車項目不存在或已過期: member=" + memberId + ", meal=" + mealId + ", store=" + storeId);
        }

        Long newQuantity = request.getQuantity();
        String customization = request.getMealCustomization();

        if (newQuantity == 0) {
            redisTemplate.opsForHash().delete(cartKey, hashField); // 數量為0則從購物車移除
            redisTemplate.expire(cartKey, CART_TTL_SECONDS, TimeUnit.SECONDS); // 重設 TTL
            return null; // 表示項目被移除
        }
        
        CartItemRedisData newData = new CartItemRedisData(newQuantity, customization != null ? customization : existingData.getMealCustomization());
        redisTemplate.opsForHash().put(cartKey, hashField, newData);
        redisTemplate.expire(cartKey, CART_TTL_SECONDS, TimeUnit.SECONDS);

        // 構建並返回更新後的 DTO
        CartItemDto dto = new CartItemDto();
        dto.setMemberId(member.getMemberId());
        dto.setMemberUsername(member.getUsername());
        dto.setMealId(meal.getMealId());
        dto.setMealName(meal.getMealName());
        dto.setStoreId(store.getStoreId());
        dto.setStoreName(store.getStoreName());
        dto.setQuantity(newData.getQuantity());
        dto.setMealCustomization(newData.getMealCustomization());
        dto.setCreatedAt(LocalDateTime.now()); 
        dto.setCartId(null); 
        dto.setMealPicUrl(getMealImageUrl(meal.getMealId())); 

        return dto;
    }

    @Override
    @Transactional
    public void removeCartItemByKeys(Long memberId, Long storeId, Long mealId) {
        String cartKey = getCartRedisKey(memberId);
        String hashField = getCartItemHashField(mealId, storeId);

        Long deletedCount = redisTemplate.opsForHash().delete(cartKey, hashField);
        if (deletedCount == 0) {
            throw new IllegalArgumentException("購物車項目不存在或已過期，無需移除: member=" + memberId + ", meal=" + mealId + ", store=" + storeId);
        }
        redisTemplate.expire(cartKey, CART_TTL_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    @Transactional
    public void clearCartByMember(Long memberId) {
        String cartKey = getCartRedisKey(memberId);
        redisTemplate.delete(cartKey);
    }

    private String getMealImageUrl(Long mealId) {
        // 替換為您的實際 context path
        return "/demo/api/meals/" + mealId + "/image"; 
    }
    
    //購物車客製化備註欄為自動更新到redis
    @Override
    public void updateAllCartItemsCustomization(Long memberId, String mealCustomization) {
        String redisKey = "cart:" + memberId;
        Map<Object, Object> cartMap = redisTemplate.opsForHash().entries(redisKey);

        for (Object key : cartMap.keySet()) {
            CartDTO.CartItemRedisData data = (CartDTO.CartItemRedisData) cartMap.get(key);
            data.setMealCustomization(mealCustomization);
            redisTemplate.opsForHash().put(redisKey, key, data);
        }
    }

    
    

    private CartItemDto convertToDto(CartEntity cartEntity) {
        CartItemDto dto = new CartItemDto();
        dto.setCartId(cartEntity.getCartId());
        dto.setQuantity(cartEntity.getQuantity());
        dto.setMealCustomization(cartEntity.getMealCustomization());
        dto.setCreatedAt(cartEntity.getCreatedAt());

        if (cartEntity.getMember() != null) {
            dto.setMemberId(cartEntity.getMember().getMemberId());
            dto.setMemberUsername(cartEntity.getMember().getUsername());
        }
        if (cartEntity.getMeal() != null) {
            dto.setMealId(cartEntity.getMeal().getMealId());
            dto.setMealName(cartEntity.getMeal().getMealName());
            dto.setMealPicUrl(getMealImageUrl(cartEntity.getMeal().getMealId()));
        }
        if (cartEntity.getStore() != null) {
            dto.setStoreId(cartEntity.getStore().getStoreId());
            dto.setStoreName(cartEntity.getStore().getStoreName());
        }
        return dto;
    }
}
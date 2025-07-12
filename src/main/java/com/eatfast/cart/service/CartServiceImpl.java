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
    private final CartRepository cartRepository;

    private static final String CART_REDIS_KEY_PREFIX = "cart:";
    private static final long CART_TTL_SECONDS = TimeUnit.DAYS.toSeconds(7);

    public CartServiceImpl(
                           MemberRepository memberRepository,
                           MealRepository mealRepository,
                           StoreRepository storeRepository,
                           RedisTemplate<String, Object> redisTemplate,
                           CartRepository cartRepository) {
        this.memberRepository = memberRepository;
        this.mealRepository = mealRepository;
        this.storeRepository = storeRepository;
        this.redisTemplate = redisTemplate;
        this.cartRepository = cartRepository;
    }
    
    
 // 【新增】實作根據 mealId 獲取餐點資訊的方法
    // ================================================================
    @Override
    public CartItemDto prepareItemForCart(Long mealId) {
        // 1. 使用已注入的 mealRepository 查詢餐點，若找不到則拋出例外
        MealEntity meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("找不到 ID 為 " + mealId + " 的餐點"));

        // 2. 建立一個我們之前修改好的 DTO 物件
        CartItemDto dto = new CartItemDto();

        // 3. 將從 MealEntity 查到的資料，填入 DTO 中
        dto.setMealId(meal.getMealId());
        dto.setMealName(meal.getMealName());
        dto.setMealPrice(meal.getMealPrice()); // 【重點】設定我們新增的價格欄位
        dto.setMealPicUrl(getMealImageUrl(meal.getMealId())); // 【重點】設定圖片 URL (使用你既有的 helper method)
        dto.setQuantity(1L); // 新準備的項目，數量預設為 1 (注意型別為 Long)

        // 4. 回傳已填充好資料的 DTO
        return dto;
    }


    private String getCartRedisKey(Long memberId) {
        return CART_REDIS_KEY_PREFIX + memberId;
    }

    private String getCartItemHashField(Long mealId, Long storeId) {
        return mealId + ":" + storeId;
    }

    // 新增的唯一 cartId 產生方法，格式為 memberId:storeId:mealId
    private String generateCartId(Long memberId, Long storeId, Long mealId) {
        return memberId + ":" + storeId + ":" + mealId;
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
        dto.setCartId(generateCartId(member.getMemberId(), store.getStoreId(), meal.getMealId())); // 這裡改成帶 cartId
        dto.setMemberId(member.getMemberId());
        dto.setMemberUsername(member.getUsername());
        dto.setMealId(meal.getMealId());
        dto.setMealName(meal.getMealName());
        dto.setStoreId(store.getStoreId());
        dto.setStoreName(store.getStoreName());
        dto.setQuantity(newData.getQuantity());
        dto.setMealCustomization(newData.getMealCustomization());
        dto.setCreatedAt(LocalDateTime.now());
        dto.setMealPicUrl(getMealImageUrl(meal.getMealId()));

        return dto;
    }
    
    
    

    @Override
    public List<CartItemDto> getCartItemsByMember(Long memberId) {
        String redisKey = getCartRedisKey(memberId);
        Map<Object, Object> redisCartMap = redisTemplate.opsForHash().entries(redisKey);

        List<CartItemDto> result = new ArrayList<>();

        for (Map.Entry<Object, Object> entry : redisCartMap.entrySet()) {
            String key = (String) entry.getKey(); // e.g., "mealId:storeId"
            CartItemRedisData redisData = (CartItemRedisData) entry.getValue();

            String[] parts = key.split(":");
            if (parts.length != 2) continue;

            Long mealId = Long.parseLong(parts[0]);
            Long storeId = Long.parseLong(parts[1]);

            Optional<MealEntity> mealOpt = mealRepository.findById(mealId);
            Optional<StoreEntity> storeOpt = storeRepository.findById(storeId);
            Optional<MemberEntity> memberOpt = memberRepository.findById(memberId);

            if (mealOpt.isEmpty() || storeOpt.isEmpty() || memberOpt.isEmpty()) continue;

            MealEntity meal = mealOpt.get();
            StoreEntity store = storeOpt.get();
            MemberEntity member = memberOpt.get();

            CartItemDto dto = new CartItemDto();
            dto.setCartId(generateCartId(memberId, storeId, mealId)); // 使用 generateCartId
            dto.setMemberId(memberId);
            dto.setMemberUsername(member.getUsername());
            dto.setMealId(mealId);
            dto.setMealName(meal.getMealName());
            dto.setMealPrice(meal.getMealPrice()); 
            dto.setStoreId(storeId);
            dto.setStoreName(store.getStoreName());
            dto.setMealPicUrl(meal.getMealPic());
            dto.setQuantity(redisData.getQuantity());
            dto.setMealCustomization(redisData.getMealCustomization());

            result.add(dto);
        }

        return result;
    }

    @Override
    @Transactional
    public CartItemDto updateCartItemByKeys(Long memberId, Long storeId, Long mealId, UpdateCartItemRequest request) {
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
            redisTemplate.opsForHash().delete(cartKey, hashField);
            redisTemplate.expire(cartKey, CART_TTL_SECONDS, TimeUnit.SECONDS);
            return null;
        }

        CartItemRedisData newData = new CartItemRedisData(newQuantity, customization != null ? customization : existingData.getMealCustomization());
        redisTemplate.opsForHash().put(cartKey, hashField, newData);
        redisTemplate.expire(cartKey, CART_TTL_SECONDS, TimeUnit.SECONDS);

        CartItemDto dto = new CartItemDto();
        dto.setCartId(generateCartId(memberId, storeId, mealId)); // 這裡帶 cartId
        dto.setMemberId(member.getMemberId());
        dto.setMemberUsername(member.getUsername());
        dto.setMealId(meal.getMealId());
        dto.setMealName(meal.getMealName());
        dto.setStoreId(store.getStoreId());
        dto.setStoreName(store.getStoreName());
        dto.setQuantity(newData.getQuantity());
        dto.setMealCustomization(newData.getMealCustomization());
        dto.setCreatedAt(LocalDateTime.now());
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
    
    @Override
    @Transactional
    public CartItemDto updateCartItemQuantity(Long memberId, Long mealId, Long quantity) {
        // 透過呼叫已有的、操作 Redis 的方法來實現更新
        // 注意：這個方法需要 storeId，但目前的參數沒有。這表示前端的呼叫也需要調整。
        // 這裡我們先假設一個固定的 storeId，或您需要修改前端傳遞此參數。
        // 為了讓程式能動，我們先假設 storeId = 1L (這是一個臨時的硬編碼)
        Long tempStoreId = 1L; 
        
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(quantity);
        request.setMealCustomization(""); // 更新數量時，通常不修改備註

        // 直接呼叫操作 Redis 的 `updateCartItemByKeys` 方法
        return this.updateCartItemByKeys(memberId, tempStoreId, mealId, request);
    }
    
    @Override
    @Transactional
    public void removeCartItemByKeys(Long memberId, Long mealId) {
        // 同樣，我們需要 storeId 才能在 Redis 中定位到正確的項目
        // 這裡我們先假設一個固定的 storeId = 1L
        Long tempStoreId = 1L;
        
        // 直接呼叫操作 Redis 的 `removeCartItemByKeys` 方法
        this.removeCartItemByKeys(memberId, tempStoreId, mealId);
    }
    

    private CartItemDto convertToDto(CartEntity cartEntity) {
        CartItemDto dto = new CartItemDto();
        dto.setCartId(String.valueOf(cartEntity.getCartId()));
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
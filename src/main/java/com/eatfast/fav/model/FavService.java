package com.eatfast.fav.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eatfast.common.enums.MealStatus;
import com.eatfast.fav.dto.FavMealDTO;
import com.eatfast.meal.model.MealEntity;
import com.eatfast.meal.model.MealRepository;
import com.eatfast.meal.model.MealService;
import com.eatfast.member.repository.MemberRepository;

@Service
public class FavService {

    private final FavRepository favRepo;
    private final MealRepository mealRepo;
    private final MemberRepository memberRepo;
    private final MealService mealService;

    public FavService(FavRepository favRepo, MealRepository mealRepo, MemberRepository memberRepo, MealService mealService) {
        this.favRepo = favRepo;
        this.mealRepo = mealRepo;
        this.memberRepo = memberRepo;
        this.mealService = mealService;
    }

    // 取得指定會員的收藏餐點詳細資料(前端顯示用)
    public List<FavMealDTO> getFavMeals(Long memberId) {
        return favRepo.findByMemberMemberId(memberId)
        			  .stream()
        			  .filter(fav -> fav.getMeal().getStatus() == MealStatus.AVAILABLE) 
        			  .map(fav -> {
            MealEntity meal = fav.getMeal();
            FavMealDTO dto = new FavMealDTO();
            dto.setFavMealId(fav.getFavMealId());
            dto.setMealId(meal.getMealId());
            dto.setMealName(meal.getMealName());
            dto.setMealPrice(meal.getMealPrice());
            dto.setMealTypeId(meal.getMealType().getMealTypeId()); // 收藏分類

            String picUrl = mealService.buildMealPicUrl(meal.getMealPic());
            dto.setMealPicUrl(picUrl);

            return dto;
        }).collect(Collectors.toList());
    }

    // 新增會員收藏的餐點
    @Transactional
	public void addFav(Long memberId, Long mealId) {
		if (favRepo.existsByMemberMemberIdAndMealMealId(memberId, mealId)) {
			// 如果已經存在收藏，則不做任何操作
			return;
		}
		
		MealEntity meal = mealRepo.findById(mealId)
				.orElseThrow(() -> new IllegalArgumentException("找不到餐點: " + mealId));
		
		FavEntity fav = new FavEntity();
		fav.setMember(memberRepo.findById(memberId)
				.orElseThrow(() -> new IllegalArgumentException("找不到會員編號: " + memberId)));
		fav.setMeal(meal);
		
		favRepo.save(fav);		
	}
    
//    // 判斷某餐點有沒有被該會員收藏
//    public List<Long> getFavMealIds(Long memberId) {
//        if (memberId == null) return List.of();
//        return favRepo.findByMemberMemberId(memberId) 
//                      .stream() 
//                      .map(fav -> fav.getMeal().getMealId()) // 對每個收藏紀錄，取得對應的餐點物件（fav.getMeal()），再取得該餐點的ID
//                      .collect(Collectors.toList());  // 把上面那串mealId的Stream收集起來，變成一個List<Long>，回傳「該會員收藏過的所有餐點ID清單」
//    }
    
    // 取得會員收藏的餐點ID與收藏ID的對應關係
    public Map<Long, Long> getFavMealIdMap(Long memberId) {  // Key 是 mealId，Value 是 favMealId
        if (memberId == null) return Map.of();
        return favRepo.findByMemberMemberId(memberId) // 透過favRepo找到該會員的所有收藏紀錄
                      .stream()  // 把這個 List 轉成 Stream，不用寫 for 迴圈就能一行處理所有轉換或過濾的需求
                      .filter(fav -> fav.getMeal().getStatus() == MealStatus.AVAILABLE) // 過濾掉下架的餐點
                      .collect(Collectors.toMap(  // 把每個收藏紀錄轉換成一個 Map
                          fav -> fav.getMeal().getMealId(), // Key 是餐點ID
                          FavEntity::getFavMealId           // Value 是收藏ID
                      ));
    }


    // 移除會員收藏的餐點
	@Transactional
	public void removeFav(Long memberId, Long mealId) {
	    favRepo.deleteByMemberMemberIdAndMealMealId(memberId, mealId);
	}

	// 根據主鍵移除收藏
    @Transactional
    public void removeFavById(Long favMealId) {
        favRepo.deleteById(favMealId);
    }

}

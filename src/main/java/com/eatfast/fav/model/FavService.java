package com.eatfast.fav.model;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eatfast.fav.dto.FavMealDTO;
import com.eatfast.meal.model.MealEntity;
import com.eatfast.meal.model.MealRepository;
import com.eatfast.member.repository.MemberRepository;

@Service
public class FavService {

    private final FavRepository favRepo;
    private final MealRepository mealRepo;
    private final MemberRepository memberRepo;

    public FavService(FavRepository favRepo, MealRepository mealRepo, MemberRepository memberRepo) {
        this.favRepo = favRepo;
        this.mealRepo = mealRepo;
        this.memberRepo = memberRepo;
    }

    // 取得指定會員的收藏餐點列表
    public List<FavMealDTO> getFavMeals(Long memberId) {
        return favRepo.findByMemberMemberId(memberId).stream().map(fav -> {
            MealEntity meal = fav.getMeal();
            FavMealDTO dto = new FavMealDTO();
            dto.setMealId(meal.getMealId());
            dto.setMealName(meal.getMealName());
            dto.setMealPrice(meal.getMealPrice());

            String picUrl = "/mealPic/" + meal.getMealId();
            dto.setMealPicUrl(picUrl);

            return dto;
        }).collect(Collectors.toList());
    }

    // 移除會員收藏的餐點
    @Transactional
    public void removeFav(Long memberId, Long mealId) {
        favRepo.deleteByMemberMemberIdAndMealMealId(memberId, mealId);
    }
}

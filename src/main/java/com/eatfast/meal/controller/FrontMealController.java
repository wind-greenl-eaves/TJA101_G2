package com.eatfast.meal.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.eatfast.common.enums.MealStatus;
import com.eatfast.fav.model.FavService;
import com.eatfast.meal.dto.MealDTO;
import com.eatfast.meal.model.MealEntity;
import com.eatfast.meal.model.MealService;
import com.eatfast.mealtype.model.MealTypeEntity;
import com.eatfast.mealtype.service.MealTypeService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/menu")
public class FrontMealController {

    private final MealService mealService;
    private final MealTypeService mealTypeService;
    private final FavService favService;

    public FrontMealController(MealService mealService, MealTypeService mealTypeService, FavService favService) {
        this.mealService = mealService;
        this.mealTypeService = mealTypeService;
        this.favService = favService;
    }

    // 前台-菜單列表（分類過濾）
    @GetMapping("")
    public String showMenuList(
        @RequestParam(value = "type", required = false) Long typeId,
        Model model, HttpSession session) {

        List<MealEntity> mealList;
        String currentMealTypeName = null;

        if (typeId != null) {
            mealList = mealService.getMealsByStatus(MealStatus.AVAILABLE)
                .stream()
                .filter(meal -> meal.getMealType() != null && meal.getMealType().getMealTypeId().equals(typeId))
                .collect(Collectors.toList());
            MealTypeEntity mealType = mealTypeService.getOneMealType(typeId);
            if (mealType != null) {
                currentMealTypeName = mealType.getMealName();
            }
        } else {
            mealList = mealService.getMealsByStatus(MealStatus.AVAILABLE);
        }
        
        Long memberId = (Long) session.getAttribute("loggedInMemberId");
        Map<Long, Long> favMealIdMap = favService.getFavMealIdMap(memberId);
        
        List<MealDTO> mealDTOList = mealList.stream().map(meal -> {
            MealDTO dto = new MealDTO();
            dto.setMealId(meal.getMealId());
            dto.setMealName(meal.getMealName());
            dto.setMealPrice(meal.getMealPrice());
            dto.setMealTypeName(meal.getMealType().getMealName());
            dto.setMealPicUrl("/meal/mealPhoto?mealId=" + meal.getMealId());
            dto.setReviewTotalStars(meal.getReviewTotalStars());
            if (favMealIdMap.containsKey(meal.getMealId())) {
                dto.setFavored(true);
                dto.setFavMealId(favMealIdMap.get(meal.getMealId()));
            } else {
                dto.setFavored(false);
                dto.setFavMealId(null);
            }
            return dto;
        }).collect(Collectors.toList());

        model.addAttribute("mealListData", mealDTOList);

        model.addAttribute("currentMealTypeName", currentMealTypeName);
        return "front-end/menu/menu-list";
    }

    // 類別清單給前端模板用（分類側邊欄）
    @ModelAttribute("mealTypeListData")
    public List<MealTypeEntity> getAllMealTypes() {
        return mealTypeService.getAll();
    }

    // 前台-餐點細節
    @GetMapping("/detail")
    public String showMenuDetail(@RequestParam("mealId") Long mealId, Model model) {
        MealEntity meal = mealService.getOneMeal(mealId);
        if (meal == null) {
            model.addAttribute("errorMessage", "查無此餐點！");
            return "front-end/menu/menu-detail";
        }
        model.addAttribute("meal", meal);
        return "front-end/menu/menu-detail";
    }
}


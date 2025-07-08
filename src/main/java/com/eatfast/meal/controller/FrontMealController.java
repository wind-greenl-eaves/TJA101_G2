package com.eatfast.meal.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.eatfast.common.enums.MealStatus;
import com.eatfast.meal.model.MealEntity;
import com.eatfast.meal.model.MealService;
import com.eatfast.mealtype.model.MealTypeEntity;
import com.eatfast.mealtype.service.MealTypeService;

@Controller
@RequestMapping("/menu")
public class FrontMealController {

    private final MealService mealService;
    private final MealTypeService mealTypeService;

    public FrontMealController(MealService mealService, MealTypeService mealTypeService) {
        this.mealService = mealService;
        this.mealTypeService = mealTypeService;
    }

    // 前台-菜單列表（分類過濾）
    @GetMapping("")
    public String showMenuList(
        @RequestParam(value = "type", required = false) Long typeId,
        Model model) {

        List<MealEntity> mealList;
        String currentMealTypeName = null;

        if (typeId != null) {
            mealList = mealService.getMealsByStatus(MealStatus.AVAILABLE)
                .stream()
                .filter(meal -> meal.getMealType() != null && meal.getMealType().getMealTypeId().equals(typeId))
                .toList();
            MealTypeEntity mealType = mealTypeService.getOneMealType(typeId);
            if (mealType != null) {
                currentMealTypeName = mealType.getMealName();
            }
        } else {
            mealList = mealService.getMealsByStatus(MealStatus.AVAILABLE);
        }

        model.addAttribute("mealListData", mealList);
        model.addAttribute("currentMealTypeName", currentMealTypeName);
        return "front-end/meal/menu-list";
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
            return "front-end/meal/menu-detail";
        }
        model.addAttribute("meal", meal);
        return "front-end/meal/menu-detail";
    }
}


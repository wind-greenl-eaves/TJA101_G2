package com.eatfast.meal.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public FrontMealController(MealService mealService, MealTypeService mealTypeService, FavService favService) {
        this.mealService = mealService;
        this.mealTypeService = mealTypeService;
    }

    // 前台-菜單列表（分類過濾）
    @GetMapping("")
    public String showMenuList(
        @RequestParam(value = "type", required = false) Long typeId,
        Model model, HttpSession session) {

        // 取得當前登入會員ID，若未登入可為 null
        Long memberId = (Long) session.getAttribute("loggedInMemberId");

        List<MealDTO> mealDTOList;
        String currentMealTypeName = null;

        if (typeId != null) {
            // 用 Service 取得該分類下所有上架餐點（已帶有 mealPicUrl 優先邏輯）
            mealDTOList = mealService.getMealsByTypeWithFavored(typeId, memberId);
            // 額外取得分類名稱
            MealTypeEntity mealType = mealTypeService.getOneMealType(typeId);
            currentMealTypeName = mealType != null ? mealType.getMealName() : null;
        } else {
            // 取得所有上架餐點（已帶有 mealPicUrl 優先邏輯）
            mealDTOList = mealService.getAllAvailableWithFavored(memberId);
        }

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
    public String showMenuDetail(@RequestParam("mealId") Long mealId, Model model, HttpSession session) {
        MealEntity meal = mealService.getOneMeal(mealId);
        if (meal == null) {
            model.addAttribute("errorMessage", "查無此餐點！");
            return "front-end/menu/menu-detail";
        }
        // 取得會員ID，傳給 DTO
        Long memberId = (Long) session.getAttribute("loggedInMemberId");
        MealDTO dto = mealService.toDTOWithFavored(meal, memberId);
        model.addAttribute("meal", dto);
        return "front-end/menu/menu-detail";
    }
}


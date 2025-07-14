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
import com.eatfast.store.dto.StoreDto;
import com.eatfast.store.model.StoreEntity;
import com.eatfast.store.service.StoreService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/menu")
public class FrontMealController {

    private final MealService mealService;
    private final MealTypeService mealTypeService;
    private final StoreService storeService;
    public FrontMealController(MealService mealService, MealTypeService mealTypeService, FavService favService,StoreService storeService) {
        this.mealService = mealService;
        this.mealTypeService = mealTypeService;
        this.storeService = storeService;
    }

    // 前台-菜單列表（分類過濾）
    @GetMapping("")
    public String showMenuList(
    	    @RequestParam(value = "type", required = false) Long typeId,
    	    @RequestParam(value = "storeId", required = false) Long storeId,
    	    Model model, HttpSession session) {

    	    Long memberId = (Long) session.getAttribute("loggedInMemberId");

    	    List<MealDTO> mealDTOList;
    	    String currentMealTypeName = null;

    	    if (typeId != null) {
    	        mealDTOList = mealService.getMealsByTypeWithFavored(typeId, memberId);
    	        MealTypeEntity mealType = mealTypeService.getOneMealType(typeId);
    	        currentMealTypeName = mealType != null ? mealType.getMealName() : null;
    	    } else {
    	        mealDTOList = mealService.getAllAvailableWithFavored(memberId);
    	    }

    	    // 取得所有門市，過濾掉總部（for 下拉選單）
    	    List<StoreDto> publicStores = storeService.findAllPublicStores();
    	    
    	    // 將已過濾的門市列表加到 Model 中，供前端 Thymeleaf 產生下拉選單
    	    model.addAttribute("storeList", publicStores);
    	    
    	    model.addAttribute("mealListData", mealDTOList);
    	    model.addAttribute("currentMealTypeName", currentMealTypeName);
    	    model.addAttribute("selectedStoreId", storeId);

    	    return "front-end/menu/menu-list";
    	}


    // 類別清單給前端模板用（分類側邊欄）
    @ModelAttribute("mealTypeListData")
    public List<MealTypeEntity> getAllMealTypes() {
        return mealTypeService.getAll();
    }
}

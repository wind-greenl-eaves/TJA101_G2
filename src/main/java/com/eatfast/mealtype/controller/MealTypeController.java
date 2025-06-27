package com.eatfast.mealtype.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.eatfast.mealtype.model.MealTypeEntity;
import com.eatfast.mealtype.model.MealTypeService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/mealtype")
public class MealTypeController {

    @Autowired
    private MealTypeService mealTypeService;

    // 顯示新增表單
    @GetMapping("/addMealType")
    public String addMealType(ModelMap model) {
        MealTypeEntity mealTypeEntity = new MealTypeEntity();
        model.addAttribute("mealTypeVO", mealTypeEntity);
        return "back-end/mealtype/addMealType";
    }

    // 新增資料處理
    @PostMapping("/insert")
    public String insert(@Valid @ModelAttribute("mealTypeEntity") MealTypeEntity mealTypeEntity,
                         BindingResult result, ModelMap model) {

        if (result.hasErrors()) {
            return "back-end/mealtype/addMealType";
        }

        mealTypeService.addMealType(mealTypeEntity.getMealName());
        model.addAttribute("success", "- (新增成功)");
        return "redirect:/mealtype/listAllMealType";
    }

    // 顯示所有餐點種類資料
    @GetMapping("/listAllMealType")
    public String listAllMealTypes(Model model) {
        List<MealTypeEntity> list = mealTypeService.getAll();
        model.addAttribute("mealTypeList", list);
        return "back-end/mealtype/listAllMealType";
    }

    // 顯示單筆餐點種類（查詢+更新）
    @PostMapping("/getOne_For_Update")
    public String getOneForUpdate(@RequestParam("meal_type_id") String mealTypeId,
                                   ModelMap model) {
        MealTypeEntity mealTypeEntity = mealTypeService.getOneMealType(Integer.valueOf(mealTypeId));
        model.addAttribute("mealTypeEntity", mealTypeEntity);
        return "back-end/mealtype/update_meal_type_input";
    }

    // 更新資料處理
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("mealTypeEntity") MealTypeEntity mealTypeEntity,
                         BindingResult result, ModelMap model) {

    	// 如果驗證失敗（表單欄位錯誤，例如名稱為空或格式不符），就會回到修改畫面
    	// 使用者填錯的資料會保留，錯誤訊息可透過 <th:errors> 顯示
        if (result.hasErrors()) {
            return "back-end/mealtype/update_meal_type_input";
        }

        mealTypeService.updateMealType(mealTypeEntity.getMealTypeId(), mealTypeEntity.getMealName());
        model.addAttribute("success", "- (修改成功)");
        model.addAttribute("mealTypeEntity", mealTypeEntity);
        return "back-end/mealtype/listOneMealType";
    }
}

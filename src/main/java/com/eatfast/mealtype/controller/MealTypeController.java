/*
 * ================================================================
 * 檔案 4: MealTypeController.java (★★ 核心重構 ★★)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/mealtype/controller/MealTypeController.java
 * - 核心改動:
 * 1. 職責簡化: Controller 不再直接處理原始參數，而是透過 @ModelAttribute 綁定 Entity。
 * 2. 驗證強化: 使用 @Valid 觸發 Entity 上的驗證規則。
 * 3. 錯誤處理: 新增 try-catch 區塊，處理 Service 層可能拋出的業務例外 (如名稱重複)。
 * 4. 路徑優化: `getOne_For_Update` 改為更符合 RESTful 風格的 GET 請求。
 */
package com.eatfast.mealtype.controller;

import com.eatfast.mealtype.model.MealTypeEntity;
import com.eatfast.mealtype.service.MealTypeService;
import jakarta.validation.Valid; // 【優化】使用 Jakarta 的 @Valid
import org.springframework.dao.DataIntegrityViolationException; //【優化】捕獲更精準的刪除錯誤
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/mealtype")
public class MealTypeController {

    private final MealTypeService mealTypeService;

    // 【優化】: 改用建構子注入，是 Spring 推薦的最佳實踐。
    public MealTypeController(MealTypeService mealTypeService) {
        this.mealTypeService = mealTypeService;
    }

    @GetMapping("/addMealType")
    public String showAddForm(Model model) {
        // 【優化】: ModelAttribute 的名稱應與類別名的小駝峰寫法一致 ("mealTypeEntity")，
        // 這樣在表單中可以省略 th:object 的名稱。
        model.addAttribute("mealTypeEntity", new MealTypeEntity());
        return "back-end/mealtype/addMealType";
    }

    @PostMapping("/insert")
    public String insert(@Valid @ModelAttribute("mealTypeEntity") MealTypeEntity mealTypeEntity,
                         BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "back-end/mealtype/addMealType";
        }
        try {
            mealTypeService.addMealType(mealTypeEntity.getMealName());
            redirectAttributes.addFlashAttribute("successMessage", "新增成功！");
        } catch (IllegalArgumentException e) {
            // 【優化】: 捕獲 Service 拋出的業務例外，並將錯誤訊息傳回。
            redirectAttributes.addFlashAttribute("errorMessage", "新增失敗: " + e.getMessage());
            // 返回新增頁面，並保留使用者輸入的資料
            return "redirect:/mealtype/addMealType";
        }
        return "redirect:/mealtype/listAllMealType";
    }

    @GetMapping("/listAllMealType")
    public String listAll(Model model) {
        List<MealTypeEntity> list = mealTypeService.getAll();
        model.addAttribute("mealTypeListData", list);
        return "back-end/mealtype/listAllMealType";
    }
    
    // 【優化】: 改用 GET 和 PathVariable，更符合 RESTful 風格。
    // 這樣可以直接透過連結 <a th:href="@{/mealtype/update/{id}(id=${mealType.mealTypeId})}"> 進入修改頁。
    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable("id") Long mealTypeId, Model model) {
        try {
            MealTypeEntity mealType = mealTypeService.getOneMealType(mealTypeId);
            model.addAttribute("mealTypeEntity", mealType);
            return "back-end/mealtype/update_meal_type_input";
        } catch (Exception e) {
            // 如果找不到ID，重定向回列表頁並顯示錯誤。
            return "redirect:/mealtype/listAllMealType";
        }
    }

    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("mealTypeEntity") MealTypeEntity mealTypeEntity,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            // 如果驗證失敗，需要將 mealTypeEntity 傳回，以便 Thymeleaf 能顯示錯誤並保留使用者輸入。
            return "back-end/mealtype/update_meal_type_input";
        }
        try {
            // 【修正】: 將 ID 從 mealTypeEntity 中取出傳遞，避免混淆。
            mealTypeService.updateMealType(mealTypeEntity.getMealTypeId(), mealTypeEntity.getMealName());
            redirectAttributes.addFlashAttribute("successMessage", "修改成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "修改失敗: " + e.getMessage());
        }
        return "redirect:/mealtype/listAllMealType";
    }
    
    @PostMapping("/delete")
    public String delete(@RequestParam("mealTypeId") Long mealTypeId, RedirectAttributes redirectAttributes) {
        try {
            mealTypeService.deleteMealType(mealTypeId);
            redirectAttributes.addFlashAttribute("successMessage", "刪除成功！");
        } catch (DataIntegrityViolationException e) {
             // 【優化】: 捕獲因外鍵關聯導致的刪除失敗，並給予友善提示。
            redirectAttributes.addFlashAttribute("errorMessage", "刪除失敗: 此種類底下尚有餐點，無法刪除。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "刪除失敗: " + e.getMessage());
        }
        return "redirect:/mealtype/listAllMealType";
    }
}
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

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException; //【優化】捕獲更精準的刪除錯誤
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.eatfast.mealtype.model.MealTypeEntity;
import com.eatfast.mealtype.service.MealTypeService;

import jakarta.validation.Valid; // 【優化】使用 Jakarta 的 @Valid

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
    
 // 新增或修正此方法，用於處理單一查詢
    @PostMapping("/getOne_For_Display") // 【關鍵】匹配 HTML 中的 th:action
    public String getOneForDisplay(@RequestParam("mealTypeId") Long mealTypeId, // 【關鍵】接收表單送出的 mealTypeId
                                   ModelMap model) {
            // 調用 Service 層方法來查詢單一餐點種類
            MealTypeEntity mealTypeEntity = mealTypeService.getOneMealType(mealTypeId);

            if (mealTypeEntity == null) {
    	        model.addAttribute("errorMessage", "查無資料");
    	    } else {
    	        // 【關鍵修改】如果找到了，將訂單物件放入 model 中
    	        model.addAttribute("mealTypeEntity", mealTypeEntity);
    	    }
    	    
    	    // 【關鍵修改】無論成功或失敗，都返回原本的查詢頁面
    	    return "back-end/mealType/select_page_mealType";
    	}
    
    // 【優化】: 改用 GET 和 PathVariable，更符合 RESTful 風格。
    // 這樣可以直接透過連結 <a th:href="@{/mealtype/update/{id}(id=${mealType.mealTypeId})}"> 進入修改頁。
    @PostMapping("/getOne_For_Update")
    public String showUpdateForm(@RequestParam("mealTypeId") Long mealTypeId, ModelMap model) {
            MealTypeEntity mealTypeEntity = mealTypeService.getOneMealType(mealTypeId);
            model.addAttribute("mealTypeEntity", mealTypeEntity);
            return "back-end/mealtype/update_meal_type_input";
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
    
    @GetMapping("/select_page_mealtype") 
    public String selectPageMealType(Model model) {
    	return "back-end/mealtype/select_page_mealtype";
    }
    
    @ModelAttribute("mealTypeListData")
    public List<MealTypeEntity> getAllMealTypes() {
		return mealTypeService.getAll();
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
/*
 * ================================================================
 * 檔案 6: StoreController.java (★★ 核心重構 ★★)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/store/controller/StoreController.java
 * - 核心改動:
 * 1. 遵循最佳實踐: 改用建構子注入。
 * 2. 錯誤處理: 為所有寫入操作加入 try-catch，捕獲 Service 拋出的業務例外。
 * 3. 實現 CRUD: 完成所有增刪改查的請求處理方法。
 */
package com.eatfast.store.controller;

import com.eatfast.store.exception.StoreNotFoundException;
import com.eatfast.store.model.StoreEntity;
import com.eatfast.store.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/store")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping("/list")
    public String listAllStores(Model model) {
        List<StoreEntity> stores = storeService.getAllStores();
        model.addAttribute("storeList", stores);
        return "back-end/store/listAllStores"; // 假設的視圖路徑
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("storeEntity", new StoreEntity());
        return "back-end/store/addStore";
    }

    @PostMapping("/insert")
    public String createStore(@Valid @ModelAttribute("storeEntity") StoreEntity storeEntity,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "back-end/store/addStore";
        }
        try {
            storeService.createStore(storeEntity);
            redirectAttributes.addFlashAttribute("successMessage", "新增門市成功！");
        } catch (IllegalArgumentException e) {
            result.rejectValue("storeName", "duplicate", e.getMessage());
            return "back-end/store/addStore";
        }
        return "redirect:/store/list";
    }

    @GetMapping("/edit/{id}")
    public String showUpdateForm(@PathVariable("id") Long storeId, Model model) {
        storeService.getStoreById(storeId).ifPresent(store -> {
            model.addAttribute("storeEntity", store);
        });
        return "back-end/store/updateStore";
    }

    @PostMapping("/update")
    public String updateStore(@RequestParam("storeId") Long storeId,
                              @Valid @ModelAttribute("storeEntity") StoreEntity storeEntity,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "back-end/store/updateStore";
        }
        try {
            storeService.updateStore(storeId, storeEntity);
            redirectAttributes.addFlashAttribute("successMessage", "更新門市成功！");
        } catch (StoreNotFoundException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "更新失敗：" + e.getMessage());
        }
        return "redirect:/store/list";
    }

    @PostMapping("/delete")
    public String deleteStore(@RequestParam("storeId") Long storeId,
                              RedirectAttributes redirectAttributes) {
        try {
            storeService.deleteStore(storeId);
            redirectAttributes.addFlashAttribute("successMessage", "刪除門市成功！");
        } catch (StoreNotFoundException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/store/list";
    }
}
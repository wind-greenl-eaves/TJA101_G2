// =================================================================================
// 檔案 2/2: StoreController.java (★★ 最終版 ★★)
// 路徑: src/main/java/com/eatfast/store/controller/StoreController.java
// 說明: 完全採用分離後的 DTO，邏輯嚴密，結構清晰，是企業級的標準實踐。
// =================================================================================
package com.eatfast.store.controller;

import com.eatfast.store.dto.CreateStoreRequest;
import com.eatfast.store.dto.StoreDto;
import com.eatfast.store.dto.UpdateStoreRequest;
import com.eatfast.store.exception.StoreNotFoundException;
import com.eatfast.store.mapper.StoreMapper;
import com.eatfast.store.service.StoreService;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/api/v1/store")
public class StoreController {

    private final StoreService storeService;
    private final StoreMapper storeMapper; // 注入 Mapper 以便進行 DTO 轉換

    public StoreController(StoreService storeService, StoreMapper storeMapper) {
        this.storeService = storeService;
        this.storeMapper = storeMapper;
    }
    
    @GetMapping("/select_page_store")
    public String showSelectStorePage() {
        
        return "back-end/store/select_page_store"; 
    }

    @GetMapping("/listAllStore")
    public String listAllStores(Model model) {
        List<StoreDto> stores = storeService.findAllStores();
        model.addAttribute("storeList", stores);
        return "back-end/store/listAllStore";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("createStoreRequest", new CreateStoreRequest());
        return "back-end/store/addStore";
    }

    @PostMapping("/insert")
    public String createStore(@Valid @ModelAttribute("createStoreRequest") CreateStoreRequest request,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "back-end/store/addStore";
        }
        try {
            storeService.createStore(request);
            redirectAttributes.addFlashAttribute("successMessage", "新增門市成功！");
        } catch (IllegalArgumentException e) {
            result.rejectValue("storeName", "duplicate", e.getMessage());
            return "back-end/store/addStore";
        }
        return "redirect:/store/list";
    }

    @GetMapping("/edit/{id}")
    public String showUpdateForm(@PathVariable("id") Long storeId, Model model, 
                                 RedirectAttributes redirectAttributes) {
        try {
            StoreDto storeDto = storeService.findStoreById(storeId);
            // 使用 Mapper 將查詢到的 StoreDto 轉換為 UpdateStoreRequest，用於表單綁定
            UpdateStoreRequest updateRequest = storeMapper.toUpdateRequest(storeDto);
            model.addAttribute("updateStoreRequest", updateRequest);
        } catch (StoreNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/store/list";
        }
        return "back-end/store/updateStore";
    }

    @PostMapping("/update")
    public String updateStore(@Valid @ModelAttribute("updateStoreRequest") UpdateStoreRequest request,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        // 從 DTO 中獲取 storeId
        Long storeId = request.getStoreId();
        
        if (result.hasErrors()) {
            return "back-end/store/updateStore"; // 返回編輯頁面並顯示錯誤
        }
        try {
            storeService.updateStore(storeId, request);
            redirectAttributes.addFlashAttribute("successMessage", "更新門市 " + storeId + " 成功！");
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
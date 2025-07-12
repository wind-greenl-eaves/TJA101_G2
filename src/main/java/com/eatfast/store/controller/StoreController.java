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
@RequestMapping("/store")
public class StoreController {

    private final StoreService storeService;
    private final StoreMapper storeMapper; // 注入 Mapper 以便進行 DTO 轉換

    public StoreController(StoreService storeService, StoreMapper storeMapper) {
        this.storeService = storeService;
        this.storeMapper = storeMapper;
    }
    
    @GetMapping("/select_page")
    public String showSelectStorePage() {
        
        return "back-end/store/select_page_store"; 
    }

    @GetMapping("/listAll")
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
        return "redirect:/store/listAll";
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
            return "redirect:/store/update_Store_input";
        }
        return "back-end/store/update_Store_input";
    }

    @PostMapping("/update")
    public String updateStore(@Valid @ModelAttribute("updateStoreRequest") UpdateStoreRequest request,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        // 從 DTO 中獲取 storeId
        Long storeId = request.getStoreId();
        
        if (result.hasErrors()) {
            return "back-end/store/update_Store_input"; // 返回編輯頁面並顯示錯誤
        }
        try {
            storeService.updateStore(storeId, request);
            redirectAttributes.addFlashAttribute("successMessage", "更新門市 " + storeId + " 成功！");
        } catch (StoreNotFoundException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "更新失敗：" + e.getMessage());
        }
        return "redirect:/store/listAll";
    }
 // 請將此方法新增到您現有的 StoreController.java 中

    @GetMapping("/storelist")
    public String showStoreListView(Model model) {
        
        // 1. 從 Service 取得所有門市的列表
        List<StoreDto> allStores = storeService.findAllStores();

        // 2. 檢查列表是否為空
        if (allStores == null || allStores.isEmpty()) {
            // 如果沒有任何門市資料，可以設定一個提示訊息
            model.addAttribute("errorMessage", "目前沒有任何門市資訊。");
        } else {
            // 3. 將【所有門市的列表】放進 Model，供左側選單使用
            model.addAttribute("storeList", allStores);
            
            // 4. 將【列表中的第一個門市】設為預設選中項，放進 Model，供中間區塊初次載入時顯示
            model.addAttribute("currentStore", allStores.get(0));
        }
        
        // 5. 指定要去渲染的 HTML 樣板檔案
        return "front-end/store/storelist";
    }
    
    

}
// =================================================================================
// 檔案 2/2: StoreController.java (★★ 最終版 ★★)
// 路徑: src/main/java/com/eatfast/store/controller/StoreController.java
// 說明: 完全採用分離後的 DTO，邏輯嚴密，結構清晰，是企業級的標準實踐。
// =================================================================================
package com.eatfast.store.controller;

import com.eatfast.common.enums.StoreStatus;
import com.eatfast.store.dto.CreateStoreRequest;
import com.eatfast.store.dto.StoreDto;
import com.eatfast.store.dto.UpdateStoreRequest;
import com.eatfast.store.exception.StoreNotFoundException;
import com.eatfast.store.mapper.StoreMapper;
import com.eatfast.store.service.StoreService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
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
    
    /* 處理前端使用者瀏覽「門市據點」頁面的請求。
    * @param model Spring MVC 模型，用於將資料傳遞給 Thymeleaf 視圖。
    * @return 視圖的路徑 "store/view"。
    */
   @GetMapping("/storelist") // 這個路徑對應前端頁面的 URL
   public String showStoreViewPage(Model model) {
       
       // 1. 【核心修改】呼叫新的 Service 方法，只取得「公開」的門市列表(已過濾總部)
       // findAllPublicStores 是我們之前定義好的自定義方法名稱
       List<StoreDto> publicStores = storeService.findAllPublicStores();

       // 2. 增加防呆機制，處理沒有任何公開門市的狀況
       if (publicStores == null || publicStores.isEmpty()) {
           // 如果沒有任何門市，我們仍然需要給前端一個空的列表和一個預設的物件，避免 Thymeleaf 出錯
           model.addAttribute("storeList", new ArrayList<>());
           model.addAttribute("currentStore", new StoreDto()); // 傳一個空的 DTO，避免 th:text 取值時報錯
       } else {
           // 3. 如果有門市，將「已過濾」的列表和列表中的第一家門市設為預設顯示
           model.addAttribute("storeList", publicStores);
           model.addAttribute("currentStore", publicStores.get(0));
       }
       
       // 4. 指向你的 Thymeleaf 模板檔案，路徑為 src/main/resources/templates/store/view.html
       return "front-end/store/storelist"; 
   }
    
    
    
    // 【★★ 新增 ★★】 給前端客戶「查詢所有門市」用的新端點
    // 這個方法會被前端頁面初次載入時呼叫
    @GetMapping("/search/All") // 我們約定 /public 路徑代表給客戶看的
    public ResponseEntity<List<StoreDto>> getAllPublicStores() {
        // 呼叫我們在 Service 層新增的 public 方法
        List<StoreDto> stores = storeService.findAllPublicStores();
        return ResponseEntity.ok(stores);
    }

    // 【★★ 新增 ★★】 給前端客戶「搜尋門市」用的新端點
    // 這個方法會被前端的搜尋表單呼叫
    @GetMapping("/search/store") // 我們約定 /public/search 是給客戶的搜尋功能
    public ResponseEntity<List<StoreDto>> searchPublicStores(
            @RequestParam(required = false) String storeName,
            @RequestParam(required = false) String storeLoc,
            @RequestParam(required = false) String storeTime,
            @RequestParam(required = false) StoreStatus storeStatus) {
        // 呼叫我們在 Service 層新增的 public 搜尋方法
        List<StoreDto> stores = storeService.searchPublicStores(storeName, storeLoc, storeTime, storeStatus);
        return ResponseEntity.ok(stores);
    }

    
    

}
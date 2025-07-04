package com.eatfast.store.controller;

import com.eatfast.common.enums.StoreStatus;
import com.eatfast.store.dto.CreateStoreRequest;
import com.eatfast.store.dto.StoreDto;
import com.eatfast.store.dto.UpdateStoreRequest;
import com.eatfast.store.exception.StoreNotFoundException;
import com.eatfast.store.service.StoreService; // 只需要 Service
import com.eatfast.store.service.StoreServiceImpl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

@RestController // 標記這是一個 RESTful Controller，所有方法預設返回 JSON/XML
@RequestMapping("/api/store") // 【重要】定義 RESTful API 的基礎 URL，與頁面請求區分開
public class StoreRestController {

    private final StoreService storeService; // 注入介面

    public StoreRestController(StoreService storeService) {
        this.storeService = storeService;
    }

    /**
     * 獲取所有門市的 RESTful API。
     * 用於前端下拉選單的載入。
     * GET /api/store
     *
     * @return 所有門市的列表 (JSON 格式)
     */
    @GetMapping // 處理 GET /api/store
    public ResponseEntity<List<StoreDto>> getAllStoresApi() {
        List<StoreDto> stores = storeService.findAllStores();
        return new ResponseEntity<>(stores, HttpStatus.OK);
    }

    /**
     * 根據門市ID查詢單一門市的 RESTful API。
     * GET /api/store/{storeId}
     *
     * @param storeId 門市ID
     * @return 門市詳細資訊 (JSON 格式)，如果找不到則返回 404 Not Found
     */
    @GetMapping("/{storeId}") // 處理 GET /api/store/{storeId}
    public ResponseEntity<StoreDto> getStoreByIdApi(@PathVariable Long storeId) {
        try {
            StoreDto storeDto = storeService.findStoreById(storeId);
            return new ResponseEntity<>(storeDto, HttpStatus.OK);
        } catch (StoreNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    /**
     * 透過 API 查詢門市 (根據多個參數)。
     * GET /api/store/search?storename=xxx&storeloc=yyy...
     * 這個方法需要您在 StoreService 中添加對應的 search 邏輯。
     *
     * @param storename 門市名稱
     * @param storeloc 門市地點
     * @param storetime 營業時間
     * @param storestatus 營業狀態
     * @return 符合條件的門市列表
     */
    @GetMapping("/search")
    public ResponseEntity<List<StoreDto>> searchStoresApi(
            @RequestParam(required = false) String storename,
            @RequestParam(required = false) String storeloc,
            @RequestParam(required = false) String storetime,
            @RequestParam(required = false) String storestatus){
    	
        StoreStatus statusEnum = null;
        if (storestatus != null && !storestatus.isEmpty()) {
            try {
                statusEnum = StoreStatus.valueOf(storestatus.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 如果轉換失敗，表示前端傳入的狀態值無效
                System.err.println("收到無效的門市狀態字串: " + storestatus + ", 錯誤: " + e.getMessage());
                // 【路徑 1: 錯誤處理】: 返回 400 Bad Request
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        List<StoreDto> filteredStores = storeService.searchStores(storename, storeloc, storetime, statusEnum);

        if (filteredStores.isEmpty()) {
            // 【路徑 2: 查詢無結果】: 返回 204 No Content
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        // 【路徑 3: 查詢有結果】: 返回 200 OK 和查詢到的數據
        return new ResponseEntity<>(filteredStores, HttpStatus.OK);
    }
        

    /**
     * 透過 API 新增門市。
     * POST /api/store
     *
     * @param request 包含新增門市資料的請求體
     * @return 新增後的 StoreDto 物件
     */
    @PostMapping
    public ResponseEntity<StoreDto> createStoreApi(@Valid @RequestBody CreateStoreRequest request) {
        try {
            StoreDto newStore = storeService.createStore(request);
            return new ResponseEntity<>(newStore, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 透過 API 更新門市。
     * PUT /api/store/{storeId}
     *
     * @param storeId 要更新的門市ID
     * @param request 包含更新門市資料的請求體
     * @return 更新後的 StoreDto 物件
     */
    @PutMapping("/{storeId}")
    public ResponseEntity<StoreDto> updateStoreApi(@PathVariable Long storeId, @Valid @RequestBody UpdateStoreRequest request) {
        try {
            StoreDto updatedStore = storeService.updateStore(storeId, request);
            return new ResponseEntity<>(updatedStore, HttpStatus.OK);
        } catch (StoreNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * 透過 API 刪除門市。
     * DELETE /api/store/{storeId}
     *
     * @param storeId 要刪除的門市ID
     * @return 無內容的 204 No Content 響應
     */
    @DeleteMapping("/{storeId}")
    public ResponseEntity<Void> deleteStoreApi(@PathVariable Long storeId) {
        try {
            storeService.deleteStore(storeId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (StoreNotFoundException | IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 或 BAD_REQUEST
        }
    }
}
package com.eatfast.storemealstatus.controller;

import com.eatfast.storemealstatus.model.StoreMealStatusEntity;
import com.eatfast.storemealstatus.service.StoreMealStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // 引入 HTTP 狀態碼
import org.springframework.http.ResponseEntity; // 引入 ResponseEntity 處理 HTTP 響應
import org.springframework.web.bind.annotation.*; // 引入各種 Spring Web 註解

import java.util.List;
import java.util.Optional;
 
/**
 * StoreMealStatusController 負責處理與門市餐點狀態相關的 HTTP 請求。
 * 提供 RESTful API 端點來執行新增、查詢、更新和刪除操作。
 */
@RestController // 標記這是一個 RESTful Controller，所有方法預設返回 JSON/XML 響應
@RequestMapping("/api/store-meal-statuses") // 定義這個 Controller 的基礎 URL 路徑
public class StoreMealStatusController {

    @Autowired // 自動注入 StoreMealStatusService 實作
    private StoreMealStatusService storeMealStatusService; // `storeMealStatusService` 是可以自定義的變數名稱

    /**
     * 新增或更新一個門市餐點狀態。
     * 使用 POST 請求，傳入 JSON 格式的 StoreMealStatusEntity 物件。
     * 如果資料已存在（根據複合主鍵），則更新；否則新增。
     *
     * @param storeMealStatus 要新增或更新的 StoreMealStatusEntity 物件 (來自請求體)
     * @return 包含新增或更新後物件的 ResponseEntity
     */
    @PostMapping // 處理 HTTP POST 請求
    public ResponseEntity<StoreMealStatusEntity> createOrUpdateStoreMealStatus(
            @RequestBody StoreMealStatusEntity storeMealStatus) { // `@RequestBody` 用於將請求體轉換為 Java 物件
        StoreMealStatusEntity savedStatus = storeMealStatusService.saveStoreMealStatus(storeMealStatus); // 調用 Service 層方法
        return new ResponseEntity<>(savedStatus, HttpStatus.CREATED); // 返回 201 Created 狀態碼
    }

    /**
     * 根據門市ID和餐點ID查詢單一餐點狀態。
     * 使用 GET 請求，路徑變數包含 storeId 和 mealId。
     *
     * @param storeId 門市ID (來自 URL 路徑)
     * @param mealId 餐點ID (來自 URL 路徑)
     * @return 包含查詢結果物件的 ResponseEntity，如果找不到則返回 404 Not Found
     */
    @GetMapping("/{storeId}/{mealId}") // 處理 HTTP GET 請求，路徑中包含變數
    public ResponseEntity<StoreMealStatusEntity> getStoreMealStatusById(
            @PathVariable Integer storeId, // `@PathVariable` 用於從 URL 路徑中獲取變數
            @PathVariable Integer mealId) {
        Optional<StoreMealStatusEntity> mealStatusOptional = storeMealStatusService.getSingleMealStatus(storeId, mealId); // 調用 Service 層方法

        return mealStatusOptional.map(mealStatus -> new ResponseEntity<>(mealStatus, HttpStatus.OK)) // 如果找到，返回 200 OK
                                 .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND)); // 如果找不到，返回 404 Not Found
    }

    /**
     * 查詢所有門市餐點狀態。
     * 使用 GET 請求。
     *
     * @return 包含所有 StoreMealStatusEntity 列表的 ResponseEntity
     */
    @GetMapping // 處理 HTTP GET 請求
    public ResponseEntity<List<StoreMealStatusEntity>> getAllStoreMealStatuses() {
        List<StoreMealStatusEntity> mealStatuses = storeMealStatusService.getAllMealStatuses(); // 調用 Service 層方法
        return new ResponseEntity<>(mealStatuses, HttpStatus.OK); // 返回 200 OK
    }

    /**
     * 根據門市ID查詢該門市下的所有餐點狀態。
     * 使用 GET 請求。
     *
     * @param storeId 門市ID (來自 URL 路徑)
     * @return 包含該門市下所有餐點狀態列表的 ResponseEntity
     */
    @GetMapping("/by-store/{storeId}") // 處理 HTTP GET 請求
    public ResponseEntity<List<StoreMealStatusEntity>> getStoreMealStatusesByStoreId(
            @PathVariable Integer storeId) {
        List<StoreMealStatusEntity> mealStatuses = storeMealStatusService.getMealStatusesByStoreId(storeId); // 調用 Service 層方法
        // 即使列表為空，也返回 200 OK 和一個空列表，符合 RESTful 規範
        return new ResponseEntity<>(mealStatuses, HttpStatus.OK);
    }

    /**
     * 根據餐點狀態查詢所有餐點。
     * 使用 GET 請求。
     *
     * @param status 餐點狀態字串 (來自 URL 查詢參數)
     * @return 包含符合該狀態所有餐點列表的 ResponseEntity
     */
    @GetMapping("/by-status") // 處理 HTTP GET 請求
    public ResponseEntity<List<StoreMealStatusEntity>> getStoreMealStatusesByStatus(
            @RequestParam String status) { // `@RequestParam` 用於從 URL 查詢參數中獲取值 (例如: /by-status?status=AVAILABLE)
        List<StoreMealStatusEntity> mealStatuses = storeMealStatusService.getMealStatusesByStatus(status); // 調用 Service 層方法
        return new ResponseEntity<>(mealStatuses, HttpStatus.OK);
    }

    /**
     * 根據門市ID和餐點狀態查詢對應餐點ID。
     * 使用 GET 請求。
     *
     * @param storeId 門市ID (來自 URL 查詢參數)
     * @param status 餐點狀態字串 (來自 URL 查詢參數)
     * @return 包含符合門市ID和餐點狀態的所有餐點列表的 ResponseEntity
     */
    @GetMapping("/by-store-status") // 處理 HTTP GET 請求
    public ResponseEntity<List<StoreMealStatusEntity>> getStoreMealStatusesByStoreIdAndStatus(
            @RequestParam Integer storeId,
            @RequestParam String status) {
        List<StoreMealStatusEntity> mealStatuses = storeMealStatusService.getMealStatusesByStoreIdAndStatus(storeId, status);
        return new ResponseEntity<>(mealStatuses, HttpStatus.OK);
    }

    /**
     * 根據門市ID和餐點ID刪除單一餐點狀態。
     * 使用 DELETE 請求。
     *
     * @param storeId 門市ID (來自 URL 路徑)
     * @param mealId 餐點ID (來自 URL 路徑)
     * @return 無內容的 ResponseEntity，狀態碼為 204 No Content
     */
    @DeleteMapping("/{storeId}/{mealId}") // 處理 HTTP DELETE 請求
    public ResponseEntity<Void> deleteStoreMealStatus(
            @PathVariable Integer storeId,
            @PathVariable Integer mealId) {
        storeMealStatusService.deleteStoreMealStatus(storeId, mealId); // 調用 Service 層方法
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 返回 204 No Content 狀態碼
    }

    /**
     * 刪除所有門市餐點狀態。
     * 使用 DELETE 請求。
     *
     * @return 無內容的 ResponseEntity，狀態碼為 204 No Content
     */
    @DeleteMapping // 處理 HTTP DELETE 請求
    public ResponseEntity<Void> deleteAllStoreMealStatuses() {
        storeMealStatusService.deleteAllMealStatuses(); // 調用 Service 層方法
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
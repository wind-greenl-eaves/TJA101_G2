package com.eatfast.storemeal.controller;

import com.eatfast.storemeal.dto.UpdateStatusRequest;
import com.eatfast.storemeal.model.StoreMealStatusEntity;
import com.eatfast.storemeal.service.StoreMealStatusService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 處理門市餐點供應狀態相關 API 請求的控制器 (Controller)。
 */
@RestController
@RequestMapping("/api/stores") // 所有此 Controller 的 API 路徑都以 /api/stores 開頭
public class StoreMealStatusController {

    private static final Logger log = LoggerFactory.getLogger(StoreMealStatusController.class);

    private final StoreMealStatusService statusService;

    // 使用建構子注入 Service
    public StoreMealStatusController(StoreMealStatusService statusService) {
        this.statusService = statusService;
    }

    /**
     * 【核心 API】更新或建立一筆門市餐點的供應狀態。
     * HTTP 方法: PUT (因為此操作具有冪等性：多次用相同狀態更新，結果都相同)
     * URL: /api/stores/{storeId}/meals/{mealId}/status
     *
     * @param storeId 門市 ID，從 URL 路徑中獲取
     * @param mealId  餐點 ID，從 URL 路徑中獲取
     * @param request 包含新狀態的請求 body，會被自動從 JSON 轉換為 DTO 物件
     * @return 更新或建立後的狀態物件，並回傳 HTTP 200 OK
     */
    @PutMapping("/{storeId}/meals/{mealId}/status")
    public ResponseEntity<StoreMealStatusEntity> updateSupplyStatus(
            @PathVariable Integer storeId,
            @PathVariable Long mealId,
            @Valid @RequestBody UpdateStatusRequest request) {

        log.info("接收到 API 請求：更新門市 ID: {}, 餐點 ID: {} 的狀態為 {}", storeId, mealId, request.getStatus());

        // 呼叫 Service 層的核心業務邏輯
        StoreMealStatusEntity updatedStatus = statusService.updateSupplyStatus(storeId, mealId, request.getStatus());

        // 使用 ResponseEntity.ok() 回傳標準的 HTTP 200 OK 回應，並將更新後的物件放在 body 中
        return ResponseEntity.ok(updatedStatus);
    }

    /**
     * 【查詢 API】獲取特定門市所有餐點的供應狀態。
     * HTTP 方法: GET
     * URL: /api/stores/{storeId}/meals/statuses
     *
     * @param storeId 門市 ID
     * @return 該門市所有餐點狀態的列表，以及 HTTP 200 OK
     */
    @GetMapping("/{storeId}/meals/statuses")
    public ResponseEntity<List<StoreMealStatusEntity>> getAllStatusesForStore(@PathVariable Integer storeId) {
        log.info("接收到 API 請求：查詢門市 ID: {} 的所有餐點狀態", storeId);
        List<StoreMealStatusEntity> statuses = statusService.getAllStatusesForStore(storeId);
        return ResponseEntity.ok(statuses);
    }
    
    /**
     * 【查詢 API】獲取特定門市中特定餐點的供應狀態。
     * HTTP 方法: GET
     * URL: /api/stores/{storeId}/meals/{mealId}/status
     *
     * @param storeId 門市 ID
     * @param mealId  餐點 ID
     * @return 如果找到，回傳該狀態物件與 HTTP 200 OK；如果找不到，回傳 HTTP 404 Not Found
     */
    @GetMapping("/{storeId}/meals/{mealId}/status")
    public ResponseEntity<StoreMealStatusEntity> getStatusForStoreAndMeal(
            @PathVariable Integer storeId,
            @PathVariable Long mealId) {
        log.info("接收到 API 請求：查詢門市 ID: {}, 餐點 ID: {} 的狀態", storeId, mealId);
        
        // Service 層回傳 Optional，Controller 層負責將其轉換為 HTTP 回應
        return statusService.getStatusForStoreAndMeal(storeId, mealId)
                .map(ResponseEntity::ok) // 如果 Optional 不為空，則執行 .map()，將內容物用 ResponseEntity.ok() 包裝
                .orElse(ResponseEntity.notFound().build()); // 如果 Optional 為空，則回傳標準的 404
    }
}

// =================================================================================
// 檔案 1/2: StoreService.java (★★ 最終版 ★★)
// 路徑: src/main/java/com/eatfast/store/service/StoreService.java
// 說明: 定義了最為清晰的服務契約，Create 與 Update 操作使用不同的 DTO。
// =================================================================================
package com.eatfast.store.service;

import com.eatfast.store.dto.CreateStoreRequest;
import com.eatfast.store.dto.StoreDto;
import com.eatfast.store.dto.UpdateStoreRequest;
import java.util.List;

public interface StoreService {
    
    /**
     * 新增一間門市。
     * @param request 包含新增所需資料的 DTO。
     * @return 成功建立後的門市資料 DTO。
     */
    StoreDto createStore(CreateStoreRequest request);

    /**
     * 更新指定 ID 的門市資料。
     * @param storeId 要更新的門市 ID。
     * @param request 包含要更新欄位的 DTO。
     * @return 更新成功後的門市資料 DTO。
     */
    StoreDto updateStore(Long storeId, UpdateStoreRequest request);

    /**
     * 刪除指定 ID 的門市。
     * @param storeId 要刪除的門市 ID。
     */
    void deleteStore(Long storeId);

    /**
     * 根據 ID 查詢門市。
     * @param storeId 門市 ID。
     * @return 查詢到的門市資料 DTO。
     */
    StoreDto findStoreById(Long storeId);

    /**
     * 查詢所有門市。
     * @return 所有門市資料 DTO 的列表。
     */
    List<StoreDto> findAllStores();
}
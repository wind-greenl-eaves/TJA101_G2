/*
 * ================================================================
 * 檔案 3: MealTypeService.java (★★ 核心重構 ★★)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/mealtype/service/MealTypeService.java
 * - 核心改動:
 * 1. 遵循最佳實踐: 改用建構子注入，並使用 Spring 的 @Transactional。移除不必要的 SessionFactory 依賴。
 * 2. 修正錯誤邏輯: `updateMealType` 改為「先讀取、再更新」，防止資料丟失。
 * 3. 提升健壯性:
 * - 新增與更新時，檢查名稱是否重複。
 * - 查詢不到資料時拋出例外，而非返回 null。
 * - 刪除前檢查 ID 是否存在。
 */
package com.eatfast.mealtype.service;

import com.eatfast.mealtype.model.MealTypeEntity;
import com.eatfast.mealtype.repository.MealTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; //【優化】使用 Spring 的 Transactional

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true) // 將服務預設為唯讀交易
public class MealTypeService {

    private final MealTypeRepository repository;

    // 【優化】: 改用建構子注入，是 Spring 推薦的最佳實踐。
    public MealTypeService(MealTypeRepository repository) {
        this.repository = repository;
    }

    @Transactional // 覆蓋類別設定，標示為寫入交易
    public MealTypeEntity addMealType(String mealName) {
        // 【優化】: 新增前檢查名稱是否已存在。
        repository.findByMealName(mealName).ifPresent(entity -> {
            throw new IllegalArgumentException("餐點種類名稱 '" + mealName + "' 已存在。");
        });

        MealTypeEntity mealTypeEntity = new MealTypeEntity();
        mealTypeEntity.setMealName(mealName);
        return repository.save(mealTypeEntity);
    }

    @Transactional
    public MealTypeEntity updateMealType(Long mealTypeId, String mealName) {
        // 【關鍵修正】: 更新操作的正確邏輯是「先從資料庫讀取，再修改，最後儲存」。
        // 直接 new 一個 Entity 再 save，會被 JPA 視為「新增」，若 ID 相同則會覆蓋，導致其他欄位資料遺失。
        MealTypeEntity existingMealType = repository.findById(mealTypeId)
                .orElseThrow(() -> new EntityNotFoundException("找不到要更新的餐點種類 ID: " + mealTypeId));

        // 【優化】: 檢查新名稱是否與「其他」現存的種類名稱衝突。
        Optional<MealTypeEntity> sameNameEntityOpt = repository.findByMealName(mealName);
        if (sameNameEntityOpt.isPresent() && !sameNameEntityOpt.get().getMealTypeId().equals(mealTypeId)) {
            throw new IllegalArgumentException("餐點種類名稱 '" + mealName + "' 已被其他種類使用。");
        }

        existingMealType.setMealName(mealName);
        // JPA 會自動偵測到物件被修改，並在交易提交時產生 UPDATE SQL。
        return repository.save(existingMealType);
    }

    public MealTypeEntity getOneMealType(Long mealTypeId) { //【修正】型別 Integer -> Long
        // 【優化】: 找不到時拋出例外，讓 Controller 層能明確地處理錯誤，而非回傳 null。
        return repository.findById(mealTypeId)
                .orElseThrow(() -> new EntityNotFoundException("找不到餐點種類 ID: " + mealTypeId));
    }

    public List<MealTypeEntity> getAll() {
        return repository.findAll();
    }
    
    @Transactional
    public void deleteMealType(Long mealTypeId) {
        // 【優化】: 刪除前先確認 ID 存在，可以提供更明確的錯誤訊息。
        if (!repository.existsById(mealTypeId)) {
            throw new EntityNotFoundException("找不到要刪除的餐點種類 ID: " + mealTypeId);
        }
        // 注意: 若此種類下尚有關聯的餐點，由於資料庫設定為 RESTRICT，此處會拋出例外。
        // 這是預期中的正確行為，應由 Controller 層捕獲並提示使用者。
        repository.deleteById(mealTypeId);
    }
}

package com.eatfast.meal.photowrite;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eatfast.meal.model.MealEntity;
import com.eatfast.meal.model.MealRepository;

@Service
public class PhotoWriteBatchService {
    private static final String FLAG_FILE = "uploads/photo-batch-done.flag";

    @Value("${app.meal-photo-batch.enabled:true}")
    private boolean enabled;

    @Value("${app.meal-photo-folder}")
    private String mealPhotoFolder;
    
    @Value("${app.meal-photo-batch.flag-file-path:uploads/photo-batch-done.flag}")
    private String flagFilePath;

    private final MealRepository mealRepository;

    public PhotoWriteBatchService(MealRepository mealRepository) {
        this.mealRepository = mealRepository;
    }

    public boolean isBatchDone() {
        return new File(FLAG_FILE).exists();
    }

    @Transactional
    public String runPhotoBatchIfNeeded() {
        if (!enabled) return "未啟用批次功能";
        if (isBatchDone()) return "已執行過，不重複批次";
        int updated = 0;
        List<MealEntity> meals = mealRepository.findAll();
        for (MealEntity meal : meals) {
            String fileName = meal.getMealId() + ".png";
            File file = new File(mealPhotoFolder, fileName);
            if (file.exists()) {
                meal.setMealPic(fileName);
                updated++;
            }
        }
        mealRepository.saveAll(meals);
        try {
            new File(FLAG_FILE).createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("建立 flag 檔失敗", e);
        }
        return "已批次更新 " + updated + " 筆餐點圖片";
        }
     // 啟動時自動刪除 flag
        public void deleteFlagFileIfExists() {
            File flag = new File(flagFilePath);
            if (flag.exists()) {
                flag.delete();
                System.out.println("PhotoWrite flag file deleted on startup.");
            }
        }
    }
package com.eatfast.meal.photowrite;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PhotoWriteBatchRunner implements CommandLineRunner {

    private final PhotoWriteBatchService photoWriteBatchService;

    public PhotoWriteBatchRunner(PhotoWriteBatchService service) {
        this.photoWriteBatchService = service;
    }

    public void run(String... args) throws Exception {
        // 1. 啟動時自動刪 flag
        photoWriteBatchService.deleteFlagFileIfExists();
        // 2. 再做批次圖片寫入（照你原本的流程）
        photoWriteBatchService.runPhotoBatchIfNeeded();
    }
}

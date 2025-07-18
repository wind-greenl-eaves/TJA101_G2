package com.eatfast.news.service;

import com.eatfast.common.enums.NewsStatus;
import com.eatfast.employee.model.EmployeeEntity;
import com.eatfast.employee.repository.EmployeeRepository;
import com.eatfast.news.model.NewsEntity;
import com.eatfast.news.repository.NewsRepository;
import org.springframework.core.io.ClassPathResource; // 確保這個 import 存在
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NewsService {

    private final NewsRepository newsRepository;
    private final EmployeeRepository employeeRepository;

    // 不再需要從 application.properties 讀取路徑
    public NewsService(NewsRepository newsRepository, EmployeeRepository employeeRepository) {
        this.newsRepository = newsRepository;
        this.employeeRepository = employeeRepository;
    }

    // --- 查詢、新增、更新、刪除等方法 (保持不變) ---
    public List<NewsEntity> getAllNews() { return newsRepository.findAll(); }
    public NewsEntity findById(Long newsId) { return newsRepository.findByIdWithEmployee(newsId).orElseThrow(() -> new RuntimeException("找不到 ID 為 " + newsId + " 的消息")); }
    public List<NewsEntity> getActivePublishedNews() { return newsRepository.findActivePublishedNews(NewsStatus.PUBLISHED, LocalDateTime.now()); }

    @Transactional
    public NewsEntity saveNews(NewsEntity news, Long employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("找不到員工 ID: " + employeeId));
        news.setEmployee(employee);
        return newsRepository.save(news);
    }

    @Transactional
    public NewsEntity updateNews(NewsEntity updatedNewsData, MultipartFile imageFile) {
        try {
            NewsEntity existingNews = findById(updatedNewsData.getNewsId());
            existingNews.setTitle(updatedNewsData.getTitle());
            existingNews.setContent(updatedNewsData.getContent());
            existingNews.setStatus(updatedNewsData.getStatus());
            existingNews.setStartTime(updatedNewsData.getStartTime());
            existingNews.setEndTime(updatedNewsData.getEndTime());

            if (imageFile != null && !imageFile.isEmpty()) {
                String newImageUrl = saveImage(imageFile);
                existingNews.setImageUrl(newImageUrl);
            }
            return newsRepository.save(existingNews);
        } catch (IOException e) {
            throw new RuntimeException("圖片儲存時發生錯誤", e);
        }
    }

    @Transactional
    public void deleteNewsById(Long newsId) {
        newsRepository.deleteById(newsId);
    }
    // --- (以上方法保持不變) ---

    /**
     * ✅ 圖片儲存輔助方法 - 【根據您的資料夾結構的最終版】
     */
    public String saveImage(MultipartFile imageFile) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }

        // ✅ 修正一：取得 "static/images/" 資料夾的絕對路徑
        File imagesDir = new ClassPathResource("static/images/").getFile();

        // ✅ 修正二：在 "static/images" 的路徑下，建立指向 "news" 的子資料夾物件
        File newsImageDir = new File(imagesDir, "news");

        // 安全地檢查 "news" 資料夾是否存在，不存在就建立
        if (!newsImageDir.exists()) {
            boolean created = newsImageDir.mkdirs();
            if (!created) {
                throw new IOException("無法建立圖片儲存資料夾於: " + newsImageDir.getAbsolutePath());
            }
        }

        // 副檔名驗證 (保留)
        String originalFileName = imageFile.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
        }
        if (!fileExtension.matches("\\.(png|jpg|jpeg|gif)$")) {
            throw new IOException("僅支援 png、jpg、jpeg、gif 圖片格式");
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        // 建立最終的目標檔案路徑並寫入
        File destFile = new File(newsImageDir, uniqueFileName);
        imageFile.transferTo(destFile);
        System.out.println(">>> [Service-DEBUG] 檔案成功寫入到: " + destFile.getAbsolutePath());

        // ✅ 修正三：回傳的 URL 必須匹配實體路徑！
        return "/images/news/" + uniqueFileName;
    }

    public List<NewsEntity> getVisibleNewsForCurrentUser() {
        return List.of();
    }
}
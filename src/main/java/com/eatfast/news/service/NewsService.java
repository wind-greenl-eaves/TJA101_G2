package com.eatfast.news.service;

import com.eatfast.common.enums.NewsStatus;
import com.eatfast.employee.model.EmployeeEntity;
import com.eatfast.employee.repository.EmployeeRepository;
import com.eatfast.news.model.NewsEntity;
import com.eatfast.news.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${file.upload-dir}")
    private String uploadDir;

    public NewsService(NewsRepository newsRepository, EmployeeRepository employeeRepository) {
        this.newsRepository = newsRepository;
        this.employeeRepository = employeeRepository;
    }

    // --- 查詢相關方法 ---
    public List<NewsEntity> getAllNews() { return newsRepository.findAll(); }
    public NewsEntity findById(Long newsId) { return newsRepository.findByIdWithEmployee(newsId).orElseThrow(() -> new RuntimeException("找不到 ID 為 " + newsId + " 的消息")); }
    public List<NewsEntity> getActivePublishedNews() { return newsRepository.findActivePublishedNews(NewsStatus.PUBLISHED, LocalDateTime.now()); }


    // --- 新增 (Create) ---
    @Transactional
    public NewsEntity saveNews(NewsEntity news, Long employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("找不到員工 ID: " + employeeId));
        news.setEmployee(employee);
        return newsRepository.save(news);
    }

    // --- 更新 (Update) ---
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

    // --- 刪除 (Delete) ---
    @Transactional
    public void deleteNewsById(Long newsId) {
        newsRepository.deleteById(newsId);
    }

    /**
     * ✅ 圖片儲存輔助方法 (已加入更多偵錯日誌)
     */
    public String saveImage(MultipartFile imageFile) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            System.out.println(">>> [Service-DEBUG] saveImage 方法收到空的 imageFile，直接返回 null。");
            return null;
        }

        System.out.println(">>> [Service-DEBUG] 開始儲存圖片，收到的 uploadDir 是: '" + uploadDir + "'");

        // 檢查 uploadDir 是否為空或未設定
        if (uploadDir == null || uploadDir.trim().isEmpty()) {
            System.err.println(">>> [Service-ERROR] 檔案上傳路徑 'file.upload-dir' 未在 application.properties 中設定！");
            throw new IOException("伺服器檔案上傳路徑未設定。");
        }

        String originalFileName = imageFile.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        // 建立 news 子資料夾的路徑
        File newsImageDir = new File(uploadDir, "news");

        // 確保 news 子資料夾存在
        if (!newsImageDir.exists()) {
            System.out.println(">>> [Service-DEBUG] 'news' 資料夾不存在，路徑: " + newsImageDir.getAbsolutePath());
            boolean created = newsImageDir.mkdirs();
            System.out.println(">>> [Service-DEBUG] 嘗試建立資料夾... 結果: " + (created ? "成功" : "失敗"));
            if (!created) {
                throw new IOException("無法建立圖片儲存資料夾: " + newsImageDir.getAbsolutePath());
            }
        }

        // 建立最終的目標檔案路徑
        File destFile = new File(newsImageDir, uniqueFileName);
        System.out.println(">>> [Service-DEBUG] 準備將檔案寫入到實體路徑: " + destFile.getAbsolutePath());

        // 執行寫入
        imageFile.transferTo(destFile);
        System.out.println(">>> [Service-DEBUG] imageFile.transferTo() 執行完畢，沒有拋出例外。");

        // 回傳可以用來在網頁上訪問的相對路徑
        return "/uploads/images/news/" + uniqueFileName;
    }

    public List<NewsEntity> getVisibleNewsForCurrentUser() {
        return List.of();
    }
}

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
    public NewsEntity updateNews(NewsEntity updatedNewsData, MultipartFile imageFile) throws IOException {
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
    }

    // --- 刪除 (Delete) ---
    @Transactional
    public void deleteNewsById(Long newsId) {
        // 可以在這裡加上刪除圖片檔案的邏輯
        newsRepository.deleteById(newsId);
    }

    // --- 圖片儲存輔助方法 ---
    public String saveImage(MultipartFile imageFile) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }
        String originalFileName = imageFile.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        File destFile = new File(uploadDir + "news/" + uniqueFileName);
        destFile.getParentFile().mkdirs();
        imageFile.transferTo(destFile);
        return "/uploads/images/news/" + uniqueFileName;
    }

    public List<NewsEntity> getVisibleNewsForCurrentUser() {
        return List.of();
    }
}

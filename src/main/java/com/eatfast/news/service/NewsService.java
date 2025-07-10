package com.eatfast.news.service;

import com.eatfast.common.enums.NewsStatus;
import com.eatfast.employee.model.EmployeeEntity;
import com.eatfast.employee.repository.EmployeeRepository;
import com.eatfast.news.model.NewsEntity;
import com.eatfast.news.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ★ 1. 引入 Transactional
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.EntityNotFoundException;       // ★ 2. 引入 EntityNotFoundException

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

    @Autowired
    public NewsService(NewsRepository newsRepository, EmployeeRepository employeeRepository) {
        this.newsRepository = newsRepository;
        this.employeeRepository = employeeRepository;
    }

    public NewsEntity saveNews(NewsEntity news, Long employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("找不到員工 ID: " + employeeId));
        news.setEmployee(employee);
        return newsRepository.save(news);
    }

    public List<NewsEntity> getActivePublishedNews() {
        LocalDateTime currentTime = LocalDateTime.now();
        return newsRepository.findActivePublishedNews(NewsStatus.PUBLISHED, currentTime);
    }

    public List<NewsEntity> getAllNews() {
        return newsRepository.findAll();
    }

    public NewsEntity findById(Long newsId) {
        return newsRepository.findByIdWithEmployee(newsId)
                .orElseThrow(() -> new EntityNotFoundException("找不到 ID 為 " + newsId + " 的消息"));
    }

    // ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
    // ★     新的 deleteNewsById 方法已整理並放置於此     ★
    // ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
    @Transactional
    public void deleteNewsById(Long id) {
        if (!newsRepository.existsById(id)) {
            throw new EntityNotFoundException("找不到 ID 為 " + id + " 的消息，無法刪除。");
        }
        newsRepository.deleteById(id);
    }

    public String saveImage(MultipartFile imageFile) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }

        // 確保 news 資料夾存在
        File newsUploadDir = new File(uploadDir, "news");
        if (!newsUploadDir.exists()) {
            newsUploadDir.mkdirs();
        }

        String originalFileName = imageFile.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        File destFile = new File(newsUploadDir, uniqueFileName);
        imageFile.transferTo(destFile);

        // ★ 注意：這裡回傳的路徑要和 MvcConfig 中的 addResourceHandler 對應
        return "/uploads/news/" + uniqueFileName;
    }
}
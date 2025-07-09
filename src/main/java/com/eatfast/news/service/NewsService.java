package com.eatfast.news.service;

import com.eatfast.common.enums.NewsStatus;
import com.eatfast.employee.model.EmployeeEntity;
import com.eatfast.employee.repository.EmployeeRepository;
import com.eatfast.news.model.NewsEntity;
import com.eatfast.news.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

    // ✅ 從 application.properties 注入我們設定好的檔案上傳路徑
    @Value("${file.upload-dir}")
    private String uploadDir;

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
        List<NewsEntity> activeNews = newsRepository.findActivePublishedNews(NewsStatus.PUBLISHED, currentTime);
        return activeNews;
    }

    public List<NewsEntity> getAllNews() {
        return newsRepository.findAll();
    }

    public NewsEntity findById(Long newsId) {
        return newsRepository.findByIdWithEmployee(newsId)
                .orElseThrow(() -> new RuntimeException("找不到 ID 為 " + newsId + " 的消息"));
    }

    /**
     * ✅ 我們新增的核心方法：儲存上傳的圖片檔案
     * @param imageFile 使用者上傳的 MultipartFile 物件
     * @return 儲存後可以用來在網頁上訪問的「相對網址路徑」
     */
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
}
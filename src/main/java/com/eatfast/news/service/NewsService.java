package com.eatfast.news.service;

import com.eatfast.common.enums.NewsStatus;
import com.eatfast.employee.model.EmployeeEntity;
import com.eatfast.employee.repository.EmployeeRepository;
import com.eatfast.news.model.NewsEntity;
import com.eatfast.news.repository.NewsRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication; // 引入 Authentication
import org.springframework.security.core.context.SecurityContextHolder; // 引入 SecurityContextHolder
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Transactional
    public NewsEntity saveNews(NewsEntity news, Long employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("找不到員工 ID: " + employeeId));
        news.setEmployee(employee);
        return newsRepository.save(news);
    }

    /**
     * ★ 修改後的方法 ★
     * 根據當前登入者的權限，回傳他能看到的消息列表。
     *
     * @return 可見的消息列表
     */
    public List<NewsEntity> getVisibleNewsForCurrentUser() {
        // 1. 從 Spring Security 的上下文(Context)中，取得當前登入者的驗證資訊
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 增加一個 null 檢查，避免在未登入狀態下測試時發生錯誤
        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("[偵錯日誌] 使用者未登入或驗證資訊無效，回傳空列表。");
            return List.of();
        }

        // ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
        // ★ 偵錯重點：在 Console 印出當前使用者的所有權限，讓我們看看他到底是誰 ★
        // ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
        System.out.println("[偵錯日誌] 開始檢查使用者 '" + authentication.getName() + "' 的權限...");
        authentication.getAuthorities().forEach(auth -> {
            System.out.println("  -> 找到的權限: " + auth.getAuthority());
        });


        // 2. 檢查使用者是否為總部管理員
        boolean isHeadquartersAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                        grantedAuthority.getAuthority().equals("HEADQUARTERS_ADMIN")
                );

        // ★ 偵錯重點：印出判斷結果
        System.out.println("[偵錯日誌] 判斷結果：此使用者是否為總部管理員？ " + isHeadquartersAdmin);


        // 3. 根據身份，回傳不同的資料
        if (isHeadquartersAdmin) {
            System.out.println("[偵錯日誌] 判斷為總部管理員，正在查詢【所有】消息...");
            return newsRepository.findAll();
        } else {
            System.out.println("[偵錯日誌] 判斷為一般員工，正在查詢【僅已發布】的消息...");
            return newsRepository.findAllByStatus(NewsStatus.PUBLISHED);
        }
    }


    public NewsEntity findById(Long newsId) {
        return newsRepository.findByIdWithEmployee(newsId)
                .orElseThrow(() -> new EntityNotFoundException("找不到 ID 為 " + newsId + " 的消息"));
    }

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

        return "/uploads/news/" + uniqueFileName;
    }

    public List<NewsEntity> getActivePublishedNews() {
        return List.of();
    }
}

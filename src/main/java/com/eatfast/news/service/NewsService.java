package com.eatfast.news.service;

import com.eatfast.common.enums.NewsStatus;
import com.eatfast.employee.model.EmployeeEntity;
import com.eatfast.employee.repository.EmployeeRepository;
import com.eatfast.news.model.NewsEntity;
import com.eatfast.news.repository.NewsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // 匯入 Java 8 時間模組
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;



import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit; // TimeUnit 的 import

@Service
public class NewsService {

    // --- 將所有依賴項放在最上面，並設為 final ---
    private final NewsRepository newsRepository;
    private final EmployeeRepository employeeRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // 定義一個在 Redis 中儲存的 Key
    private static final String LATEST_NEWS_KEY = "news:latest";

    // 統一使用建構子注入
    public NewsService(NewsRepository newsRepository,
                       EmployeeRepository employeeRepository,
                       RedisTemplate<String, String> redisTemplate) {
        this.newsRepository = newsRepository;
        this.employeeRepository = employeeRepository;
        this.redisTemplate = redisTemplate;

        // 初始化 ObjectMapper 並註冊 Java 8 時間模組
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        // 註冊 Hibernate 6 模組以處理 Hibernate 實體的序列化
        this.objectMapper.registerModule(new Hibernate6Module());
    }

    // --- 新增的 Redis 快取方法 ---

    // 將 List<News> 改為 List<NewsEntity>
    public List<NewsEntity> getLatestNewsWithCache() {
        try {
            // 1. 先查 Redis
            String newsJson = redisTemplate.opsForValue().get(LATEST_NEWS_KEY);

            if (newsJson != null && !newsJson.isEmpty()) {
                // 2. Cache Hit! Redis 有資料
                System.out.println(">>> 從 Redis 快取讀取最新消息！");
                return objectMapper.readValue(newsJson, new TypeReference<List<NewsEntity>>() {});
            } else {
                // 3. Cache Miss! Redis 沒資料，查資料庫
                System.out.println(">>> 從資料庫讀取最新消息...");
                // 假設你的 Repository 有這個方法，用來取得前台要顯示的已發布新聞
                List<NewsEntity> newsList = newsRepository.findActivePublishedNews(NewsStatus.PUBLISHED, LocalDateTime.now());

                // 3.1. 存入 Redis，並設定 10 分鐘後過期
                redisTemplate.opsForValue().set(
                        LATEST_NEWS_KEY,
                        objectMapper.writeValueAsString(newsList), // 將 List 轉成 JSON 字串
                        10,
                        TimeUnit.MINUTES
                );
                return newsList;
            }
        } catch (JsonProcessingException e) {
            // 如果 JSON 處理出錯，直接查資料庫作為備援
            System.err.println("Redis 快取處理失敗：" + e.getMessage());
            return newsRepository.findActivePublishedNews(NewsStatus.PUBLISHED, LocalDateTime.now());
        }
    }

    // 建立一個清除快取的私有方法
    private void clearCache() {
        System.out.println(">>> 清除最新消息的 Redis 快取...");
        redisTemplate.delete(LATEST_NEWS_KEY);
    }

    // --- 修改既有方法，加入快取清除邏輯 ---

    @Transactional
    public NewsEntity saveNews(NewsEntity news, Long employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("找不到員工 ID: " + employeeId));
        news.setEmployee(employee);
        NewsEntity savedNews = newsRepository.save(news);
        clearCache(); // 新增後清除快取
        return savedNews;
    }

    @Transactional
    public NewsEntity updateNews(NewsEntity updatedNewsData, MultipartFile imageFile) {
        try {
            NewsEntity existingNews = findById(updatedNewsData.getNewsId());
            // ... (省略更新欄位的程式碼)
            existingNews.setTitle(updatedNewsData.getTitle());
            existingNews.setContent(updatedNewsData.getContent());
            existingNews.setStatus(updatedNewsData.getStatus());
            existingNews.setStartTime(updatedNewsData.getStartTime());
            existingNews.setEndTime(updatedNewsData.getEndTime());


            if (imageFile != null && !imageFile.isEmpty()) {
                String newImageUrl = saveImage(imageFile);
                existingNews.setImageUrl(newImageUrl);
            }
            NewsEntity savedNews = newsRepository.save(existingNews);
            clearCache(); // 更新後清除快取
            return savedNews;
        } catch (IOException e) {
            throw new RuntimeException("圖片儲存時發生錯誤", e);
        }
    }

    @Transactional
    public void deleteNewsById(Long newsId) {
        newsRepository.deleteById(newsId);
        clearCache(); // 】刪除後清除快取
    }

    // --- 其他方法 (保持不變) ---

    public List<NewsEntity> getAllNews() {
        return newsRepository.findAll();
    }

    public NewsEntity findById(Long newsId) {
        return newsRepository.findByIdWithEmployee(newsId).orElse(null);
    }

    public List<NewsEntity> getActivePublishedNews() {
        return newsRepository.findActivePublishedNews(NewsStatus.PUBLISHED, LocalDateTime.now());
    }

    public String saveImage(MultipartFile imageFile) throws IOException {
        // ... (你的圖片儲存邏輯，保持不變)
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }
        File imagesDir = new ClassPathResource("static/images/").getFile();
        File newsImageDir = new File(imagesDir, "news");
        if (!newsImageDir.exists()) {
            if (!newsImageDir.mkdirs()) {
                throw new IOException("無法建立圖片儲存資料夾於: " + newsImageDir.getAbsolutePath());
            }
        }
        String originalFileName = imageFile.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
        }
        if (!fileExtension.matches("\\.(png|jpg|jpeg|gif)$")) {
            throw new IOException("僅支援 png、jpg、jpeg、gif 圖片格式");
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        File destFile = new File(newsImageDir, uniqueFileName);
        imageFile.transferTo(destFile);
        return "/images/news/" + uniqueFileName;
    }

    public List<NewsEntity> getVisibleNewsForCurrentUser() {
        return List.of();
    }
}
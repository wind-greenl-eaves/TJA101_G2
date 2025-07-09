package com.eatfast.news.service;

import com.eatfast.common.enums.NewsStatus;
import com.eatfast.employee.model.EmployeeEntity;
import com.eatfast.employee.repository.EmployeeRepository;
import com.eatfast.news.model.NewsEntity;
import com.eatfast.news.repository.NewsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NewsService {

    private final NewsRepository newsRepository;
    private final EmployeeRepository employeeRepository;

    public NewsService(NewsRepository newsRepository, EmployeeRepository employeeRepository) {
        this.newsRepository = newsRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * 新增或更新消息
     */
    public NewsEntity saveNews(NewsEntity news, Long employeeId) {
        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("找不到員工 ID: " + employeeId));
        news.setEmployee(employee);
        return newsRepository.save(news);
    }

    /**
     * 給前台專用的服務，獲取所有可見的已發布消息
     */
    public List<NewsEntity> getActivePublishedNews() {
        // 1. 獲取當前時間
        LocalDateTime currentTime = LocalDateTime.now();
        // 2. 呼叫 Repository 的方法，傳入枚舉 NewsStatus.PUBLISHED 和當前時間
        List<NewsEntity> activeNews = newsRepository.findActivePublishedNews(NewsStatus.PUBLISHED, currentTime);
        // 3. 回傳查詢結果
        return activeNews;
    }

    /**
     * 獲取所有消息（通常用於後台管理）
     *
     * @return 所有消息的列表
     */
    public List<NewsEntity> getAllNews() {
        return newsRepository.findAll();
    }

    /**
     * ✅ 這就是我們新增的、完整且正確的 findById 方法
     * 它被放在 NewsService 這個 class 的大括號 {} 裡面
     *
     * @param newsId 要查找的消息 ID
     * @return 找到的 NewsEntity
     */
    // 在 NewsService.java 中

    public NewsEntity findById(Long newsId) {
        // ✅ 修改成呼叫我們自訂的新方法
        return newsRepository.findByIdWithEmployee(newsId)
                .orElseThrow(() -> new RuntimeException("找不到 ID 為 " + newsId + " 的消息"));
    }

}
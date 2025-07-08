package com.eatfast.news.service;

import com.eatfast.common.enums.NewsStatus; // ❗ 注意：需要加上這個 import
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
    // ✅ 這裡被修正了
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
     * @return 所有消息的列表
     */
    public List<NewsEntity> getAllNews() {
        return newsRepository.findAll();
    }

    // ... 你可以繼續在這裡添加其他 find, delete 方法 ...

}
package com.eatfast.news.service;

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
        return newsRepository.findActivePublishedNews(LocalDateTime.now());
    } // <--- 在這裡補上右大括號，結束 getActivePublishedNews 方法

    /**
     * 獲取所有消息（通常用於後台管理）
     * @return 所有消息的列表
     */
    public List<NewsEntity> getAllNews() {
        // 讓這個方法做一些有意義的事，例如呼叫 findAll()
        return newsRepository.findAll();
    } // <--- getAllNews 方法在這裡獨立結束

    // ... 你可以繼續在這裡添加其他 find, delete 方法 ...

}
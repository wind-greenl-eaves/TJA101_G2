package com.eatfast.news.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<NewsEntity, Long> {
    // 可擴充自訂查詢方法
	List<NewsEntity> findByTitleContainingAndStatus(String keyword, String status);
	List<NewsEntity> findByEmployeeId(Long employeeId);
	List<NewsEntity> findByCreateTimeBetween(LocalDateTime start, LocalDateTime end);
    

    // 模糊查詢標題
    List<NewsEntity> findByTitleContaining(String keyword);

    // 狀態查詢（例如 status = 1）
    List<NewsEntity> findByStatus(Integer status);

	
	//測試做複合查詢,HibernateUtil_CompositeQuery_news還未完成
}

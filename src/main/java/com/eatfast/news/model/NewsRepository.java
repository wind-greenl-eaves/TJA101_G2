package com.eatfast.news.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<NewsEntity, Long> {
//測試用06/26
    // 1️ 依照標題模糊查詢
    List<NewsEntity> findByTitleContaining(String keyword);

    // 2️ 查找特定狀態的公告
    List<NewsEntity> findByStatus(Integer status);

    // 3️ 查找開始時間在某段區間內的公告
    List<NewsEntity> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    // 4️ 查找特定員工所發的公告
    List<NewsEntity> findByEmployeeId(Long employeeId);

    // 5️ 查找特定狀態＋模糊標題
    List<NewsEntity> findByTitleContainingAndStatus(String keyword, Integer status);

    // 6️ 查找目前「有效時間內」的公告（用 JPQL 寫）
    List<NewsEntity> findByStartTimeBeforeAndEndTimeAfter(LocalDateTime now1, LocalDateTime now2);

    // 7️ 查找還沒結束的公告（未過期）
    List<NewsEntity> findByEndTimeAfter(LocalDateTime now);

    // 8️ 查找永久公告（endTime 為 null）
    List<NewsEntity> findByEndTimeIsNull();

}



//以下測試用
//public interface NewsRepository extends JpaRepository<NewsEntity, Long> {
//    // 可擴充自訂查詢方法
//	List<NewsEntity> findByTitleContainingAndStatus(String title, Integer status);
//	List<NewsEntity> findByEmployeeId(Long employeeId);
//	List<NewsEntity> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
//
//    // 模糊查詢標題
//    List<NewsEntity> findByTitleContaining(String keyword);
//
//    // 狀態查詢（例如 status = 1）
//    List<NewsEntity> findByStatus(Integer status);
//
//	
//	//測試做複合查詢,HibernateUtil_CompositeQuery_news還未完成
//}

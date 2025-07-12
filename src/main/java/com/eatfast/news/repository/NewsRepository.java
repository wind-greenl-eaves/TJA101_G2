package com.eatfast.news.repository;

import com.eatfast.common.enums.NewsStatus;
import com.eatfast.news.model.NewsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 最新消息的資料存取層 (Repository)
 * <p>
 * 負責與資料庫的 `news` 資料表進行所有互動。
 * 繼承 JpaRepository 後，會自動擁有多數基本的 CRUD (新增、讀取、更新、刪除) 功能。
 */
@Repository
public interface NewsRepository extends JpaRepository<NewsEntity, Long> {

    /**
     * 【後台專用】根據指定的狀態，查詢所有符合的消息。
     * Spring Data JPA 會自動根據方法名稱產生對應的 SQL 查詢 (WHERE status = ?)。
     * @param status 要查詢的消息狀態 (例如: PUBLISHED 或 DRAFT)
     * @return 符合狀態的消息列表
     */
    List<NewsEntity> findAllByStatus(NewsStatus status);

    /**
     * 【前台專用】找出所有「已發布」且在「有效時間內」的消息。
     * 使用 @Query 自定義 JPQL 查詢，實現更複雜的邏輯：
     * (當前時間 >= startTime AND (endTime is NULL OR 當前時間 < endTime))
     * 並依照開始時間降序排列，讓最新的消息顯示在最前面。
     */
    @Query("SELECT n FROM NewsEntity n WHERE n.status = :status AND n.startTime <= :now AND (n.endTime IS NULL OR n.endTime > :now) ORDER BY n.startTime DESC")
    List<NewsEntity> findActivePublishedNews(@Param("status") NewsStatus status, @Param("now") LocalDateTime now);

    /**
     * 【效能優化】在查詢單筆消息時，立即抓取關聯的員工資料。
     * 使用 JOIN FETCH 可以避免 N+1 查詢問題，一次查詢就取得所有需要的資料。
     * @param id 要查詢的消息 ID
     * @return 包含員工資訊的 Optional<NewsEntity>
     */
    @Query("SELECT n FROM NewsEntity n JOIN FETCH n.employee WHERE n.newsId = :id")
    Optional<NewsEntity> findByIdWithEmployee(@Param("id") Long id);

}

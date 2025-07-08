package com.eatfast.news.repository;// package...
import com.eatfast.common.enums.NewsStatus;
import com.eatfast.news.model.NewsEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<NewsEntity, Long> {

    // 根據狀態查找消息
    List<NewsEntity> findByStatus(NewsStatus status);

    // 根據發布員工查找消息
    List<NewsEntity> findByEmployee_EmployeeId(Long employeeId);

    /**
     * 【推薦】給前台使用的查詢：
     * 找出所有「已發布」且在「有效時間內」的消息
     * (當前時間 >= startTime AND (endTime is NULL OR 當前時間 < endTime))
     */
    @Query("SELECT n FROM NewsEntity n WHERE n.status = :status AND n.startTime <= :now AND (n.endTime IS NULL OR n.endTime > :now) ORDER BY n.startTime DESC")
    List<NewsEntity> findActivePublishedNews(@Param("status") NewsStatus status, @Param("now") LocalDateTime now);
}
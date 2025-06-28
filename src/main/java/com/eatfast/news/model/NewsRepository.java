/*
 * ================================================================
 * 檔案: NewsRepository.java (經嚴格審查與重構)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/news/model/NewsRepository.java
 * - 重構總結:
 * 1. 【命名修正】: 已將 `findBy_EmployeeId` 修正為 `findByEmployee_EmployeeId`，
 * 使其完全符合 Spring Data JPA 對關聯屬性的查詢規則，解決啟動錯誤。
 * 2. 【結構優化】: 為所有查詢方法添加了標準 Javadoc 註解，闡明其功能與用法。
 * 3. 【程式碼清理】: 移除了檔案末端已註解掉的重複程式碼，使介面更清晰。
 */
package com.eatfast.news.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 最新消息資料存取庫 (Repository)
 * <p>
 * 負責處理與 `news` 資料表相關的所有資料庫操作。
 */
@Repository
public interface NewsRepository extends JpaRepository<NewsEntity, Long> {

    /**
     * 根據標題關鍵字進行模糊查詢。
     * @param keyword 用於模糊匹配的標題關鍵字。
     * @return 符合條件的最新消息列表。
     */
    List<NewsEntity> findByTitleContaining(String keyword);

    /**
     * 根據狀態查找最新消息。
     * @param status 狀態碼 (例如: 0=草稿, 1=發布)。
     * @return 符合特定狀態的最新消息列表。
     */
    List<NewsEntity> findByStatus(Integer status);

    /**
     * 查找發布開始時間在指定區間內的最新消息。
     * @param start 起始時間。
     * @param end 結束時間。
     * @return 符合時間區間的最新消息列表。
     */
    List<NewsEntity> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 根據發布員工的主鍵 ID 查找其發布的所有最新消息。
     * <p>
     * 命名慣例: `findByEmployee_EmployeeId` 會被 JPA 解析為 JPQL:
     * {@code "SELECT n FROM NewsEntity n WHERE n.employee.employeeId = :employeeId"}
     *
     * @param employeeId 員工的主鍵 ID。
     * @return 該員工發布的所有最新消息列表。
     */
    List<NewsEntity> findByEmployee_EmployeeId(Long employeeId);

    /**
     * 根據標題關鍵字和狀態進行複合查詢。
     * @param keyword 標題關鍵字。
     * @param status 狀態碼。
     * @return 符合複合查詢條件的最新消息列表。
     */
    List<NewsEntity> findByTitleContainingAndStatus(String keyword, Integer status);

    /**
     * 查找在指定時間點仍然有效的最新消息（即，已開始且尚未結束）。
     * @param now1 當前時間 (用於與 startTime 比較)。
     * @param now2 當前時間 (用於與 endTime 比較)。
     * @return 所有有效時間內的最新消息列表。
     */
    List<NewsEntity> findByStartTimeBeforeAndEndTimeAfter(LocalDateTime now1, LocalDateTime now2);

    /**
     * 查找所有尚未過期的最新消息。
     * @param now 當前時間。
     * @return 結束時間在當前時間之後的所有最新消息列表。
     */
    List<NewsEntity> findByEndTimeAfter(LocalDateTime now);

    /**
     * 查找所有設定為永久有效的最新消息（即，結束時間為 NULL）。
     * @return 所有結束時間為 NULL 的最新消息列表。
     */
    List<NewsEntity> findByEndTimeIsNull();
}

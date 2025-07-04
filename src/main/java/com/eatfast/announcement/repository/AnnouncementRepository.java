
package com.eatfast.announcement.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eatfast.announcement.model.AnnouncementEntity;
import com.eatfast.common.enums.AnnouncementStatus;

@Repository
public interface AnnouncementRepository extends JpaRepository<AnnouncementEntity, Long> {

    // 可依實際需求擴充自訂查詢方法
	
	//依狀態查詢
	List<AnnouncementEntity> findByStatus(AnnouncementStatus status);
	
	//查找目前有效（顯示中）的公告
	List<AnnouncementEntity> findByStartTimeBeforeAndEndTimeAfter(LocalDateTime now1, LocalDateTime now2);
	//查找指定門市的公告
	List<AnnouncementEntity> findByStore_StoreId(Long storeId);
	//查找某個員工發布的公告
	List<AnnouncementEntity> findByEmployee_EmployeeId(Long employeeId);
	
	// 處理查找目前上架的公告（狀態為 ACTIVE，時間在範圍內）
    @Query("SELECT a FROM AnnouncementEntity a " +
           "WHERE a.status = :status " +
           "AND a.startTime <= CURRENT_TIMESTAMP " +
           "AND a.endTime >= CURRENT_TIMESTAMP")
    
    List<AnnouncementEntity> findCurrentlyActiveAnnouncements(AnnouncementStatus status);
}
	


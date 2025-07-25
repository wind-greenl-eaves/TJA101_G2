package com.eatfast.announcement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.eatfast.announcement.model.AnnouncementEntity;
import com.eatfast.common.enums.AnnouncementStatus;

public interface AnnouncementService {

	// 查詢全部
	List<AnnouncementEntity> findAll();

	// 依 ID 查詢
	Optional<AnnouncementEntity> findById(Long id);

	// 新增或更新
	AnnouncementEntity save(AnnouncementEntity announcement);
	void deleteAnnouncementById(Long id); // <-- 新增這個方法
	// 刪除
	void deleteById(Long id);

	// 依狀態查詢
	List<AnnouncementEntity> findByStatus(AnnouncementStatus status);

	// 查詢目前有效的公告（startTime <= now && endTime >= now）
	List<AnnouncementEntity> findActiveAnnouncements(LocalDateTime now);

	// 查詢指定門市的公告
	List<AnnouncementEntity> findByStoreId(Long storeId);

	List<AnnouncementEntity> search(String title, AnnouncementStatus status, LocalDateTime startTime,
			LocalDateTime endTime);

	// 這是列出公告清單的方法
	List<AnnouncementEntity> findCurrentlyActive();

	default // 草稿方法
	void publishById(Long id) {
		AnnouncementEntity announcement = findById(id).orElseThrow(() -> new RuntimeException("找不到公告"));
		announcement.setStatus(AnnouncementStatus.ACTIVE);
		save(announcement);
	}

}

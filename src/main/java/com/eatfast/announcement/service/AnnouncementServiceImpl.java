package com.eatfast.announcement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eatfast.announcement.model.AnnouncementEntity;
import com.eatfast.announcement.repository.AnnouncementRepository;
import com.eatfast.common.enums.AnnouncementStatus;


@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    // @Autowired 會請 Spring 幫我們把寫好的 Repository 物件自動放進來
    @Autowired
    private AnnouncementRepository announcementRepository;

    @Override
    public List<AnnouncementEntity> findAll() {
        // 直接呼叫 Repository 的方法
        return announcementRepository.findAll();
    }

    @Override
    public Optional<AnnouncementEntity> findById(Long id) {
        // 直接呼叫 Repository 的方法
        return announcementRepository.findById(id);
    }

    @Override
    @Transactional // @Transactional 確保整個方法在同一個資料庫交易中完成，比較安全
    public AnnouncementEntity save(AnnouncementEntity announcement) {
        // 直接呼叫 Repository 的方法
        return announcementRepository.save(announcement);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        // 直接呼叫 Repository 的方法
        announcementRepository.deleteById(id);
    }

    @Override
    public List<AnnouncementEntity> findByStatus(AnnouncementStatus status) {
        // 直接呼叫 Repository 的方法
        return announcementRepository.findByStatus(status);
    }

    @Override
    public List<AnnouncementEntity> findByStoreId(Long storeId) {
        // 直接呼叫 Repository 的方法
        return announcementRepository.findByStore_StoreId(storeId);
    }

    /**
     * 【已簡化】查詢目前所有上架且有效的公告。
     * 直接呼叫我們在 Repository 中寫好的 @Query 方法。
     */
    @Override
    public List<AnnouncementEntity> findCurrentlyActive() {
        return announcementRepository.findCurrentlyActiveAnnouncements(AnnouncementStatus.ACTIVE);
    }

    /**
     * 【已簡化】這是我們新的 search 方法。
     * 它現在的工作變得很簡單：直接把從 Controller 收到的參數，
     * 原封不動地交給 Repository 裡我們剛寫好的那個萬用 @Query 方法去處理。
     */
    @Override
    public List<AnnouncementEntity> search(String title, AnnouncementStatus status, LocalDateTime startTime, LocalDateTime endTime) {
        return announcementRepository.search(title, status, startTime, endTime);
    }

    /**
     * 【已優化】發布公告的邏輯
     */
    // 在 AnnouncementServiceImpl.java 裡

    @Override
    @Transactional // 確保整個方法都在一個交易中
    public void publishById(Long id) {
        // 1. 根據 ID 找到公告，如果找不到就拋出錯誤訊息
        //    Optional 是 Java 8 的語法，用來優雅地處理可能為 null 的情況
        Optional<AnnouncementEntity> announcementOpt = announcementRepository.findById(id);

        // 2. 判斷 Optional 裡面是否有東西
        if (announcementOpt.isPresent()) {
            // 如果有，就把它拿出來
            AnnouncementEntity announcement = announcementOpt.get();

            // 3. ⭐ 核心邏輯：更新它的狀態
            announcement.setStatus(AnnouncementStatus.ACTIVE);

            // 4. ⭐ 直接呼叫 repository 的 save 方法來執行更新，這是最穩妥的方式
            announcementRepository.save(announcement);

            // 5. 在 Console 印出成功訊息，方便我們確認
            System.out.println("====== DEBUG: ID = " + id + " 的公告狀態已更新為 ACTIVE，並已儲存。 ======");

        } else {
            // 如果根據 ID 找不到公告，可以印出一個錯誤或拋出例外
            System.out.println("====== DEBUG: 找不到 ID 為 " + id + " 的公告，無法發布。 ======");
            throw new RuntimeException("找不到 ID 為 " + id + " 的公告，無法發布。");
        }
    }

    // 這個方法在目前的 Controller 中沒有被直接使用，可以先保留
    @Override
    public List<AnnouncementEntity> findActiveAnnouncements(LocalDateTime now) {
        return announcementRepository.findByStartTimeBeforeAndEndTimeAfter(now, now);
    }
}
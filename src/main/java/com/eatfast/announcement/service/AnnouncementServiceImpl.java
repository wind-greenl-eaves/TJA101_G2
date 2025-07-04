package com.eatfast.announcement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eatfast.announcement.model.AnnouncementEntity;
import com.eatfast.announcement.repository.AnnouncementRepository;
import com.eatfast.common.enums.AnnouncementStatus;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Override
    public List<AnnouncementEntity> findAll() {
        return announcementRepository.findAll();
    }

    @Override
    public Optional<AnnouncementEntity> findById(Long id) {
        return announcementRepository.findById(id);
    }

    @Override
    public AnnouncementEntity save(AnnouncementEntity announcement) {
        return announcementRepository.save(announcement);
    }

    @Override
    public void deleteById(Long id) {
        announcementRepository.deleteById(id);
    }

    @Override
    public List<AnnouncementEntity> findByStatus(AnnouncementStatus status) {
        return announcementRepository.findByStatus(status);
    }

    @Override
    public List<AnnouncementEntity> findActiveAnnouncements(LocalDateTime now) {
        return announcementRepository.findByStartTimeBeforeAndEndTimeAfter(now, now);
    }

    @Override
    public List<AnnouncementEntity> findByStoreId(Long storeId) {
        return announcementRepository.findByStore_StoreId(storeId);
    }

	@Override
	public List<AnnouncementEntity> search(String title, AnnouncementStatus status, LocalDateTime startTime,
			LocalDateTime endTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AnnouncementEntity> findCurrentlyActive() {
		// TODO Auto-generated method stub
		return null;
	}
	

}

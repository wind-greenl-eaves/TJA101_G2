package com.eatfast.feedback.model;

import com.eatfast.feedback.model.FeedbackEntity;
import com.eatfast.feedback.model.FeedbackRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    // 建構式注入
    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    // === 基本 CRUD ===

    public List<FeedbackEntity> findAll() {
        return feedbackRepository.findAll();
    }

    public Optional<FeedbackEntity> findById(Long id) {
        return feedbackRepository.findById(id);
    }

    public FeedbackEntity save(FeedbackEntity feedback) {
        return feedbackRepository.save(feedback);
    }

    public void deleteById(Long id) {
        feedbackRepository.deleteById(id);
    }

    // === 自訂查詢 ===

    public List<FeedbackEntity> findByMemberId(Long memberId) {
        return feedbackRepository.findByMember_MemberId(memberId);
    }

    public List<FeedbackEntity> findByStoreId(Long storeId) {
        return feedbackRepository.findByStore_StoreId(storeId);
    }
}

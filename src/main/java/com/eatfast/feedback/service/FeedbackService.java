package com.eatfast.feedback.service;

import com.eatfast.feedback.model.FeedbackEntity;
import com.eatfast.feedback.repository.FeedbackRepository;
import com.eatfast.member.model.MemberEntity;
import com.eatfast.member.repository.MemberRepository;
import com.eatfast.store.model.StoreEntity;
import com.eatfast.store.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class FeedbackService {
    
    private final FeedbackRepository feedbackRepository;
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           MemberRepository memberRepository,
                           StoreRepository storeRepository) {
        this.feedbackRepository = feedbackRepository;
        this.memberRepository = memberRepository;
        this.storeRepository = storeRepository;
    }

    /**
     * 創建新的意見回饋（主要方法）
     * 這是 Controller 調用的方法，包含所有必要參數
     */
    @Transactional
    public FeedbackEntity createFeedback(Long memberId, Long storeId, String phone, 
                                        String content, String diningTime, String diningStore) {
        // 驗證會員存在
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("找不到會員 ID: " + memberId));

        // 處理門市關聯（storeId 可能為 null）
        StoreEntity store = null;
        if (storeId != null) {
            store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new EntityNotFoundException("找不到門市 ID: " + storeId));
        }

        // 建立新的意見回饋實體
        FeedbackEntity feedback = new FeedbackEntity();
        feedback.setPhone(phone);
        feedback.setContent(content);
        feedback.setMember(member);
        feedback.setStore(store);
        feedback.setDiningTime(diningTime);
        feedback.setDiningStore(diningStore);
        feedback.setSubmissionDate(LocalDateTime.now());
        feedback.setStatus("待處理");

        return feedbackRepository.save(feedback);
    }

    /**
     * 取得所有意見回饋（按 ID 降序排列）
     */
    public List<FeedbackEntity> findAll() {
        return feedbackRepository.findAll(Sort.by(Sort.Direction.DESC, "feedbackId"));
    }

    /**
     * 根據 ID 查詢單一意見回饋
     */
    public Optional<FeedbackEntity> findById(Long id) {
        return feedbackRepository.findById(id);
    }

    /**
     * 根據 ID 刪除意見回饋
     */
    @Transactional
    public void deleteById(Long id) {
        feedbackRepository.deleteById(id);
    }

    /**
     * 根據會員 ID 查詢意見回饋
     */
    public List<FeedbackEntity> findByMemberId(Long memberId) {
        return feedbackRepository.findByMember_MemberId(memberId);
    }

    /**
     * 根據門市 ID 查詢意見回饋
     */
    public List<FeedbackEntity> findByStoreId(Long storeId) {
        return feedbackRepository.findByStore_StoreId(storeId);
    }
}
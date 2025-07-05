/*
 * ================================================================
 * 檔案 3: FeedbackService.java (驗證關聯邏輯 - 已修正)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/feedback/service/FeedbackService.java
 * - 說明: 在修正 StoreRepository 後，此檔案的程式碼邏輯將會是完全正確且可編譯的。
 */
package com.eatfast.feedback.service;

import com.eatfast.feedback.model.FeedbackEntity;
import com.eatfast.feedback.repository.FeedbackRepository;
import com.eatfast.member.model.MemberEntity;
import com.eatfast.member.repository.MemberRepository;
import com.eatfast.store.model.StoreEntity;
import com.eatfast.store.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public FeedbackEntity createFeedback(Long memberId, Long storeId, String phone, String content) {
        // 現在，memberRepository.findById(Long) 和 storeRepository.findById(Long) 的呼叫都是型別安全的。
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("找不到會員 ID: " + memberId));
        
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("找不到門市 ID: " + storeId));

        FeedbackEntity feedback = new FeedbackEntity();
        feedback.setPhone(phone);
        feedback.setContent(content);
        feedback.setMember(member);
        feedback.setStore(store);
        
        return feedbackRepository.save(feedback);
    }

    public List<FeedbackEntity> findAll() {
        return feedbackRepository.findAll();
    }

    public Optional<FeedbackEntity> findById(Long id) {
        return feedbackRepository.findById(id);
    }

    @Transactional
    public void deleteById(Long id) {
        feedbackRepository.deleteById(id);
    }

    public List<FeedbackEntity> findByMemberId(Long memberId) {
        return feedbackRepository.findByMember_MemberId(memberId);
    }

    public List<FeedbackEntity> findByStoreId(Long storeId) {
        return feedbackRepository.findByStore_StoreId(storeId);
    }


}
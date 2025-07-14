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
import com.eatfast.feedback.repository.FeedbackRepository;
import com.eatfast.member.repository.MemberRepository;
import com.eatfast.store.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime; // ✅ 引入時間類別
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class FeedbackService {
    // ★ 修正：在這裡宣告 final 的成員變數
    // 就像先把三個「書架」安裝好
    private final FeedbackRepository feedbackRepository;
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;

    // 您的建構子現在可以正確地為「已宣告」的成員變數賦值
    // 這個過程稱為「依賴注入 (Dependency Injection)」
    // ... 您的建構子 (維持不變)
    public FeedbackService(FeedbackRepository feedbackRepository,
                           MemberRepository memberRepository,
                           StoreRepository storeRepository) {
        this.feedbackRepository = feedbackRepository;
        this.memberRepository = memberRepository;
        this.storeRepository = storeRepository;
    }

    @Transactional
    public FeedbackEntity createFeedback(Long memberId, Long storeId, String phone, String content) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("找不到會員 ID: " + memberId));

        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("找不到門市 ID: " + storeId));

        FeedbackEntity feedback = new FeedbackEntity();
        feedback.setPhone(phone);
        feedback.setContent(content);
        feedback.setMember(member);
        feedback.setStore(store);

        // --- ★ 補充建議 ---
        feedback.setSubmissionDate(LocalDateTime.now()); // 設定當前時間為提交時間
        feedback.setStatus("待處理");                     // 設定初始狀態
        // --------------------

        return feedbackRepository.save(feedback);
    }
// 在 FeedbackService.java 的 createFeedback 方法中

    // ...
    @Transactional
    public FeedbackEntity createFeedback(Long memberId, Long storeId, String phone, String content, String diningTime, String diningStore) { // <-- 增加參數
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("找不到會員 ID: " + memberId));

        // 如果您的下拉選單是註解掉的，storeId 可能會是 null，需要處理這種情況
        // 這裡暫時假設 storeId 不會被用到，或者您之後會恢復下拉選單
        // StoreEntity store = storeRepository.findById(storeId)
        //         .orElseThrow(() -> new EntityNotFoundException("找不到門市 ID: " + storeId));

        FeedbackEntity feedback = new FeedbackEntity();
        feedback.setPhone(phone);
        feedback.setContent(content);
        feedback.setMember(member);
        // feedback.setStore(store); // 暫時註解

        // ★★★ 設定新欄位的資料 ★★★
        feedback.setDiningTime(diningTime);
        feedback.setDiningStore(diningStore);

        feedback.setSubmissionDate(LocalDateTime.now());
        feedback.setStatus("待處理");

        return feedbackRepository.save(feedback);
    }

    @Transactional(readOnly = true)

        // ... 其他程式碼 ...

        // ✅ 我們需要的就是這個方法
        public List<FeedbackEntity> findAll() {
            // 加上排序，讓最新的意見顯示在最前面
            return feedbackRepository.findAll(Sort.by(Sort.Direction.DESC, "feedbackId"));
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
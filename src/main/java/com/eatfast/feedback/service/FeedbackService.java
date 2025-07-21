package com.eatfast.feedback.service;

import com.eatfast.feedback.dto.FeedbackListDto;
import com.eatfast.feedback.model.FeedbackEntity;
import com.eatfast.feedback.repository.FeedbackRepository;
import com.eatfast.member.model.MemberEntity;
import com.eatfast.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final MemberRepository memberRepository;

    public FeedbackService(FeedbackRepository feedbackRepository, MemberRepository memberRepository) {
        this.feedbackRepository = feedbackRepository;
        this.memberRepository = memberRepository;
    }

    /**
     * 獲取所有用於顯示在後台列表的 Feedback DTO (包含會員姓名)
     */
    public List<FeedbackListDto> findAllForView() {
        // 1. 從資料庫查詢所有 feedback，並依照 ID 降冪排序
        List<FeedbackEntity> feedbacks = feedbackRepository.findAll(Sort.by(Sort.Direction.DESC, "feedbackId"));

        // 如果沒有任何意見，直接回傳空列表
        if (feedbacks.isEmpty()) {
            return List.of();
        }

        // 2. 收集所有不重複的 memberId
        List<Long> memberIds = feedbacks.stream()
                .map(FeedbackEntity::getMemberId)
                .distinct()
                .collect(Collectors.toList());

        // 3. 一次性查詢所有相關的會員資料，並轉成 Map (ID -> 姓名) 以提升效能
        Map<Long, String> memberIdToNameMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(MemberEntity::getMemberId, MemberEntity::getUsername));

        // 4. 將 FeedbackEntity 轉換成 FeedbackListDto
        return feedbacks.stream().map(feedback -> {
            FeedbackListDto dto = new FeedbackListDto();
            dto.setFeedbackId(feedback.getFeedbackId());
            dto.setMemberId(feedback.getMemberId());
            dto.setPhone(feedback.getPhone());
            dto.setContent(feedback.getContent());
            dto.setCreateTime(feedback.getCreateTime());
            dto.setStatus(feedback.getStatus());
            dto.setMemberName(memberIdToNameMap.getOrDefault(feedback.getMemberId(), "會員不存在"));
            dto.setDiningTime(feedback.getDiningTime());
            dto.setDiningStore(feedback.getDiningStore());
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 根據前端傳來的資料，建立一筆新的意見回饋
     */
    @Transactional
    public FeedbackEntity createFeedback(Long memberId, Long storeId, String phone,
                                         String content, LocalDateTime diningTime, String diningStore) {
        FeedbackEntity feedback = new FeedbackEntity();
        feedback.setMemberId(memberId);
        feedback.setStoreId(storeId);
        feedback.setPhone(phone);
        feedback.setContent(content);
        feedback.setDiningTime(diningTime);
        feedback.setDiningStore(diningStore);
        feedback.setStatus("待處理");
        return feedbackRepository.save(feedback);
    }

    /**
     * 回覆一筆意見回饋
     */
    @Transactional
    public void replyToFeedback(Long feedbackId, String replyContent) {
        FeedbackEntity feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new EntityNotFoundException("找不到 ID 為 " + feedbackId + " 的意見回饋"));
        feedback.setStatus("已處理");
        feedback.setReplyContent(replyContent);
        feedback.setRepliedAt(LocalDateTime.now());
        // processedByAdminId 可以在這裡設定
        feedbackRepository.save(feedback);
    }

    /**
     * 根據 ID 查詢單筆意見回饋
     */
    public Optional<FeedbackEntity> findById(Long id) {
        return feedbackRepository.findById(id);
    }

} // <--- 這個才是 FeedbackService class 真正的結尾
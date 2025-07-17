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
     * 獲取所有用於顯示在列表頁的 Feedback DTO (包含會員姓名)
     */
    public List<FeedbackListDto> findAllForView() {
        List<FeedbackEntity> feedbacks = feedbackRepository.findAll(Sort.by(Sort.Direction.DESC, "feedbackId"));
        if (feedbacks.isEmpty()) {
            return List.of();
        }
        List<Long> memberIds = feedbacks.stream().map(FeedbackEntity::getMemberId).distinct().collect(Collectors.toList());
        Map<Long, String> memberIdToNameMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(MemberEntity::getMemberId, MemberEntity::getUsername));

        // 將 Entity 轉換成 DTO
        return feedbacks.stream().map(feedback -> {
            FeedbackListDto dto = new FeedbackListDto();
            // --- 原有設定 ---
            dto.setFeedbackId(feedback.getFeedbackId());
            dto.setMemberId(feedback.getMemberId());
            dto.setPhone(feedback.getPhone());
            dto.setContent(feedback.getContent());
            dto.setCreateTime(feedback.getCreateTime());
            dto.setStatus(feedback.getStatus());
            dto.setMemberName(memberIdToNameMap.getOrDefault(feedback.getMemberId(), "會員不存在"));

            // ★★★ 新增的設定 ★★★
            dto.setDiningTime(feedback.getDiningTime());
            dto.setDiningStore(feedback.getDiningStore());

            return dto;
        }).collect(Collectors.toList());
    }

    // ... 您其他的 Service 方法 (createFeedback, replyToFeedback, findById 等) 維持不變 ...
    // 請保留您其他的 Service 方法
    @Transactional
    public FeedbackEntity createFeedback(Long memberId, Long storeId, String phone,
                                         String content, String diningTime, String diningStore) {
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

    @Transactional
    public void replyToFeedback(Long feedbackId, String replyContent) {
        FeedbackEntity feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new EntityNotFoundException("找不到 ID 為 " + feedbackId + " 的意見回饋"));
        feedback.setStatus("已處理");
        feedback.setReplyContent(replyContent);
        feedback.setRepliedAt(LocalDateTime.now());
        feedbackRepository.save(feedback);
    }

    public Optional<FeedbackEntity> findById(Long id) {
        return feedbackRepository.findById(id);
    }
}

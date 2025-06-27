package com.eatfast.feedback.controller;

import com.eatfast.feedback.model.FeedbackEntity;
import com.eatfast.feedback.model.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Autowired
    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    // === 查詢全部回饋 ===
    @GetMapping
    public List<FeedbackEntity> getAllFeedback() {
        return feedbackService.findAll();
    }

    // === 查詢單筆回饋 ===
    @GetMapping("/{id}")
    public Optional<FeedbackEntity> getFeedbackById(@PathVariable Long id) {
        return feedbackService.findById(id);
    }

    // === 根據會員 ID 查回饋 ===
    @GetMapping("/member/{memberId}")
    public List<FeedbackEntity> getFeedbackByMemberId(@PathVariable Long memberId) {
        return feedbackService.findByMemberId(memberId);
    }

    // === 根據門市 ID 查回饋 ===
    @GetMapping("/store/{storeId}")
    public List<FeedbackEntity> getFeedbackByStoreId(@PathVariable Long storeId) {
        return feedbackService.findByStoreId(storeId);
    }

    // === 新增一筆回饋 ===
    @PostMapping
    public FeedbackEntity createFeedback(@RequestBody FeedbackEntity feedback) {
        return feedbackService.save(feedback);
    }

    // === 刪除一筆回饋 ===
    @DeleteMapping("/{id}")
    public void deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteById(id);
    }
}

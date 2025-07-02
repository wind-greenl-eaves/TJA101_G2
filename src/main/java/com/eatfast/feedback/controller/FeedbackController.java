package com.eatfast.feedback.controller;

import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.eatfast.feedback.model.FeedbackEntity;
import com.eatfast.feedback.model.FeedbackService;
import com.eatfast.member.model.MemberEntity;
import com.eatfast.store.model.StoreEntity;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    // 顧客填寫表單的頁面（http://localhost:8080/feedback/form）
    @GetMapping("/form")
    public String showForm() {
        return "select_page"; // ✅ 對應 templates/select_page.html
    }

    // 接收表單資料（表單 method="post" action="/feedback/submit"）
    @PostMapping("/submit")
    public String submitFeedback(@RequestParam Long memberId,
                                 @RequestParam Long storeId,
                                 @RequestParam String phone,
                                 @RequestParam String content) {

        FeedbackEntity feedback = new FeedbackEntity();
        feedback.setMember(new MemberEntity(memberId));
        feedback.setStore(new StoreEntity(storeId));
        feedback.setPhone(phone);
        feedback.setContent(content);
        feedback.setCreateTime(LocalDateTime.now());

        feedbackService.save(feedback);

        return "redirect:/feedback/thanks"; // ✅ 導向感謝頁
    }

    // 顯示感謝頁（http://localhost:8080/feedback/thanks）
    @GetMapping("/thanks")
    public String showThankYouPage() {
        return "feedback_thanks"; // ✅ 對應 templates/feedback_thanks.html
    }
}


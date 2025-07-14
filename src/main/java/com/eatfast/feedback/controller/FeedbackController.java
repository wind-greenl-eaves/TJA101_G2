package com.eatfast.feedback.controller;

import com.eatfast.feedback.model.FeedbackEntity;
import com.eatfast.feedback.service.FeedbackService;
import com.eatfast.member.controller.MemberViewConstants;
import com.eatfast.member.model.MemberEntity;
import com.eatfast.member.service.MemberService;
import com.eatfast.store.model.StoreEntity;
import com.eatfast.store.service.StoreService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final MemberService memberService;
    private final StoreService storeService;

    public FeedbackController(FeedbackService feedbackService, MemberService memberService, StoreService storeService) {
        this.feedbackService = feedbackService;
        this.memberService = memberService;
        this.storeService = storeService;
    }

    @GetMapping("/form")
    public String showFeedbackForm(Model model, HttpSession session) {
        Long memberId = (Long) session.getAttribute("loggedInMemberId");


        if (memberId == null) {
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN;
        }

        try {
            MemberEntity loggedInMember = memberService.getMemberById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("登入的會員不存在，ID: " + memberId));

            List<StoreEntity> storeList = storeService.getAllStores();

            FeedbackEntity feedback = new FeedbackEntity();
            feedback.setMember(loggedInMember);
            feedback.setPhone(loggedInMember.getPhone());

            model.addAttribute("feedback", feedback);
            model.addAttribute("storeList", storeList);

            return "front-end/feedback/form";

        } catch (EntityNotFoundException e) {
            session.invalidate();
            // ✅ 這裡現在可以安全地執行了
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN + "?error=invalid_user";
        }
    }

    @PostMapping("/submit")
    public String submitFeedback(@ModelAttribute("feedback") FeedbackEntity feedback,
                                 // 這個 @RequestParam 寫法是正確的，它會去抓取名為 "store.storeId" 的參數
                                 @RequestParam("store.storeId") Long storeId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        Long memberId = (Long) session.getAttribute("loggedInMemberId");
        if (memberId == null) {
            // ✅ 簡化寫法
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN;
        }

        try {
            feedbackService.createFeedback(
                    memberId,
                    storeId,
                    feedback.getPhone(),
                    feedback.getContent()
            );

            redirectAttributes.addFlashAttribute("successMessage", "您的意見已成功送出，感謝您的回饋！");
            return "redirect:/feedback/thanks";

        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "提交失敗：" + e.getMessage());
            return "redirect:/feedback/form";
        }
    }

    @GetMapping("/thanks")
    public String showThankYouPage() {
        return "front-end/feedback/thanks";
    }
}
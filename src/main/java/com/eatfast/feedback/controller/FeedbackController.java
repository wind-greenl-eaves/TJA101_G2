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

    // 建構子 (維持不變)
    public FeedbackController(FeedbackService feedbackService, MemberService memberService, StoreService storeService) {
        this.feedbackService = feedbackService;
        this.memberService = memberService;
        this.storeService = storeService;
    }

    // 顯示表單的方法 (維持不變)
    @GetMapping("/form")
    public String showFeedbackForm(Model model, HttpSession session) {
        Long memberId = (Long) session.getAttribute("loggedInMemberId");

        if (memberId == null) {
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN;
        }

        try {
            MemberEntity loggedInMember = memberService.getMemberById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("登入的會員不存在，ID: " + memberId));

            // 即使前端沒用到，後端還是可以先準備好
            List<StoreEntity> storeList = storeService.getAllStores();

            FeedbackEntity feedback = new FeedbackEntity();
            feedback.setMember(loggedInMember);
            feedback.setPhone(loggedInMember.getPhone());

            model.addAttribute("feedback", feedback);
            model.addAttribute("storeList", storeList); // 將 storeList 傳遞給前端

            return "front-end/feedback/form";

        } catch (EntityNotFoundException e) {
            session.invalidate();
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN + "?error=invalid_user";
        }
    }

    // ✅ 這是唯一且合併修正後的 submitFeedback 方法
    @PostMapping("/submit")
    public String submitFeedback(@ModelAttribute("feedback") FeedbackEntity feedback,
                                 // ✅ 將 storeId 設為非必要參數，這樣即使前端沒傳，程式也不會出錯
                                 @RequestParam(name = "store.storeId", required = false) Long storeId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        Long memberId = (Long) session.getAttribute("loggedInMemberId");
        if (memberId == null) {
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN;
        }

        try {
            // ✅ 呼叫最新版本的 service 方法，包含所有欄位
            feedbackService.createFeedback(
                    memberId,
                    storeId, // 如果前端沒傳 storeId，這裡會是 null
                    feedback.getPhone(),
                    feedback.getContent(),
                    feedback.getDiningTime(),
                    feedback.getDiningStore()
            );

            redirectAttributes.addFlashAttribute("successMessage", "您的意見已成功送出，感謝您的回饋！");
            return "redirect:/feedback/save";

        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "提交失敗：" + e.getMessage());
            return "redirect:/feedback/form";
        }
    }

    // 顯示感謝頁面的方法 (維持不變)
    @GetMapping("/save")
    public String showSavePage() {
        return "front-end/feedback/save"; // 指向您 save.html 的實際路徑
    }
}
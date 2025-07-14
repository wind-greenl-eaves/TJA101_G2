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
    // 在 FeedbackController.java 中

    @GetMapping("/list")
    public String showFeedbackList(Model model) {
        // 1. 從 Service 獲取所有的意見回饋資料
        List<FeedbackEntity> feedbackList = feedbackService.findAll();

        // 2. 將獲取到的清單資料加入到 Model 中
        model.addAttribute("feedbackList", feedbackList);

        // ✅ 修正：將路徑指向 back-end 目錄下
        return "back-end/feedback/feedback_list";
    }
    // ✅ 這是唯一且合併修正後的 submitFeedback 方法
    // 在 FeedbackController.java 中

    // ✅ 這是唯一且合併修正後的 submitFeedback 方法
    // 在 FeedbackController.java 中

    @PostMapping("/submit")
    public String submitFeedback(@ModelAttribute("feedback") FeedbackEntity feedback,
                                 @RequestParam(name = "store.storeId", required = false) Long storeId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        Long memberId = (Long) session.getAttribute("loggedInMemberId");
        if (memberId == null) {
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN;
        }

        // ✅ 將 catch 的範圍擴大為 Exception
        try {
            feedbackService.createFeedback(
                    memberId,
                    storeId,
                    feedback.getPhone(),
                    feedback.getContent(),
                    feedback.getDiningTime(),
                    feedback.getDiningStore()
            );

            redirectAttributes.addFlashAttribute("successMessage", "您的意見已成功送出，感謝您的回饋！");
            return "redirect:/feedback/save";

        } catch (Exception e) { // <--- 已修改為 Exception
            // 捕獲到任何錯誤時，都將錯誤訊息加入重定向屬性中
            redirectAttributes.addFlashAttribute("errorMessage", "提交失敗，請檢查您的輸入或稍後再試。");

            // 在後台印出詳細的錯誤日誌，方便您自己除錯
            e.printStackTrace();

            // 重定向回表單頁面
            return "redirect:/feedback/form";
        }
    }

    // 顯示感謝頁面的方法 (維持不變)
    @GetMapping("/save")
    public String showSavePage() {
        return "front-end/feedback/save"; // 指向您 save.html 的實際路徑
    }
}
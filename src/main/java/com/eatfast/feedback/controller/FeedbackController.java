package com.eatfast.feedback.controller;

import com.eatfast.feedback.dto.FeedbackListDto;
import com.eatfast.feedback.model.FeedbackEntity;
import com.eatfast.feedback.service.FeedbackService;
import com.eatfast.member.controller.MemberViewConstants;
import com.eatfast.member.model.MemberEntity;
import com.eatfast.member.service.MemberService;
import com.eatfast.store.model.StoreEntity;
import com.eatfast.store.service.StoreService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    // ... 前台功能 (form, submit, save) 維持不變 ...
    @GetMapping("/form")
    public String showFeedbackForm(Model model, HttpSession session) {
        Long memberId = (Long) session.getAttribute("loggedInMemberId");
        if (memberId == null) { return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN; }
        try {
            MemberEntity loggedInMember = memberService.getMemberById(memberId).orElseThrow(() -> new EntityNotFoundException("登入的會員不存在，ID: " + memberId));
            FeedbackEntity feedback = new FeedbackEntity();
            feedback.setPhone(loggedInMember.getPhone());
            model.addAttribute("feedback", feedback);
            model.addAttribute("memberName", loggedInMember.getUsername());
            List<StoreEntity> storeList = storeService.getAllStores();
            model.addAttribute("storeList", storeList);
            return "front-end/feedback/form";
        } catch (EntityNotFoundException e) {
            session.invalidate();
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN + "?error=invalid_user";
        }
    }

    @PostMapping("/submit")
    public String submitFeedback(@ModelAttribute("feedback") FeedbackEntity feedback, @RequestParam(name = "storeId", required = false) Long storeId, HttpSession session, RedirectAttributes redirectAttributes) {
        Long memberId = (Long) session.getAttribute("loggedInMemberId");
        if (memberId == null) { return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN; }
        try {
            feedbackService.createFeedback(memberId, storeId, feedback.getPhone(), feedback.getContent(), feedback.getDiningTime(), feedback.getDiningStore());
            redirectAttributes.addFlashAttribute("successMessage", "您的意見已成功送出，感謝您的回饋！");
            return "redirect:/feedback/save";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "提交失敗，請檢查您的輸入或稍後再試。");
            e.printStackTrace();
            return "redirect:/feedback/form";
        }
    }

    @GetMapping("/save")
    public String showSavePage() { return "front-end/feedback/save"; }


    // ==========================================================
    // == 後台管理用功能
    // ==========================================================

    @GetMapping("/list")
    public String showFeedbackList(Model model) {
        List<FeedbackListDto> feedbackDtoList = feedbackService.findAllForView();
        model.addAttribute("feedbackList", feedbackDtoList);
        return "back-end/feedback/feedback_list";
    }

    // --- ★★★ 核心修改處 ★★★ ---
    @GetMapping("/{feedbackId}/reply")
    public String showReplyForm(@PathVariable Long feedbackId, Model model) {
        // 步驟 1：找到 feedback 物件 (維持不變)
        FeedbackEntity feedback = feedbackService.findById(feedbackId)
                .orElseThrow(() -> new EntityNotFoundException("找不到 ID 為 " + feedbackId + " 的意見回饋"));

        // 步驟 2：用 feedback 裡的 memberId 去查詢會員姓名
        String memberName = memberService.getMemberById(feedback.getMemberId())
                .map(MemberEntity::getUsername) // 從 MemberEntity 取出姓名
                .orElse("會員不存在"); // 如果找不到，給一個預設值

        // 步驟 3：把 feedback 物件和查到的 memberName 都放進 model
        model.addAttribute("feedback", feedback);
        model.addAttribute("memberName", memberName); // ★★★ 把姓名也傳過去 ★★★

        return "back-end/feedback/feedback_reply";
    }

    @PostMapping("/reply")
    public String processReply(@RequestParam("feedbackId") Long feedbackId,
                               @RequestParam("replyContent") String replyContent,
                               RedirectAttributes redirectAttributes) {
        try {
            feedbackService.replyToFeedback(feedbackId, replyContent);
            redirectAttributes.addFlashAttribute("successMessage", "意見回覆成功！狀態已更新為已處理。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "回覆失敗：" + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/feedback/list";
    }

    // ... 除錯用功能維持不變 ...
    @GetMapping("/debug-user")
    @ResponseBody
    public String debugUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) { return "偵測結果：使用者未登入！"; }
        String username = authentication.getName();
        java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities = authentication.getAuthorities();
        return "使用者: " + username + " | 權限: " + authorities;
    }
}


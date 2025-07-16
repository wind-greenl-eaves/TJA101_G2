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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ResponseBody;

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

    // ==========================================================
    // == 前台顧客用功能 (Form & Submit)
    // ==========================================================

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
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN + "?error=invalid_user";
        }
    }

    @PostMapping("/submit")
    public String submitFeedback(@ModelAttribute("feedback") FeedbackEntity feedback,
                                 @RequestParam(name = "store.storeId", required = false) Long storeId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Long memberId = (Long) session.getAttribute("loggedInMemberId");
        if (memberId == null) {
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN;
        }
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
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "提交失敗，請檢查您的輸入或稍後再試。");
            e.printStackTrace();
            return "redirect:/feedback/form";
        }
    }

    @GetMapping("/save")
    public String showSavePage() {
        return "front-end/feedback/save";
    }

    // ==========================================================
    // == 後台管理用功能 (List & Debug)
    // ==========================================================

    @GetMapping("/list")
    public String showFeedbackList(Model model) {
        List<FeedbackEntity> feedbackList = feedbackService.findAll();
        model.addAttribute("feedbackList", feedbackList);
        return "back-end/feedback/feedback_list";
    }

    @GetMapping("/debug-user")
    @ResponseBody
    public String debugUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "偵測結果：使用者未登入！";
        }
        String username = authentication.getName();
        java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities = authentication.getAuthorities();
        System.out.println("====== 使用者除錯資訊 ======");
        System.out.println("登入帳號: " + username);
        System.out.println("擁有權限: " + authorities);
        System.out.println("===========================");
        return "使用者: " + username + " | 權限: " + authorities;
    }
}
package com.eatfast.feedback.controller;

import com.eatfast.feedback.dto.FeedbackDTO; // ★ 引入我們建立的 DTO
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
import jakarta.validation.Valid; // ★ 引入 @Valid
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // ★ 引入 BindingResult
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

    // ==========================================================
    // == 前台顧客意見
    // ==========================================================

    /**
     * 顯示意見回饋表單
     */
    @GetMapping("/form")
    public String showFeedbackForm(Model model, HttpSession session) {
        Long memberId = (Long) session.getAttribute("loggedInMemberId");
        if (memberId == null) {
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN;
        }

        try {
            MemberEntity loggedInMember = memberService.getMemberById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("登入的會員不存在，ID: " + memberId));

            // ★ 修正 #1：建立 FeedbackDTO 而不是 FeedbackEntity
            FeedbackDTO feedbackDTO = new FeedbackDTO();
            // 將會員資料預先填入 DTO
            feedbackDTO.setMemberName(loggedInMember.getUsername());
            feedbackDTO.setContactPhone(loggedInMember.getPhone());

            model.addAttribute("feedbackDTO", feedbackDTO); // ★ 修正 #2：將 DTO 物件傳給前端
            model.addAttribute("storeList", storeService.getAllStores()); // 傳遞門市列表
            List<StoreEntity> stores = storeService.getAllStores();
            System.out.println(">>> [除錯] 從 StoreService 抓到的門市數量: " + stores.size());
            return "front-end/feedback/form";

        } catch (EntityNotFoundException e) {
            session.invalidate();
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN + "?error=invalid_user";
        }
    }

    /**
     * 處理意見回饋表單的提交 (已整合驗證功能)
     */
    @PostMapping("/submit")
    public String submitFeedback(@Valid @ModelAttribute("feedbackDTO") FeedbackDTO feedbackDTO,
                                 BindingResult bindingResult,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes,
                                 Model model) { // ★ 新增 Model 參數以便在驗證失敗時使用

        Long memberId = (Long) session.getAttribute("loggedInMemberId");
        if (memberId == null) {
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN;
        }

        // ★ 核心修改：先檢查驗證結果
        if (bindingResult.hasErrors()) {
            System.out.println("表單驗證失敗，返回意見回饋頁面。");
            // 如果驗證失敗，必須重新提供表單頁面所需的其他資料 (例如門市列表)
            model.addAttribute("storeList", storeService.getAllStores());
            return "front-end/feedback/form"; // 返回表單頁面，Thymeleaf 會顯示錯誤訊息
        }

        // 如果驗證成功，才執行後續的儲存邏輯
        try {
            // 從 DTO 取出資料，呼叫你的 service 方法
            feedbackService.createFeedback(
                    memberId,
                    feedbackDTO.getStoreId(),       // ✅ 正確：直接使用 getStoreId()
                    feedbackDTO.getContactPhone(),
                    feedbackDTO.getFeedbackContent(),
                    feedbackDTO.getDiningTime(),
                    null // ✅ 正確：因為已經傳遞了 storeId，原本需要 storeName 的參數現在可以傳 null
                    // (或者，更好的方式是修改 createFeedback 方法，讓它不再需要這個多餘的參數)
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
    // == 後台管理功能 (這部分維持不變)
    // ==========================================================

    @GetMapping("/list")
    public String showFeedbackList(Model model) {
        List<FeedbackListDto> feedbackDtoList = feedbackService.findAllForView();
        model.addAttribute("feedbackList", feedbackDtoList);
        return "back-end/feedback/feedback_list";
    }

    @GetMapping("/{feedbackId}/reply")
    public String showReplyForm(@PathVariable Long feedbackId, Model model) {
        FeedbackEntity feedback = feedbackService.findById(feedbackId)
                .orElseThrow(() -> new EntityNotFoundException("找不到 ID 為 " + feedbackId + " 的意見回饋"));
        String memberName = memberService.getMemberById(feedback.getMemberId())
                .map(MemberEntity::getUsername)
                .orElse("會員不存在");
        model.addAttribute("feedback", feedback);
        model.addAttribute("memberName", memberName);
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

    @GetMapping("/debug-user")
    @ResponseBody
    public String debugUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "偵測結果：使用者未登入！";
        }
        String username = authentication.getName();
        return "使用者: " + username + " | 權限: " + authentication.getAuthorities();
    }
}
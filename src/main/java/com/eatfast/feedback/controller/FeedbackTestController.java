///*
// * ================================================================
// * 檔案 2: FeedbackController.java (維持不變)
// * ================================================================
// * - 存放目錄: src/main/java/com/eatfast/feedback/controller/FeedbackController.java
// * - 核心改動:
// * 1. 職責簡化: Controller 不再負責創建 Entity，只負責接收請求參數。
// * 2. 安全性提升: 將創建邏輯完全委派給 Service 層，由 Service 層負責驗證 ID。
// * 3. 錯誤處理: 新增 try-catch 區塊，以處理 Service 層可能拋出的例外。
// */
//package com.eatfast.feedback.controller;
//
//import com.eatfast.feedback.service.FeedbackService;
//import jakarta.persistence.EntityNotFoundException;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//@Controller
//@RequestMapping("/feedback")
//public class FeedbackController {
//
//    private final FeedbackService feedbackService;
//
//    public FeedbackController(FeedbackService feedbackService) {
//        this.feedbackService = feedbackService;
//    }
//
//    // 顯示表單的頁面 (維持不變)
//    @GetMapping("/form")
//    public String showForm() {
//        // 此處應返回您的意見回饋表單頁面的視圖名稱
//        // 例如: return "front-end/feedback/feedback_form";
//
//        return "front-end/feedback/form";
//    }
//    //
//    /**
//     * 【核心重構】: 接收表單資料。
//     * - 不再創建 Entity，而是將接收到的原始參數直接傳遞給 Service 層。
//     */
//    @PostMapping("/submit")
//    public String submitFeedback(@RequestParam Long memberId,
//                                 @RequestParam Long storeId,
//                                 @RequestParam String phone,
//                                 @RequestParam String content,
//                                 RedirectAttributes redirectAttributes) {
//
//        try {
//            // 1. 【業務委派】直接呼叫 Service 層處理業務邏輯。
//            feedbackService.createFeedback(memberId, storeId, phone, content);
//
//            // 2. 【成功路徑】設定成功訊息並重定向到感謝頁面。
//            redirectAttributes.addFlashAttribute("successMessage", "您的意見已成功送出，感謝您的回饋！");
//            return "redirect:/feedback/thanks";
//
//        } catch (EntityNotFoundException e) {
//            // 3. 【失敗路徑】捕獲 Service 層拋出的「關聯找不到」的錯誤。
//            redirectAttributes.addFlashAttribute("errorMessage", "提交失敗：" + e.getMessage());
//            // 重定向回表單頁面，並顯示錯誤。
//            return "redirect:/feedback/form";
//        }
//    }
//    //
//    // 顯示感謝頁面 (維持不變)
//    @GetMapping("/thanks")
//    public String showThankYouPage() {
//        // 此處應返回您的感謝頁面的視圖名稱
//        return "feedback/thanks";
//    }
//}
package com.eatfast.feedback.controller;

import com.eatfast.announcement.controller.AdminAnnouncementController;
import com.eatfast.feedback.model.FeedbackEntity;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/feedback")
public class FeedbackTestController {

    // ✅ 顯示顧客意見表單的頁面
    @GetMapping("/form")
    public String formPage(Model model) {
        model.addAttribute("feedback", new FeedbackEntity());
        return "front-end/feedback/form";
    }
//    @PostMapping("/submit")
//    public String submitFeedback(@ModelAttribute FeedbackEntity feedback,
//                                 RedirectAttributes redirectAttributes) {
//        try {
//            AdminAnnouncementController feedbackService = null;
//            feedbackService.save(feedback); // ✅ 儲存到資料庫
//            redirectAttributes.addFlashAttribute("successMessage", "感謝您的意見！");
//            return "redirect:/feedback/thanks"; // ✅ 成功後導向感謝頁
//        } catch (EntityNotFoundException e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "提交失敗：「" + e.getMessage() + "」");
//            return "redirect:/feedback/form";
//        }
//    }

}

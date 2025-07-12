package com.eatfast.news.controller;

import com.eatfast.common.enums.NewsStatus;
import com.eatfast.employee.model.EmployeeEntity;
import com.eatfast.employee.repository.EmployeeRepository;
import com.eatfast.news.model.NewsEntity;
import com.eatfast.news.service.NewsService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 最新消息後台管理 Controller
 * <p>
 * 負責處理所有後台對「最新消息」的相關操作，包含：
 * - 顯示列表 (依權限過濾)
 * - 顯示新增表單
 * - 處理新增請求 (包含草稿與發布)
 * - 處理刪除請求
 * - 顯示單筆詳情
 * <p>
 * 已整合 Spring Security 進行方法級別的權限控制。
 */
@Controller
@RequestMapping("/admin/news")
public class AdminNewsController {

    private final NewsService newsService;
    private final EmployeeRepository employeeRepository; // 保留以便未來使用 Principal

    @Autowired
    public AdminNewsController(NewsService newsService, EmployeeRepository employeeRepository) {
        this.newsService = newsService;
        this.employeeRepository = employeeRepository;
    }

    /**
     * 顯示「新增消息」的表單頁面。
     * 權限：僅限總部管理員。
     */
    @PreAuthorize("hasAuthority('HEADQUARTERS_ADMIN')")
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("news", new NewsEntity());
        return "back-end/news/news_add_form";
    }

    /**
     * 處理「新增消息」的表單提交，可區分為「儲存為草稿」或「直接發布」。
     * 權限：僅限總部管理員。
     */
    @PreAuthorize("hasAuthority('HEADQUARTERS_ADMIN')")
    @PostMapping("/create")
    public String processCreateForm(
            @Valid @ModelAttribute("news") NewsEntity news,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam("action") String action) { // 接收按鈕的值

        if (bindingResult.hasErrors()) {
            return "back-end/news/news_add_form";
        }

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = newsService.saveImage(imageFile);
                news.setImageUrl(imageUrl);
            }
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "圖片上傳失敗！");
            return "back-end/news/news_add_form";
        }

        // 根據 action 的值，設定不同的消息狀態
        if ("publish".equals(action)) {
            news.setStatus(NewsStatus.PUBLISHED);
            redirectAttributes.addFlashAttribute("successMessage", "消息已成功發布！");
        } else {
            news.setStatus(NewsStatus.DRAFT);
            redirectAttributes.addFlashAttribute("successMessage", "消息已儲存為草稿！");
        }

        // TODO: 未來應改為從 Principal 動態獲取當前登入的員工 ID
        Long tempEmployeeId = 1L;
        newsService.saveNews(news, tempEmployeeId);

        return "redirect:/admin/news/list";
    }

    /**
     * 顯示後台消息列表頁，內容會根據使用者權限過濾。
     * 權限：所有已登入的員工皆可訪問。
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list")
    public String showNewsList(Model model) {
        List<NewsEntity> visibleNews = newsService.getVisibleNewsForCurrentUser();
        model.addAttribute("newsList", visibleNews);
        return "back-end/news/news_list_all";
    }

    /**
     * 顯示單一最新消息的詳細內容。
     * 權限：所有已登入的員工皆可訪問。
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/view/{id}")
    public String showNewsDetail(@PathVariable("id") Long newsId, Model model, RedirectAttributes redirectAttributes) {
        try {
            NewsEntity news = newsService.findById(newsId);
            model.addAttribute("newsItem", news);
            return "back-end/news/news_view_detail";
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "找不到該則消息 (ID: " + newsId + ")");
            return "redirect:/admin/news/list";
        }
    }
    // 測試版本//
    /**
     * 處理刪除最新消息的請求。
     * 權限：僅限總部管理員。
     */
    @PreAuthorize("hasAuthority('HEADQUARTERS_ADMIN')")
    @GetMapping("/delete/{id}")
    public String deleteNews(@PathVariable("id") Long newsId, RedirectAttributes redirectAttributes) {
        try {
            newsService.deleteNewsById(newsId);
            redirectAttributes.addFlashAttribute("successMessage", "消息 (ID: " + newsId + ") 已成功刪除！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "刪除失敗：" + e.getMessage());
        }
        return "redirect:/admin/news/list";
    }

    // TODO: 未來在這裡實作編輯功能 (showEditForm, processEditForm)
}

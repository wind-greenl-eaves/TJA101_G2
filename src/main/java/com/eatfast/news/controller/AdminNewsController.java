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

@Controller
@RequestMapping("/admin/news")
// ★ 移除原本在類別層級的 @PreAuthorize 註解 ★
public class AdminNewsController {

    private final NewsService newsService;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public AdminNewsController(NewsService newsService, EmployeeRepository employeeRepository) {
        this.newsService = newsService;
        this.employeeRepository = employeeRepository;
    }

    // ★ 只有總部管理員可以進入「新增」頁面 ★
    @PreAuthorize("hasRole('HEADQUARTERS_ADMIN')")
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("news", new NewsEntity());
        return "back-end/news/news_add_form";
    }

    // ★ 只有總部管理員可以提交「新增」表單 ★
    @PreAuthorize("hasRole('HEADQUARTERS_ADMIN')")
    @PostMapping("/create")
    public String processCreateForm(
            @Valid @ModelAttribute("news") NewsEntity news,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            @RequestParam("imageFile") MultipartFile imageFile) {

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

        Long tempEmployeeId = 1L;
        newsService.saveNews(news, tempEmployeeId);

        redirectAttributes.addFlashAttribute("successMessage", "消息新增成功！");
        return "redirect:/admin/news/list";
    }

    // ★ 只要是登入的員工，無論角色，都可以查看列表 ★
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list")
    public String showNewsList(Model model) {
        List<NewsEntity> allNews = newsService.getAllNews();
        model.addAttribute("newsList", allNews);
        return "back-end/news/news_list_all";
    }
    @PreAuthorize("hasRole('HEADQUARTERS_ADMIN')") // 同樣地，只有總部管理員能刪除
    @GetMapping("/delete/{id}")
    public String deleteNews(@PathVariable("id") Long newsId, RedirectAttributes redirectAttributes) {
        try {
            newsService.deleteNewsById(newsId);
            // 使用 RedirectAttributes 來傳遞成功訊息
            redirectAttributes.addFlashAttribute("successMessage", "消息 (ID: " + newsId + ") 已成功刪除！");
        } catch (Exception e) {
            // 如果刪除過程中發生任何錯誤（例如找不到ID），就在頁面上顯示錯誤訊息
            redirectAttributes.addFlashAttribute("errorMessage", "刪除失敗：" + e.getMessage());
        }
        // 無論成功或失敗，最後都重導向回列表頁
        return "redirect:/admin/news/list";
    }
    // ★ 只要是登入的員工，無論角色，都可以查看詳情 ★
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

    // (未來你的編輯和刪除方法，也應該在上方加上 @PreAuthorize("hasRole('HEADQUARTERS_ADMIN')") )

}
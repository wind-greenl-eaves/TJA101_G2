package com.eatfast.news.controller;

import com.eatfast.common.enums.NewsStatus;
import com.eatfast.employee.model.EmployeeEntity;
import com.eatfast.employee.repository.EmployeeRepository;
import com.eatfast.news.model.NewsEntity;
import com.eatfast.news.service.NewsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin/news")
public class AdminNewsController {

    private final NewsService newsService;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public AdminNewsController(NewsService newsService, EmployeeRepository employeeRepository) {
        this.newsService = newsService;
        this.employeeRepository = employeeRepository;
    }

    /**
     * 顯示後台消息列表頁
     */
    @GetMapping("/list")
    public String showNewsList(Model model) {
        List<NewsEntity> allNews = newsService.getAllNews();
        model.addAttribute("newsList", allNews);
        return "back-end/news/news_list_all";
    }

    /**
     * ✅ 處理「查看」按鈕點擊，顯示單一消息詳情
     */
    @GetMapping("/view/{id}")
    public String showNewsDetail(@PathVariable("id") Long newsId, Model model) {
        NewsEntity news = newsService.findById(newsId);
        model.addAttribute("newsDetail", news);
        return "back-end/news/news_view_detail";
    }

    /**
     * 顯示「新增消息」的表單頁面
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("news", new NewsEntity());
        return "back-end/news/news_add_form";
    }

    /**
     * 處理「新增消息」的表單提交
     */
    @PostMapping("/create")
    public String processCreateForm(@Valid @ModelAttribute("news") NewsEntity news,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
                                    @RequestParam("imageFile") MultipartFile imageFile,
                                    @RequestParam("action") String action) {
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

        if ("publish".equals(action)) {
            news.setStatus(NewsStatus.PUBLISHED);
            redirectAttributes.addFlashAttribute("successMessage", "消息已成功發布！");
        } else {
            news.setStatus(NewsStatus.DRAFT);
            redirectAttributes.addFlashAttribute("successMessage", "消息已儲存為草稿！");
        }

        Long tempEmployeeId = 1L;
        newsService.saveNews(news, tempEmployeeId);

        return "redirect:/admin/news/list";
    }

    /**
     * 顯示「編輯消息」的表單頁面
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long newsId, Model model) {
        NewsEntity newsToEdit = newsService.findById(newsId);
        model.addAttribute("news", newsToEdit);
        return "back-end/news/news_edit_form";
    }

    /**
     * 處理「編輯消息」的表單提交
     */
    @PostMapping("/update")
    public String processEditForm(@Valid @ModelAttribute("news") NewsEntity news,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  @RequestParam("imageFile") MultipartFile imageFile) {
        if (bindingResult.hasErrors()) {
            return "back-end/news/news_edit_form";
        }
        try {
            newsService.updateNews(news, imageFile);
        } catch (RuntimeException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "更新失敗：" + e.getMessage());
            return "back-end/news/news_edit_form";
        }
        redirectAttributes.addFlashAttribute("successMessage", "消息 (ID: " + news.getNewsId() + ") 已成功更新！");
        return "redirect:/admin/news/list";
    }

    /**
     * 處理刪除最新消息的請求
     */
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
}
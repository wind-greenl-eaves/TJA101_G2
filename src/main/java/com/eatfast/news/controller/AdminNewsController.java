package com.eatfast.news.controller;

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
import java.security.Principal;
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

    // --- 顯示列表 ---
    @GetMapping("/list")
    public String showNewsList(Model model) {
        List<NewsEntity> allNews = newsService.getAllNews();
        model.addAttribute("newsList", allNews);
        return "back-end/news/news_list_all";
    }

    // --- 顯示詳情 ---
    @GetMapping("/view/{id}")
    public String showNewsDetail(@PathVariable("id") Long newsId, Model model) {
        NewsEntity news = newsService.findById(newsId);
        // ✅ 【已修正】將 "newsItem" 改為 "newsDetail"，與 HTML 模板保持一致
        model.addAttribute("newsDetail", news);
        return "back-end/news/news_view_detail";
    }

    // --- 新增功能 ---
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("news", new NewsEntity());
        return "back-end/news/news_add_form";
    }

    @PostMapping("/create")
    public String processCreateForm(@Valid @ModelAttribute("news") NewsEntity news,
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

    // --- 編輯功能 ---
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long newsId, Model model) {
        NewsEntity newsToEdit = newsService.findById(newsId);
        model.addAttribute("news", newsToEdit);
        return "back-end/news/news_edit_form";
    }

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
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "圖片更新失敗！");
            return "back-end/news/news_edit_form";
        }
        redirectAttributes.addFlashAttribute("successMessage", "消息 (ID: " + news.getNewsId() + ") 已成功更新！");
        return "redirect:/admin/news/list";
    }

    // --- 刪除功能 ---
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

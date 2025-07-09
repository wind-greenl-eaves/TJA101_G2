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
    private final EmployeeRepository employeeRepository; // ✅ 需要注入 EmployeeRepository

    @Autowired
    public AdminNewsController(NewsService newsService, EmployeeRepository employeeRepository) {
        this.newsService = newsService;
        this.employeeRepository = employeeRepository; // ✅ 需要注入 EmployeeRepository
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("news", new NewsEntity());
        return "back-end/news/news_add_form";
    }

    @PostMapping("/create")
    public String processCreateForm(
            @Valid @ModelAttribute("news") NewsEntity news,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Principal principal,
            @RequestParam("imageFile") MultipartFile imageFile) { // ✅ 接收檔案參數

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
            // 在此處添加錯誤處理邏輯，例如向 redirectAttributes 添加錯誤消息
            redirectAttributes.addFlashAttribute("errorMessage", "圖片上傳失敗！");
            return "back-end/news/news_add_form";
        }

        String account = principal.getName();
        EmployeeEntity currentEmployee = employeeRepository.findByAccount(account)
                .orElseThrow(() -> new RuntimeException("系統中找不到帳號為: " + account + " 的員工"));
        newsService.saveNews(news, currentEmployee.getEmployeeId());

        redirectAttributes.addFlashAttribute("successMessage", "消息新增成功！");
        return "redirect:/admin/news/list";
    }

    @GetMapping("/list")
    public String showNewsList(Model model) {
        List<NewsEntity> allNews = newsService.getAllNews();
        model.addAttribute("newsList", allNews);
        return "back-end/news/news_list_all";
    }

    @GetMapping("/{id}")
    public String showNewsDetail(@PathVariable("id") Long newsId, Model model) {
        NewsEntity news = newsService.findById(newsId);
        model.addAttribute("newsDetail", news);
        return "back-end/news/news_view_detail";
    }
}
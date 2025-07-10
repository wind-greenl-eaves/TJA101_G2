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

    // 顯示「新增消息」的表單頁面
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("news", new NewsEntity());
        return "back-end/news/news_add_form";
    }

    // 處理「新增消息」的表單提交
    @PostMapping("/create")
    public String processCreateForm(
            @Valid @ModelAttribute("news") NewsEntity news,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            //Principal principal, // ✅ 暫時註解掉，先不使用登入者資訊
            @RequestParam("imageFile") MultipartFile imageFile) {

        if (bindingResult.hasErrors()) {
            return "back-end/news/news_add_form";
        }

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = newsService.saveImage(imageFile);
                System.out.println(">>> 儲存後的圖片路徑是: " + imageUrl);
                news.setImageUrl(imageUrl);
            }
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "圖片上傳失敗！");
            return "back-end/news/news_add_form";
        }

        /*
         ✅ 因為我們暫時拿掉了 Principal，所以底下這些動態抓取使用者的程式碼也要先註解掉
        String account = principal.getName();
        EmployeeEntity currentEmployee = employeeRepository.findByAccount(account)
                .orElseThrow(() -> new RuntimeException("系統中找不到帳號為: " + account + " 的員工"));
        newsService.saveNews(news, currentEmployee.getEmployeeId());
        */

        // ✅ 改回我們暫時寫死的版本，先假設是 1 號員工發文
        Long tempEmployeeId = 1L;
        newsService.saveNews(news, tempEmployeeId);

        redirectAttributes.addFlashAttribute("successMessage", "消息新增成功！");
        return "redirect:/admin/news/list";
    }

    // 顯示後台消息列表頁的方法
    @GetMapping("/list")
    public String showNewsList(Model model) {
        List<NewsEntity> allNews = newsService.getAllNews();
        model.addAttribute("newsList", allNews);
        return "back-end/news/news_list_all";
    }

    // 處理查看單一最新消息詳情的請求
    @GetMapping("/{id}")
    public String showNewsDetail(@PathVariable("id") Long newsId, Model model) {
        NewsEntity news = newsService.findById(newsId);
        model.addAttribute("newsDetail", news);
        return "back-end/news/news_view_detail";
    }
}
package com.eatfast.news.controller;

import com.eatfast.news.model.NewsEntity;
import com.eatfast.news.service.NewsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/news")
public class AdminNewsController {

    private final NewsService newsService;

    @Autowired
    public AdminNewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    // 顯示「新增消息」的表單頁面
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("news", new NewsEntity());
        // 假設你的新增頁面檔名是 news_add_form.html
        return "back-end/news/news_add_form";
    }

    // 處理「新增消息」的表單提交
    @PostMapping("/create")
    public String processCreateForm(
            @Valid @ModelAttribute("news") NewsEntity news,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            // 如果驗證失敗，停留在原頁面顯示錯誤訊息
            return "news/news_add_form";
        }

        Long currentEmployeeId = 1L;
        newsService.saveNews(news, currentEmployeeId);

        redirectAttributes.addFlashAttribute("successMessage", "消息新增成功！");
        return "redirect:/admin/news/list";
    }

    /**
     * 顯示後台消息列表頁的方法
     * 當使用者訪問 /admin/news/list 時，這個方法會被觸發
     */
    @GetMapping("/list")
    public String showNewsList(Model model) {
        // 1. 從 Service 獲取所有消息
        List<NewsEntity> allNews = newsService.getAllNews();

        // 2. 將消息列表物件 (allNews) 加入到 Model 中，
        //    並命名為 "newsList"，這樣 HTML 才能用 ${newsList} 來取用
        model.addAttribute("newsList", allNews);

        // 3. ✅【已修正】回傳指向 `templates/back-end/news/news_list_all` 的視圖名稱
        return "back-end/news/news_list_all";
    }
}
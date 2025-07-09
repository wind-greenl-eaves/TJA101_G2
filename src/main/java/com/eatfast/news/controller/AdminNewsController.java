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
import org.springframework.web.bind.annotation.PathVariable;
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
        return "back-end/news/news_add_form";
    }

    // 處理「新增消息」的表單提交
    @PostMapping("/create")
    public String processCreateForm(
            @Valid @ModelAttribute("news") NewsEntity news,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            // ✅ 【順手修正】這裡的路徑也要跟 create 表單頁一致
            // 如果驗證失敗，要回到原本的表單頁面，所以路徑必須一樣。
            return "back-end/news/news_add_form";
        }

        Long currentEmployeeId = 1L; // 這裡先假設員工 ID 是 1
        newsService.saveNews(news, currentEmployeeId);

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

    /**
     * ✅ 這就是我們新增的、處理查看單一最新消息詳情的請求
     * @param newsId 從 URL 路徑中獲取的 ID
     * @param model 用來傳遞資料給 View
     * @return 詳情頁面的路徑
     */
    @GetMapping("/{id}")
    public String showNewsDetail(@PathVariable("id") Long newsId, Model model) {
        NewsEntity news = newsService.findById(newsId);
        model.addAttribute("newsDetail", news);
        return "back-end/news/news_view_detail";
    }

} // <--- 這裡是 AdminNewsController class 的結束大括號，只能有一個
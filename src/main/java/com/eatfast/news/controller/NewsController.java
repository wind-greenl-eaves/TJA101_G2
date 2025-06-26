package com.eatfast.news.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.eatfast.news.model.NewsEntity;
import com.eatfast.news.model.NewsService;

@Controller
@RequestMapping("/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    // ✅ 顯示公告列表頁
    @GetMapping("/page")
    public String showNewsPage(Model model) {
        List<NewsEntity> newsList = newsService.getAllNews();
        model.addAttribute("newsList", newsList);
        return "back-end/select_page";
    }

    // ✅ 顯示新增頁面
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("news", new NewsEntity());
        return "back-end/news_add_page"; // 你之後要做的新增頁面
    }

    // ✅ 提交新增或更新（表單送出）
    @PostMapping("/save")
    public String saveNews(@ModelAttribute NewsEntity news, RedirectAttributes redirectAttributes) {
        newsService.saveOrUpdateNews(news);
        redirectAttributes.addFlashAttribute("message", "儲存成功！");
        return "redirect:/news/page";
    }

    // ✅ 顯示編輯頁
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        NewsEntity news = newsService.getNewsById(id).orElse(null);
        if (news == null) {
            return "redirect:/news/page";
        }
        model.addAttribute("news", news);
        return "back-end/news_edit_page"; // 你之後要做的編輯頁
    }

    // ✅ 刪除（用表單 POST）
    @PostMapping("/delete")
    public String deleteNews(@RequestParam Long newsId, RedirectAttributes redirectAttributes) {
        newsService.deleteNewsById(newsId);
        redirectAttributes.addFlashAttribute("message", "刪除成功！");
        return "redirect:/news/page";
    }

    // ✅ 關鍵字查詢
    @GetMapping("/search")
    public String searchNews(@RequestParam String keyword, Model model) {
        List<NewsEntity> newsList = newsService.searchByTitle(keyword);
        model.addAttribute("newsList", newsList);
        return "back-end/select_page";
    }
   
   
}

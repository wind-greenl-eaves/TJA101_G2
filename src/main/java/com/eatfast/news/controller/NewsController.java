package com.eatfast.news.controller;

import com.eatfast.news.model.NewsEntity;
import com.eatfast.news.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/news") // 👈 關鍵一：確保有這個，代表這個 Controller 負責處理 /news 開頭的所有路徑
public class NewsController {

    private final NewsService newsService;

    @Autowired
    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    /**
     * 處理對 /news 的 GET 請求
     */
    @GetMapping // 👈 關鍵二：確保是 @GetMapping，不是 @GetMapping("/") 或 @GetMapping("/list")
    public String showPublicNewsList(Model model) {
        List<NewsEntity> publicNews = newsService.getActivePublishedNews();
        model.addAttribute("publicNewsList", publicNews);
        return "front-end/news/public-list";
    }

    /**
     * 處理對 /news/{id} 的請求，顯示單一消息詳情
     */
    @GetMapping("/{id}")
    public String showPublicNewsDetail(@PathVariable("id") Long newsId, Model model) {
        NewsEntity news = newsService.findById(newsId);
        model.addAttribute("newsDetail", news);
        return "front-end/news/public-detail";
    }
}
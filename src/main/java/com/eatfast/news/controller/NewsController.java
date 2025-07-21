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
@RequestMapping("/news")
public class NewsController {

    private final NewsService newsService;

    @Autowired
    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    /**
     * 處理對 /news 的 GET 請求
     */
    @GetMapping
    public String showPublicNewsList(Model model) {
        try {
            //Redis功能
            // List<NewsEntity> publicNews = newsService.getActivePublishedNews(); // 舊的呼叫
            List<NewsEntity> publicNews = newsService.getLatestNewsWithCache(); // 新的呼叫，會走 Redis 快取


            model.addAttribute("publicNewsList", publicNews);
            return "front-end/news/public-list";
        } catch (Exception e) {
            System.err.println("Error loading news list: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("publicNewsList", List.of());
            return "front-end/news/public-list";
        }
    }

    /**
     * 處理對 /news/{id} 的請求，顯示單一消息詳情
     */
    @GetMapping("/{id}")
    public String showPublicNewsDetail(@PathVariable("id") Long newsId, Model model) {
        try {
            NewsEntity news = newsService.findById(newsId);
            if (news == null) {
                System.out.println("News not found for ID: " + newsId);
                model.addAttribute("newsDetail", null);
                return "front-end/news/public-detail";
            }
            model.addAttribute("newsDetail", news);
            return "front-end/news/public-detail";
        } catch (Exception e) {
            System.err.println("Error loading news detail for ID " + newsId + ": " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("newsDetail", null);
            return "front-end/news/public-detail";
        }
    }
}
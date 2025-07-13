package com.eatfast.news.controller;

import com.eatfast.news.model.NewsEntity;
import com.eatfast.news.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * ✅ 這是一個全新的、乾淨的 NewsController
 * 它的唯一職責就是提供跟「前台新聞」相關的頁面或資料
 */
@Controller
public class NewsController {

    private final NewsService newsService;

    @Autowired
    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    /**
     * 處理來自首頁 JavaScript 的非同步請求，只回傳「最新消息」的 HTML 片段。
     * @param model - 用來將資料傳遞給 Thymeleaf 模板
     * @return Thymeleaf 片段的路徑
     */
    @GetMapping("/news/latest-fragment")
    public String getLatestNewsFragment(Model model) {
        // 1. 從 Service 取得要顯示的公開消息
        List<NewsEntity> newsList = newsService.getActivePublishedNews();

        // 2. 將消息列表放進 Model，取名為 "publicNewsList"測試
        model.addAttribute("publicNewsList", newsList);

        // 3. 回傳到我們建立的片段檔案，並指定只渲染 news_section 這個區塊
        return "front-end/fragments/latest_news :: news_section";
    }

    // 備註：你之前處理 "/" 的 showHomePage 方法已被移除，因為它的功能和 IndexController 重複了。
}
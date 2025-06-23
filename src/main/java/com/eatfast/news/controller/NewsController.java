package com.eatfast.news.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.eatfast.news.model.NewsEntity;
import com.eatfast.news.model.NewsService;

@RestController
@RequestMapping("/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    // ✅ 查詢全部新聞
    @GetMapping
    public List<NewsEntity> getAllNews() {
        return newsService.getAllNews(); // ✔️ 呼叫實作好的方法
    }

    // ✅ 新增或更新新聞
    @PostMapping
    public NewsEntity createOrUpdateNews(@RequestBody NewsEntity news) {
        return newsService.saveOrUpdateNews(news); // ✔️ 呼叫實作好的方法
    }

    // 🆕（可選）模糊查詢標題，如 /news/search?keyword=測試
    @GetMapping("/search")
    public List<NewsEntity> searchNews(@RequestParam String keyword) {
        return newsService.searchByTitle(keyword);
    }

    // 🆕（可選）根據狀態查詢，如 /news/status?status=1
    @GetMapping("/status")
    public List<NewsEntity> getNewsByStatus(@RequestParam Integer status) {
        return newsService.findByStatus(status);
    }

    // 🆕（可選）查單筆新聞 /news/{id}
    @GetMapping("/{id}")
    public NewsEntity getNewsById(@PathVariable Long id) {
        return newsService.getNewsById(id).orElse(null);
    }

    // 🆕（可選）刪除一筆 /news/{id}
    @DeleteMapping("/{id}")
    public void deleteNews(@PathVariable Long id) {
        newsService.deleteNewsById(id);
    }
}

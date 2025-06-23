package com.eatfast.test.Autowired;
//這邊為測試news用，後期完成後可以刪除，目前已串連service(6/23)
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eatfast.news.model.NewsEntity;
import com.eatfast.news.model.NewsService;

@Controller
public class Demo_SpringDataJPA_Controller_testNews {

    @Autowired
    NewsService newsService;

    @RequestMapping("/testNews")
    @ResponseBody
    public String testNewsService() {

        // ● 新增
//        System.out.println("---- 開始測試: 新增 ----");
//        NewsEntity news = new NewsEntity();
//        news.setEmployeeId(1L); // 假設 1 是現有員工的 ID
//        news.setTitle("測試標題 A");
//        news.setContent("這是一篇測試新聞的內容，長度可以到 5000 字。");
//        news.setStartTime(LocalDateTime.now());
//        news.setEndTime(LocalDateTime.now().plusDays(7)); // 顯示七天
//        news.setStatus(1); // 1 = 上架中
//        newsService.saveOrUpdateNews(news);
//        System.out.println("---- 新增完成 ----");
//        return "測試 (新增功能) NewsService OK";

        // ● 修改
//        System.out.println("---- 開始測試: 修改 ----");
//        Optional<NewsEntity> optionalNews = newsService.getNewsById(1L); // 修改你想改的 ID
//        if (optionalNews.isPresent()) {
//            NewsEntity news = optionalNews.get();
//            news.setTitle("更新後的標題");
//            news.setContent("這是被更新的內容！");
//            news.setStatus(0); // 改為下架
//            newsService.saveOrUpdateNews(news);
//            System.out.println("---- 修改完成 ----");
//            return "測試 (修改功能) NewsService OK";
//        } else {
//            System.out.println("找不到要修改的資料");
//            return "修改失敗：找不到資料";
//        }

        // ● 刪除
//        System.out.println("---- 開始測試: 刪除 ----");
//        newsService.deleteNewsById(1L); // 修改成你要刪除的 ID
//        System.out.println("---- 刪除完成 ----");
//        return "測試 (刪除功能) NewsService OK";

        // ● 查詢單筆
//        System.out.println("---- 開始測試: 查詢單筆 ----");
//        Optional<NewsEntity> newsOpt = newsService.getNewsById(1L);
//        newsOpt.ifPresentOrElse(
//            news -> System.out.println("查詢結果：" + news),
//            () -> System.out.println("查無此 ID")
//        );
//        return "測試 (單筆查詢) NewsService OK";

        // ● 查詢全部
        System.out.println("---- 開始測試: 查詢全部 ----");
        List<NewsEntity> list = newsService.getAllNews();
        for (NewsEntity news : list) {
            System.out.println(news);
        }
        System.out.println("---- 查詢全部完成 ----");
        return "測試 (查詢全部) NewsService OK";
    }
}


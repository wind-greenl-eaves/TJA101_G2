package com.eatfast.test.Autowired;

import com.eatfast.news.model.NewsEntity;
import com.eatfast.news.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 專門用於測試 NewsService 功能的 Controller。
 * 【注意】: 這是一個臨時的開發工具，完成功能開發後建議移除或註解掉。
 */
@Controller
public class Demo_SpringDataJPA_Controller_testNews {

    @Autowired
    NewsService newsService;

    /**
     * 測試 NewsService 中的 getVisibleNewsForCurrentUser() 方法。
     * 這個方法會根據當前登入者的權限，回傳不同的消息列表。
     *
     * @return 一段表示測試結果的純文字。
     */
    @RequestMapping("/testNews")
    @ResponseBody // 加上這個註解，代表我們要在瀏覽器上直接顯示回傳的文字
    public String testNewsService() {

        System.out.println("---- 開始測試: 查詢全部 (根據權限) ----");

        try {
            // 呼叫我們在 Service 中寫好的新方法
            List<NewsEntity> visibleNews = newsService.getVisibleNewsForCurrentUser();

            // 在後台 Console 中印出查詢到的資料筆數
            System.out.println("查詢完成，共找到 " + visibleNews.size() + " 筆可見的消息。");

            // 遍歷並印出每一筆消息的標題和狀態，方便快速檢查
            visibleNews.forEach(news ->
                    System.out.println("  - 標題: " + news.getTitle() + ", 狀態: " + news.getStatus())
            );

            // 在瀏覽器上回傳成功訊息
            return "測試 (查詢全部) NewsService OK，請查看 IDE 的 Console (主控台) 來確認詳細輸出。";

        } catch (Exception e) {
            // 如果在測試過程中發生任何錯誤（例如：尚未登入就測試）
            System.err.println("測試失敗，發生例外狀況：" + e.getMessage());
            e.printStackTrace(); // 在 Console 印出詳細的錯誤堆疊
            return "測試失敗，請查看 IDE 的 Console (主控台) 來確認錯誤訊息。";
        }
    }
}

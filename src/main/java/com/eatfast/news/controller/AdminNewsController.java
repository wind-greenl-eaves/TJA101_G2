package com.eatfast.news.controller;


// 1. 確保你 import 的是正確的 NewsEntity 和 NewsService
import com.eatfast.news.model.NewsEntity;
import com.eatfast.news.service.NewsService;

// 2. 引入 Spring 和 Jakarta Validation 的相關類別
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
// ================================================================


@Controller
@RequestMapping("/admin/news")
public class AdminNewsController {

    // 這是 final 欄位，必須在建構子中初始化
    private final NewsService newsService;
    // 3. 使用 @Autowired 告訴 Spring，請透過這個建構子
    //    將 NewsService 的實例「注入」進來。
    @Autowired
    public AdminNewsController(NewsService newsService) {
        this.newsService = newsService;
    }
    // ================================================================

    // (為了讓功能完整，我幫你加上顯示表單的 GET 方法)
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("news", new NewsEntity());
        return "admin/news-form"; // 指向你的表單頁面
    }

    // 這是你原有的 POST 方法，現在它應該可以正常運作了
    @PostMapping("/create")
    public String processCreateForm(
            @Valid @ModelAttribute("news") NewsEntity news,
            BindingResult bindingResult,
            Model model) {

        // 檢查驗證是否出錯
        if (bindingResult.hasErrors()) {
            return "admin/news-form";
        }

        // 獲取當前登入者的 ID (此處為示意)
        Long currentEmployeeId = 1L;

        // 呼叫 Service 方法
        // 因為上面的 import 和建構子都正確了，這裡的紅色錯誤會消失
        newsService.saveNews(news, currentEmployeeId);

        return "redirect:/admin/news";
    }
}

package com.eatfast.announcement.controller;

import com.eatfast.announcement.model.AnnouncementEntity;
import com.eatfast.announcement.service.AnnouncementService;
import com.eatfast.common.enums.AnnouncementStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/announcements")
public class FrontAnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    // 前台：顯示目前上架的公告清單
    @GetMapping
    public String list(Model model) {
        LocalDateTime now = LocalDateTime.now();
        List<AnnouncementEntity> list = announcementService.findActiveAnnouncements(now);
        model.addAttribute("announcements", list);
        return "front-end/announcement/listall";
    }

    // 前台：顯示單筆公告（僅限上架狀態）
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Optional<AnnouncementEntity> opt = announcementService.findById(id);
        if (opt.isPresent() && opt.get().getStatus() == AnnouncementStatus.ACTIVE) {
            model.addAttribute("announcement", opt.get());
            return "front-end/announcement/view";
        } else {
            return "redirect:/announcements";
        }

    }
    // ====================================================================
//                     ⭐ 請把這段加入 Controller ⭐
// ====================================================================

    /**
     * 顯示「編輯公告」的表單
     * @param id 這是從 URL 路徑中抓下來的公告 ID，例如 /announcement/edit/5，id 就會是 5
     * @param model 我們用 Model 把從資料庫查到的舊資料，帶到前端畫面
     * @return 回傳到我們用來新增和編輯的同一個表單頁面
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {

        // 1. 根據 ID 從資料庫中找出這筆公告的舊資料
        //    我們用 orElse(null) 是為了如果找不到，就給一個 null，避免報錯
        AnnouncementEntity announcement = announcementService.findById(id).orElse(null);

        // 2. 檢查公告是否存在
        if (announcement == null) {
            // 如果找不到這筆資料，就重新導向到查詢列表頁
            return "redirect:/announcement/select_page_announcement";
        }

        // 3. 如果找到了，就把這包舊資料放進 Model 裡面，準備帶到前端
        model.addAttribute("announcement", announcement);
        //    也把狀態選項放進去，讓前端的下拉選單能顯示
        model.addAttribute("statusOptions", AnnouncementStatus.values());

        // 4. 將 Model 帶到 form.html 頁面，Thymeleaf 會自動把舊資料填入表單
        return "back-end/announcement/form";
    }
}

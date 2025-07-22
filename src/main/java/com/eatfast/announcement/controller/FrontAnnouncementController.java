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
@RequestMapping("/announcement")
public class FrontAnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 前台公告清單（只顯示當前時間內上架中的公告）
     */
    @GetMapping
    public String list(Model model) {
        LocalDateTime now = LocalDateTime.now();
        List<AnnouncementEntity> list = announcementService.findActiveAnnouncements(now);
        model.addAttribute("announcements", list);
        return "front-end/announcement/listall";
    }

    /**
     * listAll 的別名（可作為 /announcement/listAll 使用）
     */
    @GetMapping("/listAll")
    public String listAllAlias(Model model) {
        return list(model);
    }

    /**
     * 前台單筆公告檢視（只能查看狀態為 ACTIVE 的公告）
     */
    @GetMapping("/view/{id}")
    public String viewAnnouncement(@PathVariable Long id, Model model) {
        Optional<AnnouncementEntity> announcementOpt = announcementService.findById(id);
        if (announcementOpt.isPresent()) {
            AnnouncementEntity announcement = announcementOpt.get();

            // 僅允許查看 ACTIVE 狀態公告
            if (announcement.getStatus() == AnnouncementStatus.ACTIVE) {
                model.addAttribute("announcement", announcement);
                return "front-end/announcement/view";
            }
        }

        // 若找不到或不是 ACTIVE 狀態，導回公告清單
        return "redirect:/announcement";
    }
}


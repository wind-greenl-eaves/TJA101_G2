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
}

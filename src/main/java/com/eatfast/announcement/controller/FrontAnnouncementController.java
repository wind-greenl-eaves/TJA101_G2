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

    // 前台清單
    @GetMapping
    public String list(Model model) {
        LocalDateTime now = LocalDateTime.now();
        List<AnnouncementEntity> list = announcementService.findActiveAnnouncements(now);
        model.addAttribute("announcements", list);
        return "front-end/announcement/listall";
    }
    @GetMapping("/listAll")
    public String listAllAlias(Model model) {
        return list(model); // 重用原本 list() 的邏輯
    }
    // 前台單筆檢視（限制 id 為數字）
    @GetMapping("/{id:\\d+}")
    public String view(@PathVariable Long id, Model model) {
        Optional<AnnouncementEntity> opt = announcementService.findById(id);
        if (opt.isPresent() && opt.get().getStatus() == AnnouncementStatus.ACTIVE) {
            model.addAttribute("announcement", opt.get());
            return "front-end/announcement/view";
        } else {
            return "redirect:/announcement";
        }
    }
}

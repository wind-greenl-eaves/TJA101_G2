package com.eatfast.announcement.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.eatfast.announcement.model.AnnouncementEntity;
import com.eatfast.announcement.service.AnnouncementService;
import com.eatfast.common.enums.AnnouncementStatus;

@Controller
@RequestMapping("/announcement")
public class AdminAnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    // ✅ 查詢頁（GET）
    @GetMapping("/select_page_announcement")
    public String showSelectPage(Model model) {
        model.addAttribute("statusOptions", AnnouncementStatus.values());
        return "back-end/announcement/select_page_announcement";
    }

    // ✅ 查詢頁（POST）
    @PostMapping("/select_page_announcement")
    public String searchAnnouncements(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) AnnouncementStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            Model model) {

        List<AnnouncementEntity> results = announcementService.search(title, status, startTime, endTime);
        model.addAttribute("announcements", results);
        model.addAttribute("statusOptions", AnnouncementStatus.values());
        return "back-end/announcement/select_page_announcement";
    }

    // ✅ 顯示目前上架公告
    @GetMapping("/listAll")
    public String listAllCurrentlyActive(Model model) {
        List<AnnouncementEntity> list = announcementService.findCurrentlyActive();
        model.addAttribute("announcements", list);
        return "back-end/announcement/listAllAnnouncement";
    }
}

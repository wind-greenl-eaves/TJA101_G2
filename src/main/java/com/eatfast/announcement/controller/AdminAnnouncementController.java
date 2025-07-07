package com.eatfast.announcement.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.eatfast.feedback.model.FeedbackEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.eatfast.announcement.model.AnnouncementEntity;
import com.eatfast.announcement.service.AnnouncementService;
import com.eatfast.common.enums.AnnouncementStatus;
import com.eatfast.employee.model.EmployeeEntity;
import com.eatfast.store.model.StoreEntity;

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
 // ✅ 顯示新增公告的表單-->新增按鈕按下後
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        AnnouncementEntity announcement = new AnnouncementEntity();
        announcement.setStartTime(LocalDateTime.now());
        announcement.setEndTime(LocalDateTime.now().plusDays(1));
        announcement.setStatus(AnnouncementStatus.INACTIVE); // 預設草稿狀態

        model.addAttribute("announcement", announcement);
        model.addAttribute("statusOptions", AnnouncementStatus.values());
        return "back-end/announcement/form"; // 對應 form.html 畫面
    }
    @PostMapping("/save")
    public String save(
            @ModelAttribute("announcement") AnnouncementEntity announcement,
            @RequestParam("action") String action,
            Model model) {
    	

  //       設定狀態依據按鈕
        if ("publish".equals(action)) {
            announcement.setStatus(AnnouncementStatus.ACTIVE);
        } else {
            announcement.setStatus(AnnouncementStatus.INACTIVE);
        }

        // 🔒 模擬登入使用者（正式版需從登入中取得）
        EmployeeEntity emp = new EmployeeEntity();
        emp.setEmployeeId(1L);

        StoreEntity store = new StoreEntity();
        store.setStoreId(1L);

        announcement.setEmployee(emp);
        announcement.setStore(store);

        announcementService.save(announcement);

        return "redirect:/announcement/select_page_announcement";
    }
    //草稿相關
 // 顯示草稿清單
    @GetMapping("/drafts")
    public String showDrafts(Model model) {
        List<AnnouncementEntity> drafts = announcementService.findByStatus(AnnouncementStatus.INACTIVE);
        model.addAttribute("announcements", drafts);
        return "back-end/announcement/listDrafts";
    }

    // ✅ 發佈草稿（改為 ACTIVE）
    @GetMapping("/publish/{id}")
    public String publishDraft(@PathVariable Long id) {
        announcementService.publishById(id); // 後續實作 Service
        return "redirect:/announcement/drafts";
    }



    

    public void save(FeedbackEntity feedback) {
    }
}



package com.eatfast.announcement.controller;

import com.eatfast.announcement.model.AnnouncementEntity;
import com.eatfast.announcement.service.AnnouncementService;
import com.eatfast.common.enums.AnnouncementStatus;
import com.eatfast.employee.model.EmployeeEntity;
import com.eatfast.store.model.StoreEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/announcement") // <<< 1. 修改後：加上 /admin 前綴，區分前後台
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;

    @Autowired
    public AdminAnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    // ==========================================================
    // == 公告查詢與列表
    // ==========================================================

    @GetMapping("/select_page_announcement")
    public String showSelectPage(Model model) {
        model.addAttribute("statusOptions", AnnouncementStatus.values());
        List<AnnouncementEntity> allAnnouncements = announcementService.findAll(); // 預設載入全部
        model.addAttribute("announcements", allAnnouncements);
        return "back-end/announcement/select_page_announcement";
    }

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

    @GetMapping("/listAll")
    public String listAllCurrentlyActive(Model model) {
        // 查詢上架中的公告
        System.out.println("====== 按鈕點擊成功，listAllCurrentlyActive 方法被呼叫了！ ======");
        List<AnnouncementEntity> list = announcementService.findCurrentlyActive();
        model.addAttribute("announcements", list);

        // 也把查詢欄位的狀態選項放回去，保持頁面完整
        model.addAttribute("statusOptions", AnnouncementStatus.values());

        // <<< 修改後：直接渲染視圖，把 model 裡的資料帶過去
        return "back-end/announcement/select_page_announcement";
    }

    // ==========================================================
    // == 新增與編輯公告
    // ==========================================================

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        AnnouncementEntity announcement = new AnnouncementEntity();
        announcement.setStartTime(LocalDateTime.now());
        announcement.setEndTime(LocalDateTime.now().plusDays(7));
        announcement.setStatus(AnnouncementStatus.INACTIVE); // 預設草稿
        model.addAttribute("announcement", announcement);
        model.addAttribute("isEditMode", false); // 標示為新增模式
        model.addAttribute("statusOptions", AnnouncementStatus.values());
        return "back-end/announcement/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        AnnouncementEntity announcement = announcementService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("無效的公告ID: " + id));
        model.addAttribute("announcement", announcement);
        model.addAttribute("isEditMode", true); // 標示為編輯模式
        model.addAttribute("statusOptions", AnnouncementStatus.values());
        return "back-end/announcement/form";
    }

    @PostMapping("/save")
    public String saveAnnouncement(@ModelAttribute("announcement") AnnouncementEntity announcement,
                                   RedirectAttributes redirectAttributes) {
        // 模擬登入使用者（未來應從 SecurityContextHolder 取得）
        EmployeeEntity emp = new EmployeeEntity();
        emp.setEmployeeId(1L);
        StoreEntity store = new StoreEntity();
        store.setStoreId(1L);
        announcement.setEmployee(emp);
        announcement.setStore(store);

        announcementService.save(announcement);
        redirectAttributes.addFlashAttribute("successMessage", "公告已成功儲存！");
        return "redirect:/admin/announcement/select_page_announcement"; // <<< 3. 修改後：同步 redirect 路徑
    }

    // ==========================================================
    // == 草稿相關操作
    // ==========================================================

    @GetMapping("/drafts")
    public String showDrafts(Model model) {
        List<AnnouncementEntity> drafts = announcementService.findByStatus(AnnouncementStatus.INACTIVE);
        model.addAttribute("announcements", drafts);
        return "back-end/announcement/listDrafts"; // 顯示草稿專用頁面
    }

    // ==========================================================
    // == ★★★ 唯一的刪除功能 ★★★
    // ==========================================================

    @PostMapping("/{id}/delete")
    public String deleteAnnouncement(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            announcementService.deleteAnnouncementById(id);
            redirectAttributes.addFlashAttribute("successMessage", "公告 (ID: " + id + ") 已成功刪除！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "刪除失敗：" + e.getMessage());
        }
        // 無論從哪個頁面刪除，都統一回到查詢主頁
        return "redirect:/admin/announcement/select_page_announcement"; // <<< 4. 修改後：同步 redirect 路徑
    }
}
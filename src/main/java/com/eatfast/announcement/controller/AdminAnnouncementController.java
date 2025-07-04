package com.eatfast.announcement.controller;

import com.eatfast.announcement.model.AnnouncementEntity;
import com.eatfast.announcement.service.AnnouncementService;
import com.eatfast.common.enums.AnnouncementStatus;
import com.eatfast.employee.model.EmployeeEntity;
import com.eatfast.store.model.StoreEntity;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/announcements")
public class AdminAnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    // 後台：清單頁面
    @GetMapping
    public String list(Model model) {
        List<AnnouncementEntity> announcements = announcementService.findAll();
        model.addAttribute("announcements", announcements);
        return "back-end/announcement/list";
    }

    // 後台：查看單筆詳情
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Optional<AnnouncementEntity> opt = announcementService.findById(id);
        if (opt.isPresent()) {
            model.addAttribute("announcement", opt.get());
            return "back-end/announcement/view";
        } else {
            return "redirect:/admin/announcements";
        }
    }

    // 後台：顯示新增表單
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        AnnouncementEntity announcement = new AnnouncementEntity();
        announcement.setStartTime(LocalDateTime.now());
        announcement.setEndTime(LocalDateTime.now().plusDays(1));
        announcement.setStatus(AnnouncementStatus.INACTIVE); // 草稿

        model.addAttribute("announcement", announcement);
        model.addAttribute("statusOptions", AnnouncementStatus.values());
        return "back-end/announcement/form";
    }

    // 後台：儲存公告（新增 or 修改）
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("announcement") AnnouncementEntity announcement,
                       BindingResult result, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("statusOptions", AnnouncementStatus.values());
            return "back-end/announcement/form";
        }

        // 🔒 注意：正式版應從登入資訊中取 employee/store
        EmployeeEntity emp = new EmployeeEntity();
        emp.setEmployeeId(1L);

        StoreEntity store = new StoreEntity();
        store.setStoreId(1L);

        announcement.setEmployee(emp);
        announcement.setStore(store);

        announcementService.save(announcement);
        return "redirect:/admin/announcements";
    }

    // 後台：刪除公告
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        announcementService.deleteById(id);
        return "redirect:/admin/announcements";
    }
}

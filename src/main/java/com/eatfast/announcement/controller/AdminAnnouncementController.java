package com.eatfast.announcement.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.eatfast.feedback.model.FeedbackEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
// 我們把回傳類型從 String 改為 ResponseEntity<String>
    @GetMapping("/publish/{id}")
    public ResponseEntity<String> publishDraft(@PathVariable Long id) {
        System.out.println("====== DEBUG: 成功進入 publishDraft 方法！準備發布 ID = " + id + " ======");
        try {
            // 我們嘗試執行發布的業務邏輯
            announcementService.publishById(id);

            // 如果上面那行沒有報錯，就代表成功了
            // 我們回傳一個 HTTP 200 OK 狀態，並在 body 裡帶上一句成功訊息
            return ResponseEntity.ok("發布成功！ID: " + id);

        } catch (Exception e) {
            // 如果在 try 的過程中發生任何錯誤 (例如 service 拋出找不到id的例外)
            // 我們就捕捉這個錯誤，並回傳一個 HTTP 500 Internal Server Error 狀態
            // 這樣前端的 JS 也能更明確地知道是後端出錯了
            return ResponseEntity.internalServerError().body("發布失敗: " + e.getMessage());
        }
    }
    /**
     * 顯示「編輯公告」的表單
     * @param id 這是從 URL 路徑中抓下來的公告 ID
     * @param model 我們用 Model 把舊資料帶到前端畫面
     * @return 回傳到 form.html 頁面
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {

        // 為了讓您看到請求真的進來了，我們印出一行訊息
        System.out.println("====== DEBUG: 成功進入 showEditForm 方法！收到的 ID = " + id + " ======");

        // 根據 ID 從資料庫中找出這筆公告的舊資料
        AnnouncementEntity announcement = announcementService.findById(id).orElse(null);

        // 檢查公告是否存在
        if (announcement == null) {
            // 如果找不到這筆資料，就重新導向到查詢列表頁
            return "redirect:/announcement/select_page_announcement";
        }

        // 如果找到了，就把這包舊資料放進 Model 裡面，準備帶到前端
        model.addAttribute("announcement", announcement);
        // 也把狀態選項放進去，讓前端的下拉選單能顯示
        model.addAttribute("statusOptions", AnnouncementStatus.values());

        // 將 Model 帶到 form.html 頁面，Thymeleaf 會自動把舊資料填入表單
        return "back-end/announcement/form";
    }





}






/*
 * ================================================================
 * 檔案: 會員控制層 (MemberController) - ★★★ 註解強化最終版 ★★★
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/member/controller/MemberController.java
 * - 核心作用: 負責接收前端 HTTP 請求，調用 Service 層處理業務邏輯，並決定最終回應哪個視圖。
 */
package com.eatfast.member.controller;

// 【路徑】引入所有需要的 DTO，作為 Controller 與 Service 之間資料傳遞的標準格式。
import com.eatfast.member.dto.MemberCreateRequest;
import com.eatfast.member.dto.MemberUpdateRequest;
import com.eatfast.member.dto.PasswordUpdateRequest;
import com.eatfast.member.dto.MemberVerificationRequest;
// 【路徑】引入 Model, Service, 常數與驗證介面，建立 Controller -> Service -> Model 的標準呼叫鏈。
import com.eatfast.member.model.MemberEntity;
import com.eatfast.member.service.MemberService;
import com.eatfast.member.validation.CreateValidation;
import com.eatfast.member.validation.UpdateValidation;
// 【新增】訂單相關的 import
import com.eatfast.orderlist.model.OrderListEntity;
import com.eatfast.orderlist.model.OrderStatus;
// (既有 import)
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.ArrayList;
// 【新增】時間處理和檔案下載相關的 import
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
// 【新增】驗證相關的 import
import jakarta.validation.Validator;
import jakarta.validation.Validation;
import jakarta.validation.ConstraintViolation;
// 【新增】枚舉 import
import com.eatfast.common.enums.Gender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 【身分標示】@Controller 告訴 Spring 這是 Web 層的控制器。
 * 【請求路徑】@RequestMapping("/member") 定義了此控制器下所有方法共享的基礎 URL 路徑。
 */
@Controller
@RequestMapping("/member")
public class MemberController {
    
    private static final Logger log = LoggerFactory.getLogger(MemberController.class);
    
    // 【依賴注入】使用 private final 確保 Service 在建構後不可變，是線程安全的最佳實踐。
    private final MemberService memberService;

    // 【依賴注入路徑】透過建構子注入 MemberService，由 Spring 容器自動完成。
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }
    
    // ================================================================
    // 					後台管理功能 (Back-end Management)
    // ================================================================
    
    /**
     * 【功能】: 顯示新增會員的表單頁面。
     * 【請求路徑】: 處理 GET /member/addMember 請求。
     */
    @GetMapping("/addMember")
    public String showAddForm(Model model) {
        // 【資料流路徑】: 將一個空的 DTO 物件放入 Model 中，
        // 其屬性會被 Thymeleaf 的 th:object 和 th:field 標籤用來綁定表單欄位。
        model.addAttribute("memberCreateRequest", new MemberCreateRequest());
        // 【視圖路徑】: 返回視圖名稱，指向 MemberViewConstants 中定義的實體模板檔案。
        return MemberViewConstants.VIEW_ADD_MEMBER;
    }
    
    /**
     * 【功能】: 接收表單提交的資料，並執行新增會員的業務邏輯。
     * 【請求路徑】: 處理 POST /member/insert 請求。
     */
    @PostMapping("/insert")
    public String insert(@Validated(CreateValidation.class) @ModelAttribute("memberCreateRequest") MemberCreateRequest createRequest,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {

        // 【驗證路徑】: 檢查 @Validated 的結果。如果 BindingResult 中有錯誤...
        if (result.hasErrors()) {
            // 【錯誤路徑】: ...則直接返回新增頁面，Thymeleaf 會自動顯示錯誤訊息。
            return MemberViewConstants.VIEW_ADD_MEMBER;
        }

        try {
            // 【業務邏輯路徑】: 使用管理員專用的註冊方法，會員直接啟用無需驗證
            MemberEntity savedMember = memberService.registerMemberByAdmin(createRequest);
            log.info("管理員新增會員成功，ID: {}，帳號: {}，狀態: 已啟用", 
                     savedMember.getMemberId(), savedMember.getAccount());
            // 【成功訊息路徑】: 使用 RedirectAttributes 跨重定向傳遞成功訊息。
            redirectAttributes.addFlashAttribute("successMessage", 
                "新增會員 " + savedMember.getUsername() + " 成功！帳號已直接啟用，無需驗證。");
        
        } catch (IllegalArgumentException e) {
            // 【業務例外路徑】: 捕獲從 Service 層拋出的特定業務例外 (如：帳號已存在)。
            log.warn("管理員新增會員失敗: {}", e.getMessage());
            // 將後端業務錯誤轉換為前端表單錯誤，回饋給使用者。
            result.addError(new FieldError("memberCreateRequest", "account", e.getMessage()));
            return MemberViewConstants.VIEW_ADD_MEMBER;
        }
        
        // 【成功重定項路徑】: 操作成功後，重定向到列表頁，避免表單重複提交 (PRG 模式)。
        return MemberViewConstants.REDIRECT_TO_SELECT_PAGE;
    }
    
    /**
     * 【功能】: 根據 ID 獲取單一會員資料，並導向修改頁面。
     * 【請求路徑】: 處理 POST /member/getOne_For_Update 請求。
     */
    @PostMapping("/getOne_For_Update")
    public String showUpdateForm(@RequestParam("memberId") Long memberId, Model model, RedirectAttributes redirectAttributes) {
        // 【業務邏輯路徑】: 呼叫 Service 獲取 Optional<MemberEntity>。
        return memberService.getMemberById(memberId)
            .map(memberEntity -> { // 【成功路徑】如果 Optional 中有值...
                // 將 Entity 轉換為 Update DTO，確保只有需要的欄位被傳遞。
                MemberUpdateRequest updateRequest = new MemberUpdateRequest();
                updateRequest.setMemberId(memberEntity.getMemberId());
                updateRequest.setUsername(memberEntity.getUsername());
                updateRequest.setAccount(memberEntity.getAccount()); // 添加帳號欄位
                updateRequest.setEmail(memberEntity.getEmail());
                updateRequest.setPhone(memberEntity.getPhone());
                updateRequest.setBirthday(memberEntity.getBirthday());
                updateRequest.setGender(memberEntity.getGender());
                updateRequest.setEnabled(memberEntity.isEnabled()); // 【修正】使用一致的方法名稱
                updateRequest.setCreatedAt(memberEntity.getCreatedAt()); // 添加註冊時間
                
                // 【資料流路徑】: 提供「更新資料」的 DTO 給第一個表單。
                model.addAttribute("memberUpdateRequest", updateRequest);
                
                // 【資料流路徑】: 同時提供一個空的「密碼更新」DTO 給第二個表單使用。
                PasswordUpdateRequest passwordRequest = new PasswordUpdateRequest();
                passwordRequest.setMemberId(memberId);
                model.addAttribute("passwordUpdateRequest", passwordRequest);
                
                // 【視圖路徑】: 導向更新頁面。
                return MemberViewConstants.VIEW_UPDATE_MEMBER;
            })
            .orElseGet(() -> { // 【失敗路徑】如果 Optional 為空 (找不到會員)...
                // 【失敗訊息路徑】: 將錯誤訊息放入 RedirectAttributes。
                redirectAttributes.addFlashAttribute("errorMessage", "找不到會員 ID: " + memberId);
                // 【失敗重定項路徑】: 重定向回列表頁。
                return MemberViewConstants.REDIRECT_TO_SELECT_PAGE;
            });
    }

    /**
     * 【功能】: 處理「更新一般資料」的請求 (不含密碼)。
     * 【請求路徑】: 處理 POST /member/update 請求。
     */
    @PostMapping("/update")
    public String update(@Validated(UpdateValidation.class) @ModelAttribute("memberUpdateRequest") MemberUpdateRequest updateRequest,
                         BindingResult result,
                         RedirectAttributes redirectAttributes,
                         Model model) { // 加入 Model

        // 【驗證路徑】: 檢查 DTO 欄位驗證。
        if (result.hasErrors()) {
            // 【錯誤路徑】: 如果更新失敗，需要重新準備「密碼表單」的 DTO，否則頁面會出錯。
            PasswordUpdateRequest passwordRequest = new PasswordUpdateRequest();
            passwordRequest.setMemberId(updateRequest.getMemberId());
            model.addAttribute("passwordUpdateRequest", passwordRequest);
            return MemberViewConstants.VIEW_UPDATE_MEMBER;
        }

        try {
            // 【業務邏輯路徑】: 呼叫 Service 執行更新。
            memberService.updateMemberDetails(updateRequest);
            redirectAttributes.addFlashAttribute("successMessage", "會員 " + updateRequest.getUsername() + " 的資料更新成功！");
        
        } catch (EntityNotFoundException e) {
            log.error("更新會員失敗: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "更新失敗: " + e.getMessage());
        }

        // 【成功重定項路徑】: 重定向回列表頁。
        return MemberViewConstants.REDIRECT_TO_SELECT_PAGE;
    }

    /**
     * 【功能】: 處理「變更密碼」的請求 (後台管理用)。
     * 【請求路徑】: 處理 POST /member/admin/change-password 請求。
     */
    @PostMapping("/admin/change-password")
    public String handleChangePassword(@Validated @ModelAttribute("passwordUpdateRequest") PasswordUpdateRequest request,
                                     BindingResult result,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        
        // 【優化】: 準備一個私有方法來處理「錯誤時重新渲染頁面」的共同邏輯。
        // 這個方法會重新查詢會員資料，並將其放入 Model，避免頁面因缺少資料而崩潰。
        if (result.hasErrors()) {
            // 將 JSR-303 的格式驗證錯誤訊息，以 passwordUpdateErrors 的名義傳遞給前端。
            model.addAttribute("passwordUpdateErrors", result.getAllErrors());
            // 呼叫輔助方法，重新準備頁面所需的另一個表單資料。
            prepareUpdatePageModel(request.getMemberId(), model);
            // 直接返回視圖名稱，而非重定向，以保留使用者輸入的錯誤資料和驗證訊息。
            return MemberViewConstants.VIEW_UPDATE_MEMBER;
        }

        try {
            // 【業務邏輯路徑】: 呼叫專門的 Service 方法執行密碼變更。
            memberService.changePassword(request);
            redirectAttributes.addFlashAttribute("successMessage", "密碼更新成功！(密碼已使用 BCrypt 加密安全存儲)");
        
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            // 【業務例外路徑】: 捕獲 Service 拋出的錯誤 (找不到使用者或舊密碼錯誤)。
            log.warn("密碼更新失敗: {}", e.getMessage());
            // 將業務錯誤以 Flash Attribute 形式傳遞，這種方式在重定向後依然有效。
            redirectAttributes.addFlashAttribute("passwordUpdateError", e.getMessage());
        }

        // 【重定向路徑】: 不論成功或失敗，都重定向回該會員的修改頁面，以便用戶看到結果。
        // 這種方式可以觸發 showUpdateForm 方法，重新完整地載入頁面資料。
        return "redirect:/member/getOne_For_Update_view?memberId=" + request.getMemberId();
    }
    
    /**
     * 【功能】: 顯示所有會員列表頁 (支援分頁)。
     * 【請求路徑】: 處理 GET /member/select_page 請求。
     */
    @GetMapping("/select_page")
    public String showSelectPage(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "15") int size,
                                HttpSession session,
                                Model model) {
        
        // 驗證分頁參數
        if (page < 1) page = 1;
        if (size < 1) size = 15;
        if (size > 50) size = 50;
        
        // 獲取當前登入的管理員資訊
        Object loggedInEmployee = session.getAttribute("loggedInEmployee");
        String employeeName = (String) session.getAttribute("employeeName");
        String employeeAccount = (String) session.getAttribute("employeeAccount");
        Object employeeRole = session.getAttribute("employeeRole");
        Boolean isEmployeeLoggedIn = (Boolean) session.getAttribute("isEmployeeLoggedIn");
        
        // 將管理員資訊添加到模型中
        if (isEmployeeLoggedIn != null && isEmployeeLoggedIn) {
            model.addAttribute("currentAdmin", loggedInEmployee);
            model.addAttribute("currentAdminName", employeeName);
            model.addAttribute("currentAdminAccount", employeeAccount);
            
            // 【修改】將角色轉換為中文顯示名稱
            String roleDisplayName = "未知角色";
            if (employeeRole instanceof com.eatfast.common.enums.EmployeeRole) {
                roleDisplayName = ((com.eatfast.common.enums.EmployeeRole) employeeRole).getDisplayName();
            } else if (employeeRole != null) {
                // 如果是字符串形式的角色，嘗試轉換為枚舉
                try {
                    com.eatfast.common.enums.EmployeeRole role = com.eatfast.common.enums.EmployeeRole.valueOf(employeeRole.toString());
                    roleDisplayName = role.getDisplayName();
                } catch (IllegalArgumentException e) {
                    log.warn("無法解析角色: {}", employeeRole);
                    roleDisplayName = employeeRole.toString();
                }
            }
            
            model.addAttribute("currentAdminRole", roleDisplayName);
            model.addAttribute("isAdminLoggedIn", true);
            log.info("當前登入管理員: {} (帳號: {}, 角色: {})", employeeName, employeeAccount, roleDisplayName);
        } else {
            model.addAttribute("isAdminLoggedIn", false);
            log.warn("未檢測到登入的管理員資訊");
        }
        
        // 【業務邏輯路徑】: 呼叫 Service 方法獲取所有會員的列表。
        List<MemberEntity> allMembers = memberService.getAllMembers();
        
        // 手動實作分頁邏輯
        int totalElements = allMembers.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        // 計算開始和結束索引
        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, totalElements);
        
        List<MemberEntity> memberList = new ArrayList<>();
        if (startIndex < totalElements) {
            memberList = allMembers.subList(startIndex, endIndex);
        }
        
        // 【資料流路徑】: 將列表資料放入 Model，供前端模板使用。
        model.addAttribute("memberListData", memberList);
        
        // 分頁資訊
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalElements", totalElements);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasNext", page < totalPages);
        model.addAttribute("hasPrevious", page > 1);
        
        // 計算顯示範圍
        int displayStart = startIndex + 1;
        int displayEnd = endIndex;
        model.addAttribute("displayStart", displayStart);
        model.addAttribute("displayEnd", displayEnd);
        
        // 為了讓分頁控制器能正確處理，添加一個空的搜尋參數
        model.addAttribute("searchParams", new HashMap<>());
        
        log.info("顯示所有會員列表，總共 {} 筆資料，目前第 {} 頁，每頁 {} 筆", totalElements, page, size);
        
        // 【視圖路徑】: 返回列表頁的模板名稱。
        return MemberViewConstants.VIEW_SELECT_PAGE;
    }

    /**
     * 【功能】: 根據 ID 刪除會員 (軟刪除)。
     * 【請求路徑】: 處理 POST /member/delete 請求。
     */
    @PostMapping("/delete")
    public String delete(@RequestParam("memberId") Long memberId, RedirectAttributes redirectAttributes) {
        memberService.deleteMemberById(memberId);
        redirectAttributes.addFlashAttribute("successMessage", "會員編號 " + memberId + " 刪除成功！");
        return MemberViewConstants.REDIRECT_TO_SELECT_PAGE;
    }

    /**
     * 【功能】: 提供一個 RESTful API 端點，根據 ID 獲取會員資料。
     * 【請求路徑】: 處理 GET /member/api/detail/{memberId} 請求。
     * - @ResponseBody: 告訴 Spring 此方法的回傳值不是視圖名稱，而是要直接寫入 HTTP 回應主體的資料 (通常是 JSON)。
     */
    @GetMapping("/api/detail/{memberId}")
    @ResponseBody
    public ResponseEntity<MemberEntity> getMemberDetail(@PathVariable Long memberId) {
        // 【回應路徑】: 使用 ResponseEntity 來包裝回應，可以精準控制 HTTP 狀態碼。
        return memberService.getMemberById(memberId)
                .map(ResponseEntity::ok) // 如果找到，回傳 200 OK 及會員資料。
                .orElse(ResponseEntity.notFound().build()); // 如果找不到，回傳 404 Not Found。
    }

    /**
     * 【新方法 for 密碼更新後的重定向】
     * 由於 POST 不能直接重定向到另一個 POST，我們建立一個 GET 端點來顯示更新頁面。
     */
    @GetMapping("/getOne_For_Update_view")
    public String showUpdateFormAfterRedirect(@RequestParam("memberId") Long memberId, 
                                             Model model, 
                                             RedirectAttributes redirectAttributes) {
        
        // 【業務邏輯路徑】: 呼叫 Service 獲取 Optional<MemberEntity>。
        return memberService.getMemberById(memberId)
            .map(memberEntity -> { // 【成功路徑】如果 Optional 中有值...
                // 將 Entity 轉換為 Update DTO，確保只有需要的欄位被傳遞。
                MemberUpdateRequest updateRequest = new MemberUpdateRequest();
                updateRequest.setMemberId(memberEntity.getMemberId());
                updateRequest.setUsername(memberEntity.getUsername());
                updateRequest.setAccount(memberEntity.getAccount()); // 添加帳號欄位
                updateRequest.setEmail(memberEntity.getEmail());
                updateRequest.setPhone(memberEntity.getPhone());
                updateRequest.setBirthday(memberEntity.getBirthday());
                updateRequest.setGender(memberEntity.getGender());
                updateRequest.setEnabled(memberEntity.isEnabled()); // 【修正】使用一致的方法名稱
                updateRequest.setCreatedAt(memberEntity.getCreatedAt()); // 添加註冊時間
                
                // 【資料流路徑】: 提供「更新資料」的 DTO 給第一個表單。
                model.addAttribute("memberUpdateRequest", updateRequest);
                
                // 【資料流路徑】: 同時提供一個空的「密碼更新」DTO 給第二個表單使用。
                PasswordUpdateRequest passwordRequest = new PasswordUpdateRequest();
                passwordRequest.setMemberId(memberId);
                model.addAttribute("passwordUpdateRequest", passwordRequest);
                
                // 【視圖路徑】: 導向更新頁面。
                return MemberViewConstants.VIEW_UPDATE_MEMBER;
            })
            .orElseGet(() -> { // 【失敗路徑】如果 Optional 為空 (找不到會員)...
                // 【失敗訊息路徑】: 將錯誤訊息放入 RedirectAttributes。
                redirectAttributes.addFlashAttribute("errorMessage", "找不到會員 ID: " + memberId);
                // 【失敗重定項路徑】: 重定向回列表頁。
                return MemberViewConstants.REDIRECT_TO_SELECT_PAGE;
            });
    }
    
    // ================================================================
    // 					前端會員專區功能 (Front-end Member Area)
    // ================================================================
    
    /**
     * 【前端會員專區路由】會員專區主頁面
     * 
     * 路徑說明：
     * - URL: GET /member/dashboard
     * - 完整 URL: http://localhost:8080/member/dashboard
     * - 視圖路徑: src/main/resources/templates/front-end/member/member-dashboard.html
     * 
     * 功能說明：
     * 1. 檢查用戶是否已登入（Session驗證）
     * 2. 如果已登入，載入會員資訊並顯示會員專區首頁
     * 3. 如果未登入，重定向到登入頁面
     */
    @GetMapping("/dashboard")
    public String showMemberDashboard(Model model, HttpSession session) {
        // 【第一步：Session驗證】檢查用戶是否已登入
        Long memberId = (Long) session.getAttribute("loggedInMemberId");
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        
        // 如果未登入，重定向到登入頁面
        if (memberId == null || isLoggedIn == null || !isLoggedIn) {
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN;
        }
        
        // 【第二步：載入會員資訊】從資料庫獲取最新的會員資料
        try {
            MemberEntity member = memberService.getMemberById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("找不到會員資料"));
            
            // 檢查帳號是否仍然啟用
            if (!member.isEnabled()) {
                // 如果帳號被停用，清除Session並重定向到登入頁面
                session.invalidate();
                return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN + "?error=account_disabled";
            }
            
            // 【第三步：準備頁面資料】將會員資訊傳遞給前端頁面
            model.addAttribute("member", member);
            model.addAttribute("memberName", member.getUsername());
            model.addAttribute("memberAccount", member.getAccount());
            
            // 可以在這裡添加其他統計資料，如訂單數量、收藏數量等
            // model.addAttribute("orderCount", orderService.getOrderCountByMemberId(memberId));
            // model.addAttribute("favoriteCount", favService.getFavoriteCountByMemberId(memberId));
            
        } catch (Exception e) {
            log.error("載入會員專區資料時發生錯誤：{}", e.getMessage());
            model.addAttribute("errorMessage", "載入會員資料失敗，請重新登入");
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN;
        }
        
        return MemberViewConstants.VIEW_MEMBER_DASHBOARD;
    }
    
    /**
     * 【前端會員專區路由】個人資料頁面
     * 
     * 路徑說明：
     * - URL: GET /member/profile  
     * - 完整 URL: http://localhost:8080/member/profile
     * - 視圖路徑: src/main/resources/templates/front-end/member/member-profile.html
     */
    @GetMapping("/profile")
    public String showMemberProfile(Model model, HttpSession session) {
        // 【Session驗證】檢查登入狀態
        Long memberId = (Long) session.getAttribute("loggedInMemberId");
        if (memberId == null) {
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN;
        }
        
        try {
            // 【載入會員資料】獲取當前登入會員的詳細資訊
            MemberEntity member = memberService.getMemberById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("找不到會員資料"));
            
            // 【準備更新表單】將現有資料填入表單中，供用戶編輯
            MemberUpdateRequest updateRequest = new MemberUpdateRequest();
            updateRequest.setMemberId(member.getMemberId());
            updateRequest.setUsername(member.getUsername());
            updateRequest.setEmail(member.getEmail());
            updateRequest.setPhone(member.getPhone());
            updateRequest.setBirthday(member.getBirthday());
            updateRequest.setGender(member.getGender());
            // 【安全修正】移除 setIsEnabled，禁止會員修改帳號狀態
            
            // 【傳遞資料到視圖】
            model.addAttribute("memberUpdateRequest", updateRequest);
            model.addAttribute("member", member);
            
        } catch (Exception e) {
            log.error("載入個人資料頁面時發生錯誤：{}", e.getMessage());
            model.addAttribute("errorMessage", "載入個人資料失敗");
        }
        
        return MemberViewConstants.VIEW_MEMBER_PROFILE;
    }
    
    /**
     * 【前端會員專區路由】個人資料更新處理
     * 
     * 路徑說明：
     * - URL: POST /member/profile/update
     * - 重定向: 成功後回到個人資料頁面顯示成功訊息
     */
    @PostMapping("/profile/update")
    public String updateMemberProfile(@Validated(UpdateValidation.class) @ModelAttribute("memberUpdateRequest") MemberUpdateRequest updateRequest,
                                    BindingResult result,
                                    RedirectAttributes redirectAttributes,
                                    HttpSession session,
                                    Model model) {
        
        // 【Session驗證】確保用戶已登入且操作自己的帳號
        Long sessionMemberId = (Long) session.getAttribute("loggedInMemberId");
        if (sessionMemberId == null || !sessionMemberId.equals(updateRequest.getMemberId())) {
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN;
        }
        
        if (result.hasErrors()) {
            // 重新準備頁面資料
            try {
                MemberEntity member = memberService.getMemberById(updateRequest.getMemberId())
                        .orElseThrow(() -> new EntityNotFoundException("找不到會員資料"));
                model.addAttribute("member", member);
            } catch (Exception e) {
                log.error("重新載入會員資料失敗：{}", e.getMessage());
            }
            return MemberViewConstants.VIEW_MEMBER_PROFILE;
        }
        
        try {
            memberService.updateMemberDetails(updateRequest);
            redirectAttributes.addFlashAttribute("successMessage", "個人資料更新成功！");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "更新失敗：" + e.getMessage());
        }
        
        return MemberViewConstants.REDIRECT_TO_MEMBER_PROFILE;
    }
    
    /**
     * 【前端會員專區路由】密碼變更頁面 (前端專用)
     * 
     * 路徑說明：
     * - URL: GET /member/change-password
     * - 完整 URL: http://localhost:8080/member/change-password  
     * - 視圖路徑: src/main/resources/templates/front-end/member/change-password.html
     */
    @GetMapping("/change-password")
    public String showMemberChangePasswordPage(Model model, HttpSession session) {
        // 【Session驗證】檢查登入狀態
        Long memberId = (Long) session.getAttribute("loggedInMemberId");
        if (memberId == null) {
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN;
        }
        
        // 【準備密碼更新表單】創建空的密碼更新請求對象
        PasswordUpdateRequest passwordRequest = new PasswordUpdateRequest();
        passwordRequest.setMemberId(memberId);
        model.addAttribute("passwordUpdateRequest", passwordRequest);
        
        return MemberViewConstants.VIEW_CHANGE_PASSWORD;
    }
    
    /**
     * 【前端會員專區路由】處理密碼變更請求 (前端專用)
     * 
     * 路徑說明：
     * - URL: POST /member/change-password
     * - 重定向: 成功後重新登入
     */
    @PostMapping("/change-password")
    public String processMemberPasswordChange(@Validated @ModelAttribute("passwordUpdateRequest") PasswordUpdateRequest request,
                                            BindingResult result,
                                            RedirectAttributes redirectAttributes,
                                            HttpSession session) {
        
        // 【Session驗證】確保用戶已登入且操作自己的帳號
        Long sessionMemberId = (Long) session.getAttribute("loggedInMemberId");
        if (sessionMemberId == null || !sessionMemberId.equals(request.getMemberId())) {
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN;
        }
        
        // 【密碼確認驗證】檢查新密碼與確認密碼是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "password.mismatch", "新密碼與確認密碼不一致");
        }
        
        // 【新舊密碼相同檢查】防止用戶設定相同的密碼
        if (request.getOldPassword().equals(request.getNewPassword())) {
            result.rejectValue("newPassword", "password.same", "新密碼不能與舊密碼相同");
        }
        
        // 【表單驗證】檢查驗證錯誤
        if (result.hasErrors()) {
            return MemberViewConstants.VIEW_CHANGE_PASSWORD;
        }
        
        try {
            // 【執行密碼變更】調用Service層的密碼變更方法
            memberService.changePassword(request);
            redirectAttributes.addFlashAttribute("successMessage", "密碼變更成功！為了安全起見，請重新登入。");
            
            // 【安全處理】密碼變更成功後清除Session，要求重新登入
            session.invalidate();
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN;
            
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            log.warn("密碼變更失敗：{}", e.getMessage());
            result.rejectValue("oldPassword", "password.invalid", e.getMessage());
            return MemberViewConstants.VIEW_CHANGE_PASSWORD;
        }
    }
    
    /**
     * 【前端會員專區路由】訂單記錄頁面
     */
    @GetMapping("/orders")
    public String showMemberOrders(Model model, HttpSession session,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) String startDate,
                                 @RequestParam(required = false) String endDate) {
        
        // 【Session驗證】檢查登入狀態
        Long memberId = (Long) session.getAttribute("loggedInMemberId");
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        
        if (memberId == null || isLoggedIn == null || !isLoggedIn) {
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN;
        }
        
        try {
            // 【載入會員資料】驗證會員存在且啟用
            MemberEntity member = memberService.getMemberById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("找不到會員資料"));
            
            if (!member.isEnabled()) {
                session.invalidate();
                return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN + "?error=account_disabled";
            }
            
            // 【獲取會員訂單】使用 Service 層方法來確保正確載入訂單明細
            List<OrderListEntity> orders = memberService.getMemberOrdersWithDetails(memberId);
            
            // 【過濾條件處理】根據篩選條件過濾訂單
            if (status != null && !status.trim().isEmpty()) {
                if ("INCOMPLETE".equals(status.trim())) {
                    // 未完成狀態：包含 PENDING、CONFIRMED、SHIPPED
                    orders = orders.stream()
                            .filter(order -> order.getOrderStatus() == OrderStatus.PENDING || 
                                           order.getOrderStatus() == OrderStatus.CONFIRMED || 
                                           order.getOrderStatus() == OrderStatus.SHIPPED)
                            .collect(java.util.stream.Collectors.toList());
                } else {
                    // 單一狀態篩選
                    try {
                        OrderStatus orderStatus = OrderStatus.valueOf(status.trim());
                        orders = orders.stream()
                                .filter(order -> order.getOrderStatus() == orderStatus)
                                .collect(java.util.stream.Collectors.toList());
                    } catch (IllegalArgumentException e) {
                        log.warn("無效的訂單狀態參數: {}", status);
                    }
                }
            }
            
            // 【日期過濾】
            if (startDate != null && !startDate.trim().isEmpty()) {
                try {
                    LocalDate start = LocalDate.parse(startDate);
                    orders = orders.stream()
                            .filter(order -> order.getOrderDate().toLocalDate().isAfter(start.minusDays(1)))
                            .collect(java.util.stream.Collectors.toList());
                } catch (Exception e) {
                    log.warn("無效的開始日期參數: {}", startDate);
                }
            }
            
            if (endDate != null && !endDate.trim().isEmpty()) {
                try {
                    LocalDate end = LocalDate.parse(endDate);
                    orders = orders.stream()
                            .filter(order -> order.getOrderDate().toLocalDate().isBefore(end.plusDays(1)))
                            .collect(java.util.stream.Collectors.toList());
                } catch (Exception e) {
                    log.warn("無效的結束日期參數: {}", endDate);
                }
            }
            
            // 【排序】按訂單日期降序排列
            orders.sort((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()));
            
            // 【計算統計資訊】
            Map<String, Object> orderStats = calculateOrderStats(orders);
            
            // 【傳遞資料到視圖】
            model.addAttribute("orders", orders);
            model.addAttribute("orderStats", orderStats);
            model.addAttribute("member", member);
            
            // 【保持篩选參數】
            model.addAttribute("currentStatus", status);
            model.addAttribute("currentStartDate", startDate);
            model.addAttribute("currentEndDate", endDate);
            
            log.info("會員 {} 查看訂單記錄，共 {} 筆訂單", member.getAccount(), orders.size());
            
        } catch (EntityNotFoundException e) {
            log.error("載入訂單記錄失敗 - 會員不存在: {}", e.getMessage());
            session.invalidate();
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN + "?error=member_not_found";
        } catch (Exception e) {
            log.error("載入訂單記錄時發生錯誤: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "載入訂單記錄失敗，請稍後再試");
            model.addAttribute("orders", new ArrayList<>());
            model.addAttribute("orderStats", getEmptyOrderStats());
        }
        
        return MemberViewConstants.VIEW_MEMBER_ORDERS;
    }
    
    /**
     * 【輔助方法】計算訂單統計資訊
     */
    private Map<String, Object> calculateOrderStats(List<OrderListEntity> orders) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalOrders", orders.size());
        
        long pendingCount = orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.PENDING || 
                               order.getOrderStatus() == OrderStatus.CONFIRMED)
                .count();
        stats.put("pendingOrders", pendingCount);
        
        long completedCount = orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.COMPLETED)
                .count();
        stats.put("completedOrders", completedCount);
        
        long totalAmount = orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.COMPLETED)
                .mapToLong(order -> order.getOrderAmount() != null ? order.getOrderAmount() : 0L)
                .sum();
        stats.put("totalAmount", totalAmount);
        
        return stats;
    }
    
    /**
     * 【輔助方法】獲取空的統計資訊
     */
    private Map<String, Object> getEmptyOrderStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", 0);
        stats.put("pendingOrders", 0);
        stats.put("completedOrders", 0);
        stats.put("totalAmount", 0L);
        return stats;
    }
    
    /**
     * 【輔助方法】: 重新準備修改頁面所需的 Model。
     * - 作用: 抽取重複程式碼，當驗證失敗需要返回修改頁面時，由此方法統一準備頁面所需的資料。
     */
    private void prepareUpdatePageModel(Long memberId, Model model) {
        memberService.getMemberById(memberId).ifPresent(member -> {
            MemberUpdateRequest updateRequest = new MemberUpdateRequest();
            updateRequest.setMemberId(member.getMemberId());
            updateRequest.setUsername(member.getUsername());
            updateRequest.setEmail(member.getEmail());
            updateRequest.setPhone(member.getPhone());
            updateRequest.setBirthday(member.getBirthday());
            updateRequest.setGender(member.getGender());
            model.addAttribute("memberUpdateRequest", updateRequest);
        });
    }
    
    /**
     * 【輔助方法】: 將查詢參數 Map 轉換為適合前端顯示的格式
     * - 作用: 處理查詢參數，確保前端能正確顯示搜尋條件
     * 
     * @param params 原始查詢參數 Map
     * @return 處理後的查詢參數 Map
     */
    private Map<String, String> convertToSearchParamsMap(Map<String, String> params) {
        Map<String, String> searchParams = new HashMap<>();
        
        if (params != null) {
            // 處理姓名查詢參數
            String username = params.get("username");
            if (username != null && !username.trim().isEmpty()) {
                searchParams.put("username", username.trim());
            }
            
            // 處理電子郵件查詢參數
            String email = params.get("email");
            if (email != null && !email.trim().isEmpty()) {
                searchParams.put("email", email.trim());
            }
            
            // 處理電話查詢參數
            String phone = params.get("phone");
            if (phone != null && !phone.trim().isEmpty()) {
                searchParams.put("phone", phone.trim());
            }
            
            // 處理帳號查詢參數
            String account = params.get("account");
            if (account != null && !account.trim().isEmpty()) {
                searchParams.put("account", account.trim());
            }
            
            // 處理帳號狀態查詢參數
            String enabled = params.get("enabled");
            if (enabled != null && !enabled.trim().isEmpty()) {
                searchParams.put("enabled", enabled.trim());
            }
            
            // 處理性別查詢參數
            String gender = params.get("gender");
            if (gender != null && !gender.trim().isEmpty()) {
                searchParams.put("gender", gender.trim());
            }
            
            // 處理日期範圍查詢參數
            String startDate = params.get("startDate");
            if (startDate != null && !startDate.trim().isEmpty()) {
                searchParams.put("startDate", startDate.trim());
            }
            
            String endDate = params.get("endDate");
            if (endDate != null && !endDate.trim().isEmpty()) {
                searchParams.put("endDate", endDate.trim());
            }
        }
        
        return searchParams;
    }
    
    // ================================================================
    // 					前端會員註冊功能 (Front-end Member Registration)
    // ================================================================
    
    /**
     * 【功能】: 顯示前端會員註冊表單頁面。
     * 【請求路徑】: 處理 GET /member/register 請求。
     */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        try {
            model.addAttribute("memberCreateRequest", new MemberCreateRequest());
            log.info("註冊頁面載入成功");
            return "front-end/member/member-register";
        } catch (Exception e) {
            log.error("載入註冊頁面時發生錯誤: {}", e.getMessage(), e);
            // 如果發生錯誤，重定向到登入頁面
            return "redirect:/api/v1/auth/member-login";
        }
    }
    
    /**
     * 【功能】: 處理前端會員註冊表單提交。
     * 【請求路徑】: 處理 POST /member/register 請求。
     */
    @PostMapping("/register")
    public String registerMember(@Validated(CreateValidation.class) @ModelAttribute("memberCreateRequest") MemberCreateRequest createRequest,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        // 驗證表單數據
        if (result.hasErrors()) {
            return "front-end/member/member-register";
        }

        try {
            // 註冊會員（此時會自動發送驗證郵件）
            MemberEntity savedMember = memberService.registerMember(createRequest);
            log.info("前端會員 {} 註冊成功，ID: {}，等待驗證", savedMember.getAccount(), savedMember.getMemberId());
            
            // 註冊成功，重定向到驗證頁面並顯示成功訊息
            redirectAttributes.addFlashAttribute("successMessage", 
                "註冊成功！驗證郵件已發送至 young19960127@gmail.com，請查收並輸入驗證碼來啟用您的帳號。");
            redirectAttributes.addAttribute("email", savedMember.getEmail());
            return "redirect:/member/verify";
        
        } catch (IllegalArgumentException e) {
            log.warn("前端註冊失敗: {}", e.getMessage());
            result.addError(new FieldError("memberCreateRequest", "account", e.getMessage()));
            return "front-end/member/member-register";
        }
    }

    /**
     * 【測試方法】: 簡單的註冊頁面顯示 - 用於排除問題
     */
    @GetMapping("/register-test")
    public String showRegisterTestForm() {
        return "front-end/member/member-register";
    }

    // ================================================================
    // 					會員驗證功能 (Member Verification)
    // ================================================================
    
    /**
     * 顯示會員驗證頁面
     */
    @GetMapping("/verify")
    public String showVerificationPage(@RequestParam(value = "email", required = false) String email, Model model) {
        MemberVerificationRequest verificationRequest = new MemberVerificationRequest();
        if (email != null && !email.trim().isEmpty()) {
            verificationRequest.setEmail(email);
        }
        model.addAttribute("memberVerificationRequest", verificationRequest);
        return "front-end/member/member-verification";
    }

    /**
     * 處理會員驗證請求
     */
    @PostMapping("/verify")
    public String verifyMember(@Validated @ModelAttribute("memberVerificationRequest") MemberVerificationRequest verificationRequest,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        
        // 檢查表單驗證錯誤
        if (result.hasErrors()) {
            return "front-end/member/member-verification";
        }
        
        try {
            // 執行驗證
            boolean verificationSuccess = memberService.verifyMemberRegistration(verificationRequest);
            
            if (verificationSuccess) {
                log.info("會員驗證成功 - Email: {}", verificationRequest.getEmail());
                redirectAttributes.addFlashAttribute("successMessage", "帳號驗證成功！您現在可以登入系統了。");
                return "redirect:/api/v1/auth/member-login";
            } else {
                log.warn("會員驗證失敗 - Email: {}", verificationRequest.getEmail());
                result.addError(new FieldError("memberVerificationRequest", "verificationCode", "驗證碼錯誤或已過期，請重新確認。"));
                return "front-end/member/member-verification";
            }
            
        } catch (Exception e) {
            log.error("會員驗證過程發生錯誤 - Email: {}, 錯誤: {}", verificationRequest.getEmail(), e.getMessage());
            result.addError(new FieldError("memberVerificationRequest", "verificationCode", "驗證過程發生錯誤，請稍後再試。"));
            return "front-end/member/member-verification";
        }
    }

    /**
     * 重新發送驗證郵件
     */
    @PostMapping("/resend-verification")
    @ResponseBody
    public ResponseEntity<String> resendVerificationEmail(@RequestParam("email") String email) {
        try {
            boolean success = memberService.resendVerificationEmail(email);
            
            if (success) {
                log.info("重新發送驗證郵件成功 - Email: {}", email);
                return ResponseEntity.ok("驗證郵件已重新發送，請查收信箱。");
            } else {
                log.warn("重新發送驗證郵件失敗 - Email: {}", email);
                return ResponseEntity.badRequest().body("無法重新發送驗證郵件，請確認信箱地址是否正確。");
            }
            
        } catch (Exception e) {
            log.error("重新發送驗證郵件時發生錯誤 - Email: {}, 錯誤: {}", email, e.getMessage());
            return ResponseEntity.status(500).body("系統錯誤，請稍後再試。");
        }
    }

    // ================================================================
    // 					即時驗證功能 (Real-time Validation)
    // ================================================================
    
    /**
     * 【新增】前端個人資料即時驗證端點
     * 
     * @param field 要驗證的欄位名稱
     * @param value 欄位值
     * @param memberId 會員ID
     * @param session HTTP Session
     * @return JSON 回應包含驗證結果
     */
    @PostMapping("/profile/validate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateProfileField(
            @RequestParam("field") String field,
            @RequestParam("value") String value,
            @RequestParam("memberId") Long memberId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 【Session驗證】檢查用戶是否已登入且操作自己的資料
            Long sessionMemberId = (Long) session.getAttribute("loggedInMemberId");
            if (sessionMemberId == null || !sessionMemberId.equals(memberId)) {
                response.put("valid", false);
                response.put("message", "請重新登入");
                return ResponseEntity.status(401).body(response);
            }
            
            // 【欄位驗證】根據不同欄位進行相應驗證
            boolean isValid = false;
            String message = "";
            
            switch (field) {
                case "username":
                    if (value.trim().length() < 2) {
                        message = "姓名至少需要2個字元";
                    } else if (value.trim().length() > 50) {
                        message = "姓名不能超過50個字元";
                    } else {
                        isValid = true;
                        message = "姓名格式正確";
                    }
                    break;
                    
                case "email":
                    // 【修正】更嚴格的電子郵件格式驗證
                    // 支援常見格式如：user@domain.com, user.name@domain.co.uk, user+tag@domain.org
                    // 但拒絕無效格式如：user@domain.co (無效的頂級域名)
                    boolean isValidEmail = false;
                    String emailMessage = "";
                    
                    // 基本格式檢查：用戶名@域名.頂級域名
                    if (!value.matches("^[A-Za-z0-9+_.'-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                        emailMessage = "請輸入有效的電子郵件格式";
                    } else {
                        // 進一步檢查頂級域名是否合理
                        String[] parts = value.split("@");
                        if (parts.length == 2) {
                            String domain = parts[1];
                            String[] domainParts = domain.split("\\.");
                            
                            if (domainParts.length >= 2) {
                                String topLevelDomain = domainParts[domainParts.length - 1];
                                
                                // 【修正邏輯】更嚴格的頂級域名檢查
                                if (topLevelDomain.length() >= 3) {
                                    // 3個字母以上的頂級域名都接受 (.com, .org, .net, .edu 等)
                                    isValidEmail = true;
                                } else if (topLevelDomain.length() == 2 && isCommonTLD(topLevelDomain)) {
                                    // 2個字母的必須是常見的國家代碼
                                    isValidEmail = true;
                                } else {
                                    // 其他情況都拒絕
                                    emailMessage = "請輸入有效的電子郵件格式（如：example@gmail.com）";
                                }
                            } else {
                                emailMessage = "請輸入有效的電子郵件格式";
                            }
                        } else {
                            emailMessage = "請輸入有效的電子郵件格式";
                        }
                    }
                    
                    if (!isValidEmail) {
                        message = emailMessage;
                    } else {
                        // 檢查email是否被其他會員使用 - 【修正】確保與Service層邏輯一致
                        boolean emailExists = memberService.isEmailExistsForOtherMember(value, memberId);
                        if (emailExists) {
                            message = "此電子郵件已被其他會員使用";
                        } else {
                            isValid = true;
                            message = "電子郵件可以使用";
                        }
                    }
                    break;
                    
                case "phone":
                    // 【修正】統一電話號碼驗證邏輯，與 DTO 驗證保持一致
                    // 支援格式：0912345678, 0912-345-678, 09-12345678, (02)12345678, 02-12345678
                    String phonePattern = "^(09\\d{8}|09\\d{2}[\\s-]\\d{3}[\\s-]\\d{3}|09[\\s-]\\d{8}|0[2-8][\\s-]?\\d{7,8}|\\(0[2-8]\\)\\d{7,8})$";
                    
                    if (!value.matches(phonePattern)) {
                        message = "請輸入有效的電話號碼格式（如：0912345678、0912-345-678、02-12345678）";
                    } else {
                        // 檢查電話是否被其他會員使用 - 【修正】確保與Service層邏輯一致
                        boolean phoneExists = memberService.isPhoneExistsForOtherMember(value, memberId);
                        if (phoneExists) {
                            message = "此電話號碼已被其他會員使用";
                        } else {
                            isValid = true;
                            message = "電話號碼可以使用";
                        }
                    }
                    break;
                    
                case "birthday":
                    try {
                        LocalDate birthday = LocalDate.parse(value);
                        LocalDate now = LocalDate.now();
                        LocalDate minDate = now.minusYears(120); // 最大120歲
                        LocalDate maxDate = now.minusYears(10);  // 最小10歲
                        
                        if (birthday.isBefore(minDate)) {
                            message = "出生日期不能超過120年前";
                        } else if (birthday.isAfter(maxDate)) {
                            message = "年齡需滿10歲才能註冊";
                        } else if (birthday.isAfter(now)) {
                            message = "出生日期不能是未來日期";
                        } else {
                            isValid = true;
                            message = "出生日期正確";
                        }
                    } catch (Exception e) {
                        message = "請選擇有效的出生日期";
                    }
                    break;
                    
                case "gender":
                    if ("M".equals(value) || "F".equals(value)) {
                        isValid = true;
                        message = "性別選擇正確";
                    } else {
                        message = "請選擇有效的性別";
                    }
                    break;
                    
                default:
                    message = "未知的驗證欄位";
                    break;
            }
            
            response.put("valid", isValid);
            response.put("message", message);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("個人資料欄位驗證時發生錯誤: {}", e.getMessage());
            response.put("valid", false);
            response.put("message", "驗證過程發生錯誤");
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 【即時驗證】檢查舊密碼是否正確
     * 
     * @param oldPassword 用戶輸入的舊密碼
     * @param session HTTP Session 用來獲取當前會員ID
     * @return JSON 回應包含驗證結果
     */
    @PostMapping("/validate-old-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateOldPassword(
            @RequestParam("oldPassword") String oldPassword,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 【Session驗證】檢查用戶是否已登入
            Long memberId = (Long) session.getAttribute("loggedInMemberId");
            if (memberId == null) {
                response.put("valid", false);
                response.put("message", "請重新登入");
                return ResponseEntity.status(401).body(response);
            }
            
            // 【驗證舊密碼】調用Service層驗證
            boolean isOldPasswordValid = memberService.validateOldPassword(memberId, oldPassword);
            
            if (isOldPasswordValid) {
                response.put("valid", true);
                response.put("message", "舊密碼驗證成功");
            } else {
                response.put("valid", false);
                response.put("message", "舊密碼不正確");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("驗證舊密碼時發生錯誤: {}", e.getMessage());
            response.put("valid", false);
            response.put("message", "驗證過程發生錯誤");
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 【即時驗證】檢查新密碼與舊密碼是否相同
     * 
     * @param oldPassword 舊密碼
     * @param newPassword 新密碼  
     * @param session HTTP Session
     * @return JSON 回應包含驗證結果
     */
    @PostMapping("/validate-password-difference")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validatePasswordDifference(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 【Session驗證】
            Long memberId = (Long) session.getAttribute("loggedInMemberId");
            if (memberId == null) {
                response.put("valid", false);
                response.put("message", "請重新登入");
                return ResponseEntity.status(401).body(response);
            }
            
            // 【檢查新舊密碼是否相同】
            if (oldPassword.equals(newPassword)) {
                response.put("valid", false);
                response.put("message", "新密碼不能與舊密碼相同");
            } else {
                response.put("valid", true);
                response.put("message", "新密碼可以使用");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("驗證密碼差異時發生錯誤: {}", e.getMessage());
            response.put("valid", false);
            response.put("message", "驗證過程發生錯誤");
            return ResponseEntity.status(500).body(response);
        }
    }
    
    // ================================================================
    // 					會員設定功能擴展 (Member Settings Extensions)
    // ================================================================
    
    /**
     * 【前端會員專區路由】會員設定頁面
     * 
     * 路徑說明：
     * - URL: GET /member/settings
     * - 完整 URL: http://localhost:8080/member/settings
     * - 視圖路徑: src/main/resources/templates/front-end/member/member-settings.html
     * 
     * 功能說明：
     * 1. 檢查用戶是否已登入（Session驗證）
     * 2. 如果已登入，載入會員資訊並顯示設定頁面
     * 3. 如果未登入，重定向到登入頁面
     */
    @GetMapping("/settings")
    public String showMemberSettings(Model model, HttpSession session) {
        // 【第一步：Session驗證】檢查用戶是否已登入
        Long memberId = (Long) session.getAttribute("loggedInMemberId");
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        
        // 如果未登入，重定向到登入頁面
        if (memberId == null || isLoggedIn == null || !isLoggedIn) {
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN;
        }
        
        // 【第二步：載入會員資訊】從資料庫獲取最新的會員資料
        try {
            MemberEntity member = memberService.getMemberById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("找不到會員資料"));
            
            // 檢查帳號是否仍然啟用
            if (!member.isEnabled()) {
                // 如果帳號被停用，清除Session並重定向到登入頁面
                session.invalidate();
                return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN + "?error=account_disabled";
            }
            
            // 【第三步：準備頁面資料】將會員資訊傳遞給前端頁面
            model.addAttribute("member", member);
            model.addAttribute("memberName", member.getUsername());
            model.addAttribute("memberAccount", member.getAccount());
            
            log.info("會員 {} 進入設定頁面", member.getAccount());
            
        } catch (Exception e) {
            log.error("載入會員設定頁面時發生錯誤：{}", e.getMessage());
            model.addAttribute("errorMessage", "載入設定頁面失敗，請重新登入");
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN;
        }
        
        return MemberViewConstants.VIEW_MEMBER_SETTINGS;
    }
    
    /**
     * 【功能】: 停用帳號
     * 【請求路徑】: 處理 POST /member/settings/deactivate 請求
     */
    @PostMapping("/settings/deactivate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deactivateAccount(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Session驗證
            Long memberId = (Long) session.getAttribute("loggedInMemberId");
            if (memberId == null) {
                response.put("success", false);
                response.put("message", "請重新登入");
                return ResponseEntity.status(401).body(response);
            }
            
            // 獲取會員資料
            MemberEntity member = memberService.getMemberById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("找不到會員資料"));
            
            // 停用帳號
            memberService.deactivateMember(memberId);
            
            log.info("會員 {} 主動停用帳號", member.getAccount());
            
            // 【修正】停用成功後立即清除Session
            // 清除所有會員相關的Session屬性
            session.removeAttribute("loggedInMemberId");
            session.removeAttribute("loggedInMemberAccount");
            session.removeAttribute("loggedInMemberName");
            session.removeAttribute("memberName");
            session.removeAttribute("isLoggedIn");
            session.removeAttribute("loginTime");
            
            // 使整個Session失效，確保完全登出
            session.invalidate();
            
            log.info("會員 {} 停用帳號後，Session已清除", member.getAccount());
            
            response.put("success", true);
            response.put("message", "帳號已成功停用");
            return ResponseEntity.ok(response);
            
        } catch (EntityNotFoundException e) {
            log.error("停用帳號失敗 - 會員不存在: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "找不到會員資料");
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            log.error("停用帳號失敗: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "停用失敗，請稍後再試");
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 【功能】: 更新通知設定
     * 【請求路徑】: 處理 POST /member/settings/notifications 請求
     */
    @PostMapping("/settings/notifications")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateNotificationSettings(
            @RequestBody Map<String, Object> request,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Session驗證
            Long memberId = (Long) session.getAttribute("loggedInMemberId");
            if (memberId == null) {
                response.put("success", false);
                response.put("message", "請重新登入");
                return ResponseEntity.status(401).body(response);
            }
            
            String type = (String) request.get("type");
            Boolean enabled = (Boolean) request.get("enabled");
            
            // TODO: 實現通知設定的保存邏輯
            // memberService.updateNotificationSetting(memberId, type, enabled);
            
            log.info("會員 {} 更新通知設定: {} = {}", memberId, type, enabled);
            
            response.put("success", true);
            response.put("message", "通知設定已更新");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("更新通知設定失敗: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "設定更新失敗");
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 【功能】: 更新隱私設定
     * 【請求路徑】: 處理 POST /member/settings/privacy 請求
     */
    @PostMapping("/settings/privacy")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updatePrivacySettings(
            @RequestBody Map<String, Object> request,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Session驗證
            Long memberId = (Long) session.getAttribute("loggedInMemberId");
            if (memberId == null) {
                response.put("success", false);
                response.put("message", "請重新登入");
                return ResponseEntity.status(401).body(response);
            }
            
            String type = (String) request.get("type");
            Boolean enabled = (Boolean) request.get("enabled");
            
            // TODO: 實現隱私設定的保存邏輯
            // memberService.updatePrivacySetting(memberId, type, enabled);
            
            log.info("會員 {} 更新隱私設定: {} = {}", memberId, type, enabled);
            
            response.put("success", true);
            response.put("message", "隱私設定已更新");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("更新隱私設定失敗: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "設定更新失敗");
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 【功能】: 下載個人資料
     * 【請求路徑】: 處理 GET /member/settings/download-data 請求
     */
    @GetMapping("/settings/download-data")
    public ResponseEntity<byte[]> downloadPersonalData(HttpSession session) {
        try {
            // Session驗證
            Long memberId = (Long) session.getAttribute("loggedInMemberId");
            if (memberId == null) {
                log.warn("未登入用戶嘗試下載個人資料");
                return ResponseEntity.status(401).build();
            }
            
            // 獲取會員詳細資料
            MemberEntity member = memberService.getMemberById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("找不到會員資料"));
            
            log.info("會員 {} 請求下載個人資料", memberId);
            
            // 建構個人資料內容
            StringBuilder content = new StringBuilder();
            content.append("============================================================\n");
            content.append("                  EatFast 早餐店會員系統\n");
            content.append("                      個人資料匯出\n");
            content.append("============================================================\n\n");
            
            // 基本資料
            content.append("【基本資料】\n");
            content.append("----------------------------------------\n");
            content.append("會員編號：").append(member.getMemberId()).append("\n");
            content.append("帳號：").append(member.getAccount()).append("\n");
            content.append("密碼(以BCrypt加密)：").append(member.getPassword()).append("\n");
            content.append("姓名：").append(member.getUsername()).append("\n");
            content.append("電子郵件：").append(member.getEmail()).append("\n");
            content.append("電話號碼：").append(member.getPhone() != null ? member.getPhone() : "未設定").append("\n");
            content.append("生日：").append(member.getBirthday() != null ? member.getBirthday().toString() : "未設定").append("\n");
            content.append("性別：").append(getGenderText(member.getGender())).append("\n");
            content.append("帳號狀態：").append(member.isEnabled() ? "正常使用" : "已停用").append("\n\n");
            
            // 帳號資訊
            content.append("【帳號資訊】\n");
            content.append("----------------------------------------\n");
            
            // 安全處理時間格式
            if (member.getCreatedAt() != null) {
                try {
                    content.append("註冊時間：").append(
                        member.getCreatedAt().format(
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        )
                    ).append("\n");
                } catch (Exception e) {
                    content.append("註冊時間：").append(member.getCreatedAt().toString()).append("\n");
                }
            } else {
                content.append("註冊時間：不詳\n");
            }
            
            if (member.getLastUpdatedAt() != null) {
                try {
                    content.append("最後更新：").append(
                        member.getLastUpdatedAt().format(
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        )
                    ).append("\n");
                } catch (Exception e) {
                    content.append("最後更新：").append(member.getLastUpdatedAt().toString()).append("\n");
                }
            } else {
                content.append("最後更新：不詳\n");
            }
            content.append("\n");
            
            // 系統資訊
            content.append("【系統資訊】\n");
            content.append("----------------------------------------\n");
            content.append("資料匯出時間：").append(
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
            ).append("\n");
            content.append("匯出格式：UTF-8 文字檔案\n\n");
            
            // 隱私聲明
            content.append("【隱私聲明】\n");
            content.append("----------------------------------------\n");
            content.append("• 此檔案包含您的個人資料，請妥善保管\n");
            content.append("• 請勿將此檔案分享給他人\n");
            content.append("• 如有疑問，請聯繫 EatFast 客服\n");
            content.append("• 客服信箱：support@eatfast.com\n");
            content.append("• 客服電話：0800-123-456\n");
            content.append("============================================================\n");
            content.append("                 感謝您使用 EatFast 早餐店\n");
            content.append("============================================================\n");
            
            // 轉換為位元組陣列
            byte[] data = content.toString().getBytes("UTF-8");
            
            // 產生檔案名稱（包含時間戳記）
            String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("EatFast_個人資料_%s_%s.txt", 
                member.getAccount(), timestamp);
            
            // 設定回應標頭 - 使用安全的方式
            return ResponseEntity.ok()
                    .header("Content-Disposition", 
                        "attachment; filename*=UTF-8''" + 
                        java.net.URLEncoder.encode(filename, "UTF-8"))
                    .header("Content-Type", "text/plain; charset=UTF-8")
                    .header("Content-Length", String.valueOf(data.length))
                    .body(data);
            
        } catch (EntityNotFoundException e) {
            log.error("下載個人資料失敗 - 會員不存在: {}", e.getMessage());
            return ResponseEntity.status(404).build();
        } catch (java.io.UnsupportedEncodingException e) {
            log.error("下載個人資料失敗 - 編碼錯誤: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        } catch (Exception e) {
            log.error("下載個人資料失敗 - 未預期錯誤: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * 【輔助方法】將性別枚舉轉換為中文說明
     */
    private String getGenderText(com.eatfast.common.enums.Gender gender) {
        if (gender == null) {
            return "未設定";
        }
        
        switch (gender) {
            case F:
                return "女性";
            case M:
                return "男性";
            case O:
                return "其他";
            default:
                return "未知 (" + gender + ")";
        }
    }
    
    /**
     * 【輔助方法】檢查是否為常見的2字母頂級域名（國家代碼）
     * 
     * @param tld 頂級域名
     * @return true 如果是常見的2字母國家代碼
     */
    private boolean isCommonTLD(String tld) {
        // 常見的2字母國家代碼頂級域名
        String[] commonTLDs = {
            "tw", "cn", "jp", "kr", "us", "uk", "ca", "au", "de", "fr", 
            "it", "es", "nl", "se", "no", "dk", "fi", "be", "ch", "at",
            "ie", "pt", "pl", "cz", "hu", "ro", "bg", "hr", "si", "sk",
            "ee", "lv", "lt", "mt", "cy", "lu", "is", "li", "mc", "sm",
            "va", "ad", "gi", "je", "gg", "im", "fo", "gl", "aw", "an",
            "ai", "ag", "bb", "bz", "bs", "dm", "do", "gd", "gt", "ht",
            "hn", "jm", "kn", "lc", "ms", "ni", "pa", "sv", "tt", "vc",
            "vg", "vi", "pr", "mx", "cu", "cr", "bo", "br", "cl", "co",
            "ec", "gy", "pe", "py", "sr", "uy", "ve", "ar", "fk", "gf",
            "pf", "tf", "nc", "wf", "vu", "to", "tv", "tk", "nu", "nf",
            "cx", "cc", "hm", "au", "nz", "fj", "pg", "sb", "vu", "nc"
        };
        
        return java.util.Arrays.asList(commonTLDs).contains(tld.toLowerCase());
    }

    /**
     * 【功能】: 處理複合查詢請求，根據多個條件查詢會員 (支援分頁)
     * 【請求路徑】: 處理 POST /member/listMembers_ByCompositeQuery 請求
     */
    @PostMapping("/listMembers_ByCompositeQuery")
    public String listMembersByCompositeQuery(@RequestParam Map<String, String> params,
                                            Model model,
                                            HttpSession session,
                                            RedirectAttributes redirectAttributes) {
        
        log.info("執行複合查詢，參數: {}", params);
        
        // 添加管理員登入資訊到模型中
        addAdminInfoToModel(session, model);
        
        try {
            // 從參數中提取查詢條件
            String username = params.get("username");
            String email = params.get("email");
            String phone = params.get("phone");
            
            // 分頁參數
            String pageStr = params.get("page");
            String sizeStr = params.get("size");
            
            int page = 1; // 預設第1頁
            int size = 15; // 預設每頁15筆
            
            try {
                if (pageStr != null && !pageStr.trim().isEmpty()) {
                    page = Integer.parseInt(pageStr);
                    if (page < 1) page = 1;
                }
                if (sizeStr != null && !sizeStr.trim().isEmpty()) {
                    size = Integer.parseInt(sizeStr);
                    if (size < 1) size = 15;
                    // 限制每頁最多顯示數量
                    if (size > 50) size = 50;
                }
            } catch (NumberFormatException e) {
                log.warn("分頁參數格式錯誤，使用預設值");
            }
            
            // 呼叫 Service 層執行複合查詢（先取得全部結果）
            List<MemberEntity> allMembers = memberService.findMembersByCompositeQuery(username, email, phone);
            
            // 手動實作分頁邏輯
            int totalElements = allMembers.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            
            // 計算開始和結束索引
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, totalElements);
            
            List<MemberEntity> memberList = new ArrayList<>();
            if (startIndex < totalElements) {
                memberList = allMembers.subList(startIndex, endIndex);
            }
            
            // 將查詢結果和分頁資訊傳遞給視圖
            model.addAttribute("memberListData", memberList);
            model.addAttribute("searchParams", convertToSearchParamsMap(params));
            
            // 分頁資訊
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalElements", totalElements);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("hasNext", page < totalPages);
            model.addAttribute("hasPrevious", page > 1);
            
            // 計算顯示範圍
            int displayStart = startIndex + 1;
            int displayEnd = endIndex;
            model.addAttribute("displayStart", displayStart);
            model.addAttribute("displayEnd", displayEnd);
            
            // 只在首次查詢時顯示查詢結果統計訊息（非分頁切換）
            // 通過檢查是否有查詢條件來判斷是否為首次查詢
            boolean isFirstQuery = page == 1 && (
                (username != null && !username.trim().isEmpty()) ||
                (email != null && !email.trim().isEmpty()) ||
                (phone != null && !phone.trim().isEmpty())
            );
            
            if (isFirstQuery) {
                if (totalElements == 0) {
                    model.addAttribute("infoMessage", "沒有找到符合條件的會員資料");
                } else {
                    model.addAttribute("successMessage", "找到 " + totalElements + " 筆符合條件的會員資料，目前顯示第 " + displayStart + "-" + displayEnd + " 筆");
                }
            }
            
            log.info("複合查詢完成，總共找到 {} 筆資料，目前第 {} 頁，每頁 {} 筆", totalElements, page, size);
            
        } catch (Exception e) {
            log.error("複合查詢執行失敗: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "查詢執行失敗：" + e.getMessage());
            return MemberViewConstants.REDIRECT_TO_SELECT_PAGE;
        }
        
        return MemberViewConstants.VIEW_SELECT_PAGE;
    }
    
    /**
     * 【功能】: 處理單一會員查詢顯示
     * 【請求路徑】: 處理 POST /member/getOneForDisplay 請求
     */
    @PostMapping("/getOneForDisplay")
    public String getOneForDisplay(@RequestParam("account") String account,
                                 Model model,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        
        log.info("查詢單一會員，帳號: {}", account);
        
        // 添加管理員登入資訊到模型中
        addAdminInfoToModel(session, model);
        
        if (account == null || account.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "請輸入要查詢的會員帳號");
            return MemberViewConstants.REDIRECT_TO_SELECT_PAGE;
        }
        
        try {
            // 查詢單一會員
            Optional<MemberEntity> memberOpt = memberService.getMemberByAccount(account.trim());
            
            if (memberOpt.isPresent()) {
                model.addAttribute("member", memberOpt.get());
                model.addAttribute("successMessage", "成功找到會員：" + memberOpt.get().getUsername());
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "找不到帳號為 '" + account + "' 的會員");
                return MemberViewConstants.REDIRECT_TO_SELECT_PAGE;
            }
            
            // 同時載入所有會員列表供下拉選單使用
            List<MemberEntity> allMembers = memberService.getAllMembers();
            model.addAttribute("memberListData", allMembers);
            
        } catch (Exception e) {
            log.error("查詢會員失敗: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "查詢失敗：" + e.getMessage());
            return MemberViewConstants.REDIRECT_TO_SELECT_PAGE;
        }
        
        return MemberViewConstants.VIEW_SELECT_PAGE;
    }

    /**
     * 【功能】: 處理性別查詢會員列表
     * 【請求路徑】: 處理 POST /member/listMembersByGender 請求
     */
    @PostMapping("/listMembersByGender")
    public String listMembersByGender(@RequestParam("gender") String gender,
                                     Model model,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        
        log.info("查詢會員性別: {}", gender);
        
        // 添加管理員登入資訊到模型中
        addAdminInfoToModel(session, model);
        
        if (gender == null || gender.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "請選擇要查詢的性別");
            return MemberViewConstants.REDIRECT_TO_SELECT_PAGE;
        }
        
        try {
            // 將字符串轉換為 Gender 枚舉
            Gender genderEnum;
            try {
                // 修正：直接使用 gender.trim()，不需要 toUpperCase()
                genderEnum = Gender.valueOf(gender.trim());
            } catch (IllegalArgumentException e) {
                log.warn("無效的性別選項: {}", gender);
                redirectAttributes.addFlashAttribute("errorMessage", "無效的性別選項: " + gender);
                return MemberViewConstants.REDIRECT_TO_SELECT_PAGE;
            }
            
            // 查詢指定性別的會員
            List<MemberEntity> membersByGender = memberService.getMembersByGender(genderEnum);
            
            if (membersByGender.isEmpty()) {
                model.addAttribute("errorMessage", "找不到性別為 '" + getGenderDisplayName(genderEnum) + "' 的會員");
            } else {
                model.addAttribute("memberListData", membersByGender);
                model.addAttribute("successMessage", "成功找到 " + membersByGender.size() + " 位性別為 '" + getGenderDisplayName(genderEnum) + "' 的會員");
            }
            
            // 保持查詢條件
            model.addAttribute("selectedGender", gender);
            
        } catch (Exception e) {
            log.error("查詢會員性別失敗: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "查詢失敗：" + e.getMessage());
            return MemberViewConstants.REDIRECT_TO_SELECT_PAGE;
        }
        
        return MemberViewConstants.VIEW_SELECT_PAGE;
    }

    /**
     * 【輔助方法】: 獲取性別顯示名稱
     */
    private String getGenderDisplayName(Gender gender) {
        switch (gender) {
            case M:
                return "男性";
            case F:
                return "女性";
            case O:
                return "其他";
            default:
                return "未知";
        }
    }

    /**
     * 【輔助方法】: 將管理員登入資訊添加到模型中
     * 【功能】: 從 HttpSession 中提取管理員資訊並添加到 Model 中，供前端頁面使用
     */
    private void addAdminInfoToModel(HttpSession session, Model model) {
        try {
            // 獲取當前登入的管理員資訊
            Object loggedInEmployee = session.getAttribute("loggedInEmployee");
            String employeeName = (String) session.getAttribute("employeeName");
            String employeeAccount = (String) session.getAttribute("employeeAccount");
            Object employeeRole = session.getAttribute("employeeRole");
            Boolean isEmployeeLoggedIn = (Boolean) session.getAttribute("isEmployeeLoggedIn");
            
            // 將管理員資訊添加到模型中
            if (isEmployeeLoggedIn != null && isEmployeeLoggedIn) {
                model.addAttribute("currentAdmin", loggedInEmployee);
                model.addAttribute("currentAdminName", employeeName);
                model.addAttribute("currentAdminAccount", employeeAccount);
                
                // 將角色轉換為中文顯示名稱
                String roleDisplayName = "未知角色";
                if (employeeRole instanceof com.eatfast.common.enums.EmployeeRole) {
                    roleDisplayName = ((com.eatfast.common.enums.EmployeeRole) employeeRole).getDisplayName();
                } else if (employeeRole != null) {
                    // 如果是字符串形式的角色，嘗試轉換為枚舉
                    try {
                        com.eatfast.common.enums.EmployeeRole role = com.eatfast.common.enums.EmployeeRole.valueOf(employeeRole.toString());
                        roleDisplayName = role.getDisplayName();
                    } catch (IllegalArgumentException e) {
                        log.warn("無法解析角色: {}", employeeRole);
                        roleDisplayName = employeeRole.toString();
                    }
                }
                
                model.addAttribute("currentAdminRole", roleDisplayName);
                model.addAttribute("isAdminLoggedIn", true);
                log.debug("管理員資訊已添加到模型 - 姓名: {}, 帳號: {}, 角色: {}", employeeName, employeeAccount, roleDisplayName);
            } else {
                model.addAttribute("isAdminLoggedIn", false);
                log.debug("未檢測到登入的管理員資訊");
            }
        } catch (Exception e) {
            log.error("添加管理員資訊到模型時發生錯誤: {}", e.getMessage(), e);
            model.addAttribute("isAdminLoggedIn", false);
        }
    }
}

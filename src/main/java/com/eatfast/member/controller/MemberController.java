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
// (既有 import)
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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
            // 【業務邏輯路徑】: 將 DTO 傳遞給 Service 層，執行核心業務。
            MemberEntity savedMember = memberService.registerMember(createRequest);
            log.info("會員 {} 註冊/啟用成功，ID: {}", savedMember.getAccount(), savedMember.getMemberId());
            // 【成功訊息路徑】: 使用 RedirectAttributes 跨重定向傳遞成功訊息。
            redirectAttributes.addFlashAttribute("successMessage", "新增或啟用會員 " + savedMember.getUsername() + " 成功！");
        
        } catch (IllegalArgumentException e) {
            // 【業務例外路徑】: 捕獲從 Service 層拋出的特定業務例外 (如：帳號已存在)。
            log.warn("註冊失敗: {}", e.getMessage());
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
                updateRequest.setEmail(memberEntity.getEmail());
                updateRequest.setPhone(memberEntity.getPhone());
                updateRequest.setBirthday(memberEntity.getBirthday());
                updateRequest.setGender(memberEntity.getGender());
                updateRequest.setIsEnabled(memberEntity.isEnabled());
                
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
            redirectAttributes.addFlashAttribute("successMessage", "密碼更新成功！");
        
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
     * 【功能】: 顯示所有會員列表頁 (簡化版，無分頁)。
     * 【請求路徑】: 處理 GET /member/select_page 請求。
     */
    @GetMapping("/select_page")
    public String showSelectPage(Model model) {
        // 【業務邏輯路徑】: 直接呼叫 Service 方法獲取所有會員的列表。
        List<MemberEntity> memberList = memberService.getAllMembers();
        // 【資料流路徑】: 將列表資料放入 Model，供前端模板 (e.g., th:each) 使用。
        model.addAttribute("memberListData", memberList);
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
    public String showUpdateFormAfterRedirect(@RequestParam("memberId") Long memberId, Model model, RedirectAttributes redirectAttributes) {
        return showUpdateForm(memberId, model, redirectAttributes);
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
            updateRequest.setIsEnabled(member.isEnabled());
            
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
        // 獲取當前會員的訂單列表
        // List<OrderListEntity> orders = orderService.getMemberOrders(memberId, status, startDate, endDate);
        // model.addAttribute("orders", orders);
        return MemberViewConstants.VIEW_MEMBER_ORDERS;
    }
    
    /**
     * 【前端會員專區路由】我的收藏頁面
     */
    @GetMapping("/favorites")
    public String showMemberFavorites(Model model, HttpSession session) {
        // 獲取當前會員的收藏列表
        // List<FavEntity> favorites = favService.getMemberFavorites(memberId);
        // model.addAttribute("favorites", favorites);
        return MemberViewConstants.VIEW_MEMBER_FAVORITES;
    }
    
    /**
     * 【前端會員專區路由】帳號設定頁面
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
            
            // 【第三步：將會員資料傳遞給模板】
            model.addAttribute("member", member);
            
            return MemberViewConstants.VIEW_MEMBER_SETTINGS;
            
        } catch (EntityNotFoundException e) {
            log.error("會員資料載入失敗: {}", e.getMessage());
            session.invalidate();
            return MemberViewConstants.REDIRECT_TO_MEMBER_LOGIN + "?error=member_not_found";
        }
    }

    // ================================================================
    // 					帳號管理相關功能 (Account Management)
    // ================================================================
    
    /**
     * 【功能】: 停用會員帳號
     * 【請求路徑】: 處理 POST /member/settings/deactivate 請求
     * 【安全性】: 需要登入驗證，只能停用自己的帳號
     */
    @PostMapping("/settings/deactivate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deactivateAccount(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 【第一步：Session詳細驗證和調試】
            log.info("=== 開始停用帳號流程 ===");
            log.info("Session ID: {}", session.getId());
            
            Long memberId = (Long) session.getAttribute("loggedInMemberId");
            Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
            String memberAccount = (String) session.getAttribute("loggedInMemberAccount");
            String memberName = (String) session.getAttribute("loggedInMemberName");
            
            log.info("Session 中的會員資訊:");
            log.info("- Member ID: {}", memberId);
            log.info("- Is Logged In: {}", isLoggedIn);
            log.info("- Member Account: {}", memberAccount);
            log.info("- Member Name: {}", memberName);
            log.info("- Session Creation Time: {}", new java.util.Date(session.getCreationTime()));
            log.info("- Session Last Accessed Time: {}", new java.util.Date(session.getLastAccessedTime()));
            log.info("- Session Max Inactive Interval: {} seconds", session.getMaxInactiveInterval());
            
            // 詳細檢查Session狀態
            if (memberId == null) {
                log.warn("Session 中沒有會員ID (loggedInMemberId 為 null)");
                response.put("success", false);
                response.put("message", "請重新登入 - Session中無會員資訊");
                response.put("debug", "memberId is null");
                return ResponseEntity.status(401).body(response);
            }
            
            if (isLoggedIn == null || !isLoggedIn) {
                log.warn("Session 中登入狀態無效: {}", isLoggedIn);
                response.put("success", false);
                response.put("message", "請重新登入 - 登入狀態無效");
                response.put("debug", "isLoggedIn is " + isLoggedIn);
                return ResponseEntity.status(401).body(response);
            }
            
            // 【第二步：資料庫查詢和詳細調試】
            log.info("開始查詢會員資料，會員ID: {}", memberId);
            
            // 先檢查會員是否存在（包括已停用的）
            Optional<MemberEntity> memberOpt = memberService.getMemberById(memberId);
            
            if (memberOpt.isEmpty()) {
                log.error("❌ 嚴重錯誤：Session中的會員ID {} 在資料庫中不存在！", memberId);
                log.error("這可能表示:");
                log.error("1. 會員資料被意外刪除");
                log.error("2. 資料庫連線問題");
                log.error("3. Session中的ID不正確");
                
                // 清除異常的Session
                session.invalidate();
                
                response.put("success", false);
                response.put("message", "會員資料不存在，請重新登入");
                response.put("debug", "Member not found in database with ID: " + memberId);
                return ResponseEntity.badRequest().body(response);
            }
            
            MemberEntity member = memberOpt.get();
            log.info("✅ 成功找到會員資料:");
            log.info("- 會員ID: {}", member.getMemberId());
            log.info("- 會員帳號: {}", member.getAccount());
            log.info("- 會員姓名: {}", member.getUsername());
            log.info("- 帳號狀態: {}", member.isEnabled() ? "啟用" : "已停用");
            log.info("- 創建時間: {}", member.getCreatedAt());
            log.info("- 最後更新: {}", member.getLastUpdatedAt());
            
            // 【第三步：檢查帳號是否已經停用】
            if (!member.isEnabled()) {
                log.warn("帳號已處於停用狀態: {}", member.getAccount());
                response.put("success", false);
                response.put("message", "帳號已處於停用狀態");
                response.put("debug", "Account already disabled");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 【第四步：執行停用操作】
            log.info("開始停用帳號: {}", member.getAccount());
            member.setEnabled(false);
            
            MemberEntity savedMember = memberService.updateMember(member);
            log.info("✅ 帳號停用成功，更新後狀態: {}", savedMember.isEnabled() ? "啟用" : "已停用");
            
            // 【第五步：清除Session】
            log.info("清除Session: {}", session.getId());
            session.invalidate();
            
            log.info("=== 停用帳號流程完成 ===");
            log.info("會員帳號停用成功: memberId={}, account={}", memberId, member.getAccount());
            
            response.put("success", true);
            response.put("message", "帳號已成功停用");
            response.put("debug", "Account deactivated successfully");
            return ResponseEntity.ok(response);
            
        } catch (EntityNotFoundException e) {
            log.error("停用帳號失敗 - 會員不存在: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "會員資料不存在");
            response.put("debug", "EntityNotFoundException: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("停用帳號時發生未預期的錯誤: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "系統錯誤，請稍後再試");
            response.put("debug", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 【調試功能】檢查當前Session狀態 - 臨時診斷用
     * 【請求路徑】: 處理 GET /member/debug/session 請求
     */
    @GetMapping("/debug/session")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugSession(HttpSession session) {
        Map<String, Object> debugInfo = new HashMap<>();
        
        try {
            // Session基本信息
            debugInfo.put("sessionId", session.getId());
            debugInfo.put("creationTime", new java.util.Date(session.getCreationTime()));
            debugInfo.put("lastAccessedTime", new java.util.Date(session.getLastAccessedTime()));
            debugInfo.put("maxInactiveInterval", session.getMaxInactiveInterval());
            debugInfo.put("isNew", session.isNew());
            
            // Session中的會員信息
            Long memberId = (Long) session.getAttribute("loggedInMemberId");
            Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
            String memberAccount = (String) session.getAttribute("loggedInMemberAccount");
            String memberName = (String) session.getAttribute("loggedInMemberName");
            
            debugInfo.put("memberId", memberId);
            debugInfo.put("isLoggedIn", isLoggedIn);
            debugInfo.put("memberAccount", memberAccount);
            debugInfo.put("memberName", memberName);
            
            // 如果有會員ID，檢查資料庫中的會員資料
            if (memberId != null) {
                try {
                    Optional<MemberEntity> memberOpt = memberService.getMemberById(memberId);
                    if (memberOpt.isPresent()) {
                        MemberEntity member = memberOpt.get();
                        debugInfo.put("memberInDatabase", true);
                        debugInfo.put("memberAccountInDb", member.getAccount());
                        debugInfo.put("memberNameInDb", member.getUsername());
                        debugInfo.put("memberEnabled", member.isEnabled());
                        debugInfo.put("memberCreatedAt", member.getCreatedAt());
                        debugInfo.put("memberLastUpdated", member.getLastUpdatedAt());
                    } else {
                        debugInfo.put("memberInDatabase", false);
                        debugInfo.put("error", "會員ID存在於Session中，但在資料庫中找不到對應記錄");
                    }
                } catch (Exception e) {
                    debugInfo.put("databaseError", e.getMessage());
                }
            }
            
            // 所有Session屬性
            Map<String, Object> allAttributes = new HashMap<>();
            java.util.Enumeration<String> attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String name = attributeNames.nextElement();
                Object value = session.getAttribute(name);
                allAttributes.put(name, value != null ? value.toString() : "null");
            }
            debugInfo.put("allSessionAttributes", allAttributes);
            
            debugInfo.put("status", "success");
            
        } catch (Exception e) {
            debugInfo.put("status", "error");
            debugInfo.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(debugInfo);
    }

    // ================================================================
    // 					輔助方法 (Helper Methods)
    // ================================================================
    
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
            updateRequest.setIsEnabled(member.isEnabled());
            model.addAttribute("memberUpdateRequest", updateRequest);
        });
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
                return ResponseEntity.status(401).build();
            }
            
            // TODO: 實現個人資料導出邏輯
            // byte[] data = memberService.exportPersonalData(memberId);
            
            log.info("會員 {} 請求下載個人資料", memberId);
            
            // 暫時返回空文件
            String content = "個人資料導出功能開發中...";
            byte[] data = content.getBytes("UTF-8");
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=personal-data.txt")
                    .header("Content-Type", "application/octet-stream")
                    .body(data);
            
        } catch (Exception e) {
            log.error("下載個人資料失敗: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * 【功能】: 清除瀏覽記錄
     * 【請求路徑】: 處理 POST /member/settings/clear-history 請求
     */
    @PostMapping("/settings/clear-history")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearBrowsingHistory(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Session驗證
            Long memberId = (Long) session.getAttribute("loggedInMemberId");
            if (memberId == null) {
                response.put("success", false);
                response.put("message", "請重新登入");
                return ResponseEntity.status(401).body(response);
            }
            
            // TODO: 實現清除瀏覽記錄的邏輯
            // memberService.clearBrowsingHistory(memberId);
            
            log.info("會員 {} 清除瀏覽記錄", memberId);
            
            response.put("success", true);
            response.put("message", "瀏覽記錄已清除");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("清除瀏覽記錄失敗: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "清除失敗");
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 【功能】: 刪除帳號 (永久刪除)
     * 【請求路徑】: 處理 DELETE /member/settings/delete-account 請求
     */
    @DeleteMapping("/settings/delete-account")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAccount(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Session驗證
            Long memberId = (Long) session.getAttribute("loggedInMemberId");
            if (memberId == null) {
                response.put("success", false);
                response.put("message", "請重新登入");
                return ResponseEntity.status(401).body(response);
            }
            
            // 獲取會員資料用於記錄
            MemberEntity member = memberService.getMemberById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("找不到會員資料"));
            
            // TODO: 實現完整的帳號刪除邏輯
            // 1. 備份重要資料
            // 2. 刪除相關訂單記錄
            // 3. 刪除收藏記錄
            // 4. 刪除會員資料
            // memberService.permanentlyDeleteAccount(memberId);
            
            log.info("會員帳號永久刪除 - ID: {}, Account: {}", memberId, member.getAccount());
            
            // 清除Session
            session.invalidate();
            
            response.put("success", true);
            response.put("message", "帳號已永久刪除");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("刪除帳號失敗: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "刪除失敗");
            return ResponseEntity.status(500).body(response);
        }
    }
}

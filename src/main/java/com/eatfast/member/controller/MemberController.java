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
// 【路徑】引入 Model, Service, 常數與驗證介面，建立 Controller -> Service -> Model 的標準呼叫鏈。
import com.eatfast.member.model.MemberEntity;
import com.eatfast.member.service.MemberService;
import com.eatfast.member.validation.UpdateValidation;
// (既有 import)
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
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
    public String insert(@Validated @ModelAttribute("memberCreateRequest") MemberCreateRequest createRequest,
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
     * 【功能】: 處理「變更密碼」的請求。
     * 【請求路徑】: 處理 POST /member/change-password，這是一個獨立且安全的端點。
     */
    @PostMapping("/change-password")
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
    
    /**
     * 【新方法 for 密碼更新後的重定向】
     * 由於 POST 不能直接重定向到另一個 POST，我們建立一個 GET 端點來顯示更新頁面。
     */
    @GetMapping("/getOne_For_Update_view")
    public String showUpdateFormAfterRedirect(@RequestParam("memberId") Long memberId, Model model, RedirectAttributes redirectAttributes) {
        return showUpdateForm(memberId, model, redirectAttributes);
    }
}

package com.eatfast.member.controller;

import com.eatfast.member.model.MemberEntity;
import com.eatfast.member.service.MemberService;
import com.eatfast.member.validation.CreateValidation;
import com.eatfast.member.validation.UpdateValidation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * [控制層] 會員管理功能
 * 基礎路徑: /member
 */
@Controller
@RequestMapping("/member")
public class MemberController {
    
    private static final Logger log = LoggerFactory.getLogger(MemberController.class);
    
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }
    
    /**
     * 顯示新增會員頁面。
     * 請求路徑: GET /member/addMember
     * 轉發至:   /templates/back-end/member/addMember.html
     */
    @GetMapping("/addMember")
    public String showAddForm(ModelMap model) {
        model.addAttribute("memberEntity", new MemberEntity());
        return MemberViewConstants.VIEW_ADD_MEMBER;
    }
    
    /**
     * 處理新增會員請求。
     * 請求路徑: POST /member/insert
     * 成功: 重定向至 /member/select_page
     * 失敗: 轉發至 addMember.html
     */
    @PostMapping("/insert")
    public String insert(@Validated(CreateValidation.class) @ModelAttribute("memberEntity") MemberEntity formMember,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return MemberViewConstants.VIEW_ADD_MEMBER;
        }

        // 檢查帳號是否存在 (包含已停用的)
        Optional<MemberEntity> existingAccount = memberService.getMemberByAccountIncludeDisabled(formMember.getAccount());
        if (existingAccount.isPresent()) {
            MemberEntity dbMember = existingAccount.get();
            if (dbMember.isEnabled()) {
                result.addError(new FieldError("memberEntity", "account", "帳號 " + formMember.getAccount() + " 已被註冊"));
            } else {
                // 重新啟用邏輯
                dbMember.setEnabled(true);
                dbMember.setUsername(formMember.getUsername());
                dbMember.setPassword(formMember.getPassword());
                dbMember.setEmail(formMember.getEmail());
                dbMember.setPhone(formMember.getPhone());
                dbMember.setBirthday(formMember.getBirthday());
                dbMember.setGender(formMember.getGender());
                
                memberService.registerOrReactivateMember(dbMember);
                redirectAttributes.addFlashAttribute("successMessage", "已重新啟用並更新會員 " + dbMember.getUsername() + " 的資料！");
                return MemberViewConstants.REDIRECT_TO_SELECT_PAGE;
            }
        }

        // 檢查 Email 是否已存在 (包含已停用的)
        Optional<MemberEntity> existingEmail = memberService.getMemberByEmailIncludeDisabled(formMember.getEmail());
        if (existingEmail.isPresent()) {
             result.addError(new FieldError("memberEntity", "email", "Email " + formMember.getEmail() + " 已被註冊或對應到已停用帳號"));
        }
        
        if(result.hasErrors()){
            return MemberViewConstants.VIEW_ADD_MEMBER;
        }

        // 全新註冊邏輯
        memberService.registerOrReactivateMember(formMember);
        redirectAttributes.addFlashAttribute("successMessage", "新增會員 " + formMember.getUsername() + " 成功！");
        return MemberViewConstants.REDIRECT_TO_SELECT_PAGE;
    }
    
    /**
     * 顯示修改會員頁面。
     * 請求路徑: POST /member/getOne_For_Update
     * 轉發至:   /templates/back-end/member/update_member.html
     */
    @PostMapping("/getOne_For_Update")
    public String showUpdateForm(@RequestParam("memberId") Long memberId, ModelMap model) {
        Optional<MemberEntity> optionalMember = memberService.getMemberById(memberId);
        model.addAttribute("memberEntity", optionalMember.orElse(new MemberEntity()));
        return MemberViewConstants.VIEW_UPDATE_MEMBER;
    }

    /**
     * 處理修改會員請求。
     * 請求路徑: POST /member/update
     * 成功: 重定向至 /member/select_page
     * 失敗: 轉發至 update_member.html
     */
    @PostMapping("/update")
    public String update(@Validated(UpdateValidation.class) @ModelAttribute("memberEntity") MemberEntity formMember,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return MemberViewConstants.VIEW_UPDATE_MEMBER;
        }

        memberService.updateMemberDetails(formMember);
        redirectAttributes.addFlashAttribute("successMessage", "會員 " + formMember.getUsername() + " 的資料更新成功！");
        return MemberViewConstants.REDIRECT_TO_SELECT_PAGE;
    }
    
    /**
     * 處理刪除會員請求 (軟刪除)。
     * 請求路徑: POST /member/delete
     * 成功: 重定向至 /member/select_page
     */
    @PostMapping("/delete")
    public String delete(@RequestParam("memberId") Long memberId, RedirectAttributes redirectAttributes) {
        memberService.deleteMemberById(memberId);
        redirectAttributes.addFlashAttribute("successMessage", "會員編號 " + memberId + " 刪除成功！");
        return MemberViewConstants.REDIRECT_TO_SELECT_PAGE;
    }

    /**
     * 顯示所有會員列表頁。
     * 請求路徑: GET /member/select_page
     * 轉發至:   /templates/back-end/member/select_page_member.html
     */
    @GetMapping("/select_page")
    public String showSelectPage(Model model) {
        List<MemberEntity> list = memberService.getAllMembers();
        model.addAttribute("memberListData", list);
        return MemberViewConstants.VIEW_SELECT_PAGE;
    }

    /**
     * 處理單筆查詢請求 (依帳號)。
     * 請求路徑: POST /member/getOneForDisplay
     * 轉發至:   select_page_member.html 並顯示查詢結果。
     */
    @PostMapping("/getOneForDisplay")
    public String getOneForDisplay(@RequestParam("account") String account, Model model) {
        if (account == null || account.trim().isEmpty()) {
            model.addAttribute("errorMessage", "會員帳號: 請勿空白");
            model.addAttribute("memberListData", memberService.getAllMembers());
            return MemberViewConstants.VIEW_SELECT_PAGE;
        }
        Optional<MemberEntity> optionalMember = memberService.getMemberByAccount(account);
        if (optionalMember.isEmpty()) {
            model.addAttribute("errorMessage", "查無資料");
            model.addAttribute("memberListData", memberService.getAllMembers());
        } else {
            model.addAttribute("member", optionalMember.get());
            model.addAttribute("memberListData", Collections.singletonList(optionalMember.get()));
        }
        return MemberViewConstants.VIEW_SELECT_PAGE;
    }

    /**
     * 處理複合查詢請求。
     * 請求路徑: POST /member/listMembers_ByCompositeQuery
     * 轉發至:   select_page_member.html 並顯示查詢結果。
     */
    @PostMapping("/listMembers_ByCompositeQuery")
    public String listMembersByCompositeQuery(@RequestParam Map<String, String> map, Model model) {
        try {
            map.remove("_csrf"); // 移除 CSRF token，避免干擾查詢

            boolean hasValidSearchCriteria = map.values().stream().anyMatch(val -> val != null && !val.trim().isEmpty());

            if (!hasValidSearchCriteria) {
                model.addAttribute("errorMessage", "請至少輸入一個搜尋條件");
                return MemberViewConstants.VIEW_SELECT_PAGE;
            }

            Map<String, String[]> searchMap = new HashMap<>();
            map.forEach((key, value) -> {
                if (!key.startsWith("_") && value != null && !value.trim().isEmpty()) {
                    searchMap.put(key, new String[]{value.trim()});
                }
            });
            
            List<MemberEntity> list = memberService.getAllMembers(searchMap);
            
            if (list.isEmpty()) {
                model.addAttribute("errorMessage", "查無符合條件的會員資料");
            } else {
                model.addAttribute("memberListData", list);
            }
            model.addAttribute("searchParams", searchMap);
            
        } catch (Exception e) {
            log.error("複合查詢發生錯誤: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "查詢過程發生錯誤，請稍後再試");
            Map<String, String[]> errorMap = new HashMap<>();
            map.forEach((key, value) -> {
                if (!key.startsWith("_")) { errorMap.put(key, new String[]{value}); }
            });
            model.addAttribute("searchParams", errorMap);
        }
        
        return MemberViewConstants.VIEW_SELECT_PAGE; 
    }
    
    /**
     * [API] 根據 ID 獲取會員資料 (JSON)。
     * 請求路徑: GET /member/api/detail/{memberId}
     * 回應:     會員資料的 JSON 物件，或 404 Not Found。
     */
    @GetMapping("/api/detail/{memberId}")
    @ResponseBody
    public ResponseEntity<?> getMemberDetail(@PathVariable Long memberId) {
        Optional<MemberEntity> optionalMember = memberService.getMemberById(memberId);
        return optionalMember
                .map(ResponseEntity::ok) // 若找到，回傳 200 OK 與會員資料
                .orElse(ResponseEntity.notFound().build()); // 若找不到，回傳 404 Not Found
    }
}

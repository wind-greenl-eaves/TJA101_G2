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
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * [控制層] 會員功能控制器 - 已對接加密服務
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
     * [新增-步驟2] 處理新增會員的表單提交。
     * ★ 已修改，對接新的 Service 方法。
     */
    @PostMapping("/insert")
    public String insert(@Validated(CreateValidation.class) @ModelAttribute("memberEntity") MemberEntity formMember,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "back-end/member/addMember";
        }

        // 檢查帳號是否存在 (包含已停用的)
        Optional<MemberEntity> existingAccount = memberService.getMemberByAccountIncludeDisabled(formMember.getAccount());
        if (existingAccount.isPresent()) {
            MemberEntity dbMember = existingAccount.get();
            if (dbMember.isEnabled()) {
                result.addError(new FieldError("memberEntity", "account", "帳號 " + formMember.getAccount() + " 已被註冊"));
            } else {
                // 【重新啟用邏輯】
                // 將表單資料填入從DB取出的物件
                dbMember.setEnabled(true);
                dbMember.setUsername(formMember.getUsername());
                dbMember.setPassword(formMember.getPassword()); // ★傳遞原始密碼，Service層會負責加密
                dbMember.setEmail(formMember.getEmail());
                dbMember.setPhone(formMember.getPhone());
                dbMember.setBirthday(formMember.getBirthday());
                dbMember.setGender(formMember.getGender());
                
                // ★ 呼叫新的 Service 方法，此方法會處理加密
                memberService.registerOrReactivateMember(dbMember);
                redirectAttributes.addFlashAttribute("successMessage", "已重新啟用並更新會員 " + dbMember.getUsername() + " 的資料！");
                return "redirect:/member/select_page";
            }
        }

        // 檢查 Email 是否已存在 (包含已停用的)
        Optional<MemberEntity> existingEmail = memberService.getMemberByEmailIncludeDisabled(formMember.getEmail());
        if (existingEmail.isPresent()) {
             result.addError(new FieldError("memberEntity", "email", "Email " + formMember.getEmail() + " 已被註冊或對應到已停用帳號"));
        }
        
        if(result.hasErrors()){
            return "back-end/member/addMember";
        }

        // 【全新註冊邏輯】
        // ★ 呼叫新的 Service 方法，此方法會處理加密
        memberService.registerOrReactivateMember(formMember);
        redirectAttributes.addFlashAttribute("successMessage", "新增會員 " + formMember.getUsername() + " 成功！");
        return "redirect:/member/select_page";
    }
    
    /**
     * [修改-步驟2] 處理修改會員的表單提交。
     * ★ 已修改，對接新的 Service 方法，確保密碼不被意外清空。
     */
    @PostMapping("/update")
    public String update(@Validated(UpdateValidation.class) @ModelAttribute("memberEntity") MemberEntity formMember,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "back-end/member/update_member";
        }

        // ★ 呼叫專門用來更新基本資料的 Service 方法
        // 這個方法不會動到密碼欄位
        memberService.updateMemberDetails(formMember);

        redirectAttributes.addFlashAttribute("successMessage", "會員 " + formMember.getUsername() + " 的資料更新成功！");
        return "redirect:/member/select_page";
    }

    // ... 其他既有的 Controller 方法維持不變 ...
    @GetMapping("/select_page")
    public String showSelectPage(Model model) {
        List<MemberEntity> list = memberService.getAllMembers();
        model.addAttribute("memberListData", list);
        return "back-end/member/select_page_member";
    }

    @PostMapping("/getOneForDisplay")
    public String getOneForDisplay(@RequestParam("account") String account, Model model) {
        if (account == null || account.trim().isEmpty()) {
            model.addAttribute("errorMessage", "會員帳號: 請勿空白");
            model.addAttribute("memberListData", memberService.getAllMembers());
            return "back-end/member/select_page_member";
        }
        Optional<MemberEntity> optionalMember = memberService.getMemberByAccount(account);
        if (optionalMember.isEmpty()) {
            model.addAttribute("errorMessage", "查無資料");
            model.addAttribute("memberListData", memberService.getAllMembers());
        } else {
            model.addAttribute("member", optionalMember.get());
            model.addAttribute("memberListData", Collections.singletonList(optionalMember.get()));
        }
        return "back-end/member/select_page_member";
    }

    @PostMapping("/listMembers_ByCompositeQuery")
    public String listMembersByCompositeQuery(@RequestParam Map<String, String> map, Model model) {
        try {
            log.debug("接收到的查詢參數: {}", map);

            // 移除 _csrf 相關參數（如果有的話）
            map.remove("_csrf");

            // 檢查是否有任何搜尋條件
            boolean hasValidSearchCriteria = map.entrySet().stream()
                .filter(entry -> !entry.getKey().startsWith("_"))
                .anyMatch(entry -> entry.getValue() != null && !entry.getValue().trim().isEmpty());

            if (!hasValidSearchCriteria) {
                model.addAttribute("errorMessage", "請至少輸入一個搜尋條件");
                return "back-end/member/select_page_member";
            }

            // 將單一字串參數轉換為字串陣列格式
            Map<String, String[]> searchMap = new HashMap<>();
            map.forEach((key, value) -> {
                if (!key.startsWith("_") && value != null && !value.trim().isEmpty()) {
                    searchMap.put(key, new String[]{value.trim()});
                }
            });
            
            log.debug("轉換後的查詢參數: {}", searchMap);

            List<MemberEntity> list = memberService.getAllMembers(searchMap);
            
            if (list.isEmpty()) {
                model.addAttribute("errorMessage", "查無符合條件的會員資料");
            } else {
                model.addAttribute("memberListData", list);
                log.debug("查詢結果筆數: {}", list.size());
            }
            
            // 將查詢參數放回 Model
            model.addAttribute("searchParams", searchMap);
            
        } catch (Exception e) {
            log.error("複合查詢發生錯誤: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "查詢過程發生錯誤，請稍後再試");
            
            // 創建一個新的 Map 來存儲參數，以便在錯誤時顯示
            Map<String, String[]> errorMap = new HashMap<>();
            map.forEach((key, value) -> {
                if (!key.startsWith("_")) {
                    errorMap.put(key, new String[]{value});
                }
            });
            model.addAttribute("searchParams", errorMap);
        }
        
        return "back-end/member/select_page_member";
    }

    @GetMapping("/addMember")
    public String showAddForm(ModelMap model) {
        model.addAttribute("memberEntity", new MemberEntity());
        return "back-end/member/addMember";
    }
    
    @PostMapping("/delete")
    public String delete(@RequestParam("memberId") Long memberId, RedirectAttributes redirectAttributes) {
        memberService.deleteMemberById(memberId);
        redirectAttributes.addFlashAttribute("successMessage", "會員編號 " + memberId + " 刪除成功！");
        return "redirect:/member/select_page";
    }

    @PostMapping("/getOne_For_Update")
    public String showUpdateForm(@RequestParam("memberId") Long memberId, ModelMap model) {
        Optional<MemberEntity> optionalMember = memberService.getMemberById(memberId);
        model.addAttribute("memberEntity", optionalMember.orElse(new MemberEntity()));
        return "back-end/member/update_member";
    }
    
    @GetMapping("/api/detail/{memberId}")
    @ResponseBody
    public ResponseEntity<?> getMemberDetail(@PathVariable Long memberId) {
        Optional<MemberEntity> optionalMember = memberService.getMemberById(memberId);
        return optionalMember
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
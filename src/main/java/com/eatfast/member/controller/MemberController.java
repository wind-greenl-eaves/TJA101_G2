package com.eatfast.member.controller;

import com.eatfast.member.model.MemberEntity;
import com.eatfast.member.service.MemberService;
import com.eatfast.member.validation.CreateValidation;
import com.eatfast.member.validation.UpdateValidation;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * [控制層] 會員功能控制器
 * - 職責：處理所有與會員資料相關的 HTTP 請求，並回傳對應的視圖。
 */
@Controller
// ★【基底路徑】所有此 Controller 的方法，URL 都會以 "/member" 為前綴。
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * [查詢] 顯示會員資料管理主頁面。
     * - GET /member/select_page
     * - 返回視圖：/templates/back-end/member/select_page_member.html
     */
    @GetMapping("/select_page")
    public String showSelectPage(Model model) {
        List<MemberEntity> list = memberService.getAllMembers();
        model.addAttribute("memberListData", list);
        return "back-end/member/select_page_member";
    }

    /**
     * [查詢] 依會員帳號查詢單筆資料。
     * - POST /member/getOneForDisplay
     * - 來源：select_page_member.html 的「依帳號查詢」表單。
     * - 返回視圖：同上，但會帶上查詢結果或錯誤訊息。
     */
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

    /**
     * [查詢] 依多個動態條件執行複合查詢。
     * - POST /member/listMembers_ByCompositeQuery
     * - 來源：select_page_member.html 的「萬用複合查詢」表單。
     * - 返回視圖：同上，但列表為過濾後的結果。
     */
    @PostMapping("/listMembers_ByCompositeQuery")
    public String listMembersByCompositeQuery(@RequestParam Map<String, Object> params, Model model) {
        // 將參數轉換為正確的格式
        Map<String, String[]> queryParams = new HashMap<>();
        params.forEach((key, value) -> {
            if (value != null && !value.toString().trim().isEmpty()) {
                queryParams.put(key, new String[]{value.toString().trim()});
            }
        });
        
        List<MemberEntity> list = memberService.getAllMembers(queryParams);
        model.addAttribute("memberListData", list);
        model.addAttribute("param", queryParams);
        return "back-end/member/select_page_member";
    }

    /**
     * [新增-步驟1] 顯示新增會員的頁面。
     * - GET /member/addMember
     * - 來源：select_page_member.html 的「新增會員」按鈕。
     * - 返回視圖：/templates/back-end/member/addMember.html
     */
    @GetMapping("/addMember")
    public String showAddForm(ModelMap model) {
        model.addAttribute("memberEntity", new MemberEntity());
        return "back-end/member/addMember";
    }

    /**
     * [新增-步驟2] 處理新增會員的表單提交。
     * 【已升級】現在會處理「新增」或「重新啟用已停用帳號」的邏輯。
     */
    @PostMapping("/insert")
    public String insert(@Validated(CreateValidation.class) @ModelAttribute("memberEntity") MemberEntity formMember,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {

        // 1. 檢查是否有格式錯誤 (例如生日為空)
        if (result.hasErrors()) {
            return "back-end/member/addMember";
        }

        // 2. 檢查帳號是否已存在 (包含已停用的)
        Optional<MemberEntity> existingAccount = memberService.getMemberByAccountIncludeDisabled(formMember.getAccount());
        if (existingAccount.isPresent()) {
            MemberEntity dbMember = existingAccount.get();
            // 如果帳號存在且是啟用狀態，回報錯誤
            if (dbMember.isEnabled()) {
                result.addError(new FieldError("memberEntity", "account", "帳號 " + formMember.getAccount() + " 已被註冊"));
            } else {
                // 如果帳號存在但是停用狀態，執行「重新啟用」並更新資料
                dbMember.setEnabled(true);
                dbMember.setUsername(formMember.getUsername());
                dbMember.setPassword(formMember.getPassword()); // ★注意：此處應加入密碼加密邏輯
                dbMember.setEmail(formMember.getEmail());
                dbMember.setPhone(formMember.getPhone());
                dbMember.setBirthday(formMember.getBirthday());
                dbMember.setGender(formMember.getGender());
                
                memberService.saveOrUpdateMember(dbMember); // 這是更新操作
                redirectAttributes.addFlashAttribute("successMessage", "已重新啟用並更新會員 " + dbMember.getUsername() + " 的資料！");
                return "redirect:/member/select_page";
            }
        }

        // 3. 檢查 Email 是否已存在 (包含已停用的)
        Optional<MemberEntity> existingEmail = memberService.getMemberByEmailIncludeDisabled(formMember.getEmail());
        if (existingEmail.isPresent()) {
            if (existingEmail.get().isEnabled()) {
                result.addError(new FieldError("memberEntity", "email", "Email " + formMember.getEmail() + " 已被註冊"));
            } else {
                // 也可以加入重新啟用的邏輯，但通常以帳號為主，這裡先回報錯誤
                 result.addError(new FieldError("memberEntity", "email", "此 Email 對應到一個已停用帳號，請聯繫客服。"));
            }
        }
        
        // 4. 如果有任何業務錯誤，返回表單頁面
        if(result.hasErrors()){
            return "back-end/member/addMember";
        }

        // 5. 如果帳號和 Email 都是全新的，才執行新增
        memberService.saveOrUpdateMember(formMember);
        redirectAttributes.addFlashAttribute("successMessage", "新增會員 " + formMember.getUsername() + " 成功！");
        return "redirect:/member/select_page";
    }
    
    /**
     * [刪除] 處理刪除會員的請求。
     * - POST /member/delete
     * - 來源：select_page_member.html 列表中的「刪除」按鈕。
     * - 成功後重導向 (Redirect) 至 /member/select_page。
     */
    @PostMapping("/delete")
    public String delete(@RequestParam("memberId") Long memberId, RedirectAttributes redirectAttributes) {
        memberService.deleteMemberById(memberId);
        
        redirectAttributes.addFlashAttribute("successMessage", "會員編號 " + memberId + " 刪除成功！");
        return "redirect:/member/select_page";
    }

    /**
     * [修改-步驟1] 顯示修改會員的頁面，並帶入現有資料。
     * - POST /member/getOne_For_Update
     * - 來源：select_page_member.html 列表中的「修改」按鈕。
     * - 返回視圖：/templates/back-end/member/update_member.html
     */
    @PostMapping("/getOne_For_Update")
    public String showUpdateForm(@RequestParam("memberId") Long memberId, ModelMap model) {
        Optional<MemberEntity> optionalMember = memberService.getMemberById(memberId);
        model.addAttribute("memberEntity", optionalMember.orElse(new MemberEntity()));
        return "back-end/member/update_member";
    }

    /**
     * [修改-步驟2] 處理修改會員的表單提交。
     * - POST /member/update
     * - 來源：update_member.html 的表單提交。
     * - 成功後重導向 (Redirect) 至 /member/select_page。
     */
    @PostMapping("/update")
    public String update(@Validated(UpdateValidation.class) @ModelAttribute("memberEntity") MemberEntity memberEntity,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "back-end/member/update_member";
        }

        memberService.saveOrUpdateMember(memberEntity);

        redirectAttributes.addFlashAttribute("successMessage", "會員 " + memberEntity.getUsername() + " 的資料更新成功！");
        return "redirect:/member/select_page"; // 重導向至會員列表頁面 
    }

    /**
     * [API] 獲取會員詳細資訊。
     * - GET /member/api/detail/{memberId}
     * - 返回：JSON 格式的會員詳細資料
     */
    @GetMapping("/api/detail/{memberId}")
    @ResponseBody
    public ResponseEntity<?> getMemberDetail(@PathVariable Long memberId) {
        Optional<MemberEntity> optionalMember = memberService.getMemberById(memberId);
        return optionalMember
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
package com.eatfast.member.controller;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.eatfast.member.model.MemberEntity;
import com.eatfast.member.service.MemberService;

/**
 * 									[控制層] 會員功能控制器
 * - 職責：處理所有與會員資料相關的 HTTP 請求，並回傳對應的視圖。
 */
@Controller
						// ★【基底路徑】所有此 Controller 的方法，URL 都會以 "/member" 為前綴。
@RequestMapping("/member")
public class MemberController {
	
	//- 依賴注入 MemberService，這是處理會員業務邏輯的服務層。
    private final MemberService memberService;
    
    //- 使用建構子注入 MemberService，這是 Spring 官方推薦的最佳實踐。
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }
    
    /**
     * [查詢] 顯示會員資料管理主頁面。
     */
    @GetMapping("/select_page") 				//- GET /member/select_page
    public String showSelectPage(Model model) {
        List<MemberEntity> list = memberService.getAllMembers();
        model.addAttribute("memberListData", list);
        
        										//返回視圖：/templates/back-end/member/select_page_member.html
        return "back-end/member/select_page_member";
    }

    /**
     * 	[查詢] 依會員帳號查詢單筆資料。
     * - 來源：select_page_member.html 的「依帳號查詢」表單。
     */
    @PostMapping("/getOneForDisplay") 				//- POST /member/getOneForDisplay
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
        
        return "back-end/member/select_page_member"; // 返回同一個視圖，但會顯示查詢結果或錯誤訊息
    }
    
    /**
     * [查詢] 依多個動態條件執行複合查詢。
     * - 來源：select_page_member.html 的「萬用複合查詢」表單。
     */
    @PostMapping("/listMembers_ByCompositeQuery") 	//- POST /member/listMembers_ByCompositeQuery
    public String listMembersByCompositeQuery(@RequestParam Map<String, String[]> map, Model model) {
        List<MemberEntity> list = memberService.getAllMembers(map);
        model.addAttribute("memberListData", list);
        model.addAttribute("param", map);
        return "back-end/member/select_page_member";// 返回同一個視圖，但會顯示查詢結果
    }

    /**
     * [新增-步驟1] 顯示新增會員的頁面。
     * - 來源：select_page_member.html 的「新增會員」按鈕。
     */
    @GetMapping("/addMember")						 //- GET /member/addMember
    public String showAddForm(ModelMap model) {
        model.addAttribute("memberEntity", new MemberEntity());
        return "back-end/member/addMember"; 		//- 返回視圖：/templates/back-end/member/addMember.html
    }

    /**
     * [新增-步驟2] 處理新增會員的表單提交。
     * - 來源：addMember.html 的表單提交。
     * 
     */
    @PostMapping("/insert") 						//- POST /member/insert
    public String insert(@Valid @ModelAttribute("memberEntity") MemberEntity memberEntity,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {

        // 業務校驗：檢查帳號或 Email 是否已存在
        if (memberService.getMemberByAccount(memberEntity.getAccount()).isPresent()) {
             result.addError(new FieldError("memberEntity", "account", "帳號 " + memberEntity.getAccount() + " 已被註冊"));
        }
        if (memberService.getMemberByEmail(memberEntity.getEmail()).isPresent()) {
             result.addError(new FieldError("memberEntity", "email", "Email " + memberEntity.getEmail() + " 已被註冊"));
        }

        // 統一檢查所有驗證結果
        if (result.hasErrors()) {
            return "back-end/member/addMember";
        }

        memberService.saveOrUpdateMember(memberEntity);

        redirectAttributes.addFlashAttribute("successMessage", "新增會員 " + memberEntity.getUsername() + " 成功！");
        return "redirect:/member/select_page"; //- 成功後重導向 (Redirect) 至 /member/select_page，避免重複提交。
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
    public String update(@Valid @ModelAttribute("memberEntity") MemberEntity memberEntity,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "back-end/member/update_member";
        }

        memberService.saveOrUpdateMember(memberEntity);

        redirectAttributes.addFlashAttribute("successMessage", "會員 " + memberEntity.getUsername() + " 的資料更新成功！");
        return "redirect:/member/select_page";
    }
}

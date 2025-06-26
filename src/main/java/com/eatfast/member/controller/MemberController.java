package com.eatfast.member.controller;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.eatfast.member.model.MemberEntity;
import com.eatfast.member.service.MemberService;

/**
 * 會員功能控制器 (Controller)
 * 負責處理所有與會員資料相關的 HTTP 請求，例如查詢、新增等。
 * @RequestMapping("/member") - 【重點註記: 類別層級路徑】
 * - 此註解設定了這個 Controller 的「基底路徑」。
 * - 所有在此類別中的方法，其最終 URL 都會以 "/member" 作為開頭。
 */
@Controller
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }
    
    /**
     * 【功能】: 顯示會員資料管理主頁面。
     * - 此方法為使用者進入會員管理功能的「入口」。
     * - 會預先載入所有會員資料，並呈現在頁面右側的列表。
     */
    //【重點註記: 請求路徑】--> GET http://localhost:8080/member/select_page
    @GetMapping("/select_page") 
    public String showSelectPage(Model model) {
        List<MemberEntity> list = memberService.getAllMembers();
        model.addAttribute("memberListData", list);
        //【重點註記: 返回視圖】--> 前往 /templates/back-end/member/select_page_member.html
        return "back-end/member/select_page_member";
    }

    /**
     * 【功能】: 處理「依會員帳號查詢」的請求。
     * - 對應前端 <form th:action="@{/member/getOneForDisplay}"> 的提交。
     * - 使用 @RequestParam("account") 來精準獲取 name="account" 的 input 值。
     */
    //【重點註記: 請求路徑】--> POST http://localhost:8080/member/getOneForDisplay
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
        
        //【重點註記: 返回視圖】--> 重新整理 select_page_member.html 頁面，並顯示查詢結果或錯誤訊息。
        return "back-end/member/select_page_member";
    }
    
    /**
     * 【功能】: 處理「萬用複合查詢」的請求。
     * - 對應前端 <form th:action="@{/member/listMembers_ByCompositeQuery}"> 的提交。
     * - 使用 Map<String, String[]> map 來動態接收所有查詢欄位，彈性極高。
     */
    //【重點註記: 請求路徑】--> POST http://localhost:8080/member/listMembers_ByCompositeQuery
    @PostMapping("/listMembers_ByCompositeQuery") 
    public String listMembersByCompositeQuery(@RequestParam Map<String, String[]> map, Model model) {
        List<MemberEntity> list = memberService.getAllMembers(map);
        model.addAttribute("memberListData", list);
        model.addAttribute("param", map);
        //【重點註記: 返回視圖】--> 重新整理 select_page_member.html 頁面，並顯示過濾後的會員列表。
        return "back-end/member/select_page_member";
    }

    /**
     * 【功能】: 轉發至「新增會員」的頁面。
     * - 對應前端 <a th:href="@{/member/addMember}"> 的點擊事件。
     * - 準備一個空的 MemberEntity 物件，供新增表單進行資料綁定。
     */
    //【重點註記: 請求路徑】--> GET http://localhost:8080/member/addMember
    @GetMapping("/addMember") 
    public String showAddForm(ModelMap model) {
        model.addAttribute("memberEntity", new MemberEntity());
        //【重點註記: 返回視圖】--> 前往 /templates/back-end/member/addMember.html
        return "back-end/member/addMember"; 
    }

    /**
     * 【功能】: 處理從「新增會員」頁面提交的表單資料。
     * - 接收前端傳來的表單資料，並使用 @ModelAttribute 將其自動封裝成 MemberEntity 物件。
     * - @Valid 會觸發 MemberEntity 中設定的驗證規則。
     */
    //【重點註記: 請求路徑】--> POST http://localhost:8080/member/insert
    @PostMapping("/insert") 
    public String insert(@Valid @ModelAttribute("memberEntity") MemberEntity memberEntity,
                         BindingResult result,
                         ModelMap model) {

        if (result.hasErrors()) {
            //【重點註記: 返回視圖 - 驗證失敗】--> 返回原新增頁面，並顯示錯誤訊息。
            return "back-end/member/addMember";
        }

        memberService.saveOrUpdateMember(memberEntity);

        List<MemberEntity> list = memberService.getAllMembers();
        model.addAttribute("memberListData", list);
        model.addAttribute("successMessage", "新增會員 " + memberEntity.getUsername() + " 成功！");
        //【重點註記: 返回視圖 - 操作成功】--> 返回列表頁，並顯示成功訊息。
        return "back-end/member/select_page_member";
    }
}

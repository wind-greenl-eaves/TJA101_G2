package com.eatfast.member.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Optional;
import com.eatfast.member.model.MemberEntity;
import com.eatfast.member.service.MemberService;

/**
 * 會員功能控制器 (Controller)
 * 負責處理所有與會員資料相關的 HTTP 請求，例如查詢、新增等。
 */
@Controller
@RequestMapping("/member")
public class MemberController {

    // 自動注入 MemberService，用來處理實際的業務邏輯。
    @Autowired
    MemberService memberService;
    
    /**
     * 顯示會員查詢及列表頁面。
     * @param model 用於將會員列表傳遞到前端。
     * @return 會員查詢頁面的視圖名稱。
     */
    @GetMapping("/select_page")
    public String showSelectPage(Model model) {
        List<MemberEntity> list = memberService.getAllMembers();
        model.addAttribute("memberListData", list);
        return "back-end/member/select_page_member";
    }

    /**
     * 根據帳號查詢單一會員資料。
     * @param account 從請求中獲取的會員帳號。
     * @param model 用於傳遞查詢結果或錯誤訊息。
     * @return 會員查詢頁面的視圖名稱。
     */
    @PostMapping("/getOneForDisplay")
    public String getOneForDisplay(@RequestParam("account") String account, Model model) {

        // 伺服器端基本驗證：防止空值查詢。
        if (account == null || account.trim().isEmpty()) {
            model.addAttribute("errorMessage", "會員帳號: 請勿空白");
            // 為了讓錯誤頁面能正常顯示，依然需要將列表資料傳回去。
            List<MemberEntity> list = memberService.getAllMembers();
            model.addAttribute("memberListData", list);
            return "back-end/member/select_page_member";
        }
        
        // 呼叫 Service 查詢，並將結果傳遞給前端。
        Optional<MemberEntity> optionalMember = memberService.getMemberByAccount(account);
        List<MemberEntity> list = memberService.getAllMembers();
        model.addAttribute("memberListData", list);

        if (optionalMember.isEmpty()) {
            model.addAttribute("errorMessage", "查無資料");
        } else {
            model.addAttribute("member", optionalMember.get());
        }
        
        return "back-end/member/select_page_member";
    }
    
    /**
     * 轉發到新增會員的頁面。
     * @param model 用於放置一個空的 MemberEntity 物件，供表單綁定。
     * @return 新增會員頁面的視圖名稱。
     */
    @GetMapping("/addMember")
    public String showAddForm(ModelMap model) {
        model.addAttribute("memberEntity", new MemberEntity());
        return "back-end/member/addMember";
    }

    /**
     * 處理新增會員的表單提交。
     * @param memberEntity Spring 自動將表單資料綁定成的物件。
     * @param result 驗證結果的容器。
     * @param model 用於傳遞成功或失敗的相關資料。
     * @return 成功則導向列表頁；失敗則返回新增頁面。
     */
    @PostMapping("/insert")
    public String insert(
            @Valid @ModelAttribute("memberEntity") MemberEntity memberEntity,
            BindingResult result,
            ModelMap model) {

        // 如果後端驗證失敗，則返回新增頁面，並顯示詳細錯誤。
        if (result.hasErrors()) {
            return "back-end/member/addMember";
        }

        // 呼叫 Service 儲存資料。
        memberService.saveOrUpdateMember(memberEntity);

        // 新增成功後，重導向回列表頁，並附上成功訊息。
        List<MemberEntity> list = memberService.getAllMembers();
        model.addAttribute("memberListData", list);
        model.addAttribute("successMessage", "新增會員 " + memberEntity.getUsername() + " 成功！");
        return "back-end/member/select_page_member";
    }
}

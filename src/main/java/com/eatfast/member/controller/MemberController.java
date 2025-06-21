package com.eatfast.member.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotBlank; // 使用 @NotBlank 取代 @NotEmpty，更精確
import jakarta.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.eatfast.member.model.MemberEntity;
import com.eatfast.member.model.MemberService;
// 這裡的 Service 名稱是 Demo_MemberService，通常在正式專案中會命名為 MemberService
import com.eatfast.member.model.Demo_MemberService; 

/**
 * ★★★★★會員控制器 (Controller)負責接收與會員相關的 HTTP 請求，調用 Service 層處理業務邏輯，
 * 並決定最終要回傳哪個視圖 (View) 給使用者。★★★★★
 * 這個Spring MVC控制器處理會員查詢的請求，
 * 並在查詢失敗時提供錯誤處理機制,主要用於伺服器端渲染 (Server-Side Rendering)。
 * * @Controller 				- 標記這是一個控制器類別，Spring 會自動處理它的生命週期。
 * * @Validated 				- 啟用 Spring 的方法級別驗證功能，讓 @RequestParam 上的驗證註解生效。
 * * @RequestMapping 			- 定義此控制器處理的所有請求的基礎路徑，例如 /member/getOneForDisplay。
 * * @Autowired 				- 用於自動注入 MemberService 的實例，讓 Controller 可以呼叫業務邏輯方法。
 * * @PostMapping("getOneForDisplay") - 處理來自查詢頁面的 POST 請求，對應前端 <form> 的 action="/member/getOneForDisplay"。
 * * @NotBlank 				- 用於驗證請求參數 "account" 不能為空白。
 * * @ModelAttribute 			- 用於將查詢結果或錯誤訊息傳遞到視圖 (View) 中。
 */
@Controller
@Validated 
@RequestMapping("/member") 
public class MemberController {

    @Autowired 
    MemberService memberService; 
    /**
     * 處理來自查詢頁面的 POST 請求，根據會員帳號查詢單一會員。
     * 同時也驗證使用者輸入的帳號格式是否符合規範。
     * @param account 從請求中取得名為 "account" 的參數，並進行驗證。
     * @param model 用於將資料傳遞給視圖 (View) 的物件。
     * @return 返回一個字串，代表要渲染的視圖名稱。
     */
    @PostMapping("getOneForDisplay") 
    public String getOneForDisplay(
    		
            // ------ 1. 接收請求參數 & 進行輸入格式的驗證 ------
            // 這些是 Jakarta Validation 的標準註解，用於宣告驗證規則。
            @NotBlank(message = "會員帳號: 請勿空白") 
            @Size(min = 4, max = 50, message = "會員帳號: 長度必須在 {min} 到 {max} 個字之間")
            @RequestParam("account") String account, // [可變] "account" 對應前端 <input> 的 name 屬性。
            Model model) {

        	// ------ 2. 開始呼叫 Service 層查詢資料 ------
        Optional<MemberEntity> optionalMember = memberService.getMemberByAccount(account);

        // 為了讓頁面即使在查詢後，下方的列表依然存在，我們需要重新取得所有會員資料。
        List<MemberEntity> list = memberService.getAllMembers();
        model.addAttribute("memberListData", list); //  "memberListData" 是在 Thymeleaf 或 JSP 中使用的變數名稱。

        	// ------ 3. 根據查詢結果，準備要轉交的資料 ------
        // 判斷查詢結果是否存在
        if (optionalMember.isEmpty()) {
            model.addAttribute("errorMessage", "查無資料"); // "errorMessage" 是在 View 中使用的錯誤訊息變數名稱。
            // 返回原查詢頁面，並顯示 "查無資料" 的訊息。
            return "back-end/member/select_page"; //  "backend/member/select_page" 是視圖檔案的路徑。
        }

        // ------ 4. 查詢完成,準備成功畫面的資料 ------
        model.addAttribute("member", optionalMember.get()); // 將查詢到的會員物件存入 model
        
        // 返回原查詢頁面，查詢結果會顯示在頁面的特定區塊。
        return "back-end/member/select_page";
    }
    /**
     * 這是統一的例外處理器。
     * 當這個 Controller 內任何地方拋出 ConstraintViolationException 例外時 (通常是 @Validated 驗證失敗)，
     * Spring 會自動捕獲該例外，並呼叫這個方法來處理。
     * @param req HttpServletRequest 物件，可用於獲取請求相關資訊。
     * @param e 捕獲到的例外物件。
     * @param model 用於傳遞資料給錯誤頁面。
     * @return 返回一個 ModelAndView 物件，其中包含了視圖名稱和模型資料。
     */
    @ExceptionHandler(value = { ConstraintViolationException.class })
    public ModelAndView handleError(HttpServletRequest req, ConstraintViolationException e, Model model) {
        // 從例外中獲取所有驗證失敗的訊息。
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        StringBuilder strBuilder = new StringBuilder();
        for (ConstraintViolation<?> violation : violations) {
            strBuilder.append(violation.getMessage() + "<br>");
        }
        String errorMessage = strBuilder.toString();

        // 當驗證失敗時，依然需要將頁面上必要的資料（如下方的會員列表）重新載入並返回，
        // 否則頁面會因缺少資料而顯示錯誤。
        List<MemberEntity> list = memberService.getAllMembers();
        model.addAttribute("memberListData", list);

        // 使用 ModelAndView 將錯誤訊息和模型數據一起返回到指定的視圖。
        // 這是一種將「資料」和「視圖路徑」綁定在一起回傳的方式。
        return new ModelAndView("back-end/member/select_page", "errorMessage", "請修正以下錯誤:<br>" + errorMessage);
    }
}

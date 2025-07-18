package com.eatfast.fav.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eatfast.fav.dto.FavMealDTO;
import com.eatfast.fav.model.FavService;
import com.eatfast.member.controller.MemberViewConstants;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/member")
public class FavController {

    private final FavService favService;

    public FavController(FavService favService) {
        this.favService = favService;
    }
    
    // AJAX 新增收藏餐點
	@PostMapping("/api/fav")
	@ResponseBody
	public ResponseEntity<?> ajaxAddFav(HttpSession session, @RequestParam("mealId") Long mealId) {
	    Long memberId = (Long) session.getAttribute("loggedInMemberId");
	    System.out.println("Session memberId = " + memberId);
	
	    if (memberId == null) {
	        // 未登入，回傳 401 狀態和 json 結果
	        return ResponseEntity.status(401).body(
	            java.util.Map.of("success", false, "msg", "未登入")
	        );
	    }
	    // 呼叫service新增收藏（回傳收藏主鍵 favMealId）
	    Long favMealId = favService.addFav(memberId, mealId);
	
	    // 回傳 json 給前端，前端可拿 favMealId 動態同步 UI 狀態
	    return ResponseEntity.ok(
	        java.util.Map.of(
	            "success", true,
	            "msg", "已加入最愛",
	            "favMealId", favMealId
	        )
	    );
	}

	// RESTful 移除（for AJAX）
	@DeleteMapping("/favorites/{favMealId}/remove")
	@ResponseBody
	public ResponseEntity<?> removeFavById(@PathVariable Long favMealId) {
	    favService.removeFavById(favMealId);
	    return ResponseEntity.ok().build();
	}

	// 顯示會員收藏頁面
	@GetMapping("/favorites")
	public String showFav(HttpSession session, Model model) {
	    Long memberId = (Long) session.getAttribute("loggedInMemberId");
	
	    List<FavMealDTO> favorites = favService.getFavMeals(memberId);
	    model.addAttribute("favorites", favorites);
	    return MemberViewConstants.VIEW_MEMBER_FAVORITES;
	    // 返回會員收藏頁面，顯示該會員的收藏餐點列表
	}
	

	// 新增收藏餐點 (表單版)
    @PostMapping("/addFav")
    public String addFav(HttpSession session, @RequestParam("mealId") Long mealId, 
    		@RequestParam(value = "redirectUrl", required = false, defaultValue = "/menu") String redirectUrl) {
		Long memberId = (Long) session.getAttribute("loggedInMemberId");
		System.out.println("Session memberId = " + memberId);

		if (memberId == null) {
			// 如果會員ID為空，導向到登入頁面
			return "redirect:/api/v1/auth/member-login"; 
		}
		// 如果會員ID不為空，則調用服務層新增收藏
		favService.addFav(memberId, mealId);
		
		 return "redirect:" + redirectUrl;
		}
    
    // 根據主鍵移除收藏 (表單版)
    @PostMapping("/removeFav")
    public String removeFav(@RequestParam("favMealId") Long favMealId,
                            @RequestParam(value = "redirectUrl", required = false, defaultValue = "/member/favorites") String redirectUrl) {
        favService.removeFavById(favMealId);
        return "redirect:" + redirectUrl;
    }
    
    
}

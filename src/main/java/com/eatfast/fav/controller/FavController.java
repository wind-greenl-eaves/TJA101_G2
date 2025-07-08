package com.eatfast.fav.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/favorites")
    public String showFav(HttpSession session, Model model) {
        Long memberId = (Long) session.getAttribute("memberId");

        List<FavMealDTO> favorites = favService.getFavMeals(memberId);
        model.addAttribute("favorites", favorites);
        return MemberViewConstants.VIEW_MEMBER_FAVORITES;
        // 返回會員收藏頁面，顯示該會員的收藏餐點列表
    }

    @PostMapping("/removeFav")
    public String removeFav(HttpSession session, @RequestParam("mealId") Long mealId) {
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            // 如果會員ID為空，導向到登入頁面
            return "redirect:/api/v1/auth/member-login"; 
        }
        // 如果會員ID不為空，則調用服務層移除收藏
        favService.removeFav(memberId, mealId);

        return "redirect:/member/favorites"; // 移除成功後，重定向回收藏列表頁面
    }
}

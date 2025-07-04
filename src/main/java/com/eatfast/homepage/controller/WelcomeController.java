package com.eatfast.homepage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WelcomeController {
    
    @GetMapping("/welcome")
    public String showWelcomePage() {
        return "welcome"; // 這會返回 welcome.html 模板
    }
}
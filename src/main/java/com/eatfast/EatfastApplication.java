package com.eatfast;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication // 標記這是 Spring Boot 應用程式的主類別
@ServletComponentScan // 啟用 Servlet 組件掃描，讓 @WebFilter 註解生效
// 這個註解會自動掃描同一個包及其子包中的組件
// 並啟用自動配置功能
// 這樣可以簡化配置，讓開發者專注於業務邏輯
// 這是 Spring Boot 應用程式的入口點
public class EatfastApplication {

	public static void main(String[] args) {
		SpringApplication.run(EatfastApplication.class, args);
	}

}
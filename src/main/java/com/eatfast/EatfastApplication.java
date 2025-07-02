package com.eatfast;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // 標記這是 Spring Boot 應用程式的主類別
// 這個註解會自動掃描同一個包及其子包中的組件
// 並啟用自動配置功能
// 這樣可以簡化配置，讓開發者專注於業務邏輯
// 這是 Spring Boot 應用程式的入口點
public class EatfastApplication {

	public static void main(String[] args) {
		SpringApplication.run(EatfastApplication.class, args);
	}

}

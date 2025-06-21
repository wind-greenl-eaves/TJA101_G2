package com.eatfast;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Spring Boot 應用程式部署為 WAR 檔時的入口。 外部 Tomcat 伺服器會透過這個類別來啟動你的應用程式。
 */
public class ServletInitializer extends SpringBootServletInitializer {

	// [不可變] 必須繼承 SpringBootServletInitializer，這是 WAR 部署的關鍵。
	// [不可變] configure 方法名稱和參數都不能改，這是複寫父類別的標準。

	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		// 關鍵：告訴 Spring Boot 哪一個是主啟動類別。
		// [可變] EatfastApplication.class 需對應到你專案的主啟動類別 (有 @SpringBootApplication 的那個)。
		return application.sources(EatfastApplication.class);
	}

}
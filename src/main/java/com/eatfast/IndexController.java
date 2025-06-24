// 檔案路徑: src/main/java/com/eatfast/controller/IndexController.java
// (建議為 Controller 建立一個新的 package，例如 com.eatfast.controller)

package com.eatfast;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首頁控制器 (Index Controller)
 * 專門用來處理對網站根路徑 ("/") 的請求，並回傳首頁。
 */
@Controller
public class IndexController {

    /**
     * 當使用者在瀏覽器輸入 http://localhost:8080/ 時，這個方法會被觸發。
     * @param model - Spring MVC 提供的一個物件，用來將資料從後端傳遞到前端模板。
     * @return 一個字串 "index"，這是視圖的邏輯名稱。Spring Boot 會根據這個名稱，
     * 去 /resources/templates/ 資料夾下尋找名為 index.html 的檔案來渲染。
     */
    @GetMapping("/") // [不可變] 將此方法映射到網站的根路徑。
    public String index(Model model) {

        // --- 準備要傳遞給 index.html 模板的資料 ---

        // 1. 準備 ${message} 的值
        // [可變] 你可以將 "Guest" 替換成任何你想顯示的訊息，例如從登入資訊中取得的使用者名稱。
        model.addAttribute("message", "TJA101-G2");

        // 2. 準備 ${myList} 的值 (開發順序)
        // [可變] 這裡我們建立一個簡單的開發順序列表，你可以根據專案實際情況修改。
        List<String> devSteps = Arrays.asList(
            "建立資料庫與 ER-Model (DataBase / Entity)",
            "建立資料存取層 (Repository)",
            "建立商業邏輯層 (Service)",
            "建立表現層 (Controller)",
            "建立前端視圖 (View - Thymeleaf)",
            "完成！"
        );
        model.addAttribute("myList", devSteps);
        
        // 3. 準備 ${quickstartInfo} 的值 (Quickstart 資訊) - 新增的區塊
        // [可變] 這是在頁面上顯示的 Quickstart 相關資訊列表。
        List<String> quickstartInfo = Arrays.asList(
            "Spring Boot Quickstart 官網 : https://start.spring.io",
            "IDE 開發工具",
            "直接使用(匯入)官方的 Maven Spring-Boot-demo Project + pom.xml",
            "直接使用官方現成的 @SpringBootApplication + SpringBootServletInitializer 組態檔",
            "依賴注入(DI) HikariDataSource (官方建議的連線池)",
            "Thymeleaf",
            "Java WebApp (快速完成 Spring Boot Web MVC)"
        );
        model.addAttribute("quickstartInfo", quickstartInfo);


        // --- 回傳視圖名稱 ---
        return "index"; // [不可變] 這個字串必須與你的模板檔案名稱 (index.html) 一致。
    }
}

package com.eatfast.orderlist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 專門處理訂單相關頁面導向的 Controller
 */
@Controller
@RequestMapping("/orders") // 設定基礎路徑為 /orders
public class OrderViewController {

    /**
     * 導向至訂單管理的主頁面。
     * 當使用者訪問 /orders/list 時，此方法會被觸發。
     * @return Thymeleaf 模板的路徑
     */
    @GetMapping("/list")
    public String showOrderListPage() {
        // 【核心修改】返回的路徑必須與您的檔案結構完全對應
        // Spring Boot 會自動在 src/main/resources/templates/ 資料夾下尋找
        return "back-end/orderlist/listAllOrderList"; // 不需加上 .html
    }
}
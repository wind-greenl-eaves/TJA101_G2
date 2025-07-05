package com.eatfast.member.controller;

/**
 * [常數類別] 專門存放會員功能相關的視圖路徑 (View Paths)。
 * <p>
 * 目的：
 * <ul>
 * <li>將所有視圖路徑集中管理，避免「魔法字串」(Magic String) 散落在 Controller 各處。</li>
 * <li>當未來需要調整視圖檔案的資料夾結構或檔名時，只需修改此檔案一處即可。</li>
 * <li>提升程式碼的可讀性與可維護性。</li>
 * 「魔法字串」// 是指在程式碼中直接使用字串常數，這樣做會降低程式碼的可讀性和可維護性。
 * </ul>
 */
public final class MemberViewConstants { // 使用 final 關鍵字，表示這個類別不應該被繼承

    /**
     * 私有建構子，防止外部透過 new 的方式實例化這個工具類別。
     */
    private MemberViewConstants() {}

    // ============================= 後台管理模板路徑 =============================
    
    /**
     * Thymeleaf 模板的基礎路徑 - 後台管理。
     * <b>不可變:</b> private, static, final 是 Java 關鍵字。
     * <b>可自定義:</b> 變數名稱 BASE_PATH 和路徑字串 "back-end/member/" 可依專案結構修改。
     */
    private static final String BACKEND_BASE_PATH = "back-end/member/";

    /**
     * 會員列表與查詢結果頁面。
     * 對應實體檔案路徑: `resources/templates/back-end/member/select_page_member.html`
     */
    public static final String VIEW_SELECT_PAGE = BACKEND_BASE_PATH + "select_page_member";

    /**
     * 新增會員表單頁面。
     * 對應實體檔案路徑: `resources/templates/back-end/member/addMember.html`
     */
    public static final String VIEW_ADD_MEMBER = BACKEND_BASE_PATH + "addMember";

    /**
     * 修改會員表單頁面。
     * 對應實體檔案路徑: `resources/templates/back-end/member/update_member.html`
     */
    public static final String VIEW_UPDATE_MEMBER = BACKEND_BASE_PATH + "update_member";

    // ============================= 前端會員專區模板路徑 =============================
    
    /**
     * 前端會員專區模板的基礎路徑。
     */
    private static final String FRONTEND_BASE_PATH = "front-end/member/";

    /**
     * 會員專區首頁。
     * 對應實體檔案路徑: `resources/templates/front-end/member/member-dashboard.html`
     */
    public static final String VIEW_MEMBER_DASHBOARD = FRONTEND_BASE_PATH + "member-dashboard";

    /**
     * 個人資料頁面。
     * 對應實體檔案路徑: `resources/templates/front-end/member/member-profile.html`
     */
    public static final String VIEW_MEMBER_PROFILE = FRONTEND_BASE_PATH + "member-profile";

    /**
     * 密碼變更頁面。
     * 對應實體檔案路徑: `resources/templates/front-end/member/change-password.html`
     */
    public static final String VIEW_CHANGE_PASSWORD = FRONTEND_BASE_PATH + "change-password";

    /**
     * 訂單記錄頁面。
     * 對應實體檔案路徑: `resources/templates/front-end/member/member-orders.html`
     */
    public static final String VIEW_MEMBER_ORDERS = FRONTEND_BASE_PATH + "member-orders";

    /**
     * 我的收藏頁面。
     * 對應實體檔案路徑: `resources/templates/front-end/member/member-favorites.html`
     */
    public static final String VIEW_MEMBER_FAVORITES = FRONTEND_BASE_PATH + "member-favorites";

    /**
     * 帳號設定頁面。
     * 對應實體檔案路徑: `resources/templates/front-end/member/member-settings.html`
     */
    public static final String VIEW_MEMBER_SETTINGS = FRONTEND_BASE_PATH + "member-settings";

    /**
     * 會員註冊頁面。
     * 對應實體檔案路徑: `resources/templates/front-end/member/member-register.html`
     */
    public static final String VIEW_MEMBER_REGISTER = FRONTEND_BASE_PATH + "member-register";

    /**
     * 會員驗證頁面。
     * 對應實體檔案路徑: `resources/templates/front-end/member/member-verification.html`
     */
    public static final String VIEW_MEMBER_VERIFICATION = FRONTEND_BASE_PATH + "member-verification";

    // =================================== URL 路徑 (用於 redirect) ===================================

    /**
     * 重定向 (Redirect) 到會員列表頁面的 URL。
     * 此路徑會被 Spring MVC 解讀，觸發瀏覽器向 `/member/select_page` 發送一個新的 GET 請求。
     */
    public static final String REDIRECT_TO_SELECT_PAGE = "redirect:/member/select_page";

    /**
     * 重定向到會員專區首頁。
     */
    public static final String REDIRECT_TO_MEMBER_DASHBOARD = "redirect:/member/dashboard";

    /**
     * 重定向到會員登入頁面。
     */
    public static final String REDIRECT_TO_MEMBER_LOGIN = "redirect:/api/v1/auth/member-login";

    /**
     * 重定向到個人資料頁面。
     */
    public static final String REDIRECT_TO_MEMBER_PROFILE = "redirect:/member/profile";

    /**
     * 重定向到帳號設定頁面。
     */
    public static final String REDIRECT_TO_MEMBER_SETTINGS = "redirect:/member/settings";
}
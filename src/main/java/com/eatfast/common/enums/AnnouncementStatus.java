package com.eatfast.common.enums;

public enum AnnouncementStatus {

    // 我們調整順序，並為每個狀態綁定一個中文名稱
    // 這樣 ordinal 就會是: INACTIVE=0, ACTIVE=1, ARCHIVED=2 (假設有下架狀態)
    INACTIVE("草稿"),
    ACTIVE("上架"),
    ARCHIVED("下架"); // 您可以自行增減

    // 加上一個屬性來儲存中文名稱
    private final String displayName;

    // 建立一個建構子
    AnnouncementStatus(String displayName) {
        this.displayName = displayName;
    }

    // 提供一個 public 方法讓外界可以取得中文名稱
    public String getDisplayName() {
        return displayName;
    }
}

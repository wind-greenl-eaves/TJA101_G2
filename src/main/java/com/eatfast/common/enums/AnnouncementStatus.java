package com.eatfast.common.enums;

/**
 * 門市公告狀態 (公開共享版本)
 * <p>
 * 存放於共通 package，供專案中所有需要的地方引用。
 * 順序維持不變: HIDDEN(0), VISIBLE(1)。
 * </p>
 */
public enum AnnouncementStatus {
    /**
     * 隱藏 (0)
     */
    HIDDEN,
    /**
     * 顯示 (1)
     */
    VISIBLE, INACTIVE, ACTIVE
}

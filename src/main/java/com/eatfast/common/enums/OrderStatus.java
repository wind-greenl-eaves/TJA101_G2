package com.eatfast.common.enums;

/**
 * 訂單狀態 (公開共享版本)
 * <p>
 * 存放於共通 package，供專案中所有需要的地方引用。
 * 順序維持不變: PENDING(0), PREPARING(1), COMPLETED(2), CANCELLED(3)。
 * </p>
 */
public enum OrderStatus {
    /**
     * 待處理 (0)
     */
    PENDING,
    /**
     * 準備中 (1)
     */
    PREPARING,
    /**
     * 已完成 (2)
     */
    COMPLETED,
    /**
     * 已取消 (3)
     */
    CANCELLED
}

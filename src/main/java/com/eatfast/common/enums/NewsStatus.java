package com.eatfast.common.enums;

/**
 * 最新消息狀態 (公開共享版本)
 * <p>
 * ORDINAL 順序: DRAFT 為 0，PUBLISHED 為 1，與資料庫 `TINYINT` 的設計一致。
 */
public enum NewsStatus {
    /**
     * 草稿 (對應 0)
     */
    DRAFT,

    /**
     * 已發布 (對應 1)
     */
    PUBLISHED
}

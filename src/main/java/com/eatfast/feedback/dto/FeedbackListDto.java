package com.eatfast.feedback.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用於後台意見回饋列表的資料傳輸物件 (DTO)
 * 使用 Lombok 的 @Data 註解，自動產生所有 getter, setter 等方法。
 */
@Data
public class FeedbackListDto {

    // == 欄位定義 ==

    private Long feedbackId;
    private Long memberId;
    private String memberName;
    private String phone;
    private String content;
    private LocalDateTime createTime;
    private String status;
    private String diningStore;

    // ✅ 只保留一個 LocalDateTime 型別的 diningTime
    private LocalDateTime diningTime;

    // 注意：底下完全空白！
    // 因為 @Data 註解會幫我們處理好所有事情，不需要再手寫任何 getter 或 setter。
}
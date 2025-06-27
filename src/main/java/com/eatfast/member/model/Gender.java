package com.eatfast.member.model;

/**
 * 性別枚舉 (Enum)
 * <p>
 * 功能：
 * <ul>
 * <li>提供編譯時期的型別安全，避免傳入 "M", "F", "O" 以外的無效字串。</li>
 * <li>取代「魔法字串」(Magic String)，讓程式碼 <code>setGender(Gender.M)</code> 更具可讀性與可維護性。</li>
 * </ul>
 */
public enum Gender {
    /** 男性 */
    M,

    /** 女性 */
    F,

    /** 其他 */
    O
}
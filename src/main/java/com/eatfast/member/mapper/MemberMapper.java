/*
 * ================================================================
 * 檔案 3: Entity 與 DTO 轉換器 (Mapper)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/member/mapper/MemberMapper.java
 * - 作用: 一個專門負責轉換邏輯的工具類。讓 Service 層的程式碼更乾淨，
 * 專注於業務邏輯，而非繁瑣的資料轉換。
 * 使用 `static` 方法讓外部不需建立實例即可呼叫。
 */
package com.eatfast.member.mapper;

import com.eatfast.member.dto.MemberCreateRequest;

import com.eatfast.member.model.MemberEntity;
import org.springframework.security.crypto.password.PasswordEncoder;




public class MemberMapper {

    /**
     * 私有建構子，防止這個工具類被實例化。
     * 不可變動的 Java 語法結構。
     */
    private MemberMapper() {}

    /**
     * 將「新增請求 DTO」轉換為「會員實體」。
     * @param createRequest 可自定義的參數名稱，代表傳入的 DTO。
     * @param passwordEncoder 可自定義的參數名稱，用於加密密碼。
     * @return 一個準備好存入資料庫的 MemberEntity 物件。
     */
    public static MemberEntity toEntity(MemberCreateRequest createRequest, PasswordEncoder passwordEncoder) {
        // `newMember` 是可自定義的變數名稱
        MemberEntity newMember = new MemberEntity();
        newMember.setUsername(createRequest.getUsername());
        newMember.setAccount(createRequest.getAccount());
        // 密碼加密的關鍵步驟
        newMember.setPassword(passwordEncoder.encode(createRequest.getPassword()));
        newMember.setEmail(createRequest.getEmail());
        newMember.setPhone(createRequest.getPhone());
        newMember.setBirthday(createRequest.getBirthday());
        newMember.setGender(createRequest.getGender());
        // isEnabled 預設為 true，無需手動設定
        return newMember;
    }
}
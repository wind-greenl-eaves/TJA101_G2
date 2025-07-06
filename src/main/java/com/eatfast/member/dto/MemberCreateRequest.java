package com.eatfast.member.dto;

import com.eatfast.common.enums.Gender;
import com.eatfast.member.validation.CreateValidation;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * 【路徑】引入必要的外部類別。 - com.eatfast.member.model.Gender: 從 model 層引入 Gender
 * 列舉，確保性別資料的型別安全。 - jakarta.validation.constraints.*: 引入 Jakarta Bean
 * Validation API，用於欄位驗證。 - java.time.LocalDate: 引入 Java 8 的日期 API，是處理日期的最佳實踐。
 */

/*
 * ================================================================
 * 					檔案: 會員註冊請求 DTO (資料傳輸物件)
 * ================================================================
 * - 核心作用:
 * 1. 內外隔離: 作為一個獨立的資料載體，它讓 Controller 層接收的資料格式
 * 與資料庫的 MemberEntity 實體完全解耦。
 * 2. 職責單一: 專門負責承載「新增會員」時所需的資料，並定義對應的欄位驗證規則。
 * 3. 安全性: 只包含客戶端應該提交的欄位，避免惡意使用者透過請求傳遞
 * 不該被設定的欄位 (如 isEnabled, memberId)。
 */

public class MemberCreateRequest {

	// ================================================================
	// 				欄位定義 (Fields) & 驗證規則 (Validations)
	// ================================================================

	/**
	 * 會員姓名。 - @NotBlank: 不可變動的 Validation 關鍵字，驗證字串不能是 null 或只包含空白字元。 - @Size: 不可變動的
	 * Validation 關鍵字，限制字串的長度範圍。 - message: 可自定義的錯誤訊息，當驗證失敗時，會回傳給前端。
	 */
	@NotBlank(message = "會員姓名：請勿空白", groups = CreateValidation.class)
	@Size(min = 2, max = 20, message = "會員姓名：長度必須介於 2 到 20 個字元之間", groups = CreateValidation.class)
	private String username;

	/**
	 * 登入帳號。 - @Pattern: 不可變動的 Validation 關鍵字，使用正規表示式進行格式驗證。
	 */
	@NotBlank(message = "登入帳號：請勿空白", groups = CreateValidation.class)
	@Size(min = 4, max = 50, message = "登入帳號：長度必須介於 4 到 50 個字元之間", groups = CreateValidation.class)
	@Pattern(regexp = "^[a-zA-Z0-9_.]+$", message = "登入帳號：僅能包含英數字、底線及小數點", groups = CreateValidation.class)
	private String account;

	/**
	 * 登入密碼。 註：此處僅驗證格式，加密邏輯應在 Service 層處理。
	 */
	@NotBlank(message = "登入密碼：請勿空白", groups = CreateValidation.class)
	@Size(min = 8, max = 100, message = "登入密碼：長度建議介於 8 到 100 個字元之間", groups = CreateValidation.class)
	private String password;

	/**
	 * 電子郵件。 - @Email: 不可變動的 Validation 關鍵字，驗證字串是否為有效的 Email 格式。
	 */
	@NotBlank(message = "電子郵件：請勿空白", groups = CreateValidation.class)
	@Email(message = "電子郵件：請填寫有效的格式", groups = CreateValidation.class)
	@Size(max = 100, message = "電子郵件：長度不可超過 100 個字元", groups = CreateValidation.class)
	private String email;

	/**
	 * 連絡電話。
	 * 支援台灣常見的手機和市話格式：0912345678, 0912-345-678, 09-12345678, (02)12345678, 02-12345678
	 */
	@NotBlank(message = "連絡電話：請勿空白", groups = CreateValidation.class)
	@Pattern(regexp = "^(\\(0\\d{1,2}\\)|0\\d{1,2})[\\s-]?\\d{3,4}[\\s-]?\\d{3,4}$", 
	         message = "連絡電話：請填寫有效的電話號碼格式（如：0912345678、0912-345-678、02-12345678）", 
	         groups = CreateValidation.class)
	private String phone;

	/**
	 * 會員生日。 - @NotNull: 不可變動的 Validation 關鍵字，驗證物件不能是 null。 - @Past: 不可變動的
	 * Validation 關鍵字，驗證日期必須是過去的時間。
	 */
	@NotNull(message = "會員生日：請勿空白", groups = CreateValidation.class)
	@Past(message = "會員生日：必須為過去的日期", groups = CreateValidation.class)
	private LocalDate birthday;

	/**
	 * 性別。 使用 Enum 型別是確保資料正確性的最佳方式。
	 */
	@NotNull(message = "性別：請勿空白", groups = CreateValidation.class)
	private Gender gender;

	// ================================================================
	// Getters and Setters
	// ================================================================
	// 這些是標準的 Java樣板程式碼 (Boilerplate Code)，
	// 用於讓外部（如 Service 層的 Mapper）能夠存取與設定這些私有欄位的值。

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public LocalDate getBirthday() {
		return birthday;
	}

	public void setBirthday(LocalDate birthday) {
		this.birthday = birthday;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}
}
/*
 * ================================================================
 * 檔案: 密碼更新請求 DTO (資料傳輸物件)
 * ================================================================
 * - 核心作用:
 * 1. 職責單一: 專門承載「變更密碼」操作所需的資料。
 * 2. 安全性: 與一般資料更新 (如修改電話、姓名) 的 DTO 完全分離，
 * 確保密碼變更流程的獨立與安全。
 */

/**
 * 【路徑】com.eatfast.member.dto:
 * 定義此 DTO 屬於 member (會員) 功能模組下的 dto (資料傳輸物件) 層。
 */
package com.eatfast.member.dto;

/**
 * 【路徑】引入 Jakarta Bean Validation API，用於欄位驗證。
 */
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * POJO (Plain Old Java Object)，只負責攜帶「變更密碼」操作所需的資料。
 */
public class PasswordUpdateRequest {

	/**
	 * 會員 ID，用於定位要更新密碼的目標使用者。 - @NotNull: 確保此欄位為必填。
	 */
	@NotNull(message = "會員 ID 不可為空")
	private Long memberId;

	/**
	 * 使用者輸入的舊密碼，用於在 Service 層進行身份驗證。 - @NotBlank: 確保使用者必須提供舊密碼。
	 */
	@NotBlank(message = "舊密碼不可為空")
	private String oldPassword;

	/**
	 * 使用者希望設定的新密碼。 - @Size: 限制新密碼的長度，是基本的密碼策略。
	 */
	@NotBlank(message = "新密碼不可為空")
	@Size(min = 8, max = 100, message = "新密碼長度建議介於 8 到 100 個字元之間")
	private String newPassword;

	/**
	 * 確認新密碼，用於驗證使用者輸入的一致性。
	 */
	@NotBlank(message = "確認密碼不可為空")
	private String confirmPassword;

	// ================================================================
	// Getters and Setters
	// ================================================================
	// 標準 Java 樣板程式碼，用於讓外部 (如 Controller, Service) 存取私有欄位。

	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}
}
package com.eatfast.member.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * ★★★★★★會員資料實體 (Entity) 儲存前台使用者的所有資訊，對應資料庫中的 'member' 資料表。★★★★★
 * @updatable = false 		- 表示此欄位在資料庫中不可被更新 
 * @GeneratedValue			- 表示此欄位的值由資料庫自動生成，通常用於主鍵。 
 * @NotBlank				- 表示此欄位不可為空白，會在驗證時檢查。
 * @Size					- 用於限制字串長度，min 和 max 分別表示最小和最大長度。
 * @nullable = false 		- 表示此欄位在資料庫中不可為 null。
 * @uniqueConstraints		- 用於定義資料表的唯一約束條件，確保特定欄位的值在整個資料表中是唯一的。
 */
@Entity
@Table(name = "member", uniqueConstraints = 
	{ @UniqueConstraint(name = "uk_account", columnNames = "account"),
		@UniqueConstraint(name = "uk_email", columnNames = "email") })
public class MemberEntity {

	/**
	 * 會員編號 (主鍵，自動增長)
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_id")
	private Long memberId;
	/**
	 * 會員真實姓名
	 */
	@NotBlank(message = "使用者姓名不可為空")
	@Size(min = 2, max = 20, message = "使用者姓名長度必須在 2 到 20 個字之間")
	@Column(name = "username", nullable = false, length = 20)
	private String username;
	/**
	 * 登入帳號 (使用者自訂，不可重複) 為業務主鍵 (Business Key)，用於判斷物件邏輯相等性。
	 */
	@NotBlank(message = "登入帳號不可為空")
	@Size(min = 4, max = 50, message = "登入帳號長度必須在 4 到 50 個字之間")
	@Column(name = "account", nullable = false, updatable = false, length = 50)
	private String account;
	/**
	 * 登入密碼 (應儲存經過加密後的雜湊值)
	 */
	@NotBlank(message = "密碼不可為空")
	@Size(min = 8, message = "密碼長度至少需要 8 個字元")
	@Column(name = "password", nullable = false, length = 255)
	private String password;
	/**
	 * 電子郵件 (用於帳號驗證、忘記密碼等，不可重複) 為業務主鍵 (Business Key)，用於判斷物件邏輯相等性。
	 */
	@NotBlank(message = "電子郵件不可為空")
	@Email(message = "電子郵件格式不正確")
	@Column(name = "email", nullable = false, length = 100)
	private String email;
	/**
	 * 連絡電話 (用於訂單聯絡等)
	 */
	@NotBlank(message = "連絡電話不可為空")
	@Column(name = "phone", nullable = false, length = 20)
	private String phone;
	/**
	 * 會員生日
	 */
	@NotNull(message = "生日不可為空")
	@Past(message = "生日必須是過去的日期")
	@Column(name = "birthday", nullable = false)
	private LocalDate birthday;
	/**
	 * 資料最後更新時間 (當此筆記錄有任何變動時，會自動更新為當前時間)
	 */
	@UpdateTimestamp
	@Column(name = "last_updated_at", nullable = false)
	private LocalDateTime lastUpdatedAt;
	/**
	 * 帳號註冊時間 (預設為新增記錄的當下時間)
	 */
	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	/**
	 * JPA 規範要求必須提供一個無參數的建構子， 以便框架透過反射機制建立物件實例。
	 */
	public MemberEntity() {
	}
	/**
	 * 提供一個有參數的建構子，方便在業務邏輯或測試中快速建立新物件。 
	 * 只包含 "建立時" 就必須提供的欄位，像 memberId, createdAt 等由系統自動生成的欄位不需要放入。
	 */
	public MemberEntity(String username, String account, String password, String email, String phone,
			LocalDate birthday) {
		this.username = username;
		this.account = account;
		this.password = password;
		this.email = email;
		this.phone = phone;
		this.birthday = birthday;
	}

	// --- Getters and Setters ---

	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAccount() {
		return account;
	}

	// 將 setter 設為 private，只允許在物件內部使用
	@SuppressWarnings("unused")
	private void setAccount(String account) {
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

	public LocalDateTime getLastUpdatedAt() {
		return lastUpdatedAt;
	}

	public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
		this.lastUpdatedAt = lastUpdatedAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	// 將 setter 設為 private，只允許在物件內部使用
	@SuppressWarnings("unused")
	private void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	// --- Object method overrides ---

	@Override
	public String toString() {
	    return "MemberEntity{" +
	            "memberId=" + memberId +
	            ", username='" + username + '\'' +
	            ", account='" + account + '\'' +
	            ", email='" + email + '\'' +
	            // 不包含 password 欄位
	            ", phone='" + phone + '\'' +
	            ", birthday=" + birthday +
	            ", lastUpdatedAt=" + lastUpdatedAt +
	            ", createdAt=" + createdAt +
	            '}';
	}
	/**
	 * 覆寫 equals() 方法，定義物件的「邏輯相等性」。 
	 * 最佳實踐：僅使用穩定不變的「業務主鍵」(Business Key) 進行比較， 
	 * 例如 `account` 和 `email`，因為它們在業務上具有唯一性。 如此可確保無論物件是否已存入資料庫，比較邏輯都保持一致。
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		MemberEntity that = (MemberEntity) o;
		// 只比較 account 和 email，它們是業務上的唯一識別碼
		return Objects.equals(account, that.account) && Objects.equals(email, that.email);
	}
	/**
	 * 覆寫 hashCode() 方法，回傳物件的雜湊碼。 
	 * 根據 Java 規範，如果 equals() 方法比較結果為 true，則 hashCode()必須回傳相同的值。
	 * 因此，這裡使用的欄位 (`account`, `email`) 必須與 equals() 方法中完全一致。
	 */
	@Override
	public int hashCode() {
		// 只使用與 equals() 方法相同的欄位來計算 hash code
		return Objects.hash(account, email);
	}
}

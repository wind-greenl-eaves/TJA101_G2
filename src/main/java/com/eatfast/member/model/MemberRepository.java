package com.eatfast.member.model;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * ★★★★★★會員資料存取庫 (Repository) 繼承 JpaRepository 來獲得標準的 CRUD 功能。 使用原生 SQL 查詢。★★★★★
 * ★★★★★★額外繼承 JpaSpecificationExecutor 以支援動態查詢。★★★★★
 * @Repository 				- 標記這是一個資料存取層的組件，Spring 會自動處理例外轉換。
 * @Transactional 			- 確保操作的原子性。
 * @Modifying 				- 標記此為一個修改型操作 (UPDATE, DELETE)。
 * @Query 					- 用於定義自訂的 SQL 查詢。
 * @nativeQuery = true 		- 表示使用原生 SQL 查詢。
 * @Optional<T> 			- Spring Data JPA 的 Optional 包裝類型，用於處理可能不存在的查詢結果(NullPointerException)。
 * @JpaSpecificationExecutor<T> - 允許使用動態查詢條件，提供更靈活的查詢方式。
 */
@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long>,JpaSpecificationExecutor<MemberEntity> {
	
	// 【R - 讀取】根據登入帳號查詢會員 。
	@Query(value = "SELECT * FROM member WHERE account = :account", nativeQuery = true) 
	Optional<MemberEntity> findByAccount(@Param("account") String account);
	/**
	 * 【R - 讀取】根據電子郵件查詢會員 。
	 */
	@Query(value = "SELECT * FROM member WHERE email = :email", nativeQuery = true)
	Optional<MemberEntity> findByEmail(@Param("email") String email);
	/**
	 * 【R - 檢查存在】檢查指定的帳號或信箱是否存在。 
	 * 使用 `SELECT EXISTS(...)`，效能比 `SELECT COUNT(*)` 更好，因為資料庫找到第一筆匹配的資料後就會立刻回傳，不會繼續掃描。
	 */
	@Query(value = "SELECT EXISTS(SELECT 1 FROM member WHERE account = :account OR email = :email)", nativeQuery = true)
	boolean existsByAccountOrEmail(@Param("account") String account, @Param("email") String email);
	/**
	 * 【D - 刪除】根據登入帳號刪除會員 。
	 */
	@Transactional
	@Modifying
	@Query(value = "DELETE FROM member WHERE account = :account", nativeQuery = true)
	int deleteByAccount(@Param("account") String account);
	/**
	 * 【U - 更新】根據會員 ID 更新手機號碼 。 回傳 int 代表成功更新的資料筆數。
	 */
	@Transactional
	@Modifying
	@Query(value = "UPDATE member SET phone = :phone WHERE member_id = :memberId", nativeQuery = true)
	int updatePhoneById(@Param("memberId") Long memberId, @Param("phone") String phone);
}

	//對於簡單的 find, exists, delete 操作，完全不需要寫 @Query。
	//只要方法名稱遵循 Spring Data JPA 的命名規則，框架就能自動實現它
	//例如：findByAccount, existsByEmail, deleteById 等等。 
	//此頁面目前還是把SQL查詢語句寫在 @Query 中，主要是為了學習和方便觀察。
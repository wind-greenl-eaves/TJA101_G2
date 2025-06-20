package com.eatfast.member.model;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 會員資料存取庫 (Repository) 繼承 JpaRepository 來獲得標準的 CRUD 功能。 使用原生 SQL 查詢。
 */
@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

	// 【R - 讀取】根據登入帳號查詢會員 。
	@Query(value = "SELECT * FROM member WHERE account = :account", nativeQuery = true)
	Optional<MemberEntity> findByAccount(@Param("account") String account);

	/**
	 * 【R - 讀取】根據電子郵件查詢會員 。
	 */
	@Query(value = "SELECT * FROM member WHERE email = :email", nativeQuery = true)
	Optional<MemberEntity> findByEmail(@Param("email") String email);

	/**
	 * 【R - 檢查存在】檢查指定的帳號或信箱是否存在。 使用 `SELECT EXISTS(...)`，它的效能比 `SELECT COUNT(*)` 更好，
	 * 因為資料庫找到第一筆匹配的資料後就會立刻回傳，不會繼續掃描。
	 */
	@Query(value = "SELECT EXISTS(SELECT 1 FROM member WHERE account = :account OR email = :email)", nativeQuery = true)
	boolean existsByAccountOrEmail(@Param("account") String account, @Param("email") String email);

	/**
	 * 【D - 刪除】根據登入帳號刪除會員 (使用原生 SQL)。
	 * 
	 * @Modifying - 標記此為一個修改型操作 (UPDATE, DELETE)。
	 * @Transactional - 確保操作的原子性。
	 */
	@Transactional
	@Modifying
	@Query(value = "DELETE FROM member WHERE account = :account", nativeQuery = true)
	int deleteByAccount(@Param("account") String account);

	/**
	 * 【U - 更新】根據會員 ID 更新手機號碼 (使用原生 SQL)。 回傳 int 代表成功更新的資料筆數。
	 */
	@Transactional
	@Modifying
	@Query(value = "UPDATE member SET phone = :phone WHERE member_id = :memberId", nativeQuery = true)
	int updatePhoneById(@Param("memberId") Long memberId, @Param("phone") String phone);

}
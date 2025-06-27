package com.eatfast.member.repository;

import com.eatfast.member.model.MemberEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// ●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●會員資料存取層 (Repository)●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●
// 負責與資料庫進行交互，提供 CRUD 操作和自定義查詢方法。
// 使用 Spring Data JPA 的 JpaRepository 和 JpaSpecificationExecutor 來簡化資料存取操作。
// JpaRepository 提供基本的 CRUD 操作，而 JpaSpecificationExecutor 則允許使用動態查詢。
//●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long>, JpaSpecificationExecutor<MemberEntity> {

	// findByAccount 是框架關鍵字，findBy 後面的 Account 對應 MemberEntity 的 account 屬性
	Optional<MemberEntity> findByAccount(String account);

	// 【R - 讀取】Spring Data JPA 會自動實現 "依 email 查詢"
	Optional<MemberEntity> findByEmail(String email);

	// 【R - 檢查存在】Spring Data JPA 會自動實現 "依 account 或 email 檢查是否存在"
	// 框架會自動生成高效的 EXISTS 查詢
	boolean existsByAccountOrEmail(String account, String email);

	// 【D - 硬刪除】Spring Data JPA 也支援衍生刪除方法 但這樣的寫法會執行物理刪除，與 Entity 的軟刪除邏輯衝突。
	// @Transactional 建議移至 Service 層統一管理交易範圍
//	@Modifying // 對於非 SELECT 操作，@Modifying 是必要的
//	int deleteByAccount(String account);

	// 【D - 軟刪除】
	// 原本的 deleteByAccount 會執行物理刪除，與 Entity 的軟刪除邏輯衝突。
	// 為使用 JPQL 執行軟刪除（更新 is_enabled 旗標），確保刪除行為一致。
	@Modifying(clearAutomatically = true)
	@Query("UPDATE MemberEntity m SET m.isEnabled = false WHERE m.account = :account")
	int softDeleteByAccount(@Param("account") String account);

	// 【U - 更新】
	// 加上 clearAutomatically = true，在執行更新後自動清除持久性上下文，避免記憶體中的 Entity 物件與資料庫狀態不一致。
	// 不可變：UPDATE, SET, WHERE 是 JPQL 關鍵字
	@Modifying(clearAutomatically = true)
	@Query("UPDATE MemberEntity m SET m.phone = :phone WHERE m.memberId = :memberId")
	int updatePhoneById(@Param("memberId") Long memberId, @Param("phone") String phone);

	// 不受軟刪除限制，查詢會員是否存在 (包含已停用的)
	// ★注意：這是原生 SQL 查詢，因為 JPQL 會被 @SQLRestriction 影響。
	// value = "SELECT * FROM member WHERE account = :account LIMIT 1" -> 直接下令給資料庫查詢
	// nativeQuery = true -> 告訴 JPA 這是原生 SQL，請直接執行
	@Query(value = "SELECT * FROM member WHERE account = :account LIMIT 1", nativeQuery = true)
	Optional<MemberEntity> findByAccountIncludeDisabled(@Param("account") String account);

	@Query(value = "SELECT * FROM member WHERE email = :email LIMIT 1", nativeQuery = true)
	Optional<MemberEntity> findByEmailIncludeDisabled(@Param("email") String email);

}
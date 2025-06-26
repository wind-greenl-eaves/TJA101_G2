// MemberRepository.java (After Refactoring)
package com.eatfast.member.repository;

import com.eatfast.member.model.MemberEntity; // 確保引入 MemberEntity
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
// import org.springframework.transaction.annotation.Transactional; // @Transactional 通常建議放在 Service 層管理

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long>, JpaSpecificationExecutor<MemberEntity> {

	// 【R - 讀取】Spring Data JPA 會自動實現 "依 account 查詢"
	// 不可變：findByAccount 是框架關鍵字，findBy 後面的 Account 對應 MemberEntity 的 account 屬性
	Optional<MemberEntity> findByAccount(String account);

	// 【R - 讀取】Spring Data JPA 會自動實現 "依 email 查詢"
	Optional<MemberEntity> findByEmail(String email);

	// 【R - 檢查存在】Spring Data JPA 會自動實現 "依 account 或 email 檢查是否存在"
	// 框架會自動生成高效的 EXISTS 查詢
	boolean existsByAccountOrEmail(String account, String email);

	// 【D - 刪除】Spring Data JPA 也支援衍生刪除方法
	// @Transactional 建議移至 Service 層統一管理交易範圍
	@Modifying // 對於非 SELECT 操作，@Modifying 是必要的
	int deleteByAccount(String account);

	// 【U - 更新】對於自訂更新，建議使用 JPQL (Java Persistence Query Language) 而非原生 SQL
	// 這樣能保持資料庫可移植性，同時 m.phone 和 m.memberId 會受到編譯期檢查
	// 不可變：UPDATE, SET, WHERE 是 JPQL 關鍵字
	// 可變：MemberEntity, m, m.phone, m.memberId, :phone, :memberId 是根據您的模型和參數定義的
	@Modifying
	@Query("UPDATE MemberEntity m SET m.phone = :phone WHERE m.memberId = :memberId")
	int updatePhoneById(@Param("memberId") Long memberId, @Param("phone") String phone);
}
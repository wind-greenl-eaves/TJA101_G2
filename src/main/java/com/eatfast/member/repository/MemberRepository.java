package com.eatfast.member.repository;

import com.eatfast.member.model.MemberEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * 會員資料存取層 (Repository) - 【已加上專業優化選項】
 *
 * 核心功能:
 * 1.  CRUD 與動態查詢: 繼承 JpaRepository 與 JpaSpecificationExecutor。
 * 2.  混合查詢策略: 靈活運用衍生查詢、JPQL 與原生 SQL。
 * 3.  效能優化 - Projections:【新增】提供僅查詢部分欄位的查詢方法，減少資料庫負載。
 * 4.  效能優化 - EntityGraph:【新增】提供另一種解決 N+1 查詢問題的精準方案。
 */

// ●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●會員資料存取層 (Repository)●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●
// 負責與資料庫進行交互，提供 CRUD 操作和自定義查詢方法。
// 使用 Spring Data JPA 的 JpaRepository 和 JpaSpecificationExecutor 來簡化資料存取操作。
// JpaRepository 提供基本的 CRUD 操作，而 JpaSpecificationExecutor 則允許使用動態查詢。
//●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●
 

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long>, JpaSpecificationExecutor<MemberEntity> {

    //================================================================
    // 				   標準衍生查詢 (Derived Queries)
    //================================================================
    
    // findByAccount 是框架關鍵字，findBy 後面的 Account 對應 MemberEntity 的 account 屬性
	Optional<MemberEntity> findByAccount(String account);

	// 【R - 讀取】Spring Data JPA 會自動實現 "依 email 查詢"
	Optional<MemberEntity> findByEmail(String email);

	// 【R - 讀取】Spring Data JPA 會自動實現 "依 phone 查詢" - 支援即時驗證功能
	Optional<MemberEntity> findByPhone(String phone);

	// 【R - 檢查存在】Spring Data JPA 會自動實現 "依 account 或 email 檢查是否存在"
	// 框架會自動生成高效的 EXISTS 查詢
	boolean existsByAccountOrEmail(String account, String email);

	/**
	 * 【新增】查詢已停用的會員 - 使用原生 SQL 繞過 @SQLRestriction 限制
	 * 這個方法專門用於查詢 is_enabled = false 的會員記錄
	 */
	@Query(value = "SELECT * FROM member WHERE is_enabled = false ORDER BY last_updated_at DESC", nativeQuery = true)
	List<MemberEntity> findDisabledMembers();

	/**
	 * 檢查帳號是否已存在 - 用於即時驗證
	 * @param account 要檢查的帳號
	 * @return true 如果帳號已存在
	 */
    boolean existsByAccount(String account);
    
    /**
     * 檢查電子郵件是否已存在 - 用於即時驗證
     * @param email 要檢查的電子郵件
     * @return true 如果電子郵件已存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 檢查手機號碼是否已存在 - 用於即時驗證
     * @param phone 要檢查的手機號碼
     * @return true 如果手機號碼已存在
     */
    boolean existsByPhone(String phone);

    /**
     * 【新增】根據性別查詢會員列表
     * @param gender 性別枚舉值
     * @return 指定性別的會員列表
     */
    List<MemberEntity> findByGender(com.eatfast.common.enums.Gender gender);

    /**
     * 【新增】查詢手機號碼（包括已停用會員）- 支援即時驗證功能
     * @param phone 手機號碼
     * @return 會員資料的 Optional
     */
    @Query(value = "SELECT * FROM member WHERE phone = :phone LIMIT 1", nativeQuery = true)
    Optional<MemberEntity> findByPhoneIncludeDisabled(@Param("phone") String phone);

    //================================================================
    // 				   自定義 JPQL 與原生 SQL 查詢
    //================================================================

	// 【D - 軟刪除】
	// 使用 JPQL 執行軟刪除（更新 is_enabled 旗標），確保刪除行為與 Entity 的 @SQLDelete 一致。
	@Modifying(clearAutomatically = true)
	@Query("UPDATE MemberEntity m SET m.isEnabled = false WHERE m.account = :account")
	int softDeleteByAccount(@Param("account") String account);

	// 【U - 更新】
	// 加上 clearAutomatically = true，在執行更新後自動清除持久性上下文，避免快取不一致。
	@Modifying(clearAutomatically = true)
	@Query("UPDATE MemberEntity m SET m.phone = :phone WHERE m.memberId = :memberId")
	int updatePhoneById(@Param("memberId") Long memberId, @Param("phone") String phone);

	// 【R - 繞過軟刪除限制】
	// ★注意：這是原生 SQL 查詢，因為 JPQL 會被 @SQLRestriction 影響。
	@Query(value = "SELECT * FROM member WHERE account = :account LIMIT 1", nativeQuery = true)
	Optional<MemberEntity> findByAccountIncludeDisabled(@Param("account") String account);

	@Query(value = "SELECT * FROM member WHERE email = :email LIMIT 1", nativeQuery = true)
	Optional<MemberEntity> findByEmailIncludeDisabled(@Param("email") String email);


    //================================================================
    // 				   【專業優化 1: Projections (投影)】
    //================================================================
    
    /**
     * 【投影介面】只定義我們需要的欄位 Getter。
     * Spring Data JPA 會自動為這個介面產生實作，並只查詢對應的欄位。
     */
    interface MemberSummaryProjection {
        Long getMemberId();
        String getUsername();
        String getEmail();
    }
    
    /**
     * 使用投影查詢，僅回傳會員的摘要資訊。
     * - 回傳型別為 List<MemberSummaryProjection> 而非 List<MemberEntity>。
     * - JPA 將產生類似 "SELECT m.member_id, m.username, m.email FROM member m WHERE m.username LIKE ?" 的高效 SQL。
     * @param username 用於模糊查詢的會員姓名。
     * @return 只包含部分欄位的會員資料列表。
     */
    List<MemberSummaryProjection> findByUsernameContaining(String username);


    //================================================================
    // 				   【專業優化 2: EntityGraph】
    //================================================================
    
    /**
     * 透過 EntityGraph 定義一條「預先抓取路徑 (Fetch Path)」。
     * 依帳號查詢單一會員，並「立即」抓取其所有訂單。
     * - @EntityGraph: (不可變的 JPA 關鍵字) 核心優化註解。
     * - attributePaths = {"orders"}: (不可變動的邏輯配對)
     * 明確指定要「立即載入」的屬性名稱，此處的 "orders" 必須與 MemberEntity 中的 `private Set<OrderListEntity> orders` 屬性名完全一致。
     * - JPA 將產生一條包含 `LEFT JOIN` 的 SQL，在一次查詢中同時取回會員與其所有訂單的資料，徹底解決此查詢的 N+1 問題。
     * @param account 會員帳號。
     * @return 包含已載入訂單的會員 Optional 物件。
     */
    @EntityGraph(attributePaths = {"orders", "favorites"}) // 可以同時抓取多個關聯
    Optional<MemberEntity> findOneWithDetailsByAccount(String account);

    /**
     * 【新增】查詢會員資料（包括已停用的）- 使用原生 SQL 繞過 @SQLRestriction
     * @param memberId 會員ID
     * @return 會員資料的 Optional
     */
    @Query(value = "SELECT * FROM member WHERE member_id = :memberId LIMIT 1", nativeQuery = true)
    Optional<MemberEntity> findByIdIncludeDisabled(@Param("memberId") Long memberId);

    /**
     * 檢查電子郵件是否被其他會員使用（排除特定會員ID）- 用於即時驗證
     * @param email 要檢查的電子郵件
     * @param excludeMemberId 要排除的會員ID
     * @return true 如果電子郵件被其他會員使用
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MemberEntity m WHERE m.email = :email AND m.memberId != :excludeMemberId AND m.isEnabled = true")
    boolean existsByEmailAndMemberIdNotAndIsEnabledTrue(@Param("email") String email, @Param("excludeMemberId") Long excludeMemberId);

    /**
     * 檢查電話號碼是否被其他會員使用（排除特定會員ID）- 用於即時驗證
     * @param phone 要檢查的電話號碼
     * @param excludeMemberId 要排除的會員ID
     * @return true 如果電話號碼被其他會員使用
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MemberEntity m WHERE m.phone = :phone AND m.memberId != :excludeMemberId AND m.isEnabled = true")
    boolean existsByPhoneAndMemberIdNotAndIsEnabledTrue(@Param("phone") String phone, @Param("excludeMemberId") Long excludeMemberId);
}
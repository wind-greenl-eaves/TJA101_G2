package com.eatfast.fav.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FavRepository extends JpaRepository<FavEntity, Long> {
	
	// 查會員所有收藏（不管餐點下架沒）
	List<FavEntity> findByMemberMemberId(Long memberId);

	// 查會員收藏的餐點（只查上架的）
	@Query("SELECT f FROM FavEntity f JOIN FETCH f.meal m WHERE f.member.memberId = :memberId AND m.status = :status")
	List<FavEntity> findByMemberIdAndMealStatus(Long memberId, com.eatfast.common.enums.MealStatus status);

	// 刪除會員收藏的餐點
	void deleteByMemberMemberIdAndMealMealId(Long memberId, Long mealId);

	// 檢查會員是否已收藏指定餐點
    boolean existsByMemberMemberIdAndMealMealId(Long memberId, Long mealId);
    
    // 加入或移除收藏時，判斷是否已收藏（避免重複收藏）
    // 前台愛心圖示的顯示邏輯
    // 根據會員ID和餐點ID查詢是否有收藏
    Optional<FavEntity> findByMemberMemberIdAndMealMealId(Long memberId, Long mealId);
}

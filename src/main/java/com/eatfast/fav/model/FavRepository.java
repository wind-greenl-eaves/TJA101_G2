package com.eatfast.fav.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavRepository extends JpaRepository<FavEntity, Long> {
	
	// 查詢
	List<FavEntity> findByMemberMemberId(Long memberId);

	// 刪除指定會員收藏的餐點
	void deleteByMemberMemberIdAndMealMealId(Long memberId, Long mealId);

	// 檢查指定會員是否已收藏指定餐點
    boolean existsByMemberMemberIdAndMealMealId(Long memberId, Long mealId);
}

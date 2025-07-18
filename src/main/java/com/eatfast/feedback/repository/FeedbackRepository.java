package com.eatfast.feedback.repository;

import com.eatfast.feedback.model.FeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {

	// Spring Data JPA 會根據方法名稱自動生成查詢
	// 由於 FeedbackEntity 中的欄位是 "memberId"，所以方法名稱是 findByMemberId
	List<FeedbackEntity> findByMemberId(Long memberId);

	// 同理，對應 "storeId" 欄位
	List<FeedbackEntity> findByStoreId(Long storeId);
}

package com.eatfast.feedback.model;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {
    // 這裡可以加上自訂查詢方法
	List<FeedbackEntity> findByMember_MemberId(Long memberId);
	//針對會員提出的回饋做查詢
	List<FeedbackEntity> findByStore_StoreId(Long storeId);
	//針對分店提出的回饋做查詢
}

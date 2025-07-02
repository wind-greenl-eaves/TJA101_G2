/*
 * ================================================================
 * 檔案 2: OrderListRepository.java (★★ 核心修正 ★★)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/orderlist/repository/OrderListRepository.java
 */
package com.eatfast.orderlist.repository;

import com.eatfast.member.model.MemberEntity;
import com.eatfast.orderlist.model.OrderListEntity;
import com.eatfast.orderlist.model.OrderStatus; // 【新】引入 OrderStatus Enum
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderListRepository extends JpaRepository<OrderListEntity, String> {
    
    // 【修正】: 參數型別從 Long 改為 OrderStatus Enum，與 Entity 保持一致。
    List<OrderListEntity> findByOrderStatus(OrderStatus orderStatus);

    List<OrderListEntity> findByMemberOrderByOrderDateDesc(MemberEntity member);

    // 【修正】: 參數型別從 Long 改為 OrderStatus Enum。
    List<OrderListEntity> findByMemberAndOrderStatus(MemberEntity member, OrderStatus orderStatus);
}

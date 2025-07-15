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
import com.eatfast.store.model.StoreEntity; // 【新增】引入 StoreEntity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderListRepository extends JpaRepository<OrderListEntity, String> {
    
    // 【修正】: 參數型別從 Long 改為 OrderStatus Enum，與 Entity 保持一致。
    List<OrderListEntity> findByOrderStatus(OrderStatus orderStatus);

    List<OrderListEntity> findByMemberOrderByOrderDateDesc(MemberEntity member);

    // 【修正】: 參數型別從 Long 改為 OrderStatus Enum。
    List<OrderListEntity> findByMemberAndOrderStatus(MemberEntity member, OrderStatus orderStatus);
    
    // 【新增】根據會員ID查詢訂單並按日期降序排列 - 解決 ERR_INCOMPLETE_CHUNKED_ENCODING 錯誤
    List<OrderListEntity> findByMemberMemberIdOrderByOrderDateDesc(Long memberId);
    
    // 【新增】根據會員ID和訂單狀態查詢訂單
    List<OrderListEntity> findByMemberMemberIdAndOrderStatus(Long memberId, OrderStatus orderStatus);
    
    // 【新增】按門市過濾訂單的方法
    List<OrderListEntity> findByStoreOrderByOrderDateDesc(StoreEntity store);
    
    // 【新增】按門市ID過濾訂單的方法
    List<OrderListEntity> findByStore_StoreIdOrderByOrderDateDesc(Long storeId);
    
    // 【新增】按門市和訂單狀態過濾的方法
    List<OrderListEntity> findByStoreAndOrderStatus(StoreEntity store, OrderStatus orderStatus);
    
    // 【新增】根據訂單編號前綴查詢最大訂單編號
    Optional<OrderListEntity> findTopByOrderListIdStartingWithOrderByOrderListIdDesc(String prefix);
}
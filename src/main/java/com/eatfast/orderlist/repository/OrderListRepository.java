package com.eatfast.orderlist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.eatfast.member.model.MemberEntity;
import com.eatfast.orderlist.model.OrderListEntity;

/**
 * OrderListEntity 的 Repository 介面。
 * 繼承 JpaRepository 來獲得所有標準的資料庫操作方法。
 */
@Repository // 📌【不可變】建議加上此註解，明確標示這是一個由 Spring 管理的 Repository Bean。
public interface OrderListRepository extends JpaRepository<OrderListEntity, String> {
    // 🔹【可自定義】介面名稱通常是 "Entity名稱" + "Repository"。
    // 📌【不可變】JpaRepository<OrderListEntity, String>
    //            第一個參數 OrderListEntity: 指定這個 Repository 是為哪個 Entity 服務的。
    //            第二個參數 String:          指定這個 Entity 的主鍵(Primary Key)是什麼型別。
    //                                      (在 OrderListEntity 中，主鍵 orderListId 的型別是 String)。

    // --- 以下是 Spring Data JPA 的神奇之處 ---
    // 你不需要寫任何 SQL！只需要根據規範定義方法名稱，Spring 就會自動幫你產生查詢。

    /**
     * 根據訂單狀態查詢訂單列表。
     * 方法名稱 findByOrderStatus 會被 Spring Data JPA 自動解析為：
     * SELECT * FROM order_list WHERE order_status = ?
     * @param orderStatus 訂單狀態
     * @return 符合條件的訂單列表
     */
    List<OrderListEntity> findByOrderStatus(Long orderStatus);

    /**
     * 根據會員查詢其所有訂單，並依照訂單時間降序排列。
     * 方法名稱 findByMemberOrderByOrderDateDesc 會被解析為：
     * SELECT * FROM order_list WHERE member_id = ? ORDER BY order_date DESC
     * @param member 會員物件
     * @return 該會員的所有訂單列表
     */
    List<OrderListEntity> findByMemberOrderByOrderDateDesc(MemberEntity member);

    /**
     * 查詢特定會員的特定狀態的訂單。
     * 方法名稱 findByMemberAndOrderStatus 會被解析為：
     * SELECT * FROM order_list WHERE member_id = ? AND order_status = ?
     * @param member 會員物件
     * @param orderStatus 訂單狀態
     * @return 符合條件的訂單列表
     */
    List<OrderListEntity> findByMemberAndOrderStatus(MemberEntity member, Long orderStatus);

}
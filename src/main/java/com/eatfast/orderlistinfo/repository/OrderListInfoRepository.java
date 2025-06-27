package com.eatfast.orderlistinfo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eatfast.orderlist.model.OrderListEntity;
import com.eatfast.orderlistinfo.model.OrderListInfoEntity;

/**
 * OrderListInfoEntity 的 Repository 介面。
 * 繼承 JpaRepository 來獲得所有標準的資料庫操作方法。
 */
@Repository // 📌【不可變】建議加上此註解，明確標示這是一個由 Spring 管理的 Repository Bean。
public interface OrderListInfoRepository extends JpaRepository<OrderListInfoEntity, Long> {
    // 🔹【可自定義】介面名稱通常是 "Entity名稱" + "Repository"。
    // 📌【不可變】JpaRepository<OrderListInfoEntity, Long>
    //            第一個參數 OrderListInfoEntity: 指定這個 Repository 是為哪個 Entity 服務的。
    //            第二個參數 Long:                 指定這個 Entity 的主鍵(Primary Key)是什麼型別。
    //                                          (在 OrderListInfoEntity 中，主鍵 orderListInfoId 的型別是 Long)。

    // --- 以下為根據實務需求，可能會用到的客製化查詢方法 ---
    // 您只需要定義方法，Spring Data JPA 會自動幫您實現查詢邏輯。

    /**
     * 根據「訂單主檔物件」查詢其所有的訂單明細。
     * 這是最常用的一種查詢。
     * 方法名稱 findByOrderList 會被解析為：
     * SELECT * FROM order_list_info WHERE order_list_id = ?
     * @param orderList 訂單主檔的 Entity 物件
     * @return 該訂單的所有明細列表
     */
    List<OrderListInfoEntity> findByOrderList(OrderListEntity orderList);

    /**
     * 根據「訂單ID」查詢其所有的訂單明細。
     * 這是另一種更直接的查詢方式，展示了 Spring Data JPA 查詢巢狀屬性的能力。
     * 方法名稱中的 "OrderList_OrderListId" 中的底線(_)代表向下查詢一層。
     * 它會被解析為：
     * SELECT * FROM order_list_info WHERE order_list_id = ?
     * @param orderListId 訂單的 ID (字串)
     * @return 該訂單的所有明細列表
     */
    List<OrderListInfoEntity> findByOrderList_OrderListId(String orderListId);
    
    /**
     * 查詢某張訂單中，尚未給予評論的餐點明細。
     * 這是一個多條件查詢的範例。
     * 方法名稱 findByOrderListAndReviewStars 會被解析為：
     * SELECT * FROM order_list_info WHERE order_list_id = ? AND review_stars = ?
     * @param orderList 訂單主檔的 Entity 物件
     * @param reviewStars 評論星等 (例如傳入 0 代表尚未評論)
     * @return 符合條件的訂單明細列表
     */
    List<OrderListInfoEntity> findByOrderListAndReviewStars(OrderListEntity orderList, Long reviewStars);

}
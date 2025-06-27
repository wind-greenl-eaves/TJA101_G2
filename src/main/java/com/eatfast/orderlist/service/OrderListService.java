package com.eatfast.orderlist.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eatfast.member.model.MemberEntity;
import com.eatfast.orderlist.model.OrderListEntity;
import com.eatfast.orderlist.repository.OrderListRepository;

/**
 * 處理訂單相關商業邏輯的 Service 層。
 */
@Service // 📌【不可變】聲明這是一個 Service 元件，交由 Spring 管理。
public class OrderListService { // 🔹【可自定義】類別名稱

    // 依賴注入 (DI): 將 Repository 管家注入到 Service 經理中
    private final OrderListRepository orderListRepository;

    @Autowired // 📌【不可變】建議使用建構子注入，讓依賴關係更清晰且不可變。
    public OrderListService(OrderListRepository orderListRepository) {
        this.orderListRepository = orderListRepository;
    }

    /**
     * 建立一筆新的訂單。
     * 這裡可以加入更多商業邏輯，例如：檢查庫存、驗證使用者資格等。
     * @param order 準備要儲存的訂單物件
     * @return 已儲存的訂單物件 (包含由資料庫生成的資訊)
     */
    @Transactional // 📌【不可變】建議在會修改資料的方法上加上此註解，確保資料一致性。
    public OrderListEntity createOrder(OrderListEntity order) {
        // 目前只做簡單的儲存，未來可在此擴充商業邏輯
        return orderListRepository.save(order);
    }

    /**
     * 根據訂單ID查詢單筆訂單。
     * @param orderId 訂單的ID (主鍵)
     * @return 包含訂單的 Optional 物件，如果找不到則為空。
     */
    public Optional<OrderListEntity> getOrderById(String orderId) {
        return orderListRepository.findById(orderId);
    }

    /**
     * 根據會員查詢其所有訂單，並按日期排序。
     * @param member 會員物件
     * @return 該會員的訂單列表
     */
    public List<OrderListEntity> getOrdersByMember(MemberEntity member) {
        // 直接呼叫 Repository 定義好的方法
        return orderListRepository.findByMemberOrderByOrderDateDesc(member);
    }

    /**
     * 更新訂單狀態。
     * 這是一個典型的商業邏輯：先讀取、再修改、後儲存。
     * @param orderId 要更新的訂單ID
     * @param newStatus 新的訂單狀態
     * @return 更新後的訂單物件
     * @throws RuntimeException 如果訂單不存在
     */
    @Transactional
    public OrderListEntity updateOrderStatus(String orderId, Long newStatus) {
        // 1. 根據 ID 找到訂單，如果找不到就拋出例外
        OrderListEntity order = orderListRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("找不到訂單，ID: " + orderId));

        // 2. 執行商業邏輯：更新狀態
        order.setOrderStatus(newStatus);

        // 3. 儲存變更 (因為有 @Transactional，JPA 會自動儲存)
        return orderListRepository.save(order);
    }
}
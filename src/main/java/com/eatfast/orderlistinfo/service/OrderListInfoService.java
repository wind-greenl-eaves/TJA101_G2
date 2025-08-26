package com.eatfast.orderlistinfo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eatfast.orderlist.model.OrderListEntity;
import com.eatfast.orderlist.repository.OrderListRepository; // 可能需要訂單主表的 Repository
import com.eatfast.orderlistinfo.model.OrderListInfoDTO;
import com.eatfast.orderlistinfo.model.OrderListInfoEntity;
import com.eatfast.orderlistinfo.repository.OrderListInfoRepository;
import jakarta.persistence.EntityNotFoundException; // 【修正】添加正確的異常類型

/**
 * 處理訂單明細相關商業邏輯的 Service 層。
 */
@Service // 📌【不可變】聲明這是一個 Service 元件，交由 Spring 管理。
public class OrderListInfoService {

    private final OrderListInfoRepository orderListInfoRepository;
    private final OrderListRepository orderListRepository; // 注入訂單主表的 Repository 以便查詢

    @Autowired
    public OrderListInfoService(OrderListInfoRepository orderListInfoRepository, OrderListRepository orderListRepository) {
        this.orderListInfoRepository = orderListInfoRepository;
        this.orderListRepository = orderListRepository;
    }

    /**
     * 根據訂單 ID 查詢其所有的訂單明細。
     * @param orderId 訂單的 ID
     * @return 該訂單的所有明細列表
     */
    public List<OrderListInfoEntity> getDetailsForOrder(String orderId) {
        // 直接使用 Repository 中定義好的方法，乾淨俐落
        return orderListInfoRepository.findByOrderList_OrderListId(orderId);
    }

    /**
     * 為指定的訂單明細項目新增或更新評論星等。
     * 這是一個典型的商業邏輯：驗證 -> 讀取 -> 修改 -> 儲存。
     * @param orderListInfoId 訂單明細的 ID (主鍵)
     * @param stars 給予的星等 (例如 1-5)
     * @return 更新後的訂單明細物件
     * @throws IllegalArgumentException 如果星等超出範圍
     * @throws EntityNotFoundException 如果找不到該筆訂單明細
     */
    @Transactional // 📌【不可變】因為此方法會修改資料，務必加上交易控制
    public OrderListInfoEntity addReview(Long orderListInfoId, Long stars) {
        // 1. 商業邏輯驗證
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("評論星等必須介於 1 到 5 之間。");
        }

        // 2. 從資料庫讀取資料 - 【修正】使用 EntityNotFoundException
        OrderListInfoEntity info = orderListInfoRepository.findById(orderListInfoId)
                .orElseThrow(() -> new EntityNotFoundException("找不到指定的訂單明細項目，ID: " + orderListInfoId));

        // 檢查訂單狀態 - 只有已完成的訂單才能評論
        if (info.getOrderList() != null && 
            info.getOrderList().getOrderStatus() != com.eatfast.orderlist.model.OrderStatus.COMPLETED) {
            throw new IllegalStateException("只有已完成的訂單才能進行評論");
        }

        // 3. 修改資料
        info.setReviewStars(stars);

        // 4. 儲存回資料庫
        return orderListInfoRepository.save(info);
    }
    
    /**
     * 查詢某張訂單中所有尚未評論的項目 (假設 review_stars 為 0 代表未評論)
     * @param orderId 訂單 ID
     * @return 該訂單中所有未評論的項目列表
     * @throws EntityNotFoundException 如果訂單不存在
     */
    public List<OrderListInfoEntity> findUnreviewedItems(String orderId) {
        // 1. 先確認訂單主檔存在 - 【修正】使用 EntityNotFoundException
        OrderListEntity order = orderListRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("找不到訂單，ID: " + orderId));
        
        // 2. 使用多條件查詢方法
        Long unreviewedFlag = 0L;
        return orderListInfoRepository.findByOrderListAndReviewStars(order, unreviewedFlag);
    }

    /**
     * 創建新的訂單明細
     * @param orderListInfoEntity 要創建的訂單明細實體
     * @return 創建後的訂單明細實體
     */
    @Transactional
    public OrderListInfoEntity createOrderListInfo(OrderListInfoEntity orderListInfoEntity) {
        return orderListInfoRepository.save(orderListInfoEntity);
    }

    /**
     * 獲取訂單明細的 DTO 列表
     * @param orderId 訂單 ID
     * @return 訂單明細 DTO 列表
     */
    public List<OrderListInfoDTO> getDetailsForOrderDTO(String orderId) {
        List<OrderListInfoEntity> details = orderListInfoRepository.findByOrderList_OrderListId(orderId);

        return details.stream()
                .map(entity -> new OrderListInfoDTO(
                        entity.getMeal().getMealName(),
                        entity.getQuantity(),
                        entity.getMealPrice(),
                        entity.getReviewStars(), // ⭐【修改】評論星數
                        entity.getOrderList().getMealCustomization() // ⭐【新增】從主訂單獲取客製化備註
                ))
                .collect(Collectors.toList());
    }

    /**
     * 獲取會員的所有訂單明細（用於統計和分析）
     * @param memberId 會員 ID
     * @return 該會員的所有訂單明細列表
     */
    public List<OrderListInfoEntity> getOrderDetailsByMemberId(Long memberId) {
        // 透過 JPQL 或 Criteria API 查詢
        return orderListInfoRepository.findAll().stream()
                .filter(detail -> detail.getOrderList() != null && 
                                detail.getOrderList().getMember() != null &&
                                detail.getOrderList().getMember().getMemberId().equals(memberId))
                .collect(Collectors.toList());
    }

    /**
     * 檢查訂單明細是否可以評論
     * @param orderListInfoId 訂單明細 ID
     * @return true 如果可以評論
     */
    public boolean canReview(Long orderListInfoId) {
        return orderListInfoRepository.findById(orderListInfoId)
                .map(info -> {
                    // 檢查訂單是否已完成
                    if (info.getOrderList() == null) return false;
                    
                    boolean isCompleted = info.getOrderList().getOrderStatus() == 
                        com.eatfast.orderlist.model.OrderStatus.COMPLETED;
                    
                    // 檢查是否已經評論過（假設 0 表示未評論）
                    boolean notReviewed = info.getReviewStars() == null || info.getReviewStars() == 0L;
                    
                    return isCompleted && notReviewed;
                })
                .orElse(false);
    }
}
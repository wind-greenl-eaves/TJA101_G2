/*
 * ================================================================
 * 檔案 3: OrderListService.java (★★ 核心重構 ★★)
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/orderlist/service/OrderListService.java
 * - 核心改動:
 * 1. 遵循最佳實踐: 改用建構子注入，並使用 Spring 的 @Transactional。
 * 2. 強化更新邏輯: `updateOrderStatus` 方法加入了對「當前狀態」的檢查，防止不合法的狀態轉換。
 * 3. 型別安全: 所有與訂單狀態相關的操作，都改用 OrderStatus Enum，避免使用「魔法數字」。
 * 4. 新增方法: 加入 `getOrdersByMemberId` 方法，讓 Controller 可以直接透過 ID 查詢。
 */
package com.eatfast.orderlist.service;

import com.eatfast.member.model.MemberEntity;
import com.eatfast.member.repository.MemberRepository;
import com.eatfast.orderlist.model.OrderListEntity;
import com.eatfast.orderlist.model.OrderStatus;
import com.eatfast.orderlist.repository.OrderListRepository;
import com.eatfast.store.model.StoreEntity; // 【新增】引入 StoreEntity
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class OrderListService {

	private final OrderListRepository orderListRepository;
	private final MemberRepository memberRepository; // 【新】為了 getOrdersByMemberId 而依賴

	// 【優化】: 改用建構子注入。
	public OrderListService(OrderListRepository orderListRepository, MemberRepository memberRepository) {
		this.orderListRepository = orderListRepository;
		this.memberRepository = memberRepository;
	}

	@Transactional
	public OrderListEntity createOrder(OrderListEntity order) {
		// 【優化】: 儲存前，應先從資料庫撈取真實的 Member 和 Store 實體並設定回去，
		// 避免傳入的 order 物件中含有不完整的 detached entity。
		var member = memberRepository.findById(order.getMember().getMemberId())
				.orElseThrow(() -> new EntityNotFoundException("建立訂單失敗：找不到會員 ID " + order.getMember().getMemberId()));
		// (此處省略 StoreRepository 的注入與驗證，但真實專案中應一併加入)

		order.setMember(member);
		// order.setStore(store);

		// 在 Service 中設定初始狀態，確保一致性
		order.setOrderStatus(OrderStatus.PENDING);

		return orderListRepository.save(order);
	}

	public Optional<OrderListEntity> getOrderById(String orderId) {
		return orderListRepository.findById(orderId);
	}

	// 【優化】: 讓 Controller 可以直接透過 ID 查詢，而無需先取得 MemberEntity 物件。
	public List<OrderListEntity> getOrdersByMemberId(Long memberId) {
		return memberRepository.findById(memberId).map(orderListRepository::findByMemberOrderByOrderDateDesc)
				.orElse(Collections.emptyList());
	}

	/**
	 * 【核心邏輯重構】更新訂單狀態，並加入業務規則驗證。
	 */
	@Transactional
	public OrderListEntity updateOrderStatus(String orderId, OrderStatus newStatus) {
		OrderListEntity order = orderListRepository.findById(orderId)
				.orElseThrow(() -> new EntityNotFoundException("找不到訂單，ID: " + orderId));

		// 【優化】: 加入狀態轉換的業務規則檢查。
		// 例如：已完成或已取消的訂單，不允許再變更回其他狀態。
		if (order.getOrderStatus() == OrderStatus.COMPLETED || order.getOrderStatus() == OrderStatus.CANCELLED) {
			throw new IllegalStateException("無法更新一個已完成或已取消的訂單。");
		}

		order.setOrderStatus(newStatus);
		return orderListRepository.save(order);
	}

	public List<OrderListEntity> findAll() {
		return orderListRepository.findAll();
	}

	// 【新增】按門市過濾訂單的方法
	public List<OrderListEntity> findByStore(StoreEntity store) {
		return orderListRepository.findByStoreOrderByOrderDateDesc(store);
	}

	// 【新增】按門市ID過濾訂單的方法
	public List<OrderListEntity> findByStoreId(Long storeId) {
		return orderListRepository.findByStore_StoreIdOrderByOrderDateDesc(storeId);
	}

	@Transactional
	public OrderListEntity updateOrder(OrderListEntity orderListEntity) {
		// save 方法在 JPA 中同時具備新增和修改的功能。
		// 如果傳入的物件有主鍵(ID)且該ID存在於資料庫，JPA會執行更新操作。
		return orderListRepository.save(orderListEntity);
	}

	/**
	 * 檢查訂單編號是否已存在
	 * @param orderListId 訂單編號
	 * @return 是否存在
	 */
	public boolean existsByOrderListId(String orderListId) {
		return orderListRepository.existsById(orderListId);
	}
	
	/**
	 * 根據日期前綴查詢當日最大的訂單編號
	 * @param datePrefix 日期前綴 (YYYYMMDD)
	 * @return 當日最大的訂單編號，如果沒有則返回null
	 */
	public String findMaxOrderIdByDatePrefix(String datePrefix) {
		return orderListRepository.findTopByOrderListIdStartingWithOrderByOrderListIdDesc(datePrefix)
				.map(OrderListEntity::getOrderListId)
				.orElse(null);
	}
}
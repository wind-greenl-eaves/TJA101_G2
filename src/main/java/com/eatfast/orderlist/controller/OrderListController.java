package com.eatfast.orderlist.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.eatfast.orderlist.model.OrderListEntity;
import com.eatfast.orderlist.model.OrderStatus;
import com.eatfast.orderlist.service.OrderListService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/orderlist") // 將所有訂單相關的請求映射到 /orderlist 路徑下
public class OrderListController {

	@Autowired
	OrderListService orderSvc;

	// 為了在表單中顯示會員和門市的選項，我們需要它們的 Service (此處為示意)
	// @Autowired
	// MemberService memberSvc;
	// @Autowired
	// StoreService storeSvc;

	/*
	 * 此方法將返回一個包含所有訂單資料的 List
	 */
	@ModelAttribute("orderListData")
	protected List<OrderListEntity> referenceListData() {
		return orderSvc.findAll();
	}

	// (此處為示意，您需要提供對應的 Service 方法來獲取)
	// @ModelAttribute("memberListData")
	// protected List<MemberEntity> referenceMemberData() {
	// return memberSvc.findAll();
	// }
	// @ModelAttribute("storeListData")
	// protected List<StoreEntity> referenceStoreData() {
	// return storeSvc.findAll();
	// }

	/**
	 * 前往新增訂單頁面
	 */
	@GetMapping("/addOrderList")
	public String addOrderList(ModelMap model) {
		// 提供一個空的 OrderListEntity 物件給表單進行數據綁定
		model.addAttribute("orderListVO", new OrderListEntity());
		model.addAttribute("orderStatusOptions", OrderStatus.values()); // 提供訂單狀態選項
		return "back-end/orderlist/addOrderList";
	}


	
	@PostMapping("/getOne_For_Display")
	public String getOne_For_Display(@RequestParam("orderListId") String orderListId, ModelMap model) {
	    
	    OrderListEntity orderListVO = orderSvc.getOrderById(orderListId).orElse(null);
	    
	    if (orderListVO == null) {
	        model.addAttribute("errorMessage", "查無資料");
	    } else {
	        // 【關鍵修改】如果找到了，將訂單物件放入 model 中
	        model.addAttribute("orderListVO", orderListVO);
	    }
	    
	    // 【關鍵修改】無論成功或失敗，都返回原本的查詢頁面
	    return "back-end/orderlist/select_page_OrderList";
	}

	/**
	 * 導向至修改訂單的頁面
	 */
	@PostMapping("/getOne_For_Update")
	public String getOne_For_Update(@RequestParam("orderListId") String orderListId, ModelMap model) {
		OrderListEntity orderListVO = orderSvc.getOrderById(orderListId).orElse(null);
		model.addAttribute("orderListVO", orderListVO);
		model.addAttribute("orderStatusOptions", OrderStatus.values());
		return "back-end/orderlist/update_orderlist_input";
	}

	/**
	 * 修改訂單的請求處理
	 */
	@PostMapping("/update")
	public String update(
	    @Valid @ModelAttribute("orderListVO") OrderListEntity orderListVO, 
	    BindingResult result,
	    RedirectAttributes redirectAttributes,
	    Model model
	) {
	    
	    // 檢查是否有基本的資料綁定錯誤
	    if (result.hasErrors()) {
	        // 如果有錯，將下拉選單選項重新加入 model，並返回原頁面
	        model.addAttribute("orderStatusOptions", OrderStatus.values());
	        return "back-end/orderlist/update_orderlist_input";
	    }
	    
	    // --- 【核心邏輯】Fetch-then-Update ---
	    
	    // 1. 先透過 ID 從資料庫取得原始的、完整的訂單資料
	    //    您提供的 Entity 中 orderListId 型別為 String，這裡的 getOrderListId() 返回的也是 String，完全匹配。
	    //    這筆 originalOrder 裡面就包含了正確的 Member 和 Store 關聯。
	    OrderListEntity originalOrder = orderSvc.getOrderById(orderListVO.getOrderListId()).orElse(null);
	    
	    if (originalOrder == null) {
	        model.addAttribute("errorMessage", "錯誤：找不到要修改的訂單，ID: " + orderListVO.getOrderListId());
	        model.addAttribute("orderStatusOptions", OrderStatus.values());
	        return "back-end/orderlist/update_orderlist_input";
	    }
	    
	    // 2. 將表單送過來的新值，更新到從資料庫取出的原始物件上
	    //    我們只更新需要變動的欄位，Member 和 Store 關聯會自動保持不變。
	    originalOrder.setOrderAmount(orderListVO.getOrderAmount());
	    originalOrder.setOrderStatus(orderListVO.getOrderStatus());
	    originalOrder.setMealPickupNumber(orderListVO.getMealPickupNumber());
	    // 注意：您 Entity 中的 cardNumber 欄位在表單上沒有，所以這裡不會更新到它，這是正確的。
	    
	    // 3. 儲存已經更新好的「原始物件」，JPA 會自動處理變更
	    orderSvc.updateOrder(originalOrder);
	    
	    // 4. 使用 redirectAttributes 傳遞成功訊息並重新導向
	    redirectAttributes.addFlashAttribute("success", "-(訂單編號: " + originalOrder.getOrderListId() + " 修改成功)");
	    
	    return "redirect:/orderlist/listAllOrderList";
	}

	/**
	 * 刪除(取消)訂單的請求處理 實務上通常是更新狀態為 CANCELLED，而非真的刪除
	 */
	@PostMapping("/delete")
	public String delete(@RequestParam("orderListId") String orderListId, RedirectAttributes redirectAttributes) {
	    
	    // 1. 先從資料庫取得訂單
	    OrderListEntity order = orderSvc.getOrderById(orderListId).orElse(null);

	    // 2. 檢查訂單狀態
	    if (order != null && order.getOrderStatus() == OrderStatus.COMPLETED) {
	        // 如果訂單已完成，設定錯誤訊息並返回，不執行更新
	        redirectAttributes.addFlashAttribute("errorMessage", "操作失敗：訂單 " + orderListId + " 已完成，無法取消！");
	        return "redirect:/orderlist/listAllOrderList";
	    }
	    
	    // 3. 只有在訂單存在且狀態不是 COMPLETED 時，才執行更新
	    if (order != null) {
	        orderSvc.updateOrderStatus(orderListId, OrderStatus.CANCELLED);
	        redirectAttributes.addFlashAttribute("success", "-(訂單編號: " + orderListId + " 已取消)");
	    } else {
	        redirectAttributes.addFlashAttribute("errorMessage", "操作失敗：找不到訂單 " + orderListId);
	    }
	    
	    return "redirect:/orderlist/listAllOrderList";
	}

	/**
	 * 導向至訂單查詢主頁面
	 */
	@GetMapping("/select_page_OrderList")
	public String selectPage(Model model) {
	    return "back-end/orderlist/select_page_OrderList"; 
	}

	/**
	 * 顯示所有訂單列表
	 */
	@GetMapping("/listAllOrderList")
	public String listAllOrderList(Model model) {
		// @ModelAttribute("orderListData") 已經提供了資料
		return "back-end/orderlist/listAllOrderList";
	}
	
	
	@PostMapping("/markAsCompleted")
	public String markAsCompleted(@RequestParam("orderListId") String orderListId, RedirectAttributes redirectAttributes) {
	    
	    // 我們可以重複使用之前在 delete 方法中用過的 updateOrderStatus 服務
	    // 直接將狀態更新為 COMPLETED
	    orderSvc.updateOrderStatus(orderListId, OrderStatus.COMPLETED);
	    
	    // 設定成功訊息並重導向
	    redirectAttributes.addFlashAttribute("success", "-(訂單編號: " + orderListId + " 已標示為完成)");
	    
	    return "redirect:/orderlist/listAllOrderList";
	}
}
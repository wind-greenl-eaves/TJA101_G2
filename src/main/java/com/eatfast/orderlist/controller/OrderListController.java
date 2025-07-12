package com.eatfast.orderlist.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.eatfast.orderlist.model.OrderListEntity;
import com.eatfast.orderlist.model.OrderStatus;
import com.eatfast.orderlist.service.OrderListService;

import jakarta.servlet.http.HttpSession;
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
	public String getOne_For_Display(@RequestParam("orderListId") String orderListId, ModelMap model, HttpSession session) {
	    
	    // 【修復】確保員工資訊也被傳遞到查詢結果頁面 - 修復所有類型轉換問題
	    String employeeName = (String) session.getAttribute("employeeName");
	    Object employeeIdObj = session.getAttribute("employeeId");
	    Object employeeRoleObj = session.getAttribute("employeeRole");
	    
	    // 【修復】安全地轉換employeeId
	    String employeeId = null;
	    if (employeeIdObj != null) {
	        employeeId = employeeIdObj.toString();
	    }
	    
	    // 【修復】安全地轉換employeeRole
	    String employeeRole = null;
	    if (employeeRoleObj != null) {
	        employeeRole = employeeRoleObj.toString();
	    }
	    
	    if (employeeName != null) {
	        model.addAttribute("currentEmployeeName", employeeName);
	        model.addAttribute("currentEmployeeId", employeeId);
	        model.addAttribute("currentEmployeeRole", employeeRole);
	    }
	    
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
	 * 前往訂單查詢頁面
	 */
	@GetMapping("/select_page_OrderList")
	public String selectPage(Model model, HttpSession session) {
		try {
			// 【修復】正確獲取當前登入的員工資訊 - 注意所有類型轉換
			String employeeName = (String) session.getAttribute("employeeName");
			Object employeeIdObj = session.getAttribute("employeeId");
			Object employeeRoleObj = session.getAttribute("employeeRole");
			
			// 【修復】安全地轉換employeeId
			String employeeId = null;
			if (employeeIdObj != null) {
				employeeId = employeeIdObj.toString();
			}
			
			// 【修復】安全地轉換employeeRole
			String employeeRole = null;
			if (employeeRoleObj != null) {
				employeeRole = employeeRoleObj.toString();
			}
			
			// 【調試】輸出Session中的員工資訊
			System.out.println("=== 調試信息 ===");
			System.out.println("Session ID: " + session.getId());
			System.out.println("Employee Name: " + employeeName);
			System.out.println("Employee ID Object: " + employeeIdObj + " (type: " + (employeeIdObj != null ? employeeIdObj.getClass().getSimpleName() : "null") + ")");
			System.out.println("Employee ID: " + employeeId);
			System.out.println("Employee Role Object: " + employeeRoleObj + " (type: " + (employeeRoleObj != null ? employeeRoleObj.getClass().getSimpleName() : "null") + ")");
			System.out.println("Employee Role: " + employeeRole);
			System.out.println("================");
			
			// 將員工資訊添加到模型中
			if (employeeName != null) {
				model.addAttribute("currentEmployeeName", employeeName);
				model.addAttribute("currentEmployeeId", employeeId);
				model.addAttribute("currentEmployeeRole", employeeRole);
				System.out.println("員工資訊已添加到模型中");
			} else {
				System.out.println("警告：Session中沒有找到員工資訊");
			}
			
			// 【新增】計算各種狀態的訂單數量，提供給前端統計卡片顯示
			List<OrderListEntity> allOrders = orderSvc.findAll();
			
			// 確保 allOrders 不為 null
			if (allOrders != null && !allOrders.isEmpty()) {
				// 統計各狀態訂單數量
				long pendingCount = allOrders.stream()
						.filter(order -> order.getOrderStatus() == OrderStatus.PENDING)
						.count();
				
				long confirmedCount = allOrders.stream()
						.filter(order -> order.getOrderStatus() == OrderStatus.CONFIRMED)
						.count();
				
				long completedCount = allOrders.stream()
						.filter(order -> order.getOrderStatus() == OrderStatus.COMPLETED)
						.count();
				
				long cancelledCount = allOrders.stream()
						.filter(order -> order.getOrderStatus() == OrderStatus.CANCELLED)
						.count();
				
				// 將統計資料加入 Model
				model.addAttribute("pendingOrdersCount", pendingCount);
				model.addAttribute("confirmedOrdersCount", confirmedCount);
				model.addAttribute("completedOrdersCount", completedCount);
				model.addAttribute("cancelledOrdersCount", cancelledCount);
				
				// 總訂單數和今日訂單數
				model.addAttribute("totalOrdersCount", allOrders.size());
				
				// 計算今日訂單數
				long todayOrdersCount = allOrders.stream()
						.filter(order -> {
							if (order.getOrderDate() != null) {
								return order.getOrderDate().toLocalDate().equals(java.time.LocalDate.now());
							}
							return false;
						})
						.count();
				model.addAttribute("todayOrdersCount", todayOrdersCount);
			} else {
				// 如果沒有訂單資料，設定預設值
				model.addAttribute("pendingOrdersCount", 0L);
				model.addAttribute("confirmedOrdersCount", 0L);
				model.addAttribute("completedOrdersCount", 0L);
				model.addAttribute("cancelledOrdersCount", 0L);
				model.addAttribute("totalOrdersCount", 0L);
				model.addAttribute("todayOrdersCount", 0L);
			}
			
		} catch (Exception e) {
			// 記錄錯誤並設定預設值
			System.err.println("Error in selectPage: " + e.getMessage());
			e.printStackTrace();
			
			// 設定預設值以防止頁面錯誤
			model.addAttribute("pendingOrdersCount", 0L);
			model.addAttribute("confirmedOrdersCount", 0L);
			model.addAttribute("completedOrdersCount", 0L);
			model.addAttribute("cancelledOrdersCount", 0L);
			model.addAttribute("totalOrdersCount", 0L);
			model.addAttribute("todayOrdersCount", 0L);
		}
		
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
	@GetMapping("/pay/{orderListId}") // 不可變更: @GetMapping, 可自定義: "/pay/{orderListId}" (URL路徑)
    public String showPaymentPage(@PathVariable("orderListId") String orderListId, Model model) {
        
        // 1. 根據 ID 查找訂單資料
        // orderSvc.getOrderById() 是您 Service 中既有的方法，直接使用
        // orderToPay 是可自定義的變數名稱
        OrderListEntity orderToPay = orderSvc.getOrderById(orderListId).orElse(null);

        // 2. 檢查訂單是否存在
        if (orderToPay == null) {
            // 如果訂單不存在，可以導向到一個錯誤頁面或顯示錯誤訊息
            model.addAttribute("errorMessage", "錯誤：找不到指定的訂單 (ID: " + orderListId + ")");
            // "error_page" 是可自定義的錯誤頁面路徑
            return "error_page"; 
        }

        // 3. 將查找到的訂單物件放入 Model，準備傳給 pay.html
        // "orderToPay" 是可自定義的屬性名稱，稍後在 pay.html 中會用到它
        model.addAttribute("orderToPay", orderToPay);

        // 4. 回傳視圖名稱
        // "pay" 對應到 /resources/templates/pay.html 檔案，這裡是可自定義的
        return "pay";
    }
	

}
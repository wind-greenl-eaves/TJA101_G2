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
	public String update(@Valid @ModelAttribute("orderListVO") OrderListEntity orderListVO, BindingResult result,
			ModelMap model) {
		if (result.hasErrors()) {
			model.addAttribute("orderStatusOptions", OrderStatus.values());
			return "back-end/orderlist/update_orderlist_input";
		}
		// 注意：這裡的更新邏輯可能需要更複雜的處理，例如只更新部分欄位
		orderSvc.updateOrder(orderListVO);
		model.addAttribute("success", "-(修改成功)");
		return "redirect:/orderlist/listAllOrderList";
	}

	/**
	 * 刪除(取消)訂單的請求處理 實務上通常是更新狀態為 CANCELLED，而非真的刪除
	 */
	@PostMapping("/delete")
	public String delete(@RequestParam("orderListId") String orderListId, ModelMap model) {
		orderSvc.updateOrderStatus(orderListId, OrderStatus.CANCELLED);
		model.addAttribute("success", "-(刪除成功)");
		return "redirect:/orderlist/listAllOrderList";
	}

	/**
	 * 導向至訂單查詢主頁面
	 */
	@GetMapping("/select_page_OrderList")
	public String select_page(Model model) {
		// 此處可以預先載入一些查詢表單需要的資料
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
	
}

	
package com.eatfast.test.Autowired;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eatfast.member.model.MemberEntity;
import com.eatfast.member.service.MemberService;

/**
 * 開發階段專用的 CRUD 功能測試控制器。這個 Controller 的主要目的，是提供一個簡單的網址入口，
 * 開發者可以直接在瀏覽器上觸發後端的資料庫操作，並在 Eclipse 的控制台 (Console) 觀察結果，
 * 以此來驗證 Service 和 Repository 層的邏輯是否正確。
 * * @Controller - 標記這個類別為 Spring 的 Controller，讓 Spring 容器管理它。
 * * @RequestMapping("/testMember") - 設定這個方法處理的 HTTP 請求路徑。
 * * @ResponseBody - [關鍵！]這個註解告訴 Spring：不要去尋找視圖(View)來渲染，而是直接將方法的返回值作為 HTTP 響應的主體 (Body)。
 * @Autowired - 用於自動注入 MemberService 的實例，讓這個 Controller 可以使用它來執行 CRUD 操作。
 */
@Controller 
public class Demo_SpringDataJPA_Controller_testMember1 {

	@Autowired 
	MemberService memberService; 
	/**
	 * 處理對 "/testMember1" 路徑的請求。
	 * @return 一個簡單的字串，會直接顯示在瀏覽器畫面上，表示測試已執行。
	 */
	@RequestMapping("/testMember1") 
	@ResponseBody 
	public String testMemberService() {
// ========================================================================================================
		// ● 測試 MemberService 的 CRUD 功能
		// 說明：透過註解/取消註解的方式，一次只執行一個測試區塊。
		// 操作方式：
		// 1. 啟動 Spring Boot 應用程式。
		// 2. 打開瀏覽器，輸入網址： http://localhost:8080/testMember1 (假設你的埠號是8080)
		// 3. 按下 Enter，這個方法就會被執行。
		// 4. 回到你的 Eclipse，查看下方 Console 視窗的輸出結果。
// ========================================================================================================
		
		// ● 新增 (Create)
		// ========================================================================
//        System.out.println("---------- 開始測試: 新增 ----------");
//        MemberEntity member1 = new MemberEntity("測試員A", "testA", "password123", "testA@example.com", "0911222333", LocalDate.of(2000, 1, 1));
//        memberService.saveOrUpdateMember(member1);
//        System.out.println("---------- 新增測試完成 ----------");
//    	return "測試 (新增功能) MemberService OK";

		// ● 修改 (Update)
		// =========================================================================
		// 注意：執行修改前，請確保資料庫中已有 member_id = 1 的資料，或指定一個存在的 ID。
//        System.out.println("---------- 開始測試: 修改 ----------");
//        // 首先，從資料庫讀取要修改的物件
//        Optional<MemberEntity> memberToUpdateOpt = memberService.getMemberByAccount("testB"); 
//        if (memberToUpdateOpt.isPresent()) {
//            MemberEntity member2 = memberToUpdateOpt.get();
//            // 只修改需要變更的欄位
//            member2.setUsername("測試員B_Updated");
//            member2.setPassword("new_password_456");
//            member2.setPhone("0987654321");
//            memberService.saveOrUpdateMember(member2); // 執行更新
//            System.out.println("---------- 修改測試完成 ----------");
//            return "測試 (修改功能) MemberService OK";
//        } else {
//            System.out.println("---------- 修改失敗: 找不到要修改的資料 ----------");
//            return "測試 (修改功能) 失敗: 找不到資料";
//        }


		// ● 刪除 (Delete)
		// ==========================================================================
		// 注意：執行刪除前，請確保資料庫中有對應的資料。
//        System.out.println("---------- 開始測試: 刪除 ----------");
//        memberService.deleteMemberById(3L); // 根據 ID 刪除
//        memberService.deleteMemberByAccount("testA"); // 根據帳號刪除
//        System.out.println("---------- 刪除測試完成 ----------");
//    	return "測試 (刪除功能) MemberService OK";

		// ● 查詢 - 根據 ID (Read by ID)
		// =============================================================
//        System.out.println("---------- 開始測試: 根據ID查詢 ----------");
//        MemberEntity member3 = memberService.getMemberById(1L);
//        if (member3 != null) {
//            System.out.println("查詢結果: " + member3);
//        } else {
//            System.out.println("查無此ID的會員");
//        }
//        System.out.println("------------------------------------");
//    	return "測試 (ID查詢功能) MemberService OK";

		// ● 查詢 - 根據帳號 (Read by Account)
		// =======================================================
//		System.out.println("---------- 開始測試: 根據帳號查詢 ----------");
//		Optional<MemberEntity> optionalMember = memberService.getMemberByAccount("yijun.chen");
//		// 使用 ifPresentOrElse 讓程式碼更簡潔
//		optionalMember.ifPresentOrElse(
//              member -> System.out.println("查詢結果: " + member), // 如果找到資料，就執行這個 Lambda
//			    () -> System.out.println("查無此帳號的會員")     // 如果沒找到，就執行這個 Lambda
//        );
//		System.out.println("------------------------------------");
//		return "測試 (帳號查詢功能) MemberService OK";

		// ● 查詢 - 全部 (Read All)
		// ==================================================================
		System.out.println("---------- 開始測試: 查詢全部 ----------");
		List<MemberEntity> list = memberService.getAllMembers();
		for (MemberEntity member : list) {
			System.out.println(member); // 直接使用 MemberEntity 中覆寫的 toString() 方法，更簡潔
		}
		System.out.println("---------- 查詢全部測試完成 ----------");

		return "測試 (查詢全部) MemberService OK";
	}
}

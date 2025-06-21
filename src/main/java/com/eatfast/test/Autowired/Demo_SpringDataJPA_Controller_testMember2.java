package com.eatfast.test.Autowired;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 這是一個專門用來動態產生會員報表的 Controller。
 * 它示範了如何僅用後端 Java 程式碼，透過傳統的 JDBC API 生成一個完整的網頁報表。
 * *@Controller - 標記這個類別為 Spring 的 Controller，讓 Spring 容器管理它。
 * * @GetMapping("/report/members") - 定義這個方法處理的 HTTP GET 請求路徑。
 * * @ResponseBody - [關鍵！] 這個註解告訴 Spring：不要去尋找視圖(View)來渲染，而是直接將方法的返回值作為 HTTP 響應的主體 (Body)。
 */
@Controller 
public class Demo_SpringDataJPA_Controller_testMember2 {

	//  Spring Boot 會自動配置一個 DataSource，我們只需注入即可使用。
	@Autowired
	DataSource dataSource;
	/**
	 * 當使用者訪問 /report/members 時，此方法會被觸發。
	 * @return 一個包含完整 HTML 結構的字串。
	 */
	@GetMapping("/report/members") // [可變] 定義請求路徑
	@ResponseBody // [關鍵！] 這個註解就是你問題的答案。它改變了 Spring MVC 的預設行為。
	              // 預設情況下，@Controller 方法回傳的字串會被視為「視圖名稱」，Spring 會去找對應的 HTML/JSP 檔案。
	              // 加上 @ResponseBody 後，Spring 就知道：「不要去找檔案了，直接把這個方法回傳的內容 (無論是字串、JSON物件等) 當作 HTTP 回應的主體(Body)送出去。」
	public String generateMemberReport() {

		//  使用 StringBuilder 來高效地建立 HTML 字串，比用 "+" 連接字串效能更好。
		StringBuilder reportHtml = new StringBuilder();
		
		// 使用 try-with-resources 語法，確保無論是否發生錯誤，
		// Connection, Statement, ResultSet 這些昂貴的資料庫資源都會被【自動關閉】，避免資源洩漏。
		try (Connection con = dataSource.getConnection();
			 Statement stmt = con.createStatement();
			 // "SELECT * FROM member" 是您的 SQL 查詢，可以替換成任何您想查詢的語句。
			 ResultSet rs = stmt.executeQuery("SELECT * FROM member ORDER BY member_id ASC")) {

			// 取得查詢結果的元數據 (MetaData)，用來動態獲取欄位名稱和數量。
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();

			// --- 開始組合 HTML 字串 ---
			reportHtml.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>動態會員報表</title>");
			// 加入一些簡單的 CSS 樣式讓報表更美觀。
			reportHtml.append("<style>");
			reportHtml.append("body { font-family: Arial, '微軟正黑體', sans-serif; margin: 40px; background-color: #f9f9f9; }");
			reportHtml.append("h1 { color: #333; }");
			reportHtml.append("table { width: 100%; border-collapse: collapse; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
			reportHtml.append("th, td { border: 1px solid #ccc; padding: 12px; text-align: left; }");
			reportHtml.append("thead { background-color: #007bff; color: white; }");
			reportHtml.append("tbody tr:nth-child(even) { background-color: #f2f2f2; }");
			reportHtml.append("tbody tr:hover { background-color: #e9ecef; }");
			reportHtml.append("</style>");
			reportHtml.append("</head><body>");
			reportHtml.append("<h1>早餐店會員動態報表</h1>");
			reportHtml.append("<table>");

			// 步驟 1: 動態生成表格標頭 (Header)
			reportHtml.append("<thead><tr>");
			for (int i = 1; i <= columnCount; i++) {
				//  getColumnLabel() 會取得 SQL 查詢結果的欄位名稱。
				reportHtml.append("<th>").append(rsmd.getColumnLabel(i).toUpperCase()).append("</th>");
			}
			reportHtml.append("</tr></thead>");

			// 步驟 2: 動態生成表格內容 (Body)
			reportHtml.append("<tbody>");
			while (rs.next()) {
				reportHtml.append("<tr>");
				for (int i = 1; i <= columnCount; i++) {
					reportHtml.append("<td>");
					Object value = rs.getObject(i);
					// 處理資料庫中的 NULL 值，避免在畫面上顯示 "null" 字樣，改為顯示空白。
					reportHtml.append(value == null ? "&nbsp;" : value.toString());
					reportHtml.append("</td>");
				}
				reportHtml.append("</tr>");
			}
			reportHtml.append("</tbody>");

			// --- 結束 HTML 組合 ---
			reportHtml.append("</table></body></html>");

		} catch (Exception e) {
			//  如果發生任何資料庫錯誤，回傳一個簡單的錯誤訊息頁面。
			e.printStackTrace(); // 在 Eclipse Console 印出詳細錯誤堆疊，方便除錯
			return "<html><body><h1>報表產生失敗</h1><p>錯誤訊息: " + e.getMessage() + "</p></body></html>";
		}

		//  將組合好的完整 HTML 字串回傳。因為有 @ResponseBody，瀏覽器會直接收到這段 HTML 並渲染它。
		return reportHtml.toString();
	}
}

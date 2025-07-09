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
 * 這是一個專門用來動態產生員工報表的 Controller。
 * 它示範了如何僅用後端 Java 程式碼，透過傳統的 JDBC API 生成一個完整的網頁報表。
 * @Controller - 標記這個類別為 Spring 的 Controller，讓 Spring 容器管理它。
 * @GetMapping("/report/employees") - 定義這個方法處理的 HTTP GET 請求路徑。
 * @ResponseBody - [關鍵！] 這個註解告訴 Spring：不要去尋找視圖(View)來渲染，而是直接將方法的返回值作為 HTTP 響應的主體 (Body)。
 */
@Controller 
public class Demo_SpringDataJPA_Controller_testEmployee {

	// Spring Boot 會自動配置一個 DataSource，我們只需注入即可使用。
	@Autowired
	DataSource dataSource;
	
	/**
	 * 當使用者訪問 /report/employees 時，此方法會被觸發。
	 * @return 一個包含完整 HTML 結構的字串。
	 */
	@GetMapping("/report/employees") // [可變] 定義請求路徑
	@ResponseBody // [關鍵！] 這個註解就是你問題的答案。它改變了 Spring MVC 的預設行為。
	              // 預設情況下，@Controller 方法回傳的字串會被視為「視圖名稱」，Spring 會去找對應的 HTML/JSP 檔案。
	              // 加上 @ResponseBody 後，Spring 就知道：「不要去找檔案了，直接把這個方法回傳的內容 (無論是字串、JSON物件等) 當作 HTTP 回應的主體(Body)送出去。」
	public String generateEmployeeReport() {

		// 使用 StringBuilder 來高效地建立 HTML 字串，比用 "+" 連接字串效能更好。
		StringBuilder reportHtml = new StringBuilder();
		
		// 使用 try-with-resources 語法，確保無論是否發生錯誤，
		// Connection, Statement, ResultSet 這些昂貴的資料庫資源都會被【自動關閉】，避免資源洩漏。
		try (Connection con = dataSource.getConnection();
			 Statement stmt = con.createStatement();
			 // "SELECT * FROM eatfast_db.employee" 查詢員工表格的所有資料，依照員工編號排序
			 ResultSet rs = stmt.executeQuery("SELECT * FROM eatfast_db.employee ORDER BY employee_id ASC")) {

			// 取得查詢結果的元數據 (MetaData)，用來動態獲取欄位名稱和數量。
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();

			// --- 開始組合 HTML 字串 ---
			reportHtml.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>動態員工報表</title>");
			// 加入一些簡單的 CSS 樣式讓報表更美觀。
			reportHtml.append("<style>");
			reportHtml.append("body { font-family: Arial, '微軟正黑體', sans-serif; margin: 40px; background-color: #FDFBF6; }");
			reportHtml.append("h1 { color: #A67B5B; text-align: center; font-size: 2.5em; margin-bottom: 30px; }");
			reportHtml.append("h2 { color: #5D534A; text-align: center; margin-bottom: 20px; }");
			reportHtml.append("table { width: 100%; border-collapse: collapse; box-shadow: 0 4px 15px rgba(0,0,0,0.1); background-color: white; }");
			reportHtml.append("th, td { border: 1px solid #DED0B6; padding: 12px; text-align: left; }");
			reportHtml.append("thead { background-color: #A67B5B; color: white; }");
			reportHtml.append("tbody tr:nth-child(even) { background-color: #F5EFE6; }");
			reportHtml.append("tbody tr:hover { background-color: #DED0B6; }");
			reportHtml.append(".status-active { color: #28a745; font-weight: bold; }");
			reportHtml.append(".status-inactive { color: #dc3545; font-weight: bold; }");
			reportHtml.append(".role-admin { color: #007bff; font-weight: bold; }");
			reportHtml.append(".role-manager { color: #17a2b8; font-weight: bold; }");
			reportHtml.append(".role-staff { color: #6c757d; font-weight: bold; }");
			reportHtml.append(".header-info { text-align: center; margin-bottom: 20px; color: #5D534A; }");
			reportHtml.append(".photo-cell { text-align: center; width: 80px; }");
			reportHtml.append(".photo-cell img { width: 60px; height: 60px; border-radius: 50%; object-fit: cover; border: 2px solid #A67B5B; }");
			reportHtml.append("</style>");
			reportHtml.append("</head><body>");
			reportHtml.append("<h1>🍳 EatFast 早餐店員工動態報表</h1>");
			reportHtml.append("<div class='header-info'>");
			reportHtml.append("<p>📊 報表生成時間：" + new java.util.Date() + "</p>");
			reportHtml.append("<p>📋 資料來源：eatfast_db.employee</p>");
			reportHtml.append("</div>");
			reportHtml.append("<table>");

			// 步驟 1: 動態生成表格標頭 (Header)
			reportHtml.append("<thead><tr>");
			for (int i = 1; i <= columnCount; i++) {
				String columnName = rsmd.getColumnLabel(i);
				// 美化欄位名稱顯示
				String displayName = beautifyColumnName(columnName);
				reportHtml.append("<th>").append(displayName).append("</th>");
			}
			reportHtml.append("</tr></thead>");

			// 步驟 2: 動態生成表格內容 (Body)
			reportHtml.append("<tbody>");
			int rowCount = 0;
			while (rs.next()) {
				reportHtml.append("<tr>");
				for (int i = 1; i <= columnCount; i++) {
					String columnName = rsmd.getColumnLabel(i).toLowerCase();
					Object value = rs.getObject(i);
					
					reportHtml.append("<td");
					
					// 針對照片欄位特殊處理
					if ("photo_url".equals(columnName)) {
						reportHtml.append(" class='photo-cell'");
					}
					
					reportHtml.append(">");
					
					if (value == null) {
						reportHtml.append("&nbsp;");
					} else {
						String valueStr = value.toString();
						
						// 根據不同欄位進行特殊格式化
						if ("status".equals(columnName)) {
							if ("ACTIVE".equals(valueStr)) {
								reportHtml.append("<span class='status-active'>✅ 啟用</span>");
							} else if ("INACTIVE".equals(valueStr)) {
								reportHtml.append("<span class='status-inactive'>❌ 停用</span>");
							} else {
								reportHtml.append(valueStr);
							}
						} else if ("role".equals(columnName)) {
							if ("HEADQUARTERS_ADMIN".equals(valueStr)) {
								reportHtml.append("<span class='role-admin'>👑 總部管理員</span>");
							} else if ("MANAGER".equals(valueStr)) {
								reportHtml.append("<span class='role-manager'>👔 門市經理</span>");
							} else if ("STAFF".equals(valueStr)) {
								reportHtml.append("<span class='role-staff'>👤 一般員工</span>");
							} else {
								reportHtml.append(valueStr);
							}
						} else if ("gender".equals(columnName)) {
							if ("M".equals(valueStr)) {
								reportHtml.append("👨 男");
							} else if ("F".equals(valueStr)) {
								reportHtml.append("👩 女");
							} else {
								reportHtml.append(valueStr);
							}
						} else if ("photo_url".equals(columnName)) {
							if (valueStr != null && !valueStr.trim().isEmpty()) {
								reportHtml.append("<img src='").append(valueStr).append("' alt='員工照片' onerror='this.src=\"/images/no_image.png\"'>");
							} else {
								reportHtml.append("📷 無照片");
							}
						} else if ("email".equals(columnName)) {
							reportHtml.append("📧 ").append(valueStr);
						} else if ("phone".equals(columnName)) {
							reportHtml.append("📱 ").append(valueStr);
						} else if ("national_id".equals(columnName)) {
							// 遮罩身分證字號以保護隱私
							if (valueStr.length() >= 10) {
								reportHtml.append(valueStr.substring(0, 3)).append("****").append(valueStr.substring(7));
							} else {
								reportHtml.append(valueStr);
							}
						} else if (columnName.contains("time") || columnName.contains("date")) {
							// 格式化時間顯示
							reportHtml.append("🕒 ").append(valueStr);
						} else {
							reportHtml.append(valueStr);
						}
					}
					reportHtml.append("</td>");
				}
				reportHtml.append("</tr>");
				rowCount++;
			}
			reportHtml.append("</tbody>");

			// --- 結束 HTML 組合 ---
			reportHtml.append("</table>");
			reportHtml.append("<div class='header-info'>");
			reportHtml.append("<p>📈 總計員工數量：<strong>").append(rowCount).append("</strong> 人</p>");
			reportHtml.append("<p>🔄 報表更新：即時動態查詢</p>");
			reportHtml.append("</div>");
			reportHtml.append("</body></html>");

		} catch (Exception e) {
			// 如果發生任何資料庫錯誤，回傳一個簡單的錯誤訊息頁面。
			e.printStackTrace(); // 在 Eclipse Console 印出詳細錯誤堆疊，方便除錯
			return "<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif; margin: 40px; background-color: #FDFBF6;'>" +
			       "<h1 style='color: #dc3545;'>❌ 員工報表產生失敗</h1>" +
			       "<div style='background-color: #f8d7da; border: 1px solid #f5c6cb; color: #721c24; padding: 15px; border-radius: 5px;'>" +
			       "<h3>錯誤訊息:</h3><p>" + e.getMessage() + "</p>" +
			       "<h3>可能原因:</h3>" +
			       "<ul>" +
			       "<li>資料庫連線失敗</li>" +
			       "<li>employee 資料表不存在</li>" +
			       "<li>資料庫權限不足</li>" +
			       "</ul>" +
			       "</div></body></html>";
		}

		// 將組合好的完整 HTML 字串回傳。因為有 @ResponseBody，瀏覽器會直接收到這段 HTML 並渲染它。
		return reportHtml.toString();
	}
	
	/**
	 * 美化欄位名稱顯示
	 * @param columnName 原始欄位名稱
	 * @return 美化後的顯示名稱
	 */
	private String beautifyColumnName(String columnName) {
		switch (columnName.toLowerCase()) {
			case "employee_id": return "👤 員工編號";
			case "username": return "📝 員工姓名";
			case "account": return "🔑 登入帳號";
			case "password": return "🔒 密碼";
			case "email": return "📧 電子郵件";
			case "phone": return "📱 聯絡電話";
			case "role": return "👔 員工角色";
			case "status": return "📊 帳號狀態";
			case "gender": return "⚧ 性別";
			case "national_id": return "🆔 身分證字號";
			case "store_id": return "🏪 所屬門市編號";
			case "created_by": return "👨‍💼 建立者";
			case "create_time": return "📅 建立時間";
			case "last_updated_at": return "🔄 最後更新時間";
			case "photo_url": return "📷 員工照片";
			default: return columnName.toUpperCase().replace("_", " ");
		}
	}
}
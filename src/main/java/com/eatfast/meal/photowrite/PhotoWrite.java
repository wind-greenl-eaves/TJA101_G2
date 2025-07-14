package com.eatfast.meal.photowrite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class PhotoWrite {

	public static void main(String argv[]) {
		Connection con = null;
		PreparedStatement pstmt = null;
//		InputStream fin = null;
		String url = "jdbc:mysql://localhost:3306/eatfast_db?serverTimezone=Asia/Taipei";
		String userid = "root";
		String passwd = "123456";
		String photos = "src/main/resources/static/images/meal_pic"; 
		String update = "update meal set meal_pic =? where meal_Id=?";
		
		int startId = 101; // mealId 起始值
		int totalCount = 20; // 總共要更新的餐點數量
		
		try {
			con = DriverManager.getConnection(url, userid, passwd);
			pstmt = con.prepareStatement(update);
			File[] photoFiles = new File(photos).listFiles();
			if (photoFiles == null || photoFiles.length == 0) {
				pstmt = con.prepareStatement(update);
				for(int mealId = startId; mealId <startId + totalCount; mealId++) {
					File f = new File(photos, mealId + ".png");
				    String fileName;
				    if (f.exists()) {
				        fileName = mealId + ".png";
				    } else {
				        fileName = "nopic.png";
				    }
				    pstmt.setString(1, fileName);
				    pstmt.setInt(2, mealId);
				    pstmt.executeUpdate();
				    System.out.println("mealId=" + mealId + " 設定圖片：" + fileName);
				}
				pstmt.close();
			 } else {
	                pstmt = con.prepareStatement(update);
	                int mealId = startId;
	                for (File f : photoFiles) {
	                    String fileName = f.getName();
	                    pstmt.setString(1, fileName);
	                    pstmt.setInt(2, mealId);
	                    pstmt.executeUpdate();
	                    System.out.println("mealId=" + mealId + " 設定圖片：" + fileName);
	                    mealId++;
	                }
	                pstmt.close();
	            }
	            System.out.println("批次更新完成！");
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                if(con != null) con.close();
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	}
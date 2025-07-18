package com.eatfast.meal.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.eatfast.common.enums.MealStatus;
import com.eatfast.meal.model.MealEntity;
import com.eatfast.meal.model.MealService;
import com.eatfast.mealtype.model.MealTypeEntity;
import com.eatfast.mealtype.service.MealTypeService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/meal")
public class MealController {
	
	private static final String IMAGE_UPLOAD_DIR = "uploads/meal_pic/";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; 

    @Autowired
    private MealService mealService;

    @Autowired
    private MealTypeService mealTypeService;

    // 處理顯示新增餐點的表單頁面
    @GetMapping("/addMeal")
    public String addMealForm(ModelMap model) {
        MealEntity mealEntity = new MealEntity();
        mealEntity.setMealType(new MealTypeEntity()); // 初始化 mealType 關聯物件，避免 NullPointerException
        model.addAttribute("mealEntity", mealEntity);
        return "back-end/meal/addMeal"; 
    }

    // 新增餐點（只存檔名，圖片存在 static/images/meal_pic/）
    @PostMapping("/insert")
    public String insert(@Validated MealEntity mealEntity, BindingResult result, ModelMap model,
                        RedirectAttributes redirectAttributes) throws IOException {
        MultipartFile mealPic = mealEntity.getMealPicFile();

        Long mealTypeId = mealEntity.getMealType() != null ? mealEntity.getMealType().getMealTypeId() : null;
        if (mealTypeId == null) {
            result.addError(new FieldError("mealEntity", "mealType.mealTypeId", "餐點種類不可為空"));
        } else {
            // 查出完整 MealTypeEntity，再 set 回 mealEntity
            MealTypeEntity fullType = mealTypeService.getOneMealType(mealTypeId);
            mealEntity.setMealType(fullType);
        }
        
        if (mealPic != null && !mealPic.isEmpty()) {
        	// 存 uploads/meal_pic 資料夾
			if (mealPic.getSize() > MAX_FILE_SIZE) {
				result.addError(new FieldError("mealEntity", "mealPicFile", "圖片大小不可超過 10MB"));
			}
			// 生成唯一檔名，避免重複
        	String filename = UUID.randomUUID() + "_" + mealPic.getOriginalFilename();
            Path savePath = Paths.get(IMAGE_UPLOAD_DIR, filename);
            Files.createDirectories(savePath.getParent());
            Files.write(savePath, mealPic.getBytes());
            mealEntity.setMealPic(filename); // 存檔名
        }else {
            mealEntity.setMealPic(null); // 沒選圖則欄位為 null
        }

        if (result.hasErrors()) {
            return "back-end/meal/addMeal";
        }
        try {
            mealService.addMeal(mealEntity);
            redirectAttributes.addFlashAttribute("successMessage", "新增成功！");
        } catch (IllegalArgumentException e) {
            // 捕獲 Service 拋出的業務例外，並將錯誤訊息傳回。
            redirectAttributes.addFlashAttribute("errorMessage", "新增失敗: " + e.getMessage());
            // 返回新增頁面，並保留使用者輸入的資料
            return "redirect:/meal/addMeal";
        }
       
        return "redirect:/meal/listAllMeal";
    }


    // 更新餐點資料（包含圖片上傳）
	@PostMapping("/update")
	public String update(@Validated MealEntity mealEntity, BindingResult result, ModelMap model, RedirectAttributes redirectAttributes) throws IOException {
	    final String UPLOAD_DIR = "uploads/meal_pic/";
	
	    MultipartFile mealPic = mealEntity.getMealPicFile();
	    removeFieldError(mealEntity, result, "mealPic"); // 移除 mealPic 欄位的錯誤紀錄，避免圖片上傳錯誤影響其他欄位驗證

	
	    MealEntity existingMeal = mealService.getOneMeal(mealEntity.getMealId());
	    
	    Long mealTypeId = mealEntity.getMealType() != null ? mealEntity.getMealType().getMealTypeId() : null;
	    if (mealTypeId == null) {
	        result.addError(new FieldError("mealEntity", "mealType.mealTypeId", "餐點種類不可為空"));
	    } else {
	        MealTypeEntity fullType = mealTypeService.getOneMealType(mealTypeId);
	        mealEntity.setMealType(fullType);
	    }
	    
	    if (mealPic != null && !mealPic.isEmpty()) {
	        if (mealPic.getSize() > MAX_FILE_SIZE) {
	            result.addError(new FieldError("mealEntity", "mealPicFile", "圖片大小不可超過 10MB"));
	        } else {
	            // 上傳新圖片
	            String filename = UUID.randomUUID() + "_" + mealPic.getOriginalFilename();
	            Path savePath = Paths.get(UPLOAD_DIR, filename); // 使用 uploads/meal_pic/ 資料夾
	            Files.createDirectories(savePath.getParent());   // 確保目錄存在
	            Files.write(savePath, mealPic.getBytes()); // 寫入檔案
	            mealEntity.setMealPic(filename);
	
	            // 刪除舊檔案（僅刪 uploads/meal_pic/ 下的舊圖）
	            if (existingMeal != null && existingMeal.getMealPic() != null && !existingMeal.getMealPic().isBlank()) {
	                Path oldImgPath = Paths.get(UPLOAD_DIR, existingMeal.getMealPic()); // 獲取舊圖片的路徑
	                Files.deleteIfExists(oldImgPath); // 刪除舊圖片檔案
	            }
	        }
	    } else {
	        // 沒有上傳新圖片，保留原有的檔名
	        if (existingMeal != null) {
	            mealEntity.setMealPic(existingMeal.getMealPic());
	        }
	    }
	    
	    if (result.hasErrors()) {
	        if (mealEntity.getMealType() == null) {
	            mealEntity.setMealType(new MealTypeEntity());
	        }
	        model.addAttribute("mealEntity", mealEntity);// 將錯誤的 mealEntity 傳回表單
	        model.addAttribute("mealTypeListData", mealTypeService.getAll());
	        return "back-end/meal/update_meal_input";
	    }
	
	    mealService.updateMeal(mealEntity);
	    redirectAttributes.addFlashAttribute("success", "- (修改成功)");
	    return "redirect:/meal/listAllMeal";
	}

	// 顯示或查詢一筆餐點資料（依ID）
    @PostMapping("/getOne_For_Display")
    public String getOne_For_Display(@RequestParam(value = "mealId", required = false) String mealId, Model model) {
        // 先載入所有餐點列表，確保頁面數據完整性
        List<MealEntity> allMeals = mealService.getAll();
        model.addAttribute("mealListData", allMeals);

        // 伺服器端基本驗證：防止空值查詢
        if (mealId == null || mealId.trim().isEmpty()) {
            model.addAttribute("errorMessage", "請輸入或選擇餐點編號");
            return "back-end/meal/select_page_meal"; // 統一導向餐點列表頁面
        }

        Long id = null;
        try {
            id = Long.valueOf(mealId);
        } catch (NumberFormatException e) {
            model.addAttribute("errorMessage", "餐點編號格式錯誤，請輸入數字");
            return "back-end/meal/select_page_meal"; // 統一導向餐點列表頁面
        }

        MealEntity meal = mealService.getOneMeal(id); // 查詢單一餐點

        if (meal == null) {
            model.addAttribute("errorMessage", "查無資料");
        } else {
            model.addAttribute("meal", meal); // 將查詢到的單一餐點添加到 Model
            // 也將選中的 mealId 傳回，用於在下拉選單中保留上次的選擇 (如果該下拉選單也在 listAllMeal 頁面)
            model.addAttribute("param.mealId", mealId);
        }
        return "back-end/meal/listAllMeal"; // 統一導向餐點列表頁面
    }

    // 取得餐點資料並準備進行修改
    @PostMapping("/getOne_For_Update")
    public String getOneForUpdate(@RequestParam("mealId") String mealId, ModelMap model) {
        MealEntity mealEntity = mealService.getOneMeal(Long.valueOf(mealId));
        // 確保 mealType 被初始化，因為更新表單也會使用下拉選單，避免 NullPointerException
        if (mealEntity.getMealType() == null) {
            mealEntity.setMealType(new MealTypeEntity());
        }

        model.addAttribute("mealEntity", mealEntity);
        return "back-end/meal/update_meal_input"; // 返回修改餐點頁面
    }

    // 查詢特定類別的餐點
    @PostMapping("/byType")
    public String getMealsByType(@RequestParam("mealTypeId") Long mealTypeId, Model model) {
    	// 檢查 mealTypeId 是否為預設的 "0" 或 null
        if (mealTypeId == null || mealTypeId == 0L) {
            model.addAttribute("errorMessage", "請選擇有效的餐點種類");
            // 導向回原始查詢頁面，顯示錯誤訊息
            return "back-end/meal/select_page_meal";
        }

        // 執行查詢
        List<MealEntity> meals = mealService.getMealsByType(mealTypeId); // 假設你的 service 有這個方法

        if (meals.isEmpty()) {
            model.addAttribute("errorMessage", "查無此餐點種類的資料。"); // 如果查無資料，顯示錯誤
        } else {
        	model.addAttribute("mealListData", meals); // ListAllMeal頁面用這個key
        }
        // 將選中的 mealTypeId 傳回，用於在下拉選單中保留上次的選擇
        model.addAttribute("selectedMealTypeId", mealTypeId);

        return "back-end/meal/listAllMeal"; // 返回餐點列表頁面
    }

    // 查詢特定狀態的餐點（透過 Enum）
    @PostMapping("/byStatus")
    public String getMealsByStatus(@RequestParam(value = "status", required = false) MealStatus status, Model model) {
        // 檢查 status 是否為 null（前端沒有選擇任何狀態或選擇預設的空值）
        if (status == null) {
            model.addAttribute("errorMessage", "請選擇有效的餐點狀態");
            // 導向回原始查詢頁面，顯示錯誤訊息
            return "back-end/meal/select_page_meal";
        }

        // 執行查詢
        List<MealEntity> meals = mealService.getMealsByStatus(status); 

        if (meals.isEmpty()) {
            model.addAttribute("errorMessage", "查無此餐點狀態的資料。"); 
        } else {
	        model.addAttribute("mealListData", meals);
	        model.addAttribute("selectedStatus", status); // 用於保留下拉選單的選擇狀態
        }
        return "back-end/meal/listAllMeal"; 
    }

    // 去除BindingResult中某個欄位的FieldError紀錄
    public BindingResult removeFieldError(MealEntity mealEntity, BindingResult result, String removedFieldname) {
        List<FieldError> errorsListToKeep = result.getFieldErrors().stream()
                .filter(fieldname -> !fieldname.getField().equals(removedFieldname))
                .collect(Collectors.toList());
        // 重新建立 BindingResult 物件以移除特定 FieldError
        BindingResult newResult = new BeanPropertyBindingResult(mealEntity, "mealEntity");
        for (FieldError fieldError : errorsListToKeep) {
            newResult.addError(fieldError);
        }
        return newResult;
    }
    
    // 從 select_page_meal 表單頁面進行複合查詢
    @PostMapping("/select_page_ByCompositeQuery")
    public String compositeQueryFromSelectPage(HttpServletRequest req, Model model) {
        Map<String, String[]> map = req.getParameterMap();
        String[] mealNameArr = map.get("mealName");
        String mealName = (mealNameArr != null && mealNameArr.length > 0) ? mealNameArr[0].trim() : "";
        if (!mealName.isEmpty() && !mealName.matches("^[\u4e00-\u9fa5]+$")) {
            // 查詢失敗，停留在 select_page
            model.addAttribute("compositeError", "餐點名稱僅能輸入中文");
            model.addAttribute("param", map);
            model.addAttribute("mealTypeListData", mealTypeService.getAll());
            model.addAttribute("mealListData", mealService.getAll());
            return "back-end/meal/select_page_meal"; // 停留在 select_page
        }
        // 查詢成功
        List<MealEntity> list = mealService.getAll(map);
        model.addAttribute("mealListData", list);
        model.addAttribute("param", map);
        model.addAttribute("mealTypeListData", mealTypeService.getAll());
        return "back-end/meal/listAllMeal";
    }


    // 條件複合查詢（搭配 HibernateUtil）
    @PostMapping("/listMeal_ByCompositeQuery")
    public String listAllMeal(HttpServletRequest req, ModelMap model) {
        Map<String, String[]> map = req.getParameterMap();
        
        // 後端驗證: 餐點名稱只能輸入中文（可空白，不檢查長度）
        String[] mealNameArr = map.get("mealName");
        String mealName = (mealNameArr != null && mealNameArr.length > 0) ? mealNameArr[0].trim() : "";
        if (!mealName.isEmpty() && !mealName.matches("^[\u4e00-\u9fa5]+$")) {
            model.addAttribute("errorMessage", "餐點名稱僅能輸入中文");
            // 必要時，把查詢欄位值帶回
            model.addAttribute("param", map);
            // 重新查全部餐點避免查詢條件出錯沒資料
            model.addAttribute("mealListData", mealService.getAll());
            // 下拉選單選項
            model.addAttribute("mealTypeListData", mealTypeService.getAll());
            return "back-end/meal/listAllMeal";
        }
        
        List<MealEntity> list = mealService.getAll(map);
        model.addAttribute("mealListData", list);
        model.addAttribute("param", map);
        
        // 保留種類選擇
        String[] mealTypeIdArr = map.get("mealTypeId");
        Long selectedMealTypeId = null;
        if (mealTypeIdArr != null && mealTypeIdArr.length > 0 && !mealTypeIdArr[0].isBlank()) {
            try { selectedMealTypeId = Long.valueOf(mealTypeIdArr[0]); 
            } catch(Exception e) {
            	
            }
        }
        model.addAttribute("selectedMealTypeId", selectedMealTypeId);
        
        // 保留狀態選擇
        String[] statusArr = map.get("status");
        if (statusArr != null && statusArr.length > 0 && !statusArr[0].isBlank()) {
            try {
                model.addAttribute("selectedStatus", MealStatus.valueOf(statusArr[0].trim()));
            } catch (IllegalArgumentException e) {
                model.addAttribute("selectedStatus", null); // 無效值時不指定
            }
        } else {
            model.addAttribute("selectedStatus", null);
        }

        return "back-end/meal/listAllMeal"; // 返回餐點列表頁面
    }

    // 顯示所有餐點的列表頁面
    @GetMapping("/listAllMeal")
    public String listAllMeal(ModelMap model) {
    	
        List<MealEntity> list = mealService.getAll();
        model.addAttribute("mealListData", list);
        return "back-end/meal/listAllMeal"; // 返回餐點列表頁面
    }

    // 自動將餐點種類列表加入 Model，供所有方法使用 (選擇餐點類別的下拉選單)
    @ModelAttribute("mealTypeListData")
    public List<MealTypeEntity> referenceTypeList() {
        return mealTypeService.getAll();
    }
    
    
    @ModelAttribute("mealListData")
    public List<MealEntity> getAllMeals() {
        return mealService.getAll();
    }

    // 這個方法現在統一導向 listAllMeal，而不是 select_page_meal
    @GetMapping("/select_page_meal")
    public String showSelectPage(Model model) {
        List<MealEntity> list = mealService.getAll(); // 預設查詢所有餐點
        model.addAttribute("mealListData", list);       // 傳給畫面顯示
        return "back-end/meal/select_page_meal";       // 統一導向餐點列表頁面
    }
    
//    // 刪除餐點
//    @PostMapping("/delete")
//    public String deleteMeal(@RequestParam("mealId") Long mealId, RedirectAttributes redirectAttributes) {
//    	try {
//        	// 先刪除圖片檔案
//            MealEntity meal = mealService.getOneMeal(mealId);
//            // 檢查圖片是否存在，若存在則刪除
//            if (meal != null && meal.getMealPic() != null && !meal.getMealPic().isBlank()) {
//            	// 使用 Paths.get() 取得圖片的完整路徑
//                Path imgPath = Paths.get(IMAGE_UPLOAD_DIR, meal.getMealPic());
//                if (Files.exists(imgPath)) {
//                    Files.delete(imgPath); // 只刪 uploads 裡的圖檔
//                }
//            }
//            mealService.deleteMeal(mealId);
//            redirectAttributes.addFlashAttribute("successMessage", "刪除成功！");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "刪除失敗：" + e.getMessage());
//        }
//        return "redirect:/meal/listAllMeal";
//    }

}
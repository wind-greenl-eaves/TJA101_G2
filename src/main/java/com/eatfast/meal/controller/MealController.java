package com.eatfast.meal.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.StreamUtils;
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

import com.eatfast.meal.model.MealEntity;
import com.eatfast.meal.model.MealService;
import com.eatfast.mealtype.model.MealTypeEntity;
import com.eatfast.mealtype.model.MealTypeService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/meal")
public class MealController {

    @Autowired
    private MealService mealService;

    @Autowired
    private MealTypeService mealTypeService;

    // 顯示新增餐點的表單頁面
    @GetMapping("/addMeal")
    public String addMealForm(ModelMap model) {
    	MealEntity mealEntity = new MealEntity();
        // 初始化 mealEntity 的 mealType 關聯物件，避免發生 NullPointerException
    	mealEntity.setMealType(new MealTypeEntity());
        model.addAttribute("mealEntity", mealEntity);
     // @ModelAttribute("mealTypeListData") 會自動將餐點種類列表加入 Model，這裡不需重複添加
        return "back-end/meal/addMeal"; 
    }

    // 處理新增餐點請求，將填寫的資料、圖片存入資料庫
    @PostMapping("/insert")
    // 使用 @ModelAttribute("mealEntity") 確保表單數據正確綁定回 mealEntity
    public String insert(@Validated MealEntity mealEntity, BindingResult result, ModelMap model, 
    		@RequestParam("mealPic") MultipartFile[] parts, RedirectAttributes redirectAttributes) throws IOException {
    	
    	// 移除 mealPic 欄位的錯誤，因為圖片驗證我們手動處理
        result = removeFieldError(mealEntity, result, "mealPic");
        
        // 圖片處理及驗證
        if (parts[0].isEmpty()) {
        	// 如果圖片是必填且未上傳，手動將錯誤添加到 BindingResult
            result.addError(new FieldError("mealEntity", "mealPic", "請上傳圖片"));
        } else {
            byte[] bytes = parts[0].getBytes();
            mealEntity.setMealPic(bytes);
        }

        // 如果存在任何驗證錯誤（包括圖片未上傳），則返回表單頁面
        if (result.hasErrors() || parts[0].isEmpty()) {
            return "back-end/meal/addMeal";
        }

        // 如果所有驗證都通過，執行新增操作
        mealService.addMeal(mealEntity);
        
        // 使用 RedirectAttributes 傳遞一次性的成功訊息，因為是重導向操作
        redirectAttributes.addFlashAttribute("success", "- (新增成功)");
        
        // 重導向到顯示所有餐點的列表頁面，這會觸發一個新的請求，重新載入最新數據
        return "redirect:/meal/listAllMeal";
    }
    
    // 顯示或查詢一筆餐點資料（依ID）
    @PostMapping("/getOne_For_Display")
    public String getOne_For_Display(@RequestParam("mealId") String mealId, Model model) {
        // 傳遞所有餐點列表
        List<MealEntity> list = mealService.getAll();
        model.addAttribute("mealListData", list);
        
        // 伺服器端基本驗證：防止空值查詢
        if (mealId == null || mealId.trim().isEmpty()) {
            model.addAttribute("errorMessage", "餐點編號不可為空");
            return "back-end/meal/select_page_meal";
        }

        Integer id = null;
        try {
            id = Integer.valueOf(mealId);
        } catch (NumberFormatException e) {
            model.addAttribute("errorMessage", "餐點編號格式錯誤");
            return "back-end/meal/select_page_meal";
        }

        MealEntity meal = mealService.getOneMeal(id); // 查詢單一餐點

        if (meal == null) {
            model.addAttribute("errorMessage", "查無資料");
        } else {
            model.addAttribute("meal", meal); // 將查詢到的單一餐點添加到 Model
        }
        return "back-end/meal/select_page_meal";
    }
			
    // 取得餐點資料並準備進行修改
    @PostMapping("/getOne_For_Update")
    public String getOneForUpdate(@RequestParam("mealId") String mealId, ModelMap model) {
        MealEntity mealEntity = mealService.getOneMeal(Integer.valueOf(mealId));
        // 確保 mealType 被初始化，因為更新表單也會使用下拉選單，避免 NullPointerException
        if (mealEntity.getMealType() == null) {
            mealEntity.setMealType(new MealTypeEntity());
        }
        
        model.addAttribute("mealEntity", mealEntity);
        return "back-end/meal/update_meal_input"; // 查詢完成後轉交update_meal_input.html
    }
    
    // 儲存或更新餐點
    @PostMapping("/update")
    public String update(@Validated MealEntity mealEntity, BindingResult result, ModelMap model, 
    		@RequestParam("mealPic") MultipartFile[] parts, RedirectAttributes redirectAttributes) throws IOException {
    	// 移除 mealPic 欄位的錯誤
        result = removeFieldError(mealEntity, result, "mealPic");

        // 使用者新上傳的圖片，將新上傳的圖片回傳至mealEntity
        if (parts[0] != null && !parts[0].isEmpty()) {
        	byte[] newPicBytes = parts[0].getBytes();
            mealEntity.setMealPic(newPicBytes);
        } else {  // 如果沒有上傳新的圖片，就從資料庫中取得舊圖片，確保圖片不會被清除
            if(mealEntity.getMealId() != null) {
            	MealEntity existingMeal = mealService.getOneMeal(mealEntity.getMealId());
            	if (existingMeal != null && existingMeal.getMealPic() != null) {
                    // 保留舊圖片
            		mealEntity.setMealPic(existingMeal.getMealPic());
                } else {
                    // 如果圖片是必填但沒有舊圖且沒上傳新圖片
                    result.addError(new FieldError("mealEntity", "mealPic", "請上傳圖片"));
                } 
			}
        }

        if (result.hasErrors()) {
            return "back-end/meal/update_meal_input";
        }

        mealService.updateMeal(mealEntity);
        redirectAttributes.addFlashAttribute("success", "- (修改成功)");
        // 重定向到所有餐點列表頁面
        return "redirect:/meal/listAllMeal";
    }

    // 查詢特定類別的餐點
    @PostMapping("/byType")
    public String getMealsByType(@RequestParam("mealTypeId") Integer mealTypeId, Model model) {
    	List<MealEntity> meals;
        if (mealTypeId == null || mealTypeId == 0) { // 假設 0 或 null 代表顯示所有種類
             meals = mealService.getAll();
        } else {
             meals = mealService.getMealsByType(mealTypeId);
        }
        model.addAttribute("mealListData", meals); // ListAllMeal頁面用這個key
        // 將選中的 mealTypeId 傳回，用於在下拉選單中保留上次的選擇
        model.addAttribute("selectedMealTypeId", mealTypeId);
        return "back-end/meal/listAllMeal";
    }

    // 查詢特定狀態的餐點（1:上架、0:下架）
    @PostMapping("/byStatus")
    public String getMealsByStatus(@RequestParam("status") Integer status, Model model) {
        List<MealEntity> meals = mealService.getMealsByStatus(status);
        model.addAttribute("mealListData", meals);
        model.addAttribute("selectedStatus", status); // 用於保留下拉選單的選擇狀態
        return "back-end/meal/listAllMeal";  // 共用查詢結果頁面
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

	// 條件複合查詢（搭配 HibernateUtil）
	@PostMapping("/listMeal_ByCompositeQuery")
	public String listAllMeal(HttpServletRequest req, ModelMap model) {
	    Map<String, String[]> map = req.getParameterMap();
	    List<MealEntity> list = mealService.getAll(map);
	    model.addAttribute("mealListData", list);
	    return "back-end/meal/listAllMeal";
	}
	// 顯示所有餐點的列表頁面
	@GetMapping("/listAllMeal")
	public String listAllMeal(ModelMap model) {
	    List<MealEntity> list = mealService.getAll();
	    model.addAttribute("mealListData", list);
	    return "back-end/meal/listAllMeal"; 
	}

	// 自動將餐點種類列表加入 Model，供所有方法使用(選擇餐點類別的下拉選單)
	@ModelAttribute("mealTypeListData")
	public List<MealTypeEntity> referenceTypeList() {
	    return mealTypeService.getAll();
	}
	
	// 獲取餐點圖片，如果沒有則返回預設圖片
    @GetMapping("/mealPhoto")
    public ResponseEntity<byte[]> getMealPhoto(@RequestParam("mealId") Integer mealId) throws IOException {
        MealEntity mealEntity = mealService.getOneMeal(mealId); // 透過 mealId 取得餐點實體
        
        byte[] imageBytes = null;
        MediaType mediaType = MediaType.IMAGE_PNG; // 預設圖片類型為 PNG，如果預設圖片是 JPG，請改為 MediaType.IMAGE_JPEG

        if (mealEntity != null && mealEntity.getMealPic() != null && mealEntity.getMealPic().length > 0) {
            // 如果資料庫中有圖片，則使用資料庫中的圖片
            imageBytes = mealEntity.getMealPic();
            // 假設資料庫中的圖片是 JPEG
            mediaType = MediaType.IMAGE_JPEG; // 例如：設定為 JPEG
        } else {
            // 如果資料庫中沒有圖片，載入預設圖片
            ClassPathResource imgFile = new ClassPathResource("static/images/nopic.png"); // 預設圖片路徑
            // 讀取預設圖片的位元組
            imageBytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
            mediaType = MediaType.IMAGE_PNG; // 預設圖片的媒體類型
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType); // 設定回應的內容類型
        
        // 返回圖片的位元組陣列，並設定 HTTP 狀態碼為 OK
        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }
    
    @GetMapping("/select_page_meal")
    public String showSelectPage(Model model) {
        List<MealEntity> list = mealService.getAll(); // 預設查詢所有餐點
        model.addAttribute("mealListData", list);     // 傳給畫面顯示
        return "back-end/meal/select_page_meal";      // 回傳 Thymeleaf 頁面
    }
    
}


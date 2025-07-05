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

import com.eatfast.common.enums.MealStatus;
import com.eatfast.meal.model.MealEntity;
import com.eatfast.meal.model.MealService;
import com.eatfast.mealtype.model.MealTypeEntity;
import com.eatfast.mealtype.service.MealTypeService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/meal")
public class MealController {
	
	 private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB 上限

    @Autowired
    private MealService mealService;

    @Autowired
    private MealTypeService mealTypeService;

    // 處理顯示新增餐點的表單頁面
    @GetMapping("/addMeal")
    public String addMealForm(ModelMap model) {
        MealEntity mealEntity = new MealEntity();
        mealEntity.setReviewTotalStars(0L); // 設定預設星數
        mealEntity.setMealType(new MealTypeEntity()); // 初始化 mealType 關聯物件，避免 NullPointerException
        model.addAttribute("mealEntity", mealEntity);
        // @ModelAttribute("mealTypeListData") 會自動將餐點種類列表加入 Model，這裡不需重複添加
        return "back-end/meal/addMeal"; // 返回新增餐點頁面
    }

    // 處理新增餐點請求，將填寫的資料、圖片存入資料庫
    @PostMapping("/insert")
    // 使用 @ModelAttribute("mealEntity") 確保表單數據正確綁定回 mealEntity
    public String insert(@Validated MealEntity mealEntity, BindingResult result, ModelMap model,
           RedirectAttributes redirectAttributes) throws IOException {
    	
    	MultipartFile mealPic = mealEntity.getMealPicFile();
        // 移除 mealPic 欄位的錯誤，因為圖片驗證我們手動處理
        result = removeFieldError(mealEntity, result, "mealPic");

        // 圖片處理及驗證
        if (mealPic != null && !mealPic.isEmpty()) {
          if (mealPic.getSize() > MAX_FILE_SIZE) {
        	 result.addError(new FieldError("mealEntity", "mealPicFile", "圖片大小不可超過 10MB"));
	        } else {
	            mealEntity.setMealPic(mealPic.getBytes()); // 將上傳的圖片轉換為位元組陣列
	        }
        }

        // 表單驗證錯誤則返回新增餐點表單頁面
        if (result.hasErrors()) {
            return "back-end/meal/addMeal";
        }

        // 如果所有驗證都通過，執行新增操作
        mealService.addMeal(mealEntity);
        redirectAttributes.addFlashAttribute("success", "- (新增成功)");

        // 重導向到顯示所有餐點的列表頁面，這會觸發一個新的請求，重新載入最新數據
        return "redirect:/meal/listAllMeal";
    
        }

    // 顯示或查詢一筆餐點資料（依ID）
    @PostMapping("/getOne_For_Display")
    public String getOne_For_Display(@RequestParam("mealId") String mealId, Model model) {
        // 先載入所有餐點列表，確保頁面數據完整性
        List<MealEntity> allMeals = mealService.getAll();
        model.addAttribute("mealListData", allMeals);

        // 伺服器端基本驗證：防止空值查詢
        if (mealId == null || mealId.trim().isEmpty()) {
            model.addAttribute("errorMessage", "餐點編號不可為空");
            return "back-end/meal/listAllMeal"; // 統一導向餐點列表頁面
        }

        Long id = null;
        try {
            id = Long.valueOf(mealId);
        } catch (NumberFormatException e) {
            model.addAttribute("errorMessage", "餐點編號格式錯誤");
            return "back-end/meal/listAllMeal"; // 統一導向餐點列表頁面
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

    // 儲存或更新餐點
    @PostMapping("/update")
    public String update(@Validated MealEntity mealEntity, BindingResult result, ModelMap model, RedirectAttributes redirectAttributes) throws IOException {
        
    	MultipartFile mealPic = mealEntity.getMealPicFile();
    	// 移除 mealPic 欄位的錯誤
        result = removeFieldError(mealEntity, result, "mealPic");

        // 如果 mealPic 有上傳新的圖片，則將其轉換為位元組陣列
        if (mealPic != null && !mealPic.isEmpty()) {
        	if (mealPic.getSize() > MAX_FILE_SIZE) {
				result.addError(new FieldError("mealEntity", "mealPicFile", "圖片大小不可超過 10MB"));
			} else {
            mealEntity.setMealPic(mealPic.getBytes());
			}
          // 如果沒有上傳新的圖片，就從資料庫中取得舊圖片，確保圖片不會被清除
        } else if(mealEntity.getMealId() != null) {
                MealEntity existingMeal = mealService.getOneMeal(mealEntity.getMealId());
                if (existingMeal != null && existingMeal.getMealPic() != null) {
                    // 保留舊圖片
                    mealEntity.setMealPic(existingMeal.getMealPic());
                } else {
                    // 如果圖片是必填但沒有舊圖且沒上傳新圖片
                    result.addError(new FieldError("mealEntity", "mealPic", "請上傳圖片"));
                }
            }

        if (result.hasErrors()) {
            // 如果有錯誤，需要確保 mealType 已被初始化，以免在 update_meal_input 頁面報錯
            if (mealEntity.getMealType() == null) {
                mealEntity.setMealType(new MealTypeEntity());
            }
            model.addAttribute("mealTypeListData", mealTypeService.getAll());
            return "back-end/meal/update_meal_input"; // 返回修改餐點頁面
        }

        mealService.updateMeal(mealEntity);
        redirectAttributes.addFlashAttribute("success", "- (修改成功)");
        return "redirect:/meal/listAllMeal";
    }

    // 查詢特定類別的餐點
    @PostMapping("/byType")
    public String getMealsByType(@RequestParam("mealTypeId") Long mealTypeId, Model model) {
        List<MealEntity> meals;
        if (mealTypeId == null || mealTypeId == 0L) { // 假設 0 或 null 代表顯示所有種類
             meals = mealService.getAll();
        } else {
             meals = mealService.getMealsByType(mealTypeId);
        }
        model.addAttribute("mealListData", meals); // ListAllMeal頁面用這個key
        // 將選中的 mealTypeId 傳回，用於在下拉選單中保留上次的選擇
        model.addAttribute("selectedMealTypeId", mealTypeId);

        return "back-end/meal/listAllMeal"; // 返回餐點列表頁面
    }

    // 查詢特定狀態的餐點（透過 Enum）
    @PostMapping("/byStatus")
    public String getMealsByStatus(@RequestParam(value = "status", required = false) MealStatus status, Model model) {
        List<MealEntity> meals;
        if (status == null) { // 如果沒有選擇狀態，則顯示所有餐點
            meals = mealService.getAll(); // 這裡假設 getAll() 獲取所有餐點
        } else {
            meals = mealService.getMealsByStatus(status);
        }
        model.addAttribute("mealListData", meals);
        model.addAttribute("selectedStatus", status); // 用於保留下拉選單的選擇狀態
        return "back-end/meal/listAllMeal"; // 返回餐點列表頁面
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
        List<MealEntity> list = mealService.getAll(map); // 假設此 getAll(map) 方法處理複合查詢
        model.addAttribute("mealListData", list);
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

    // 獲取餐點圖片，如果沒有則返回預設圖片
    @GetMapping("/mealPhoto")
    public ResponseEntity<byte[]> getMealPhoto(@RequestParam("mealId") Long mealId) throws IOException {
        MealEntity mealEntity = mealService.getOneMeal(mealId); // 透過 mealId 取得餐點實體

        byte[] imageBytes;
        MediaType mediaType = MediaType.IMAGE_JPEG; // 預設圖片類型為JPG

        if (mealEntity != null && mealEntity.getMealPic() != null && mealEntity.getMealPic().length > 0) {
            // 如果資料庫中有圖片，則使用資料庫中的圖片
            imageBytes = mealEntity.getMealPic();
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

    // 這個方法現在統一導向 listAllMeal，而不是 select_page_meal
    @GetMapping("/select_page_meal")
    public String showSelectPage(Model model) {
        List<MealEntity> list = mealService.getAll(); // 預設查詢所有餐點
        model.addAttribute("mealListData", list);       // 傳給畫面顯示
        return "back-end/meal/select_page_meal";       // 統一導向餐點列表頁面
    }
}
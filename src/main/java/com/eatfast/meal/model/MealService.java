package com.eatfast.meal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eatfast.common.enums.MealStatus;
import com.eatfast.fav.model.FavRepository;
import com.eatfast.meal.dto.MealDTO;
import com.eatfast.meal.dto.MealRatingDto;

@Service("mealService")
public class MealService {

	@Autowired
	MealRepository repository;
	
	@Autowired
    private FavRepository favRepository;  // 用於判斷有無收藏
	
	@Autowired
    private SessionFactory sessionFactory;
	
	@Autowired
    private MealRatingService mealRatingService; // 用於計算餐點平均評分
	
	// 新增餐點
	@Transactional
	public void addMeal(MealEntity mealEntity) {
		repository.save(mealEntity);
	}

	// 儲存或更新餐點（JPA 自動判斷新增/更新）
	@Transactional
	public void updateMeal(MealEntity mealEntity) {
		repository.save(mealEntity);
	}

	// 查一筆餐點（依ID）
	@Transactional(readOnly = true)
	public MealEntity getOneMeal(Long mealId) {
		Optional<MealEntity> optional = repository.findById(mealId);
		return optional.orElse(null);  // public T orElse(T other) : 如果值存在就回傳其值，否則回傳other的值
	}

	// 查特定類別餐點
    public List<MealEntity> getMealsByType(Long mealTypeId) {
        return repository.findByMealTypeMealTypeId(mealTypeId);
    }

    // 查特定狀態餐點（例如上架 1 / 下架 0）
    public List<MealEntity> getMealsByStatus(MealStatus status) {
        return repository.findByStatus(status);
    }

    // 條件複合查詢（搭配 HibernateUtil）
    public List<MealEntity> getAll(Map<String, String[]> map) {
        try (var session = sessionFactory.openSession()) {
            return HibernateUtil_CompositeQuery_Meal.getAllC(map, session);
        }
    }
    
	public List<MealEntity> getAll() {
		return repository.findAll();
	}
	
	// 刪除餐點
	@Transactional
	public void deleteMeal(Long mealId) {
		repository.deleteById(mealId);
	}
	
	// === 前台:查詢所有上架餐點 ===
	public List<MealEntity> getAllAVAILABLE() {
	    return repository.findByStatus(MealStatus.AVAILABLE); // 只找上架餐點
	}


	// === 前台：取得所有上架餐點，並標註會員是否已收藏 ===
    public List<MealDTO> getAllAvailableWithFavored(Long memberId) {
        List<MealEntity> meals = repository.findByStatus(MealStatus.AVAILABLE);
        
        // 1. 取得所有餐點的平均評分列表
        List<MealRatingDto> allMealRatings = mealRatingService.getAllMealAvgStars();
        
        // 2. 將評分列表轉換為一個 Map，方便透過 mealId 快速查找平均評分
        //    鍵 (Key) 是餐點ID (Long)，值 (Value) 是平均星級 (Double)
        Map<Long, Double> mealRatingsMap = new HashMap<>();
        for (MealRatingDto ratingDto : allMealRatings) {
            mealRatingsMap.put(ratingDto.getMealId(), ratingDto.getAvgStars());
        }

        // 3. 建立一個新的 List 來存放轉換後的 MealDTO 物件
        List<MealDTO> mealDTOList = new ArrayList<>();
        
        // 4. 遍歷每個 MealEntity，將其轉換為 MealDTO，並補上平均評分
        for (MealEntity meal : meals) {
            MealDTO dto = toDTOWithFavored(meal, memberId); // 轉換基本資訊和收藏狀態
            
            // 從 Map 中查找該餐點的平均評分
            // mealRatingsMap.get(meal.getMealId()) 會返回 Double 或 null (如果沒有找到)
            // .getOrDefault(key, defaultValue) 是一個很方便的方法，如果找不到就給個預設值
            Double avgStars = mealRatingsMap.getOrDefault(meal.getMealId(), 0.0);
            
            dto.setAvgStars(avgStars); // 設定餐點的平均星級評分
            mealDTOList.add(dto);     // 將完成的 MealDTO 加入列表
        }
        
        return mealDTOList; // 返回包含平均評分的 MealDTO 列表
    }

    // === 前台：依分類查詢，並標註會員是否已收藏 ===
    public List<MealDTO> getMealsByTypeWithFavored(Long mealTypeId, Long memberId) {
        List<MealEntity> meals = repository.findByMealTypeMealTypeId(mealTypeId);

        // 1. 取得所有餐點的平均評分列表
        List<MealRatingDto> allMealRatings = mealRatingService.getAllMealAvgStars();
        
        // 2. 將評分列表轉換為一個 Map，方便透過 mealId 快速查找平均評分
        Map<Long, Double> mealRatingsMap = new HashMap<>();
        for (MealRatingDto ratingDto : allMealRatings) {
            mealRatingsMap.put(ratingDto.getMealId(), ratingDto.getAvgStars());
        }
        
        // 3. 建立一個新的 List 來存放轉換後的 MealDTO 物件
        List<MealDTO> mealDTOList = new ArrayList<>();
        
        // 4. 遍歷每個 MealEntity，將其轉換為 MealDTO，並補上平均評分
        for (MealEntity meal : meals) {
            MealDTO dto = toDTOWithFavored(meal, memberId); // 轉換基本資訊和收藏狀態
            
            // 從 Map 中查找該餐點的平均評分，如果沒有就給 0.0
            Double avgStars = mealRatingsMap.getOrDefault(meal.getMealId(), 0.0);
            
            dto.setAvgStars(avgStars); // 設定餐點的平均星級評分
            mealDTOList.add(dto);     // 將完成的 MealDTO 加入列表
        }
        
        return mealDTOList; // 返回包含平均評分的 MealDTO 列表
    }
    
	 // === 圖片 URL 處理 ===
	 public String buildMealPicUrl(String mealPic) {
	     if (mealPic == null || mealPic.isBlank()) {
	         return "/images/nopic.png"; // 如果沒有圖片，回傳預設無圖片路徑
	     }
	     // 判斷是否為上傳的圖片 (例如檔名以 "upload_" 開頭或包含 UUID)
	     if (mealPic.startsWith("upload_") || mealPic.matches(".*[a-f0-9\\-]{36}.*")) {
	         return "/meal-pic/" + mealPic; 
	     }
	     // 其他的舊圖或預設圖 (存在於 src/main/resources/static/images/meal_pic/ )
	     return "/images/meal_pic/" + mealPic; 
	 }


    // === Entity to DTO（關鍵轉換）===
    public MealDTO toDTOWithFavored(MealEntity meal, Long memberId) {
        boolean favored = false;
        Long favMealId = null;
        
        if (memberId != null) {
            // 檢查會員是否已收藏此餐點
        	var favOpt = favRepository.findByMemberMemberIdAndMealMealId(memberId, meal.getMealId());
            if (favOpt.isPresent()) {
                favored = true;
                favMealId = favOpt.get().getFavMealId();
            }
        }
        // 餐點類型名稱
        String mealTypeName = meal.getMealType() != null ? meal.getMealType().getMealName() : "";

        // 建立 MealDTO 並設定屬性
        MealDTO dto = new MealDTO();
        dto.setMealId(meal.getMealId());
        dto.setMealName(meal.getMealName());
        dto.setMealPrice(meal.getMealPrice());
        dto.setMealTypeName(mealTypeName);
        dto.setMealPicUrl(buildMealPicUrl(meal.getMealPic())); // 圖片 URL 處理
        dto.setFavored(favored);
        dto.setFavMealId(favMealId); // 收藏ID（若有收藏）
        dto.setMealPic(meal.getMealPic()); // 取得圖片檔名
        return dto;
   
    }
    
    // === 後台獲取所有餐點的 MealDTO 列表，不顯示星級，不處理收藏狀態 === 
    public List<MealDTO> getAllMealsForBackendDisplay() {
        List<MealEntity> meals = repository.findAll();
        return convertEntitiesToDTOsForBackend(meals);
    }

    // === 後台根據複合條件查詢餐點，返回 MealDTO 列表，不顯示星級，不處理收藏狀態 === 
    public List<MealDTO> getAllMealsForBackendDisplay(Map<String, String[]> map) {
        List<MealEntity> meals = getAll(map);
        return convertEntitiesToDTOsForBackend(meals);
    }
    
    // === 後台獲取單一餐點的 MealDTO === 
    public MealDTO getOneMealDTOForBackendDisplay(Long mealId) {
        MealEntity mealEntity = getOneMeal(mealId);
        if (mealEntity == null) {
            return null;
        }
        return toMealDTOForBackend(mealEntity);
    }


    /**
     * 【私有輔助方法】將 MealEntity 列表轉換為 MealDTO 列表。
     * 此方法專為後台數據準備，不填充 avgStars 且不處理收藏狀態。
     * @param meals 要轉換的 MealEntity 列表。
     * @return 轉換後的 MealDTO 列表。
     */
    private List<MealDTO> convertEntitiesToDTOsForBackend(List<MealEntity> meals) {
        if (meals.isEmpty()) {
            return new ArrayList<>();
        }
        List<MealDTO> dtoList = new ArrayList<>();
        for (MealEntity meal : meals) {
            MealDTO dto = toMealDTOForBackend(meal);
            dtoList.add(dto);
        }
        return dtoList;
    }

    private MealDTO toMealDTOForBackend(MealEntity meal) {
        MealDTO dto = new MealDTO();
        dto.setMealId(meal.getMealId());
        dto.setMealName(meal.getMealName());
        dto.setMealPrice(meal.getMealPrice()); // <--- 確保這裡使用 Long
        dto.setMealTypeName(meal.getMealType() != null ? meal.getMealType().getMealName() : "");
        dto.setMealPicUrl(buildMealPicUrl(meal.getMealPic()));
        dto.setMealPic(meal.getMealPic());

        dto.setAvgStars(0.0); // 後台不顯示平均星級，設為 0.0
        dto.setFavored(false); 
        dto.setFavMealId(null); 
        
        return dto;
    }
}

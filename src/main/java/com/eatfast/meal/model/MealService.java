package com.eatfast.meal.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eatfast.common.enums.MealStatus;
import com.eatfast.fav.model.FavRepository;
import com.eatfast.meal.dto.MealDTO;

@Service("mealService")
public class MealService {

	@Autowired
	MealRepository repository;
	
	@Autowired
    private FavRepository favRepository;  // 用於判斷有無收藏
	
	@Autowired
    private SessionFactory sessionFactory;
	
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
	
	// 查詢所有上架餐點(前台用)
	public List<MealEntity> getAllAVAILABLE() {
	    return repository.findByStatus(MealStatus.AVAILABLE); // 只找上架餐點
	}


	// === 前台：取得所有上架餐點，並標註會員是否已收藏 ===
    public List<MealDTO> getAllAvailableWithFavored(Long memberId) {
        List<MealEntity> meals = repository.findByStatus(MealStatus.AVAILABLE);
        return meals.stream()
                .map(meal -> toDTOWithFavored(meal, memberId))
                .collect(Collectors.toList());
    }

    // === 前台：依分類查詢，並標註會員是否已收藏 ===
    public List<MealDTO> getMealsByTypeWithFavored(Long mealTypeId, Long memberId) {
        List<MealEntity> meals = repository.findByMealTypeMealTypeId(mealTypeId);
        return meals.stream()
                .map(meal -> toDTOWithFavored(meal, memberId))
                .collect(Collectors.toList());
    }
    
	 // === 圖片 URL 處理 ===
	 public String buildMealPicUrl(String mealPic) {
	     if (mealPic == null || mealPic.isEmpty()) {
	         return "/images/nopic.png"; // 如果沒有圖片，回傳預設無圖片路徑
	     }
	     // 判斷是否為上傳的圖片 (例如檔名以 "upload_" 開頭或包含 UUID)
	     if (mealPic.startsWith("upload_") || mealPic.matches(".*[a-f0-9\\-]{36}.*")) {
	         // 這個路徑應與 MealPicResourceConfig 中的 addResourceHandler 一致
	         return "/meal-pic/" + mealPic; 
	     }
	     // 其他的舊圖或預設圖 (存在於 src/main/resources/static/images/meal_pic/ )
	     return "/images/meal_pic/" + mealPic; 
	 }


    // === Entity to DTO（關鍵轉換）===
    private MealDTO toDTOWithFavored(MealEntity meal, Long memberId) {
        boolean favored = false;
        if (memberId != null) {
            // 判斷該會員是否收藏過這道餐點
            favored = favRepository.existsByMemberMemberIdAndMealMealId(memberId, meal.getMealId());
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
        dto.setReviewTotalStars(meal.getReviewTotalStars());
        dto.setFavored(favored);
        return dto;
    }
	
}
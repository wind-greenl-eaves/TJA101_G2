package com.eatfast.meal.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eatfast.common.enums.MealStatus;

@Service("mealService")
public class MealService {

	@Autowired
	MealRepository repository;
	
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
	
}
package com.eatfast.mealtype.model;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
@Transactional // 管理資料庫交易（讀寫都包含）
public class MealTypeService {

    @Autowired
    private MealTypeRepository repository;
    
	@Autowired
    private SessionFactory sessionFactory;

    // 新增餐點類別
    public void addMealType(String mealName) {
        MealTypeEntity mealTypeEntity = new MealTypeEntity();
        mealTypeEntity.setMealName(mealName);
        repository.save(mealTypeEntity);
    }

    // 修改餐點類別
    public void updateMealType(Integer mealTypeId, String mealName) {
        MealTypeEntity mealTypeEntity = new MealTypeEntity();
        mealTypeEntity.setMealTypeId(mealTypeId);
        mealTypeEntity.setMealName(mealName);
        repository.save(mealTypeEntity);
    }

    // 查詢單一餐點類別
    public MealTypeEntity getOneMealType(Integer mealTypeId) {
        Optional<MealTypeEntity> optional = repository.findById(mealTypeId);
        return optional.orElse(null);// public T orElse(T other) : 如果值存在就回傳其值，否則回傳other的值
    }

    // 查詢全部餐點類別
    public List<MealTypeEntity> getAll() {
        return repository.findAll();
    }

}

package com.eatfast.mealtype.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MealTypeRepository extends JpaRepository<MealTypeEntity, Integer> {
    
}

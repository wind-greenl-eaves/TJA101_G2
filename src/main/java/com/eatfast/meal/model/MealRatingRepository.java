package com.eatfast.meal.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MealRatingRepository extends JpaRepository<MealEntity, Long> {
    @Query( // 使用原生SQL查詢所有餐點的平均評分
            value = "SELECT m.meal_id, " 
            		+ " IFNULL(ROUND(SUM(oli.review_stars * oli.quantity) / NULLIF(SUM(oli.quantity),0), 1), 0) "
                    + "AS avg_stars " 
            		+ "FROM meal m " 
                    + "LEFT JOIN order_list_info oli ON m.meal_id = oli.meal_id AND oli.review_stars > 0 " 
            		+ "GROUP BY m.meal_id",
            nativeQuery = true
        )
        List<Object[]> findAllMealAvgStars(); // 查詢所有餐點的平均評分，返回一個包含餐點ID和平均評分的Object數組列表
        
        
        
    }

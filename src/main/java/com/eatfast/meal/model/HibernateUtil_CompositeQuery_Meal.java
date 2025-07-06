package com.eatfast.meal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.eatfast.common.enums.MealStatus;
import com.eatfast.mealtype.model.MealTypeEntity;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;


public class HibernateUtil_CompositeQuery_Meal {
	public static Predicate get_aPredicate_For_AnyDB(CriteriaBuilder builder, Root<MealEntity> root, String columnName, String value) {

		Predicate predicate = null;
		
		if ("meal_id".equals(columnName)) // 用於Integer
			predicate = builder.equal(root.get(columnName), Integer.valueOf(value));
		else if ("meal_price".equals(columnName)) // 用於Integer
			predicate = builder.equal(root.get(columnName), Integer.valueOf(value));
		else if ("meal_name".equals(columnName)) // 用於varchar
			predicate = builder.like(root.get(columnName), "%" + value + "%");
		else if ("meal_type_id".equals(columnName)) {
			MealTypeEntity mealTypeEntity = new MealTypeEntity();
			mealTypeEntity.setMealTypeId(Long.valueOf(value));
			predicate = builder.equal(root.get("mealTypeEntity"), mealTypeEntity); // 用ManyToOne
		}

		return predicate;
	}

	@SuppressWarnings("unchecked")
	public static List<MealEntity> getAllC(Map<String, String[]> map, Session session) {
	    Transaction tx = session.beginTransaction();
	    List<MealEntity> list = null;

	    try {
	        CriteriaBuilder builder = session.getCriteriaBuilder();
	        CriteriaQuery<MealEntity> criteriaQuery = builder.createQuery(MealEntity.class);
	        Root<MealEntity> root = criteriaQuery.from(MealEntity.class);

	        List<Predicate> predicateList = new ArrayList<>();

	        // 餐點名稱 (模糊查詢)
	        String mealName = getParam(map, "mealName");
	        if (mealName != null && !mealName.isBlank()) {
	            predicateList.add(builder.like(root.get("mealName"), "%" + mealName.trim() + "%"));
	        }

	        // 餐點種類 (多對一關聯)
	        String mealTypeIdStr = getParam(map, "mealTypeId");
	        if (mealTypeIdStr != null && !mealTypeIdStr.isBlank()) {
	            predicateList.add(builder.equal(root.get("mealType").get("mealTypeId"), Long.valueOf(mealTypeIdStr.trim())));
	        }

	        // 餐點狀態 (Enum name 字串)
	        String statusStr = getParam(map, "status");
	        if (statusStr != null && !statusStr.isBlank()) {
	            predicateList.add(builder.equal(root.get("status"), MealStatus.valueOf(statusStr.trim())));
	        }

	        // 價格區間：startPrice (>=)
	        String startPriceStr = getParam(map, "startPrice");
	        if (startPriceStr != null && !startPriceStr.isBlank()) {
	            predicateList.add(builder.ge(root.get("mealPrice"), Long.valueOf(startPriceStr.trim())));
	        }

	        // 價格區間：endPrice (<=)
	        String endPriceStr = getParam(map, "endPrice");
	        if (endPriceStr != null && !endPriceStr.isBlank()) {
	            predicateList.add(builder.le(root.get("mealPrice"), Long.valueOf(endPriceStr.trim())));
	        }

	        criteriaQuery.where(predicateList.toArray(new Predicate[0]));
	        criteriaQuery.orderBy(builder.asc(root.get("mealId")));

	        list = session.createQuery(criteriaQuery).getResultList();
	        tx.commit();

	    } catch (RuntimeException ex) {
	        if (tx != null) tx.rollback();
	        throw ex;
	    } finally {
	        session.close();
	    }

	    return list;
	}

	// 取參數用的小工具方法
	private static String getParam(Map<String, String[]> map, String key) {
	    return map.containsKey(key) ? map.get(key)[0] : null;
	}

}

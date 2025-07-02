package com.eatfast.meal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.eatfast.mealtype.model.MealTypeEntity;

import jakarta.persistence.Query;
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

//		Session session = HibernateUtil.getSessionFactory().openSession(); // 缺點是要寫一隻HibernateUtil，所以直接aotowired就好
		Transaction tx = session.beginTransaction(); // 交易可以不用(此範例只是複製貼上而已)
		List<MealEntity> list = null;
		try {
			// 【●創建 CriteriaBuilder】 讓我們建立條件用的
			CriteriaBuilder builder = session.getCriteriaBuilder();
			// 【●創建 CriteriaQuery】 把永續類別的東西丟進來
			CriteriaQuery<MealEntity> criteriaQuery = builder.createQuery(MealEntity.class);
			// 【●創建 Root】 讓我們下條件用的
			Root<MealEntity> root = criteriaQuery.from(MealEntity.class);

			List<Predicate> predicateList = new ArrayList<Predicate>();
			
			Set<String> keys = map.keySet();
			int count = 0;
			for (String key : keys) {
				String value = map.get(key)[0];
				if (value != null && value.trim().length() != 0 && !"action".equals(key)) {
					count++;
					predicateList.add(get_aPredicate_For_AnyDB(builder, root, key, value.trim()));
					System.out.println("有送出查詢資料的欄位數count = " + count);
				}
			}
			System.out.println("predicateList.size()="+predicateList.size());
			criteriaQuery.where(predicateList.toArray(new Predicate[predicateList.size()]));
			criteriaQuery.orderBy(builder.asc(root.get("mealId")));
			// 【●最後完成創建 javax.persistence.Query●】
			Query query = session.createQuery(criteriaQuery); //javax.persistence.Query; //Hibernate 5 開始 取代原 org.hibernate.Query 介面
			list = query.getResultList();

			tx.commit();
		} catch (RuntimeException ex) {
			if (tx != null)
				tx.rollback();
			throw ex; // System.out.println(ex.getMessage());
		} finally {
			session.close();
			// HibernateUtil.getSessionFactory().close();
		}

		return list;
	}
}

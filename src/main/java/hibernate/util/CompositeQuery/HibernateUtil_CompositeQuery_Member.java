package hibernate.util.CompositeQuery; // 建議將工具類放在獨立的 util 套件中

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.query.Query;
import com.eatfast.member.model.MemberEntity;

public class HibernateUtil_CompositeQuery_Member {

	/**
	 * [不可變] 靜態方法，用於執行複合查詢。
	 * 
	 * @param map     [可變] 前端傳來的查詢條件，key 為欄位名，value 為查詢值。
	 * @param session [不可變] 由 Service 層傳入的 Hibernate Session。
	 * @return 查詢結果的 List<MemberEntity>。
	 */
	public static List<MemberEntity> getAllC(Map<String, String[]> map, Session session) {

		// 1. 透過 Session 取得 CriteriaBuilder 物件
		CriteriaBuilder builder = session.getCriteriaBuilder();
		// 2. 建立 CriteriaQuery 物件，並指定回傳的型別為 MemberEntity
		CriteriaQuery<MemberEntity> criteriaQuery = builder.createQuery(MemberEntity.class);
		// 3. 建立 Root 物件，代表我們要查詢的目標 Entity
		Root<MemberEntity> root = criteriaQuery.from(MemberEntity.class);

		List<Predicate> predicateList = new ArrayList<>();

		Set<String> keys = map.keySet();
		for (String key : keys) {
			String value = map.get(key)[0]; // 假設每個條件只會有一個值
			if (value != null && !value.trim().isEmpty()) {
				// 根據 map 的 key 動態新增查詢條件 (Predicate)
				// 使用 like 進行模糊比對
				if ("username".equals(key)) {
					predicateList.add(builder.like(root.get("username"), "%" + value + "%"));
				}
				if ("account".equals(key)) {
					predicateList.add(builder.like(root.get("account"), "%" + value + "%"));
				}
				if ("email".equals(key)) {
					predicateList.add(builder.like(root.get("email"), "%" + value + "%"));
				}
				if ("phone".equals(key)) {
					predicateList.add(builder.like(root.get("phone"), "%" + value + "%"));
				}
				// 若有其他欄位，可在此繼續添加...
			}
		}

		// 4. 將所有條件用 AND 連接
		criteriaQuery.where(builder.and(predicateList.toArray(new Predicate[0])));
		// 5. 執行查詢
		Query<MemberEntity> query = session.createQuery(criteriaQuery);

		return query.getResultList();
	}
}
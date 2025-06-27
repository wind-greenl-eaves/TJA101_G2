package hibernate.util.CompositeQuery;

import com.eatfast.news.model.NewsEntity;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.*;

public class NewsHibernateUtil_CompositeQuery {

    public static Predicate getPredicate(CriteriaBuilder builder, Root<NewsEntity> root, String column, String value) {
        Predicate predicate = null;

        switch (column) {
            case "newsId":
            case "employeeId":
                predicate = builder.equal(root.get(column), Long.valueOf(value));
                break;
            case "status":
                predicate = builder.equal(root.get(column), Integer.valueOf(value));
                break;
            case "title":
            case "content":
                predicate = builder.like(root.get(column), "%" + value + "%");
                break;
            case "startTimeFrom":
                predicate = builder.greaterThanOrEqualTo(root.get("startTime"), LocalDateTime.parse(value));
                break;
            case "startTimeTo":
                predicate = builder.lessThanOrEqualTo(root.get("startTime"), LocalDateTime.parse(value));
                break;
            case "endTimeFrom":
                predicate = builder.greaterThanOrEqualTo(root.get("endTime"), LocalDateTime.parse(value));
                break;
            case "endTimeTo":
                predicate = builder.lessThanOrEqualTo(root.get("endTime"), LocalDateTime.parse(value));
                break;
        }
        return predicate;
    }

    public static List<NewsEntity> getAllByCompositeQuery(Map<String, String[]> map, Session session) {
        Transaction tx = session.beginTransaction();
        List<NewsEntity> list = null;
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<NewsEntity> query = builder.createQuery(NewsEntity.class);
            Root<NewsEntity> root = query.from(NewsEntity.class);

            List<Predicate> predicates = new ArrayList<>();

            for (Map.Entry<String, String[]> entry : map.entrySet()) {
                String column = entry.getKey();
                String value = entry.getValue()[0];

                if (value != null && !value.trim().isEmpty() && !"action".equals(column)) {
                    Predicate p = getPredicate(builder, root, column, value.trim());
                    if (p != null) predicates.add(p);
                }
            }

            query.where(predicates.toArray(new Predicate[0]));
            query.orderBy(builder.desc(root.get("startTime")));

            Query q = session.createQuery(query);
            list = q.getResultList();

            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null) tx.rollback();
            throw ex;
        } finally {
            session.close();
        }

        return list;
    }
} 

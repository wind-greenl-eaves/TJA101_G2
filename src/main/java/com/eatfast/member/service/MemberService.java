package com.eatfast.member.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.eatfast.member.model.MemberEntity;
import com.eatfast.member.model.MemberRepository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * ★★★★★會員服務層 (Service)負責處理會員相關的業務邏輯，並作為 Controller 和 Repository 之間的中介。
 * 它封裝了資料存取的細節，向上層提供清晰的業務方法。
 * @Service - 標記這是一個服務層的組件，Spring 會自動管理它的生命週期。
 * @readOnly = true - 標記此方法為只讀，避免不必要的事務開銷。
 * @Optional<T> - Spring Data JPA 的 Optional 包裝類型，用於處理可能不存在的查詢結果(NullPointerException)。
 */
@Service
public class MemberService {
	
    // 注入 MemberRepository。
    @Autowired
    private MemberRepository memberRepository;
    /**
     * 新增或更新一筆會員資料。
     * Spring Data JPA 的 save() 方法會自動判斷：
     * 如果傳入的 MemberEntity 的主鍵 (memberId) 為 null，則執行新增 (INSERT)；
     * 如果主鍵存在，則執行更新 (UPDATE)。
     * @param memberEntity 要儲存的會員物件。
     * @return 儲存後 (包含新ID) 的會員物件。
     */
    @Transactional
    public MemberEntity saveOrUpdateMember(MemberEntity memberEntity) {
        return memberRepository.save(memberEntity);
    }
    /**
     * 根據會員ID刪除會員。
     */
    @Transactional
    public void deleteMemberById(Long memberId) {
        memberRepository.deleteById(memberId);
    }
    /**
     * 根據會員帳號刪除會員。
     */
    @Transactional
    public void deleteMemberByAccount(String account) {
        memberRepository.deleteByAccount(account);
    }
    /**
     * 根據會員ID查詢會員。
     * @return 一個包含會員物件的 Optional；如果找不到，則回傳一個空的 Optional。
     */
    @Transactional(readOnly = true)
    public Optional<MemberEntity> getMemberById(Long memberId) {
        // 直接回傳 Repository 回傳的 Optional，保持風格一致
        return memberRepository.findById(memberId);
    }
    /**
     * 根據會員帳號查詢會員。
     * @return 一個包含會員物件的 Optional；如果找不到，則回傳一個空的 Optional。
     */
    @Transactional(readOnly = true)
    public Optional<MemberEntity> getMemberByAccount(String account) {
        return memberRepository.findByAccount(account);
    }
    /**
     * 檢查指定的帳號或電子郵件是否已經存在。
     * @return 如果任一項已存在，回傳 true；否則回傳 false。
     */
    @Transactional(readOnly = true)
    public boolean memberExists(String account, String email) {
        return memberRepository.existsByAccountOrEmail(account, email);
    }
    /**
     * 查詢所有會員資料。
     * @return 包含所有會員的列表。
     */
    @Transactional(readOnly = true)
    public List<MemberEntity> getAllMembers() {
        return memberRepository.findAll();
    }
    /**
     * ★★★★★【複合查詢】根據多個動態條件查詢會員 (使用 JPA Specification)。★★★★★
     * @param map [可變] 一個 Map，其 Key 是查詢欄位(如 "username")，Value 是查詢值的字串陣列。
     * @return 符合查詢條件的會員列表。
     */
    @Transactional(readOnly = true)
    public List<MemberEntity> getAllMembers(Map<String, String[]> map) {
        
        // 這是「匿名內部類別」的寫法。
        // 直接 new 一個 Specification 介面，並在大括號中實作它唯一的方法 toPredicate。
    	// Specification 是一個 JPA 的查詢條件組合器，
        Specification<MemberEntity> spec = new Specification<MemberEntity>() {
            
            @Override
            public Predicate toPredicate(Root<MemberEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                
                List<Predicate> predicates = new ArrayList<>();
            
                for (Map.Entry<String, String[]> entry : map.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue()[0]; 

                    if (!StringUtils.hasText(value)) {
                        continue;
                    }

                    switch (key) {
                        case "memberId":
                            try {
                                predicates.add(criteriaBuilder.equal(root.get("memberId"), Long.valueOf(value)));
                            } catch (NumberFormatException e) {
                                // 忽略無效輸入
                            }
                            break;
                        case "username":
                            predicates.add(criteriaBuilder.like(root.get("username"), "%" + value + "%"));
                            break;
                        case "account":
                            predicates.add(criteriaBuilder.equal(root.get("account"), value));
                            break;
                        case "email":
                            predicates.add(criteriaBuilder.equal(root.get("email"), value));
                            break;
                        case "phone":
                            predicates.add(criteriaBuilder.like(root.get("phone"), "%" + value + "%"));
                            break;
                        case "birthday":
                            try {
                                LocalDate birthday = LocalDate.parse(value);
                                predicates.add(criteriaBuilder.equal(root.get("birthday"), birthday));
                            } catch (DateTimeParseException e) {
                                System.err.println("生日日期格式錯誤: " + value);
                            }
                            break;
                    }
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };

        // 將組合好的查詢條件 (spec) 交給 Repository 執行，這部分呼叫不變。
        return memberRepository.findAll(spec);
    }
}

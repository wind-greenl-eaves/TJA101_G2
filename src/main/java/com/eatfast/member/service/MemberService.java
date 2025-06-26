package com.eatfast.member.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// 引入 SLF4J 日誌框架
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.eatfast.member.model.MemberEntity;
import com.eatfast.member.repository.MemberRepository; 

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * ★★★★★會員服務層 (Service)負責處理會員相關的業務邏輯，並作為 Controller 和 Repository 之間的中介。
 * 它封裝了資料存取的細節，向上層提供清晰的業務方法。
 * @Service - 標記這是一個服務層的組件，Spring 會自動管理它的生命週期。
 * @Transactional(readOnly = true) - 在類別層級設定，預設所有公開方法都處於「唯讀」交易中，以提升查詢效能。
 * 需要寫入資料的方法則需單獨標記 @Transactional 來覆寫此設定。
 */
@Service
@Transactional(readOnly = true) 
public class MemberService {

    // 建立日誌物件 (Logger)，'log' 是約定俗成的變數名稱
    private static final Logger log = LoggerFactory.getLogger(MemberService.class);
	
    private final MemberRepository memberRepository;

    /**
     * 使用建構子注入 MemberRepository。
     * 這是 Spring 官方推薦的最佳實踐，能讓依賴關係更明確且易於單元測試。
     * @param memberRepository Spring 容器會自動傳入 MemberRepository 的實例。
     */
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * 新增或更新一筆會員資料。
     * 【注意】此處尚未實作密碼加密邏輯。
     */
    @Transactional
    public MemberEntity saveOrUpdateMember(MemberEntity memberEntity) {
        return memberRepository.save(memberEntity);
    }

    /**
     * 根據會員ID刪除會員。
     * 因為我們在 Entity 中使用了 @SQLDelete，此操作會觸發軟刪除。
     */
    @Transactional
    public void deleteMemberById(Long memberId) {
        memberRepository.deleteById(memberId);
    }

    /**
     * 根據會員帳號刪除會員。
     * 【已修改】呼叫 Repository 中正確的軟刪除方法。
     */
    @Transactional 
    public void deleteMemberByAccount(String account) {
        // 修正：呼叫 softDeleteByAccount，確保執行的是軟刪除
        memberRepository.softDeleteByAccount(account);
    }

    /**
     * 根據會員ID查詢會員。
     */
    public Optional<MemberEntity> getMemberById(Long memberId) {
        return memberRepository.findById(memberId);
    }

    /**
     * 根據會員帳號查詢會員。
     */
    public Optional<MemberEntity> getMemberByAccount(String account) {
        return memberRepository.findByAccount(account);
    }
    
    /**
     * 根據會員 Email 查詢會員。
     * @param email 要查詢的電子郵件
     * @return 包含會員的 Optional，如果找不到則為空
     */
    public Optional<MemberEntity> getMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    /**
     * 檢查指定的帳號或電子郵件是否已經存在。
     */
    public boolean memberExists(String account, String email) {
        return memberRepository.existsByAccountOrEmail(account, email);
    }

    /**
     * 查詢所有會員資料。
     */
    public List<MemberEntity> getAllMembers() {
        return memberRepository.findAll();
    }

    /**
     * ★★★★★【複合查詢】根據多個動態條件查詢會員 (使用 JPA Specification)。★★★★★
     * @param map [可變] 一個 Map，其 Key 是查詢欄位(如 "username")，Value 是查詢值的字串陣列。
     * @return 符合查詢條件的會員列表。
     */
    public List<MemberEntity> getAllMembers(Map<String, String[]> map) {
        
        Specification<MemberEntity> spec = new Specification<MemberEntity>() {
            
            @Override
            public Predicate toPredicate(Root<MemberEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                
                List<Predicate> predicates = new ArrayList<>();
            
                for (Map.Entry<String, String[]> entry : map.entrySet()) {
                    String key = entry.getKey();
                    if (entry.getValue() == null || entry.getValue().length == 0) {
                        continue;
                    }
                    String value = entry.getValue()[0]; 

                    if (!StringUtils.hasText(value)) {
                        continue;
                    }

                    switch (key) {
                        case "memberId":
                            try {
                                predicates.add(criteriaBuilder.equal(root.get("memberId"), Long.valueOf(value)));
                            } catch (NumberFormatException e) {
                                // 使用 Logger 紀錄警告訊息，而不是吞掉錯誤。
                                // {} 是參數佔位符，會安全地將 value 填入。
                                log.warn("複合查詢時，傳入的 memberId 格式無效: '{}'，此查詢條件已被忽略。", value);
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
                               // 紀錄日期格式錯誤的警告。
                               log.warn("複合查詢時，傳入的 birthday 日期格式無效: '{}'，此查詢條件已被忽略。", value);
                            }
                            break;
                    }
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };

        return memberRepository.findAll(spec);
    }
}
package com.eatfast.member.service;

import com.eatfast.member.model.MemberEntity;
import com.eatfast.member.repository.MemberRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder; // ★ 步驟1: 引入 PasswordEncoder
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * ★★★★★會員服務層 (Service) - 已整合密碼加密功能。
 */
@Service
@Transactional(readOnly = true) 
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);
	
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder; // ★ 步驟2: 宣告 PasswordEncoder 變數

    /**
     * ★ 步驟3: 修改建構子，注入 PasswordEncoder
     * 這是 Spring 官方推薦的最佳實踐，能讓依賴關係更明確且易於單元測試。
     * @param memberRepository Spring 容器會自動傳入 MemberRepository 的實例。
     * @param passwordEncoder Spring 容器會自動傳入我們在 SecurityConfig 中定義的 BCryptPasswordEncoder 實例。
     */
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder; // 將注入的實例指派給成員變數
    }
    
    /**
     * ★ 步驟4: 【核心邏輯】註冊新會員或重新啟用舊會員。
     * 這個方法專門處理來自註冊表單的請求，並包含完整的密碼加密流程。
     *
     * @param memberEntity 從 Controller 傳來、包含使用者在表單中填寫資料的 MemberEntity 物件。
     * @return 儲存到資料庫後、包含最終狀態 (如加密後密碼) 的 MemberEntity。
     */
    @Transactional // 標記為可寫入交易
    public MemberEntity registerOrReactivateMember(MemberEntity memberEntity) {
        
        // 取得從表單提交過來的原始密碼
        String rawPassword = memberEntity.getPassword();
        
        // 檢查原始密碼是否存在且不為空
        if (rawPassword != null && !rawPassword.trim().isEmpty()) {
            // ★ 使用注入的 passwordEncoder 對原始密碼進行加密
            String encodedPassword = passwordEncoder.encode(rawPassword);
            // ★ 將加密後的密碼設定回 memberEntity 物件
            memberEntity.setPassword(encodedPassword);
            log.info("帳號 {} 的密碼已成功加密。", memberEntity.getAccount());
        } else {
            // 如果是更新一個沒有提供密碼的會員資料，就不處理密碼欄位
            // 在「重新啟用」的案例中，我們需要確保密碼欄位有被處理
            // 此處的邏輯假設「重新啟用」時一定會要求使用者重設密碼
            log.warn("註冊或重新啟用會員 {} 時未提供密碼。", memberEntity.getAccount());
        }

        // 呼叫 repository 的 save 方法，此時傳入的 memberEntity 中的密碼已經是加密過的了
        return memberRepository.save(memberEntity);
    }
    
    /**
     * 【更新專用】更新現有會員的基本資料。
     * 這個方法被設計為不處理密碼欄位，以避免意外覆蓋掉已加密的密碼。
     *
     * @param memberToUpdate 從 Controller 傳來、包含要更新資料的 MemberEntity。
     * @return 更新後的 MemberEntity。
     */
    @Transactional
    public MemberEntity updateMemberDetails(MemberEntity memberToUpdate) {
        // 從資料庫讀取最新的會員資料
        return memberRepository.findById(memberToUpdate.getMemberId()).map(dbMember -> {
            // 將表單中的資料更新到從資料庫讀取出的物件上
            dbMember.setUsername(memberToUpdate.getUsername());
            dbMember.setEmail(memberToUpdate.getEmail());
            dbMember.setPhone(memberToUpdate.getPhone());
            dbMember.setBirthday(memberToUpdate.getBirthday());
            dbMember.setGender(memberToUpdate.getGender());
            dbMember.setEnabled(memberToUpdate.isEnabled());
            // **注意**：此處【刻意不】設定密碼 (dbMember.setPassword(...))
            
            return memberRepository.save(dbMember); // 儲存更新
        }).orElseThrow(() -> new RuntimeException("找不到ID為 " + memberToUpdate.getMemberId() + " 的會員"));
    }


    /**
     * 根據會員ID刪除會員。
     * 因為我們在 Entity 中使用了 @SQLDelete，此操作會觸發軟刪除。
     */
    @Transactional
    public void deleteMemberById(Long memberId) {
        memberRepository.deleteById(memberId);
    }

    // ... 其他既有的查詢方法維持不變 ...
    public Optional<MemberEntity> getMemberById(Long memberId) {
        return memberRepository.findById(memberId);
    }

    public Optional<MemberEntity> getMemberByAccount(String account) {
        return memberRepository.findByAccount(account);
    }
    
    public Optional<MemberEntity> getMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public boolean memberExists(String account, String email) {
        return memberRepository.existsByAccountOrEmail(account, email);
    }

    public List<MemberEntity> getAllMembers() {
        return memberRepository.findAll();
    }
    
    public Optional<MemberEntity> getMemberByAccountIncludeDisabled(String account) {
        return memberRepository.findByAccountIncludeDisabled(account);
    }

    public Optional<MemberEntity> getMemberByEmailIncludeDisabled(String email) {
        return memberRepository.findByEmailIncludeDisabled(email);
    }

    public List<MemberEntity> getAllMembers(Map<String, String[]> map) {
        try {
            log.debug("開始建構複合查詢條件，接收到的參數: {}", map);
            
            Specification<MemberEntity> spec = (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                if (map != null && !map.isEmpty()) {
                    for (Map.Entry<String, String[]> entry : map.entrySet()) {
                        String key = entry.getKey();
                        String[] values = entry.getValue();
                        
                        // 改進的空值檢查
                        if (values == null || values.length == 0 || values[0] == null) {
                            log.debug("欄位 {} 的值為空，跳過此條件", key);
                            continue;
                        }
                        
                        String value = values[0].trim();
                        if (value.isEmpty()) {
                            log.debug("欄位 {} 的值為空字串，跳過此條件", key);
                            continue;
                        }
                        
                        log.debug("處理查詢條件 - 欄位: {}, 值: {}", key, value);
                        
                        try {
                            // 根據不同欄位使用不同的查詢策略
                            switch (key) {
                                case "username":
                                    predicates.add(criteriaBuilder.like(
                                        root.get("username"),
                                        "%" + value + "%"
                                    ));
                                    break;
                                case "email":
                                    predicates.add(criteriaBuilder.like(
                                        root.get("email"),
                                        "%" + value + "%"
                                    ));
                                    break;
                                case "phone":
                                    predicates.add(criteriaBuilder.like(
                                        root.get("phone"),
                                        "%" + value + "%"
                                    ));
                                    break;
                                default:
                                    log.debug("未知的查詢欄位: {}", key);
                                    break;
                            }
                        } catch (Exception e) {
                            log.error("建立查詢條件時發生錯誤 - 欄位: {}, 值: {}, 錯誤: {}", key, value, e.getMessage());
                        }
                    }
                }
                
                log.debug("建立了 {} 個查詢條件", predicates.size());
                
                return predicates.isEmpty() ? 
                    criteriaBuilder.conjunction() : 
                    criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };

            List<MemberEntity> results = memberRepository.findAll(spec);
            log.debug("查詢完成，返回 {} 筆結果", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("執行複合查詢時發生錯誤: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}
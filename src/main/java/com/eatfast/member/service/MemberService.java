package com.eatfast.member.service;

// 引入 DTO 和 Mapper
import com.eatfast.member.dto.MemberCreateRequest;
import com.eatfast.member.dto.MemberUpdateRequest;
import com.eatfast.member.dto.PasswordUpdateRequest;
import com.eatfast.member.dto.ForgotPasswordRequest;
import com.eatfast.member.dto.ResetPasswordRequest;
import com.eatfast.member.mapper.MemberMapper;
// 【新增】引入郵件服務
import com.eatfast.common.service.EmailService;
// (既有 import)
import com.eatfast.member.model.MemberEntity;
import com.eatfast.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/*
 * ================================================================
 * 		會員服務層 (MemberService) - ★★★ 附 changePassword 方法最終版 ★★★
 * ================================================================
 * - 存放目錄: src/main/java/com/eatfast/member/service/MemberService.java
 * - 作用: 業務邏輯核心。現在，它的公開方法簽名完全與 Entity 解耦，
 * - 僅依賴於 DTO，大幅提升了架構的穩定性與安全性。
 */

@Service
@Transactional(readOnly = true) 
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);
	
    // 不可變動的 final 宣告，確保依賴在建構後不被修改。
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    // 【新增】注入郵件服務
    private final EmailService emailService;

    // 依賴注入的標準建構子模式
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }
    /**
     * 【新方法】變更會員密碼。
     * - @Transactional: (不可變的 Spring 關鍵字)
     * 此處沒有 readOnly=true，覆蓋了類別層級的設定，
     * 明確表示這是一個「寫入」操作，必須在一個完整的交易中執行。
     * 若任一步驟失敗，所有資料庫變更都會被回滾 (Rollback)。
     * - @param request: 可自定義的參數名稱，代表從 Controller 傳來的密碼更新 DTO。
     * - @throws EntityNotFoundException 如果找不到對應ID的會員。
     * - @throws IllegalArgumentException 如果舊密碼驗證失敗。
     */
    @Transactional
    public void changePassword(PasswordUpdateRequest request) {
        // 1. 【資料庫讀取路徑】根據 ID 從 Repository 取得最新的會員實體。
        // orElseThrow 是處理 Optional 的標準做法，若找不到則直接拋出例外，終止後續流程。
        MemberEntity member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new EntityNotFoundException("找不到ID為 " + request.getMemberId() + " 的會員"));

        // 2. 【核心安全驗證】
        // - passwordEncoder.matches() 是 Spring Security 提供的標準驗證方法。
        // - 它的內部機制是：將使用者輸入的「舊密碼明文」(request.getOldPassword())
        //   使用與資料庫中儲存的密碼相同的演算法和鹽值 (salt) 進行一次加密，
        //   然後比對加密後的結果是否與資料庫中的雜湊值 (member.getPassword()) 完全一致。
        // - 絕對不可使用 `request.getOldPassword().equals(member.getPassword())` 這種方式比對！
        if (!passwordEncoder.matches(request.getOldPassword(), member.getPassword())) {
            // 如果驗證失敗，拋出明確的業務例外，由 Controller 層捕獲並提示使用者。
            throw new IllegalArgumentException("舊密碼不正確");
        }
        
        // 3. 【加密與更新】驗證通過後，將使用者提供的「新密碼明文」加密。
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        member.setPassword(encodedNewPassword);
        
        // 4. 【資料庫寫入路徑】呼叫 save 方法。
        // - JPA 的持久性上下文 (Persistence Context) 會偵測到 member 物件的 password 欄位已變更。
        // - 若 MemberEntity 上有 @DynamicUpdate，此處會產生一條只更新 password 欄位的 UPDATE SQL。
        memberRepository.save(member);
        log.info("會員 ID {} 的密碼已成功更新。", member.getMemberId());
    }
    
    /**
     * 【核心邏輯 - DTO 重構版】註冊新會員。
     * @param createRequest 可自定義的參數名稱，代表從 Controller 傳來的註冊資料 DTO。
     * @return 儲存到資料庫後、包含最終狀態的 MemberEntity。
     * @throws IllegalArgumentException 當帳號或 Email 已被使用時拋出。
     */
    @Transactional
    public MemberEntity registerMember(MemberCreateRequest createRequest) {
        // 1. 檢查帳號或 Email 是否已被註冊 (只查活躍帳號)
        if (memberRepository.existsByAccountOrEmail(createRequest.getAccount(), createRequest.getEmail())) {
            // 拋出明確的業務例外，由 Controller 層去處理後續流程
            throw new IllegalArgumentException("帳號或 Email 已被註冊。");
        }

        // 2. 檢查帳號是否對應到「已停用」的帳號
        Optional<MemberEntity> disabledAccountOpt = memberRepository.findByAccountIncludeDisabled(createRequest.getAccount());
        if (disabledAccountOpt.isPresent() && !disabledAccountOpt.get().isEnabled()) {
            MemberEntity memberToReactivate = disabledAccountOpt.get();
            log.info("偵測到已停用的帳號 {}，將進行重新啟用並更新資料。", createRequest.getAccount());
            // 更新資料並重新啟用
            memberToReactivate.setUsername(createRequest.getUsername());
            memberToReactivate.setPassword(passwordEncoder.encode(createRequest.getPassword()));
            memberToReactivate.setEmail(createRequest.getEmail());
            memberToReactivate.setPhone(createRequest.getPhone());
            memberToReactivate.setBirthday(createRequest.getBirthday());
            memberToReactivate.setGender(createRequest.getGender());
            memberToReactivate.setEnabled(true); // 重新啟用
            return memberRepository.save(memberToReactivate);
        }

        // 3. 如果是全新帳號，則將 DTO 轉換為 Entity 並儲存
        log.info("全新帳號 {} 註冊。", createRequest.getAccount());
        MemberEntity newMember = MemberMapper.toEntity(createRequest, passwordEncoder);
        return memberRepository.save(newMember);
    }
    
    /**
     * 【更新專用 - DTO 重構版】更新現有會員的基本資料。
     * @param updateRequest 從 Controller 傳來、包含要更新資料的 DTO。
     * @return 更新後的 MemberEntity。
     * @throws EntityNotFoundException 如果找不到對應ID的會員。
     */
    @Transactional
    public MemberEntity updateMemberDetails(MemberUpdateRequest updateRequest) {
        // 從資料庫讀取最新的會員資料
        MemberEntity dbMember = memberRepository.findById(updateRequest.getMemberId())
            .orElseThrow(() -> new EntityNotFoundException("找不到ID為 " + updateRequest.getMemberId() + " 的會員"));

        // 將 DTO 中的資料更新到從資料庫讀取出的物件上
        dbMember.setUsername(updateRequest.getUsername());
        dbMember.setEmail(updateRequest.getEmail());
        dbMember.setPhone(updateRequest.getPhone());
        dbMember.setBirthday(updateRequest.getBirthday());
        dbMember.setGender(updateRequest.getGender());
        dbMember.setEnabled(updateRequest.getIsEnabled());
        // **注意**：此處【刻意不】處理密碼，保持了更新操作的安全性。
        
        return memberRepository.save(dbMember); // 儲存更新
    }


    /**
     * 根據會員ID刪除會員 (軟刪除)。
     */
    @Transactional
    public void deleteMemberById(Long memberId) {
        memberRepository.deleteById(memberId);
    }

    /**
     * 處理忘記密碼請求
     * 根據電子郵件查找會員並生成重設密碼的 Token
     * 
     * @param request 忘記密碼請求，包含電子郵件
     * @return 重設密碼的 Token（同時透過郵件發送）
     * @throws EntityNotFoundException 如果找不到對應的會員
     */
    @Transactional
    public String processForgotPassword(ForgotPasswordRequest request) {
        // 根據電子郵件查找會員
        Optional<MemberEntity> memberOpt = memberRepository.findByEmail(request.getEmail());
        
        if (memberOpt.isEmpty()) {
            throw new EntityNotFoundException("找不到使用此電子郵件的會員帳號：" + request.getEmail());
        }
        
        MemberEntity member = memberOpt.get();
        
        // 檢查帳號是否啟用
        if (!member.isEnabled()) {
            throw new IllegalArgumentException("此帳號已被停用，無法重設密碼");
        }
        
        // 生成重設密碼的 Token（簡化版本，實際應用中應該更安全）
        String resetToken = generateResetToken(member);
        
        try {
            // 【修改】建構完整的重設密碼 URL
            String resetUrl = "http://localhost:8080/api/v1/auth/reset-password?token=" + resetToken;
            
            // 【新增】發送郵件到統一郵箱
            emailService.sendPasswordResetEmail(
                member.getEmail(),           // 會員原本的信箱（用於識別）
                member.getAccount(),         // 會員帳號
                member.getUsername(),        // 會員姓名
                resetToken,                  // 重設 Token
                resetUrl                     // 完整重設 URL
            );
            
            log.info("會員 {} 請求重設密碼成功，郵件已發送到 young19960127@gmail.com", member.getAccount());
            
        } catch (Exception e) {
            log.error("發送重設密碼郵件失敗 - 會員: {} ({}), 錯誤: {}", 
                member.getAccount(), member.getUsername(), e.getMessage(), e);
            
            // 可以選擇是否要拋出例外，或者只記錄錯誤但繼續處理
            throw new RuntimeException("郵件發送失敗，請稍後再試或聯繫客服", e);
        }
        
        return resetToken;
    }
    
    /**
     * 處理密碼重設請求
     * 驗證 Token 並更新會員密碼
     * 
     * @param request 密碼重設請求，包含 Token 和新密碼
     * @throws EntityNotFoundException 如果 Token 無效或會員不存在
     * @throws IllegalArgumentException 如果密碼不符合要求
     */
    @Transactional
    public void processResetPassword(ResetPasswordRequest request) {
        // 檢查密碼是否一致
        if (!request.isPasswordMatching()) {
            throw new IllegalArgumentException("兩次輸入的密碼不一致");
        }
        
        // 驗證並解析 Token
        Long memberId = validateAndParseResetToken(request.getToken());
        
        // 查找會員
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("找不到對應的會員"));
        
        // 檢查帳號是否啟用
        if (!member.isEnabled()) {
            throw new IllegalArgumentException("此帳號已被停用，無法重設密碼");
        }
        
        // 更新密碼
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        member.setPassword(encodedPassword);
        memberRepository.save(member);
        
        log.info("會員 {} 成功重設密碼", member.getAccount());
    }
    
    /**
     * 生成重設密碼的 Token
     * 簡化版本：使用 Base64 編碼的會員ID和時間戳
     * 實際應用中應該使用更安全的方式，如 JWT
     */
    private String generateResetToken(MemberEntity member) {
        String tokenData = member.getMemberId() + ":" + System.currentTimeMillis();
        return Base64.getEncoder().encodeToString(tokenData.getBytes());
    }
    
    /**
     * 驗證並解析重設密碼的 Token
     * 
     * @param token 重設密碼的 Token
     * @return 會員ID
     * @throws IllegalArgumentException 如果 Token 無效或過期
     */
    private Long validateAndParseResetToken(String token) {
        try {
            String tokenData = new String(Base64.getDecoder().decode(token));
            String[] parts = tokenData.split(":");
            
            if (parts.length != 2) {
                throw new IllegalArgumentException("Token 格式無效");
            }
            
            Long memberId = Long.parseLong(parts[0]);
            long timestamp = Long.parseLong(parts[1]);
            
            // 檢查 Token 是否過期（24小時有效期）
            long currentTime = System.currentTimeMillis();
            long validDuration = 24 * 60 * 60 * 1000; // 24小時
            
            if (currentTime - timestamp > validDuration) {
                throw new IllegalArgumentException("重設連結已過期，請重新申請");
            }
            
            return memberId;
            
        } catch (Exception e) {
            throw new IllegalArgumentException("無效的重設連結：" + e.getMessage());
        }
    }
    
    // --- 以下為查詢相關方法，維持不變 ---
    
    public Optional<MemberEntity> getMemberById(Long memberId) {
        return memberRepository.findById(memberId);
    }

    public Optional<MemberEntity> getMemberByAccount(String account) {
        return memberRepository.findByAccount(account);
    }
    
    public Optional<MemberEntity> getMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
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
                // (複合查詢邏輯維持不變) ...
                if (map != null && !map.isEmpty()) {
                    for (Map.Entry<String, String[]> entry : map.entrySet()) {
                        String key = entry.getKey();
                        String[] values = entry.getValue();
                        
                        if (values == null || values.length == 0 || values[0] == null) {
                            continue;
                        }
                        String value = values[0].trim();
                        if (value.isEmpty()) {
                            continue;
                        }
                        
                        switch (key) {
                            case "username":
                                predicates.add(criteriaBuilder.like(root.get("username"), "%" + value + "%"));
                                break;
                            case "email":
                                predicates.add(criteriaBuilder.like(root.get("email"), "%" + value + "%"));
                                break;
                            case "phone":
                                predicates.add(criteriaBuilder.like(root.get("phone"), "%" + value + "%"));
                                break;
                        }
                    }
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };

            return memberRepository.findAll(spec);
            
        } catch (Exception e) {
            log.error("執行複合查詢時發生錯誤: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}
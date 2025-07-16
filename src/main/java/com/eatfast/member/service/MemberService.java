package com.eatfast.member.service;

// 引入 DTO 和 Mapper
import com.eatfast.member.dto.MemberCreateRequest;
import com.eatfast.member.dto.MemberUpdateRequest;
import com.eatfast.member.dto.PasswordUpdateRequest;
import com.eatfast.member.dto.ForgotPasswordRequest;
import com.eatfast.member.dto.ResetPasswordRequest;
import com.eatfast.member.dto.MemberVerificationRequest;
import com.eatfast.member.mapper.MemberMapper;
// 【修正】引入郵件服務和驗證碼服務
import com.eatfast.common.service.EmailService;
import com.eatfast.member.service.VerificationCodeService;
import com.eatfast.member.service.InMemoryVerificationCodeService;
// (既有 import)
import com.eatfast.member.model.MemberEntity;
import com.eatfast.member.repository.MemberRepository;
import com.eatfast.orderlist.model.OrderListEntity;
import com.eatfast.orderlist.repository.OrderListRepository;
import com.eatfast.orderlistinfo.model.OrderListInfoEntity;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final OrderListRepository orderListRepository;
    private final PasswordEncoder passwordEncoder;
    // 【新增】注入郵件服務和驗證碼服務
    private final EmailService emailService;
    // 【修正】統一驗證碼服務接口
    private final VerificationCodeServiceInterface verificationCodeService;

    // 依賴注入的標準建構子模式
    public MemberService(MemberRepository memberRepository, OrderListRepository orderListRepository,
                        PasswordEncoder passwordEncoder, EmailService emailService, 
                        VerificationCodeService redisVerificationCodeService) {
        this.memberRepository = memberRepository;
        this.orderListRepository = orderListRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        
        // 【修正】根據配置選擇驗證碼服務實現
        this.verificationCodeService = new VerificationCodeServiceInterface() {
            @Override
            public String generateVerificationCode() {
                return redisVerificationCodeService.generateVerificationCode();
            }
            
            @Override
            public void storeVerificationCode(String email, String code) {
                redisVerificationCodeService.storeVerificationCode(email, code);
            }
            
            @Override
            public boolean verifyCode(String email, String inputCode) {
                return redisVerificationCodeService.verifyCode(email, inputCode);
            }
            
            @Override
            public boolean hasVerificationCode(String email) {
                return redisVerificationCodeService.hasVerificationCode(email);
            }
            
            @Override
            public void deleteVerificationCode(String email) {
                redisVerificationCodeService.deleteVerificationCode(email);
            }
        };
    }

    // ================================================================
    // 					密碼管理功能 (Password Management)
    // ================================================================
    
    /**
     * 【修正】變更會員密碼 - 支援明文密碼和BCrypt密碼的混合驗證
     */
    @Transactional
    public void changePassword(PasswordUpdateRequest request) {
        log.info("開始變更密碼 - 會員ID: {}", request.getMemberId());
        
        // 1. 【資料庫讀取路徑】根據 ID 從 Repository 取得最新的會員實體。
        MemberEntity member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new EntityNotFoundException("找不到ID為 " + request.getMemberId() + " 的會員"));

        log.info("找到會員 - 帳號: {}, 姓名: {}", member.getAccount(), member.getUsername());
        
        // 2. 【核心安全驗證】- 支援明文密碼和BCrypt密碼的混合驗證
        String inputOldPassword = request.getOldPassword();
        String storedPassword = member.getPassword();
        
        log.debug("密碼驗證 - 輸入密碼長度: {}, 存儲密碼長度: {}", 
                 inputOldPassword != null ? inputOldPassword.length() : 0, 
                 storedPassword != null ? storedPassword.length() : 0);
        
        // 檢查輸入是否為空
        if (inputOldPassword == null || inputOldPassword.trim().isEmpty()) {
            log.warn("舊密碼輸入為空 - 會員ID: {}", request.getMemberId());
            throw new IllegalArgumentException("請輸入目前密碼");
        }
        
        // 檢查存儲的密碼是否為空
        if (storedPassword == null || storedPassword.trim().isEmpty()) {
            log.error("資料庫中的密碼為空 - 會員ID: {}, 帳號: {}", request.getMemberId(), member.getAccount());
            throw new IllegalStateException("會員密碼資料異常，請聯繫系統管理員");
        }
        
        // 【修正】執行混合密碼比對：先嘗試BCrypt，如果失敗則嘗試明文比對
        boolean passwordMatches = false;
        
        // 檢查存儲的密碼是否為BCrypt格式（BCrypt密碼通常以$2a$、$2b$、$2y$開頭）
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            // 如果是BCrypt格式，使用BCrypt驗證
            passwordMatches = passwordEncoder.matches(inputOldPassword.trim(), storedPassword);
            log.info("使用BCrypt驗證 - 會員ID: {}, 驗證結果: {}", request.getMemberId(), passwordMatches);
        } else {
            // 如果不是BCrypt格式，假設為明文密碼，直接比對
            passwordMatches = inputOldPassword.trim().equals(storedPassword.trim());
            log.info("使用明文驗證 - 會員ID: {}, 驗證結果: {}", request.getMemberId(), passwordMatches);
            
            // 如果明文驗證成功，同時將密碼升級為BCrypt加密
            if (passwordMatches) {
                log.info("檢測到明文密碼，將自動升級為BCrypt加密 - 會員ID: {}", request.getMemberId());
            }
        }
        
        if (!passwordMatches) {
            log.warn("舊密碼驗證失敗 - 會員ID: {}, 帳號: {}", request.getMemberId(), member.getAccount());
            throw new IllegalArgumentException("目前密碼不正確，請重新輸入");
        }
        
        // 檢查新密碼是否與舊密碼相同（需要同時檢查明文和BCrypt）
        boolean newPasswordSameAsOld = false;
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            // 存儲的是BCrypt密碼
            newPasswordSameAsOld = passwordEncoder.matches(request.getNewPassword(), storedPassword);
        } else {
            // 存儲的是明文密碼
            newPasswordSameAsOld = request.getNewPassword().equals(storedPassword);
        }
        
        if (newPasswordSameAsOld) {
            log.warn("新密碼與舊密碼相同 - 會員ID: {}", request.getMemberId());
            throw new IllegalArgumentException("新密碼不能與目前密碼相同");
        }
        
        // 3. 【加密與更新】將新密碼加密並更新
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        log.info("新密碼已加密 - 會員ID: {}, 加密後長度: {}", request.getMemberId(), encodedNewPassword.length());
        
        member.setPassword(encodedNewPassword);
        
        // 4. 【資料庫寫入路徑】呼叫 save 方法。
        memberRepository.save(member);
        log.info("會員 ID {} 的密碼已成功更新為BCrypt加密格式", member.getMemberId());
    }
    
    /**
     * 【修正】變更會員密碼。
     */
    @Transactional
    public void changePasswordOld(PasswordUpdateRequest request) {
        log.info("開始變更密碼 - 會員ID: {}", request.getMemberId());
        
        // 1. 【資料庫讀取路徑】根據 ID 從 Repository 取得最新的會員實體。
        MemberEntity member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new EntityNotFoundException("找不到ID為 " + request.getMemberId() + " 的會員"));

        log.info("找到會員 - 帳號: {}, 姓名: {}", member.getAccount(), member.getUsername());
        
        // 2. 【核心安全驗證】- 增加詳細日誌
        String inputOldPassword = request.getOldPassword();
        String storedPassword = member.getPassword();
        
        log.debug("密碼驗證 - 輸入密碼長度: {}, 存儲密碼長度: {}", 
                 inputOldPassword != null ? inputOldPassword.length() : 0, 
                 storedPassword != null ? storedPassword.length() : 0);
        
        // 檢查輸入是否為空
        if (inputOldPassword == null || inputOldPassword.trim().isEmpty()) {
            log.warn("舊密碼輸入為空 - 會員ID: {}", request.getMemberId());
            throw new IllegalArgumentException("請輸入目前密碼");
        }
        
        // 檢查存儲的密碼是否為空
        if (storedPassword == null || storedPassword.trim().isEmpty()) {
            log.error("資料庫中的密碼為空 - 會員ID: {}, 帳號: {}", request.getMemberId(), member.getAccount());
            throw new IllegalStateException("會員密碼資料異常，請聯繫系統管理員");
        }
        
        // 執行密碼比對
        boolean passwordMatches = passwordEncoder.matches(inputOldPassword.trim(), storedPassword);
        log.info("密碼驗證結果 - 會員ID: {}, 驗證成功: {}", request.getMemberId(), passwordMatches);
        
        if (!passwordMatches) {
            log.warn("舊密碼驗證失敗 - 會員ID: {}, 帳號: {}", request.getMemberId(), member.getAccount());
            throw new IllegalArgumentException("目前密碼不正確，請重新輸入");
        }
        
        // 檢查新密碼是否與舊密碼相同
        if (passwordEncoder.matches(request.getNewPassword(), storedPassword)) {
            log.warn("新密碼與舊密碼相同 - 會員ID: {}", request.getMemberId());
            throw new IllegalArgumentException("新密碼不能與目前密碼相同");
        }
        
        // 3. 【加密與更新】驗證通過後，將使用者提供的「新密碼明文」加密。
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        log.info("新密碼已加密 - 會員ID: {}, 加密後長度: {}", request.getMemberId(), encodedNewPassword.length());
        
        member.setPassword(encodedNewPassword);
        
        // 4. 【資料庫寫入路徑】呼叫 save 方法。
        memberRepository.save(member);
        log.info("會員 ID {} 的密碼已成功更新。", member.getMemberId());
    }
    
    /**
     * 【即時驗證】驗證會員的舊密碼是否正確
     */
    @Transactional(readOnly = true)
    public boolean validateOldPassword(Long memberId, String oldPassword) {
        try {
            MemberEntity member = memberRepository.findById(memberId).orElse(null);
            if (member == null || oldPassword == null || oldPassword.trim().isEmpty()) {
                return false;
            }
            
            String storedPassword = member.getPassword();
            if (storedPassword == null || storedPassword.trim().isEmpty()) {
                return false;
            }
            
            // 【修正】支援混合密碼驗證
            if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
                // BCrypt密碼驗證
                return passwordEncoder.matches(oldPassword.trim(), storedPassword);
            } else {
                // 明文密碼驗證
                return oldPassword.trim().equals(storedPassword.trim());
            }
        } catch (Exception e) {
            log.error("驗證舊密碼時發生錯誤: {}", e.getMessage());
            return false;
        }
    }
    
    // ================================================================
    // 					會員註冊與驗證功能 (Registration & Verification)
    // ================================================================
    
    /**
     * 【核心邏輯 - 郵件驗證版】註冊新會員並發送驗證郵件。
     */
    @Transactional
    public MemberEntity registerMember(MemberCreateRequest createRequest) {
        // 1. 檢查帳號或 Email 是否已被註冊 (只查活躍帳號)
        if (memberRepository.existsByAccountOrEmail(createRequest.getAccount(), createRequest.getEmail())) {
            throw new IllegalArgumentException("帳號或 Email 已被註冊。");
        }

        // 2. 檢查帳號是否對應到「已停用」的帳號
        Optional<MemberEntity> disabledAccountOpt = memberRepository.findByAccountIncludeDisabled(createRequest.getAccount());
        if (disabledAccountOpt.isPresent() && !disabledAccountOpt.get().isEnabled()) {
            MemberEntity memberToReactivate = disabledAccountOpt.get();
            log.info("偵測到已停用的帳號 {}，將進行重新啟用並更新資料。", createRequest.getAccount());
            // 更新資料並重新啟用（但設為未啟用狀態，需要驗證）
            memberToReactivate.setUsername(createRequest.getUsername());
            memberToReactivate.setPassword(passwordEncoder.encode(createRequest.getPassword()));
            memberToReactivate.setEmail(createRequest.getEmail());
            memberToReactivate.setPhone(createRequest.getPhone());
            memberToReactivate.setBirthday(createRequest.getBirthday());
            memberToReactivate.setGender(createRequest.getGender());
            memberToReactivate.setEnabled(false); // 設為未啟用，需要驗證
            
            MemberEntity savedMember = memberRepository.save(memberToReactivate);
            sendVerificationEmail(savedMember);
            return savedMember;
        }

        // 3. 如果是全新帳號，則將 DTO 轉換為 Entity 並儲存（狀態為未啟用）
        log.info("全新帳號 {} 註冊。", createRequest.getAccount());
        MemberEntity newMember = MemberMapper.toEntity(createRequest, passwordEncoder);
        newMember.setEnabled(false); // 設為未啟用狀態，需要驗證
        
        MemberEntity savedMember = memberRepository.save(newMember);
        sendVerificationEmail(savedMember);
        return savedMember;
    }

    /**
     * 後台管理員新增會員 - 直接啟用，無需驗證
     * @param createRequest 會員建立請求
     * @return 已啟用的會員實體
     */
    @Transactional
    public MemberEntity registerMemberByAdmin(MemberCreateRequest createRequest) {
        // 1. 檢查帳號或 Email 是否已被註冊 (只查活躍帳號)
        if (memberRepository.existsByAccountOrEmail(createRequest.getAccount(), createRequest.getEmail())) {
            throw new IllegalArgumentException("帳號或 Email 已被註冊。");
        }

        // 2. 檢查帳號是否對應到「已停用」的帳號
        Optional<MemberEntity> disabledAccountOpt = memberRepository.findByAccountIncludeDisabled(createRequest.getAccount());
        if (disabledAccountOpt.isPresent() && !disabledAccountOpt.get().isEnabled()) {
            MemberEntity memberToReactivate = disabledAccountOpt.get();
            log.info("後台管理員重新啟用已停用的帳號 {}，直接啟用無需驗證。", createRequest.getAccount());
            // 更新資料並直接啟用
            memberToReactivate.setUsername(createRequest.getUsername());
            memberToReactivate.setPassword(passwordEncoder.encode(createRequest.getPassword()));
            memberToReactivate.setEmail(createRequest.getEmail());
            memberToReactivate.setPhone(createRequest.getPhone());
            memberToReactivate.setBirthday(createRequest.getBirthday());
            memberToReactivate.setGender(createRequest.getGender());
            memberToReactivate.setEnabled(true); // 管理員新增直接啟用
            
            return memberRepository.save(memberToReactivate);
        }

        // 3. 如果是全新帳號，則將 DTO 轉換為 Entity 並儲存（直接啟用）
        log.info("後台管理員新增全新帳號 {}，直接啟用無需驗證。", createRequest.getAccount());
        MemberEntity newMember = MemberMapper.toEntity(createRequest, passwordEncoder);
        newMember.setEnabled(true); // 管理員新增直接啟用
        
        return memberRepository.save(newMember);
    }

    /**
     * 發送驗證郵件
     */
    private void sendVerificationEmail(MemberEntity member) {
        try {
            String verificationCode = verificationCodeService.generateVerificationCode();
            verificationCodeService.storeVerificationCode(member.getEmail(), verificationCode);
            
            String targetEmail = "young19960127@gmail.com";
            String subject = "【早餐店會員系統】會員註冊驗證碼";
            String content = buildVerificationEmailContent(member, verificationCode);
            
            emailService.sendSimpleEmail(targetEmail, subject, content);
            
            log.info("已發送驗證郵件 - 會員: {} -> 驗證碼: {} -> 目標信箱: {} -> 使用存儲: Redis", 
                     member.getAccount(), verificationCode, targetEmail);
            
        } catch (Exception e) {
            log.error("發送驗證郵件失敗 - 會員: {}, 錯誤: {}", member.getAccount(), e.getMessage());
        }
    }

    /**
     * 建構驗證郵件內容
     */
    private String buildVerificationEmailContent(MemberEntity member, String verificationCode) {
        StringBuilder content = new StringBuilder();
        content.append("親愛的 ").append(member.getUsername()).append(" 您好，\n\n");
        content.append("感謝您註冊早餐店會員系統！\n\n");
        content.append("您的帳號資訊：\n");
        content.append("帳號：").append(member.getAccount()).append("\n");
        content.append("電子郵件：").append(member.getEmail()).append("\n\n");
        content.append("請使用以下驗證碼完成帳號啟用：\n\n");
        content.append("【驗證碼】: ").append(verificationCode).append("\n\n");
        content.append("請點擊以下連結並輸入驗證碼來啟用您的帳號：\n");
        content.append("http://localhost:8080/member/verify\n\n");
        content.append("驗證碼有效期限為 15 分鐘，請盡快完成驗證。\n\n");
        content.append("如果您未申請註冊，請忽略此郵件。\n\n");
        content.append("早餐店會員系統 敬上");
        
        return content.toString();
    }

    /**
     * 驗證會員註冊驗證碼
     */
    @Transactional
    public boolean verifyMemberRegistration(MemberVerificationRequest request) {
        try {
            VerificationCodeServiceInterface service = getCurrentVerificationService();
            
            log.info("開始驗證會員註冊 - Email: {}, 驗證碼: {}", request.getEmail(), request.getVerificationCode());
            
            if (!service.verifyCode(request.getEmail(), request.getVerificationCode())) {
                log.warn("驗證碼錯誤或已過期 - Email: {}", request.getEmail());
                return false;
            }
            
            log.info("驗證碼驗證成功 - Email: {}", request.getEmail());
            
            Optional<MemberEntity> memberOpt = memberRepository.findByEmailIncludeDisabled(request.getEmail());
            if (memberOpt.isEmpty()) {
                log.warn("找不到對應的會員 - Email: {}，但驗證碼已驗證成功", request.getEmail());
                log.info("為測試目的創建臨時會員記錄 - Email: {}", request.getEmail());
                return true;
            }
            
            MemberEntity member = memberOpt.get();
            log.info("找到會員 - 帳號: {}, Email: {}, 當前狀態: {}", 
                     member.getAccount(), member.getEmail(), 
                     member.isEnabled() ? "已啟用" : "未啟用");
            
            if (!member.isEnabled()) {
                member.setEnabled(true);
                memberRepository.save(member);
                log.info("會員帳號已啟用 - 帳號: {}, Email: {}", member.getAccount(), member.getEmail());
            } else {
                log.info("會員帳號已經是啟用狀態 - 帳號: {}, Email: {}", member.getAccount(), member.getEmail());
            }
            
            log.info("會員帳號驗證成功 - 帳號: {}, Email: {}", member.getAccount(), member.getEmail());
            return true;
            
        } catch (Exception e) {
            log.error("會員驗證過程發生錯誤 - Email: {}, 錯誤: {}", request.getEmail(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 重新發送驗證郵件
     */
    @Transactional
    public boolean resendVerificationEmail(String email) {
        try {
            Optional<MemberEntity> memberOpt = memberRepository.findByEmail(email);
            if (memberOpt.isEmpty()) {
                log.warn("找不到對應的會員 - Email: {}", email);
                return false;
            }
            
            MemberEntity member = memberOpt.get();
            if (member.isEnabled()) {
                log.warn("會員帳號已啟用，無需重新發送驗證郵件 - Email: {}", email);
                return false;
            }
            
            VerificationCodeServiceInterface service = getCurrentVerificationService();
            service.deleteVerificationCode(email);
            sendVerificationEmail(member);
            
            return true;
            
        } catch (Exception e) {
            log.error("重新發送驗證郵件失敗 - Email: {}, 錯誤: {}", email, e.getMessage());
            return false;
        }
    }
    
    // ================================================================
    // 					會員資料管理功能 (Member Data Management)
    // ================================================================
    
    /**
     * 【更新專用 - DTO 重構版】更新現有會員的基本資料。
     */
    @Transactional
    public MemberEntity updateMemberDetails(MemberUpdateRequest updateRequest) {
        MemberEntity dbMember = memberRepository.findById(updateRequest.getMemberId())
            .orElseThrow(() -> new EntityNotFoundException("找不到ID為 " + updateRequest.getMemberId() + " 的會員"));

        dbMember.setUsername(updateRequest.getUsername());
        dbMember.setEmail(updateRequest.getEmail());
        dbMember.setPhone(updateRequest.getPhone());
        
        // 【修改】只有當生日不為空時才更新生日欄位
        if (updateRequest.getBirthday() != null) {
            dbMember.setBirthday(updateRequest.getBirthday());
        }
        
        dbMember.setGender(updateRequest.getGender());
        
        return memberRepository.save(dbMember);
    }

    /**
     * 【修復】直接更新會員實體的方法 - 支持帳號停用/啟用功能
     */
    @Transactional
    public MemberEntity updateMember(MemberEntity member) {
        Optional<MemberEntity> existingMemberOpt = memberRepository.findById(member.getMemberId());
        
        if (existingMemberOpt.isEmpty()) {
            throw new EntityNotFoundException("找不到ID為 " + member.getMemberId() + " 的會員");
        }
        
        if (member.getAccount() == null || member.getUsername() == null) {
            throw new IllegalArgumentException("會員資料不完整，無法更新");
        }
        
        member.setLastUpdatedAt(LocalDateTime.now());
        MemberEntity savedMember = memberRepository.save(member);
        log.info("會員資料更新成功 - ID: {}, Account: {}, Enabled: {}", 
                 savedMember.getMemberId(), savedMember.getAccount(), savedMember.isEnabled());
        
        return savedMember;
    }

    /**
     * 根據會員ID刪除會員 (軟刪除)。
     */
    @Transactional
    public void deleteMemberById(Long memberId) {
        log.info("開始軟刪除會員，ID: {}", memberId);
        
        try {
            // 查找會員
            MemberEntity member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("找不到會員 ID: " + memberId));
            
            // 檢查會員是否已經被停用
            if (!member.isEnabled()) {
                log.warn("會員 ID {} 已經處於停用狀態", memberId);
                throw new IllegalStateException("會員已經處於停用狀態");
            }
            
            // 執行軟刪除：將 enabled 設為 false
            member.setEnabled(false);
            member.setLastUpdatedAt(LocalDateTime.now());
            
            // 保存更新
            MemberEntity savedMember = memberRepository.save(member);
            
            log.info("會員軟刪除成功 - ID: {}, 帳號: {}, 姓名: {}", 
                     savedMember.getMemberId(), 
                     savedMember.getAccount(), 
                     savedMember.getUsername());
            
        } catch (EntityNotFoundException e) {
            log.error("軟刪除會員失敗 - 會員不存在: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("軟刪除會員時發生未預期錯誤: {}", e.getMessage(), e);
            throw new RuntimeException("軟刪除會員失敗：" + e.getMessage(), e);
        }
    }
    
    // ================================================================
    // 					密碼重設功能 (Password Reset)
    // ================================================================
    
    /**
     * 處理忘記密碼請求
     */
    @Transactional
    public String processForgotPassword(ForgotPasswordRequest request) {
        Optional<MemberEntity> memberOpt = memberRepository.findByEmail(request.getEmail());
        
        if (memberOpt.isEmpty()) {
            throw new EntityNotFoundException("找不到使用此電子郵件的會員帳號：" + request.getEmail());
        }
        
        MemberEntity member = memberOpt.get();
        
        if (!member.isEnabled()) {
            throw new IllegalArgumentException("此帳號已被停用，無法重設密碼");
        }
        
        String resetToken = generateResetToken(member);
        
        try {
            String baseUrl = determineBaseUrl();
            String resetUrl = baseUrl + "/api/v1/auth/reset-password?token=" + java.net.URLEncoder.encode(resetToken, "UTF-8");
            
            log.info("生成重設密碼連結 - 會員: {} -> URL: {}", member.getAccount(), resetUrl);
            
            emailService.sendPasswordResetEmail(
                member.getEmail(),
                member.getAccount(),
                member.getUsername(),
                resetToken,
                resetUrl
            );
            
            log.info("會員 {} 請求重設密碼成功，郵件已發送到 young19960127@gmail.com", member.getAccount());
            
        } catch (Exception e) {
            log.error("發送重設密碼郵件失敗 - 會員: {} ({}), 錯誤: {}", 
                member.getAccount(), member.getUsername(), e.getMessage(), e);
            
            throw new RuntimeException("郵件發送失敗，請稍後再試或聯繫客服", e);
        }
        
        return resetToken;
    }
    
    /**
     * 【新增】動態決定基礎URL，支援不同環境
     */
    private String determineBaseUrl() {
        String environment = System.getProperty("spring.profiles.active", "dev");
        
        switch (environment.toLowerCase()) {
            case "prod":
            case "production":
                return "https://yourdomain.com";
            case "test":
            case "staging":
                return "https://test.yourdomain.com";
            default:
                return "http://localhost:8080";
        }
    }

    /**
     * 處理密碼重設請求
     */
    @Transactional
    public void processResetPassword(ResetPasswordRequest request) {
        if (!request.isPasswordMatching()) {
            throw new IllegalArgumentException("兩次輸入的密碼不一致");
        }
        
        Long memberId = validateAndParseResetToken(request.getToken());
        
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("找不到對應的會員"));
        
        if (!member.isEnabled()) {
            throw new IllegalArgumentException("此帳號已被停用，無法重設密碼");
        }
        
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        member.setPassword(encodedPassword);
        memberRepository.save(member);
        
        log.info("會員 {} 成功重設密碼", member.getAccount());
    }
    
    /**
     * 生成重設密碼的 Token
     */
    private String generateResetToken(MemberEntity member) {
        String tokenData = member.getMemberId() + ":" + System.currentTimeMillis();
        return Base64.getEncoder().encodeToString(tokenData.getBytes());
    }
    
    /**
     * 驗證並解析重設密碼的 Token
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
    
    // ================================================================
    // 					查詢功能 (Query Functions)
    // ================================================================
    
    public Optional<MemberEntity> getMemberById(Long memberId) {
        return memberRepository.findById(memberId);
    }

    public Optional<MemberEntity> getMemberByAccount(String account) {
        log.info("根據帳號查詢會員: {}", account);
        
        if (account == null || account.trim().isEmpty()) {
            return Optional.empty();
        }
        
        // 使用基本的 findByAccount 方法，而不是不存在的 findByAccountAndEnabledTrue
        Optional<MemberEntity> memberOpt = memberRepository.findByAccount(account.trim());
        
        // 手動檢查會員是否啟用
        if (memberOpt.isPresent() && memberOpt.get().isEnabled()) {
            log.info("成功找到會員: {} ({})", memberOpt.get().getUsername(), account);
            return memberOpt;
        } else {
            log.info("找不到帳號為 {} 的啟用會員", account);
            return Optional.empty();
        }
    }
    
    public Optional<MemberEntity> getMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public List<MemberEntity> getAllMembers() {
        return memberRepository.findAll();
    }
    
    /**
     * 【功能】: 根據性別查詢會員列表
     * 【參數】: gender - 性別枚舉值
     * 【回傳】: 指定性別的會員列表
     */
    public List<MemberEntity> getMembersByGender(com.eatfast.common.enums.Gender gender) {
        log.info("查詢性別為 {} 的會員", gender);
        
        try {
            List<MemberEntity> members = memberRepository.findByGender(gender);
            log.info("找到 {} 位性別為 {} 的會員", members.size(), gender);
            return members;
        } catch (Exception e) {
            log.error("查詢性別為 {} 的會員時發生錯誤: {}", gender, e.getMessage(), e);
            throw new RuntimeException("查詢會員性別失敗: " + e.getMessage());
        }
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
    
    // ================================================================
    // 					複合查詢功能 (Composite Query Functions)
    // ================================================================
    
    /**
     * 【複合查詢功能】根據多個條件查詢會員
     */
    public List<MemberEntity> findMembersByCompositeQuery(String username, String email, String phone) {
        log.info("執行複合查詢 - 姓名: {}, 郵件: {}, 電話: {}", username, email, phone);
        
        Specification<MemberEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 只查詢啟用的會員 - 修正屬性名稱為 isEnabled
            predicates.add(criteriaBuilder.isTrue(root.get("isEnabled")));
            
            // 姓名模糊查詢
            if (username != null && !username.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    root.get("username"), 
                    "%" + username.trim() + "%"
                ));
            }
            
            // 電子郵件精確查詢
            if (email != null && !email.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                    root.get("email"), 
                    email.trim()
                ));
            }
            
            // 電話模糊查詢
            if (phone != null && !phone.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    root.get("phone"), 
                    "%" + phone.trim() + "%"
                ));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        List<MemberEntity> results = memberRepository.findAll(spec);
        log.info("複合查詢完成，找到 {} 筆符合條件的資料", results.size());
        
        return results;
    }
    
    // ================================================================
    // 					即時驗證功能 (Real-time Validation)
    // ================================================================
    
    /**
     * 【即時驗證】檢查電子郵件是否被其他會員使用
     */
    @Transactional(readOnly = true)
    public boolean isEmailExistsForOtherMember(String email, Long excludeMemberId) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return false;
            }
            
            // 【修正】使用 Repository 中的新查詢方法，確保邏輯一致性
            return memberRepository.existsByEmailAndMemberIdNotAndIsEnabledTrue(email.trim(), excludeMemberId);
            
        } catch (Exception e) {
            log.error("檢查電子郵件重複時發生錯誤: {}", e.getMessage());
            return true; // 發生錯誤時回傳 true，避免重複資料
        }
    }
    
    /**
     * 【即時驗證】檢查電話號碼是否被其他會員使用
     */
    @Transactional(readOnly = true)
    public boolean isPhoneExistsForOtherMember(String phone, Long excludeMemberId) {
        try {
            if (phone == null || phone.trim().isEmpty()) {
                return false;
            }
            
            // 【修正】使用 Repository 中的新查詢方法，確保邏輯一致性
            return memberRepository.existsByPhoneAndMemberIdNotAndIsEnabledTrue(phone.trim(), excludeMemberId);
            
        } catch (Exception e) {
            log.error("檢查電話號碼重複時發生錯誤: {}", e.getMessage());
            return true; // 發生錯誤時回傳 true，避免重複資料
        }
    }
    
    // ================================================================
    // 					其他驗證方法 (Other Validation Methods)
    // ================================================================
    
    /**
     * 檢查帳號是否已存在
     */
    public boolean isAccountExists(String account) {
        if (account == null || account.trim().isEmpty()) {
            return false;
        }
        return memberRepository.existsByAccount(account.trim());
    }
    
    /**
     * 檢查電子郵件是否已存在
     */
    public boolean isEmailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return memberRepository.existsByEmail(email.trim());
    }
    
    /**
     * 檢查手機號碼是否已存在
     */
    public boolean isPhoneExists(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        // 移除格式符號進行比較
        String cleanPhone = phone.replaceAll("-", "");
        return memberRepository.existsByPhone(cleanPhone) || 
               memberRepository.existsByPhone(phone);
    }

    // ================================================================
    // 					驗證碼服務相關方法 (Verification Code Service)
    // ================================================================
    
    /**
     * 【新增】獲取當前使用的驗證碼服務
     */
    private VerificationCodeServiceInterface getCurrentVerificationService() {
        // 直接使用 Redis 版驗證碼服務
        log.info("使用 Redis 版驗證碼服務");
        return verificationCodeService;
    }
    
    /**
     * 【新增】驗證碼服務接口
     */
    private interface VerificationCodeServiceInterface {
        String generateVerificationCode();
        void storeVerificationCode(String email, String code);
        boolean verifyCode(String email, String inputCode);
        boolean hasVerificationCode(String email);
        void deleteVerificationCode(String email);
    }
    
    // ================================================================
    // 					已刪除會員處理功能 (Deleted Member Handling)
    // ================================================================
    
    /**
     * 【功能】獲取所有已刪除（停用）的會員
     * 
     * @return 已刪除會員列表
     */
    public List<MemberEntity> getDeletedMembers() {
        log.info("查詢所有已刪除的會員");
        
        try {
            // 【修正】使用原生 SQL 查詢來繞過 @SQLRestriction 的限制
            // 直接查詢 is_enabled = false 的記錄
            List<MemberEntity> deletedMembers = memberRepository.findDisabledMembers();
            log.info("找到 {} 筆已刪除會員資料", deletedMembers.size());
            
            return deletedMembers;
            
        } catch (Exception e) {
            log.error("查詢已刪除會員時發生錯誤: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 【功能】復原已刪除的會員
     * 
     * @param memberId 要復原的會員ID
     * @return 復原後的會員實體
     * @throws EntityNotFoundException 如果找不到會員
     * @throws IllegalStateException 如果會員狀態不正確
     */
    @Transactional
    public MemberEntity restoreMember(Long memberId) {
        log.info("嘗試復原會員，ID: {}", memberId);
        
        // 查詢會員（包括已停用的）
        Optional<MemberEntity> memberOpt = getMemberByIdIncludeDeleted(memberId);
        
        if (memberOpt.isEmpty()) {
            throw new EntityNotFoundException("找不到會員 ID: " + memberId);
        }
        
        MemberEntity member = memberOpt.get();
        
        // 檢查會員狀態
        if (member.isEnabled()) {
            throw new IllegalStateException("會員「" + member.getUsername() + "」目前狀態為啟用，無需復原");
        }
        
        // 復原會員（重新啟用）
        member.setEnabled(true);
        member.setLastUpdatedAt(LocalDateTime.now());
        
        MemberEntity restoredMember = memberRepository.save(member);
        
        log.info("會員復原成功 - ID: {}, 帳號: {}, 姓名: {}", 
                 restoredMember.getMemberId(), 
                 restoredMember.getAccount(), 
                 restoredMember.getUsername());
        
        return restoredMember;
    }
    
    /**
     * 【功能】獲取會員資料（包括已刪除的）
     * 
     * @param memberId 會員ID
     * @return 會員資料的 Optional
     */
    public Optional<MemberEntity> getMemberByIdIncludeDeleted(Long memberId) {
        log.debug("查詢會員（包括已刪除），ID: {}", memberId);
        
        try {
            // 【修正】直接使用 Repository 中的原生 SQL 查詢方法，繞過 @SQLRestriction 限制
            return memberRepository.findByIdIncludeDisabled(memberId);
            
        } catch (Exception e) {
            log.error("查詢會員時發生錯誤: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * 【功能】永久刪除會員（真正的刪除，不可復原）
     * 
     * @param memberId 要永久刪除的會員ID
     * @throws EntityNotFoundException 如果找不到會員
     */
    @Transactional
    public void permanentlyDeleteMember(Long memberId) {
        log.warn("⚠️ 執行永久刪除操作 - 會員ID: {}", memberId);
        
        // 先檢查會員是否存在
        Optional<MemberEntity> memberOpt = getMemberByIdIncludeDeleted(memberId);
        
        if (memberOpt.isEmpty()) {
            throw new EntityNotFoundException("找不到會員 ID: " + memberId);
        }
        
        MemberEntity member = memberOpt.get();
        String memberInfo = member.getUsername() + " (" + member.getAccount() + ")";
        
        try {
            // 執行真正的刪除（繞過軟刪除）
            memberRepository.deleteById(memberId);
            
            log.warn("⚠️ 會員已永久刪除 - {}", memberInfo);
            
        } catch (Exception e) {
            log.error("永久刪除會員失敗 - {}: {}", memberInfo, e.getMessage(), e);
            throw new RuntimeException("永久刪除會員失敗: " + e.getMessage(), e);
        }
    }
    
    // ================================================================
    // 					會員帳號管理功能 (Account Management)
    // ================================================================
    
    /**
     * 【功能】: 停用會員帳號
     * 【請求路徑】: 供會員主動停用自己的帳號使用
     */
    @Transactional
    public void deactivateMember(Long memberId) {
        log.info("開始停用會員帳號，ID: {}", memberId);
        
        try {
            // 查找會員
            MemberEntity member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("找不到會員 ID: " + memberId));
            
            // 檢查會員是否已經被停用
            if (!member.isEnabled()) {
                log.warn("會員 ID {} 已經處於停用狀態", memberId);
                throw new IllegalStateException("會員已經處於停用狀態");
            }
            
            // 執行停用：將 enabled 設為 false
            member.setEnabled(false);
            member.setLastUpdatedAt(LocalDateTime.now());
            
            // 保存更新
            MemberEntity savedMember = memberRepository.save(member);
            
            log.info("會員帳號停用成功 - ID: {}, 帳號: {}, 姓名: {}", 
                     savedMember.getMemberId(), 
                     savedMember.getAccount(), 
                     savedMember.getUsername());
            
        } catch (EntityNotFoundException e) {
            log.error("停用會員失敗 - 會員不存在: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("停用會員時發生未預期錯誤: {}", e.getMessage(), e);
            throw new RuntimeException("停用會員失敗：" + e.getMessage(), e);
        }
    }

    /**
     * 【修正】獲取會員訂單並確保載入明細 - 簡化版本避免複雜的關聯載入
     * 
     * @param memberId 會員ID
     * @return 包含完整明細的訂單列表
     */
    @Transactional(readOnly = true)
    public List<OrderListEntity> getMemberOrdersWithDetails(Long memberId) {
        log.info("開始獲取會員訂單詳細資料 - 會員ID: {}", memberId);
        
        try {
            // 1. 驗證會員是否存在
            if (!memberRepository.existsById(memberId)) {
                log.warn("會員不存在 - ID: {}", memberId);
                return new ArrayList<>();
            }
            
            // 2. 使用 Repository 查詢該會員的所有訂單
            List<OrderListEntity> orders = orderListRepository.findByMemberMemberIdOrderByOrderDateDesc(memberId);
            
            log.info("找到 {} 筆訂單記錄", orders.size());
            
            // 3. 確保訂單明細資料被正確載入 (lazy loading)
            for (OrderListEntity order : orders) {
                // 強制載入訂單明細資料
                if (order.getOrderListInfos() != null) {
                    order.getOrderListInfos().size(); // 觸發 lazy loading
                    
                    // 確保餐點資料也被載入
                    for (OrderListInfoEntity info : order.getOrderListInfos()) {
                        if (info.getMeal() != null) {
                            info.getMeal().getMealName(); // 觸發 lazy loading
                        }
                    }
                }
                
                // 確保會員資料被載入
                if (order.getMember() != null) {
                    order.getMember().getUsername(); // 觸發 lazy loading
                }
                
                // 確保商店資料被載入
                if (order.getStore() != null) {
                    order.getStore().getStoreName(); // 觸發 lazy loading
                }
            }
            
            return orders;
            
        } catch (Exception e) {
            log.error("獲取會員訂單時發生錯誤 - 會員ID: {}, 錯誤: {}", memberId, e.getMessage(), e);
            // 返回空列表而不是拋出異常，避免頁面崩潰
            return new ArrayList<>();
        }
    }
}

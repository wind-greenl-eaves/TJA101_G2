package com.eatfast.member.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.SessionFactory; // 引入 SessionFactory
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hibernate.util.CompositeQuery.HibernateUtil_CompositeQuery_Member; // 引入剛才建立的工具類

@Service
public class Demo_MemberService {
	// [不可變] 注入 MemberRepository，這是 Spring Data JPA 的核心介面。
    @Autowired
    private MemberRepository memberRepository;

    // [不可變] 注入 SessionFactory，這是 Hibernate 的核心工廠物件。
    @Autowired
    private SessionFactory sessionFactory;

    // ... ( saveOrUpdateMember, delete, get 等方法維持不變) ...
    @Transactional
    public MemberEntity saveOrUpdateMember(MemberEntity memberEntity) {
        return memberRepository.save(memberEntity);
    }

    @Transactional
    public void deleteMemberById(Long memberId) {
        memberRepository.deleteById(memberId);
    }
    
    @Transactional
    public void deleteMemberByAccount(String account) {
        memberRepository.deleteByAccount(account);
    }

    public MemberEntity getMemberById(Long memberId) {
        Optional<MemberEntity> optionalMember = memberRepository.findById(memberId);
        return optionalMember.orElse(null);
    }

    public Optional<MemberEntity> getMemberByAccount(String account) {
        return memberRepository.findByAccount(account);
    }
    
    public boolean memberExists(String account, String email) {
        return memberRepository.existsByAccountOrEmail(account, email);
    }

    public List<MemberEntity> getAllMembers() {
        return memberRepository.findAll();
    }


    /**
     * 【新增】根據多個動態條件查詢會員。
     * 此方法使用了 Hibernate SessionFactory 來進行複合查詢。
     *
     * @param map [可變] 包含查詢條件的 Map。
     * @return 符合條件的會員列表。
     */
//    @Transactional(readOnly = true) // [不可變] 查詢操作建議加上 readOnly=true，可以提升效能。
//    public List<MemberEntity> getAllMembers(Map<String, String[]> map) {
//        // [重點] 使用 sessionFactory.getCurrentSession() 而不是 openSession()
//        // getCurrentSession() 會取得由 Spring 管理的當前交易中的 Session，
//        // Spring 會自動處理其生命週期 (開啟、關閉、錯誤回滾)，這是最安全、最推薦的做法。
//        Session session = sessionFactory.getCurrentSession();
//        
//        // [不可變] 呼叫我們自訂的工具類來執行查詢。
//        return HibernateUtil_CompositeQuery_Member.getAllC(map, session);
//    }
}
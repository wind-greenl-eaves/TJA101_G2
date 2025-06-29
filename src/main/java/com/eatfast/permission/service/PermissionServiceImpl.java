package com.eatfast.permission.service; // 可自定義的 package 路徑

import com.eatfast.permission.dto.PermissionDto;
import com.eatfast.permission.mapper.PermissionMapper;
import com.eatfast.permission.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * [可自定義的類別名稱]: PermissionServiceImpl
 * 權限服務 (PermissionService) 的主要實作類別。
 *
 * - @Service: (不可變動) 標記此類別為服務層的 Spring Bean。
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    // 關鍵依賴：透過 final 宣告，並在建構子中注入，是現代 Spring 開發的最佳實踐。
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    /**
     * 建構子注入 (Constructor Injection)。
     * - @Autowired: (不可變動) Spring 會自動尋找對應型別的 Bean 並傳入。
     *
     * @param permissionRepository (可自定義的參數名): 權限的資料存取層。
     * @param permissionMapper (可自定義的參數名): 權限的物件轉換器。
     */
    @Autowired
    public PermissionServiceImpl(PermissionRepository permissionRepository, PermissionMapper permissionMapper) {
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
    }

    /**
     * 查詢所有權限的實作。
     * - @Transactional(readOnly = true): (不可變動) 宣告此為一個交易性方法。
     * readOnly = true 是一個效能優化，它告訴資料庫此操作不涉及資料寫入。
     */
    @Override
    @Transactional(readOnly = true)
    public List<PermissionDto> findAllPermissions() {
        // 1. 從資料庫查詢出所有的 PermissionEntity
        return permissionRepository.findAll()
                // 2. 使用 Java Stream API 進行流式處理
                .stream()
                // 3. 將每一個 Entity 物件透過 Mapper 轉換為 DTO 物件
                .map(permissionMapper::toDto)
                // 4. 將轉換後的 DTO 收集成一個 List 並返回
                .collect(Collectors.toList());
    }
}

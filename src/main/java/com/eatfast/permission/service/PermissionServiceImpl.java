package com.eatfast.permission.service;

import com.eatfast.common.enums.EmployeeRole;
import com.eatfast.permission.dto.PermissionDto;
import com.eatfast.permission.mapper.PermissionMapper;
import com.eatfast.permission.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [可自定義的類別名稱]: PermissionServiceImpl - 【已重構】
 * 權限服務 (PermissionService) 的主要實作類別。
 * @Service: (不可變動) 標記此類別為服務層的 Spring Bean。
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Autowired
    public PermissionServiceImpl(PermissionRepository permissionRepository, PermissionMapper permissionMapper) {
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDto> findAllPermissions() {
        return permissionRepository.findAll()
                .stream()
                .map(permissionMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 【新增方法實作】: 根據角色查詢權限。
     * - @Transactional(readOnly = true): (不可變動) 宣告為唯讀交易以優化效能。
     */
    @Override
    @Transactional(readOnly = true)
    public Set<PermissionDto> findPermissionsByRole(EmployeeRole role) {
        // 1. 呼叫 Repository 中新定義的查詢方法
        return permissionRepository.findPermissionsByEmployeeRole(role)
                // 2. 使用 Stream API 將實體集合轉換為 DTO 集合
                .stream()
                .map(permissionMapper::toDto)
                // 3. 收集成 Set 並返回
                .collect(Collectors.toSet());
    }
}

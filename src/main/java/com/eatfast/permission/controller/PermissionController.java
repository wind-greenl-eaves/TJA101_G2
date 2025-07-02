package com.eatfast.permission.controller;

import com.eatfast.common.enums.EmployeeRole;
import com.eatfast.permission.dto.PermissionDto;
import com.eatfast.permission.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * [可自定義的類別名稱]: PermissionController - 【已重構】
 * 權限管理的 RESTful API 控制器。
 * @RestController: (不可變動) 宣告這是一個 RESTful 控制器。
 * @RequestMapping: (不可變動) 定義此控制器下所有 API 的基礎路徑。
 */
@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    @Autowired
    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * [路徑配對]: GET /api/v1/permissions
     * 獲取系統中所有可用的權限列表。
     */
    @GetMapping
    public ResponseEntity<List<PermissionDto>> getAllPermissions() {
        List<PermissionDto> permissions = permissionService.findAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    /**
     * 【新增 API 端點】
     * [路徑配對]: GET /api/v1/permissions/roles/{role}
     * 說明: 根據員工角色名稱，獲取該角色擁有的所有權限。
     * 這將取代前端的硬編碼資料，成為系統的「唯一事實來源」。
     * @param role (可自定義的參數名): 員工角色 Enum 名稱 (例如: "STAFF", "MANAGER")。
     * @return 返回一個包含該角色所有權限 DTO 的 Set 集合。
     */
    @GetMapping("/roles/{role}")
    public ResponseEntity<Set<PermissionDto>> getPermissionsByRole(@PathVariable("role") EmployeeRole role) {
        Set<PermissionDto> permissions = permissionService.findPermissionsByRole(role);
        return ResponseEntity.ok(permissions);
    }
}

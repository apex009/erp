package com.fy.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fy.erp.entities.SysPermission;
import com.fy.erp.entities.SysRole;
import com.fy.erp.entities.SysRolePermission;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.Collections;
import java.util.List;

@Service
public class RbacService {
    private final SysRoleService roleService;
    private final SysRolePermissionService rolePermissionService;
    private final SysPermissionService permissionService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public RbacService(SysRoleService roleService,
                       SysRolePermissionService rolePermissionService,
                       SysPermissionService permissionService) {
        this.roleService = roleService;
        this.rolePermissionService = rolePermissionService;
        this.permissionService = permissionService;
    }

    public boolean hasPermission(List<String> roleCodes, String method, String path) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return false;
        }
        if (roleCodes.contains("ADMIN")) {
            return true;
        }
        if (permissionService.count() == 0) {
            return true;
        }
        List<SysRole> roles = roleService.list(new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleCode, roleCodes));
        if (roles.isEmpty()) {
            return false;
        }
        List<Long> roleIds = roles.stream().map(SysRole::getId).toList();
        List<SysRolePermission> rolePerms = rolePermissionService.list(
                new LambdaQueryWrapper<SysRolePermission>().in(SysRolePermission::getRoleId, roleIds)
        );
        if (rolePerms.isEmpty()) {
            return false;
        }
        List<Long> permIds = rolePerms.stream().map(SysRolePermission::getPermId).toList();
        List<SysPermission> perms = permissionService.list(
                new LambdaQueryWrapper<SysPermission>().in(SysPermission::getId, permIds)
        );
        if (perms.isEmpty()) {
            return false;
        }
        String requestMethod = method == null ? "" : method.toUpperCase();
        for (SysPermission perm : perms) {
            String permMethod = perm.getMethod();
            String permPath = perm.getPath();
            if (permPath == null || permPath.isBlank()) {
                continue;
            }
            boolean methodMatch = permMethod == null || permMethod.isBlank() || requestMethod.equalsIgnoreCase(permMethod);
            if (methodMatch && pathMatcher.match(permPath, path)) {
                return true;
            }
        }
        return false;
    }
}

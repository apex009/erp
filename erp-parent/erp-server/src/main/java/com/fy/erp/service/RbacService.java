package com.fy.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fy.erp.entities.SysPermission;
import com.fy.erp.entities.SysRole;
import com.fy.erp.entities.SysRolePermission;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@lombok.extern.slf4j.Slf4j
public class RbacService {
    private final SysRoleService roleService;
    private final SysRolePermissionService rolePermissionService;
    private final SysPermissionService permissionService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public RbacService(SysRoleService roleService,
            SysRolePermissionService rolePermissionService,
            SysPermissionService permissionService,
            RedisTemplate<String, Object> redisTemplate) {
        this.roleService = roleService;
        this.rolePermissionService = rolePermissionService;
        this.permissionService = permissionService;
        this.redisTemplate = redisTemplate;
    }

    public boolean hasPermission(Long userId, List<String> roleCodes, String method, String path) {
        // 用户自身信息和菜单接口，所有已登录用户可访问
        if (path != null && path.startsWith("/api/auth/")) {
            return true;
        }
        if (roleCodes == null || roleCodes.isEmpty()) {
            return false;
        }
        if (roleCodes.contains("ADMIN")) {
            return true;
        }

        // 1. 尝试从缓存获取权限列表（缓存的是精简 Map，不是 Entity）
        String cacheKey = com.fy.erp.constant.RedisKeyPrefix.AUTH_PERM + userId;
        @SuppressWarnings("unchecked")
        List<Map<String, String>> perms = (List<Map<String, String>>) redisTemplate.opsForValue().get(cacheKey);

        if (perms == null) {
            log.debug("[Redis] Cache MISS for user permissions: {}", userId);
            // 2. 缓存失效，查询数据库并转为精简 Map
            List<SysPermission> dbPerms = getPermissionsFromDb(roleCodes);
            perms = dbPerms.stream().map(p -> {
                Map<String, String> m = new HashMap<>();
                m.put("path", p.getPath());
                m.put("method", p.getMethod());
                return m;
            }).collect(Collectors.toList());
            // 3. 写入缓存（只缓存 path+method，无 LocalDateTime 问题）
            redisTemplate.opsForValue().set(cacheKey, perms,
                    com.fy.erp.constant.RedisKeyPrefix.AUTH_PERM_TTL.toSeconds(), TimeUnit.SECONDS);

            // 4. 更新角色用户索引
            indexUserRoles(userId, roleCodes);
        } else {
            log.debug("[Redis] Cache HIT for user permissions: {}", userId);
        }

        if (perms.isEmpty()) {
            return false;
        }

        String requestMethod = method == null ? "" : method.toUpperCase();
        for (Map<String, String> perm : perms) {
            String permMethod = perm.get("method");
            String permPath = perm.get("path");
            if (permPath == null || permPath.isBlank()) {
                continue;
            }
            boolean methodMatch = permMethod == null || permMethod.isBlank() || "ALL".equalsIgnoreCase(permMethod)
                    || requestMethod.equalsIgnoreCase(permMethod);
            if (methodMatch && pathMatcher.match(permPath, path)) {
                return true;
            }
        }
        return false;
    }

    private void indexUserRoles(Long userId, List<String> roleCodes) {
        List<SysRole> roles = roleService.list(new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleCode, roleCodes));
        for (SysRole role : roles) {
            String indexKey = com.fy.erp.constant.RedisKeyPrefix.AUTH_ROLE_USERS + role.getId();
            redisTemplate.opsForSet().add(indexKey, userId);
            redisTemplate.expire(indexKey, com.fy.erp.constant.RedisKeyPrefix.AUTH_PERM_TTL);
        }
    }

    /**
     * 清理由角色变更受影响的所有用户缓存
     */
    public void evictByRole(Long roleId) {
        String indexKey = com.fy.erp.constant.RedisKeyPrefix.AUTH_ROLE_USERS + roleId;
        java.util.Set<Object> userIds = redisTemplate.opsForSet().members(indexKey);
        if (userIds != null && !userIds.isEmpty()) {
            java.util.List<String> keysToEvict = userIds.stream()
                    .map(u -> com.fy.erp.constant.RedisKeyPrefix.AUTH_PERM + u.toString())
                    .toList();
            redisTemplate.delete(keysToEvict);
            log.info("[Redis] Evicted {} user permissions due to role {} change", keysToEvict.size(), roleId);
        }
        redisTemplate.delete(indexKey);
    }

    private List<SysPermission> getPermissionsFromDb(List<String> roleCodes) {
        List<SysRole> roles = roleService.list(new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleCode, roleCodes));
        if (roles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = roles.stream().map(SysRole::getId).toList();
        List<SysRolePermission> rolePerms = rolePermissionService.list(
                new LambdaQueryWrapper<SysRolePermission>().in(SysRolePermission::getRoleId, roleIds));
        if (rolePerms.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> permIds = rolePerms.stream().map(SysRolePermission::getPermId).toList();
        return permissionService.list(new LambdaQueryWrapper<SysPermission>().in(SysPermission::getId, permIds));
    }
}

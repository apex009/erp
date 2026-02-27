package com.fy.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fy.erp.entities.SysRole;
import com.fy.erp.result.Result;
import com.fy.erp.entities.SysRolePermission;
import com.fy.erp.service.SysRolePermissionService;
import com.fy.erp.service.SysRoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private final SysRoleService roleService;
    private final SysRolePermissionService rolePermissionService;

    public RoleController(SysRoleService roleService, SysRolePermissionService rolePermissionService) {
        this.roleService = roleService;
        this.rolePermissionService = rolePermissionService;
    }

    @GetMapping
    public Result<Page<SysRole>> page(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(SysRole::getRoleName, keyword).or().like(SysRole::getRoleCode, keyword));
        }
        if (status != null) {
            wrapper.eq(SysRole::getStatus, status);
        }
        return Result.success(roleService.page(new Page<>(page, size), wrapper));
    }

    @PostMapping
    public Result<SysRole> create(@RequestBody SysRole role) {
        roleService.save(role);
        return Result.success(role);
    }

    @PutMapping("/{id}")
    public Result<SysRole> update(@PathVariable Long id, @RequestBody SysRole role) {
        role.setId(id);
        roleService.updateById(role);
        return Result.success(role);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.removeById(id);
        return Result.success();
    }

    @GetMapping("/{id}/permissions")
    public Result<List<SysRolePermission>> permissions(@PathVariable Long id) {
        return Result.success(rolePermissionService.lambdaQuery().eq(SysRolePermission::getRoleId, id).list());
    }

    @PutMapping("/{id}/permissions")
    public Result<Void> updatePermissions(@PathVariable Long id, @RequestBody List<Long> permIds) {
        rolePermissionService.lambdaUpdate().eq(SysRolePermission::getRoleId, id).remove();
        if (permIds != null) {
            for (Long permId : permIds) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(id);
                rp.setPermId(permId);
                rolePermissionService.save(rp);
            }
        }
        return Result.success();
    }
}

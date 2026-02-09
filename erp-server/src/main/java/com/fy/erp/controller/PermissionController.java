package com.fy.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fy.erp.entities.SysPermission;
import com.fy.erp.result.Result;
import com.fy.erp.service.SysPermissionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {
    private final SysPermissionService permissionService;

    public PermissionController(SysPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public Result<Page<SysPermission>> page(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(SysPermission::getPermName, keyword)
                    .or().like(SysPermission::getPermCode, keyword)
                    .or().like(SysPermission::getPath, keyword));
        }
        if (status != null) {
            wrapper.eq(SysPermission::getStatus, status);
        }
        return Result.success(permissionService.page(new Page<>(page, size), wrapper));
    }

    @PostMapping
    public Result<SysPermission> create(@RequestBody SysPermission permission) {
        permissionService.save(permission);
        return Result.success(permission);
    }

    @PutMapping("/{id}")
    public Result<SysPermission> update(@PathVariable Long id, @RequestBody SysPermission permission) {
        permission.setId(id);
        permissionService.updateById(permission);
        return Result.success(permission);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.removeById(id);
        return Result.success();
    }
}

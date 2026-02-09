package com.fy.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fy.erp.dto.UserCreateRequest;
import com.fy.erp.dto.UserUpdateRequest;
import com.fy.erp.entities.SysUser;
import com.fy.erp.entities.SysUserRole;
import com.fy.erp.exception.BizException;
import com.fy.erp.result.Result;
import com.fy.erp.service.SysUserRoleService;
import com.fy.erp.service.SysUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final SysUserService userService;
    private final SysUserRoleService userRoleService;

    public UserController(SysUserService userService, SysUserRoleService userRoleService) {
        this.userService = userService;
        this.userRoleService = userRoleService;
    }

    @GetMapping
    public Result<Page<SysUser>> page(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) Long deptId,
                                      @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getNickname, keyword)
                    .or().like(SysUser::getPhone, keyword));
        }
        if (deptId != null) {
            wrapper.eq(SysUser::getDeptId, deptId);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        return Result.success(userService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/{id}")
    public Result<SysUser> detail(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PostMapping
    public Result<SysUser> create(@Valid @RequestBody UserCreateRequest request) {
        if (userService.findByUsername(request.getUsername()) != null) {
            throw new BizException(400, "username exists");
        }
        SysUser user = new SysUser();
        user.setDeptId(request.getDeptId());
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        userService.setPassword(user, request.getPassword());
        userService.save(user);
        saveUserRoles(user.getId(), request.getRoleIds());
        return Result.success(user);
    }

    @PutMapping("/{id}")
    public Result<SysUser> update(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        SysUser user = userService.getById(id);
        if (user == null) {
            throw new BizException(404, "user not found");
        }
        if (request.getDeptId() != null) {
            user.setDeptId(request.getDeptId());
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            userService.setPassword(user, request.getPassword());
        }
        userService.updateById(user);
        if (request.getRoleIds() != null) {
            saveUserRoles(id, request.getRoleIds());
        }
        return Result.success(user);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        userRoleService.remove(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        return Result.success();
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        userRoleService.remove(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleService.save(ur);
        }
    }
}

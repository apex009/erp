package com.fy.erp.controller;

import com.fy.erp.dto.LoginRequest;
import com.fy.erp.dto.LoginResponse;
import com.fy.erp.entities.SysUser;
import com.fy.erp.exception.BizException;
import com.fy.erp.result.Result;
import com.fy.erp.service.SysUserService;
import com.fy.erp.security.UserContext;
import com.fy.erp.security.UserPrincipal;
import com.fy.erp.utils.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final SysUserService userService;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;
    private final com.fy.erp.service.SysPermissionService sysPermissionService;
    private final com.fy.erp.service.SysRoleService sysRoleService;
    private final com.fy.erp.service.SysRolePermissionService sysRolePermissionService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire-millis}")
    private long expireMillis;

    public AuthController(SysUserService userService,
            org.springframework.data.redis.core.StringRedisTemplate redisTemplate,
            com.fy.erp.service.SysPermissionService sysPermissionService,
            com.fy.erp.service.SysRoleService sysRoleService,
            com.fy.erp.service.SysRolePermissionService sysRolePermissionService) {
        this.userService = userService;
        this.redisTemplate = redisTemplate;
        this.sysPermissionService = sysPermissionService;
        this.sysRoleService = sysRoleService;
        this.sysRolePermissionService = sysRolePermissionService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        SysUser user = userService.findByUsername(request.getUsername());
        if (user == null || user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(401, "invalid username or password");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(401, "invalid username or password");
        }
        return getLoginResponse(user);
    }

    @PostMapping("/login/sms")
    public Result<LoginResponse> loginBySms(@Valid @RequestBody com.fy.erp.dto.SmsLoginRequest request) {
        String phone = request.getPhone();
        String code = request.getCode();

        // 验证验证码
        String key = com.fy.erp.constant.RedisKeyPrefix.SMS_CODE + phone;
        String cachedCode = redisTemplate.opsForValue().get(key);

        if (cachedCode == null || !cachedCode.equals(code)) {
            throw new BizException(400, "验证码错误或已过期");
        }

        // 验证通过，查找用户
        SysUser user = userService.findByPhone(phone);
        if (user == null || user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(404, "手机号未注册或账号已禁用");
        }

        // 登录成功，清除验证码
        redisTemplate.delete(key);

        return getLoginResponse(user);
    }

    private Result<LoginResponse> getLoginResponse(SysUser user) {
        List<String> roles = userService.getRoleCodes(user.getId());
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("nickname", user.getNickname());
        claims.put("roles", String.join(",", roles));
        String token = JwtUtil.createToken(user.getUsername(), claims, secret, expireMillis);
        long expiresAt = System.currentTimeMillis() + expireMillis;
        return Result.success(new LoginResponse(token, "Bearer", expiresAt, user.getId(), user.getNickname(), roles));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @GetMapping("/me")
    public Result<UserPrincipal> me() {
        return Result.success(UserContext.get());
    }

    /**
     * 获取当前用户的菜单权限列表（RBAC 动态菜单）
     */
    @GetMapping("/menus")
    public Result<java.util.Map<String, Object>> getMenus() {
        UserPrincipal user = UserContext.get();
        List<String> roles = user.getRoles();

        List<java.util.Map<String, String>> menus = new java.util.ArrayList<>();
        List<String> permCodes = new java.util.ArrayList<>();

        if (roles.contains("ADMIN")) {
            // 超级管理员：返回所有菜单
            List<com.fy.erp.entities.SysPermission> allPerms = sysPermissionService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.fy.erp.entities.SysPermission>()
                            .eq(com.fy.erp.entities.SysPermission::getStatus, 1));
            for (com.fy.erp.entities.SysPermission p : allPerms) {
                if ("MENU".equals(p.getType())) {
                    java.util.Map<String, String> m = new java.util.HashMap<>();
                    m.put("permCode", p.getPermCode());
                    m.put("permName", p.getPermName());
                    m.put("path", p.getPath());
                    menus.add(m);
                }
                permCodes.add(p.getPermCode());
            }
        } else {
            // 非管理员：根据角色权限过滤
            List<com.fy.erp.entities.SysRole> roleEntities = sysRoleService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.fy.erp.entities.SysRole>()
                            .in(com.fy.erp.entities.SysRole::getRoleCode, roles));
            List<Long> roleIds = roleEntities.stream().map(com.fy.erp.entities.SysRole::getId).toList();
            if (!roleIds.isEmpty()) {
                List<com.fy.erp.entities.SysRolePermission> rolePerms = sysRolePermissionService.list(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.fy.erp.entities.SysRolePermission>()
                                .in(com.fy.erp.entities.SysRolePermission::getRoleId, roleIds));
                List<Long> permIds = rolePerms.stream().map(com.fy.erp.entities.SysRolePermission::getPermId).toList();
                if (!permIds.isEmpty()) {
                    List<com.fy.erp.entities.SysPermission> perms = sysPermissionService.list(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.fy.erp.entities.SysPermission>()
                                    .in(com.fy.erp.entities.SysPermission::getId, permIds)
                                    .eq(com.fy.erp.entities.SysPermission::getStatus, 1));
                    for (com.fy.erp.entities.SysPermission p : perms) {
                        if ("MENU".equals(p.getType())) {
                            java.util.Map<String, String> m = new java.util.HashMap<>();
                            m.put("permCode", p.getPermCode());
                            m.put("permName", p.getPermName());
                            m.put("path", p.getPath());
                            menus.add(m);
                        }
                        permCodes.add(p.getPermCode());
                    }
                }
            }
        }

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("menus", menus);
        result.put("permissions", permCodes);
        return Result.success(result);
    }
}

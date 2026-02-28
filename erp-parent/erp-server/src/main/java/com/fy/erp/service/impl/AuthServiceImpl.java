package com.fy.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fy.erp.constant.RedisKeyPrefix;
import com.fy.erp.dto.LoginRequestDTO;
import com.fy.erp.dto.LoginResponseDTO;
import com.fy.erp.dto.SmsLoginRequestDTO;
import com.fy.erp.entities.*;
import com.fy.erp.exception.BizException;
import com.fy.erp.security.UserContext;
import com.fy.erp.security.UserPrincipal;
import com.fy.erp.service.*;
import com.fy.erp.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AuthServiceImpl implements IAuthService {

  private final SysUserService userService;
  private final StringRedisTemplate redisTemplate;
  private final SysPermissionService sysPermissionService;
  private final SysRoleService sysRoleService;
  private final SysRolePermissionService sysRolePermissionService;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expire-millis}")
  private long expireMillis;

  public AuthServiceImpl(SysUserService userService,
      StringRedisTemplate redisTemplate,
      SysPermissionService sysPermissionService,
      SysRoleService sysRoleService,
      SysRolePermissionService sysRolePermissionService) {
    this.userService = userService;
    this.redisTemplate = redisTemplate;
    this.sysPermissionService = sysPermissionService;
    this.sysRoleService = sysRoleService;
    this.sysRolePermissionService = sysRolePermissionService;
  }

  @Override
  public LoginResponseDTO login(LoginRequestDTO request) {
    SysUser user = userService.findByUsername(request.getUsername());
    if (user == null || user.getStatus() != null && user.getStatus() == 0) {
      throw new BizException(401, "invalid username or password");
    }
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new BizException(401, "invalid username or password");
    }
    return buildLoginResponse(user);
  }

  @Override
  public LoginResponseDTO loginBySms(SmsLoginRequestDTO request) {
    String phone = request.getPhone();
    String code = request.getCode();

    String key = RedisKeyPrefix.SMS_CODE + phone;
    String cachedCode = redisTemplate.opsForValue().get(key);

    if (cachedCode == null || !cachedCode.equals(code)) {
      throw new BizException(400, "验证码错误或已过期");
    }

    SysUser user = userService.findByPhone(phone);
    if (user == null || user.getStatus() != null && user.getStatus() == 0) {
      throw new BizException(404, "手机号未注册或账号已禁用");
    }

    redisTemplate.delete(key);
    return buildLoginResponse(user);
  }

  @Override
  public Map<String, Object> getMenusForCurrentUser() {
    UserPrincipal user = UserContext.get();
    List<String> roles = user.getRoles();

    List<Map<String, String>> menus = new ArrayList<>();
    List<String> permCodes = new ArrayList<>();

    if (roles.contains("ADMIN")) {
      List<SysPermission> allPerms = sysPermissionService.list(
          new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getStatus, 1));
      for (SysPermission p : allPerms) {
        if ("MENU".equals(p.getType())) {
          Map<String, String> m = new HashMap<>();
          m.put("permCode", p.getPermCode());
          m.put("permName", p.getPermName());
          m.put("path", p.getPath());
          menus.add(m);
        }
        permCodes.add(p.getPermCode());
      }
    } else {
      List<SysRole> roleEntities = sysRoleService.list(
          new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleCode, roles));
      List<Long> roleIds = roleEntities.stream().map(SysRole::getId).toList();
      if (!roleIds.isEmpty()) {
        List<SysRolePermission> rolePerms = sysRolePermissionService.list(
            new LambdaQueryWrapper<SysRolePermission>().in(SysRolePermission::getRoleId, roleIds));
        List<Long> permIds = rolePerms.stream().map(SysRolePermission::getPermId).toList();
        if (!permIds.isEmpty()) {
          List<SysPermission> perms = sysPermissionService.list(
              new LambdaQueryWrapper<SysPermission>()
                  .in(SysPermission::getId, permIds)
                  .eq(SysPermission::getStatus, 1));
          for (SysPermission p : perms) {
            if ("MENU".equals(p.getType())) {
              Map<String, String> m = new HashMap<>();
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

    Map<String, Object> result = new HashMap<>();
    result.put("menus", menus);
    result.put("permissions", permCodes);
    return result;
  }

  // ---- private ----

  private LoginResponseDTO buildLoginResponse(SysUser user) {
    List<String> roles = userService.getRoleCodes(user.getId());
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId());
    claims.put("username", user.getUsername());
    claims.put("nickname", user.getNickname());
    claims.put("roles", String.join(",", roles));
    String token = JwtUtil.createToken(user.getUsername(), claims, secret, expireMillis);
    long expiresAt = System.currentTimeMillis() + expireMillis;
    return new LoginResponseDTO(token, "Bearer", expiresAt, user.getId(), user.getNickname(), roles);
  }
}

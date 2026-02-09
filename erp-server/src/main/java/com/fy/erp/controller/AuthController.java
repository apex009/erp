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
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire-millis}")
    private long expireMillis;

    public AuthController(SysUserService userService) {
        this.userService = userService;
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
}

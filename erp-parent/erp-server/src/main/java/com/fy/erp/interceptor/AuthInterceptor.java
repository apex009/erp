package com.fy.erp.interceptor;

import com.fy.erp.exception.BizException;
import com.fy.erp.security.UserContext;
import com.fy.erp.security.UserPrincipal;
import com.fy.erp.service.RbacService;
import com.fy.erp.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final RbacService rbacService;

    public AuthInterceptor(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String auth = request.getHeader("Authorization");
        if (auth == null || auth.isBlank()) {
            throw new BizException(401, "unauthorized");
        }
        String token = auth.startsWith("Bearer ") ? auth.substring(7) : auth;
        Claims claims;
        try {
            claims = JwtUtil.parseToken(token, secret);
        } catch (Exception e) {
            throw new BizException(401, "invalid token");
        }
        Long userId = claims.get("userId", Long.class);
        String username = claims.get("username", String.class);
        String nickname = claims.get("nickname", String.class);
        String roleStr = claims.get("roles", String.class);
        List<String> roles = roleStr == null || roleStr.isBlank()
                ? Collections.emptyList()
                : Arrays.asList(roleStr.split(","));
        UserContext.set(new UserPrincipal(userId, username, nickname, roles));
        String requestPath = request.getRequestURI();
        String requestMethod = request.getMethod();
        if (!rbacService.hasPermission(userId, roles, requestMethod, requestPath)) {
            throw new BizException(403, "forbidden");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        UserContext.clear();
    }
}

package com.fy.erp.controller;

import com.fy.erp.dto.LoginRequestDTO;
import com.fy.erp.dto.LoginResponseDTO;
import com.fy.erp.dto.SmsLoginRequestDTO;
import com.fy.erp.result.Result;
import com.fy.erp.security.UserContext;
import com.fy.erp.security.UserPrincipal;
import com.fy.erp.service.IAuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public com.fy.erp.result.Result<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/login/sms")
    public Result<LoginResponseDTO> loginBySms(@Valid @RequestBody SmsLoginRequestDTO request) {
        return Result.success(authService.loginBySms(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return com.fy.erp.result.Result.success();
    }

    @GetMapping("/me")
    public Result<UserPrincipal> me() {
        return com.fy.erp.result.Result.success(UserContext.get());
    }

    @GetMapping("/menus")
    public Result<Map<String, Object>> getMenus() {
        return Result.success(authService.getMenusForCurrentUser());
    }
}

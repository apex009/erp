package com.fy.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tokenType;
    private Long expiresAt;
    private Long userId;
    private String nickname;
    private List<String> roles;
}

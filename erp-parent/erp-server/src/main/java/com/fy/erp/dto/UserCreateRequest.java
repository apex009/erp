package com.fy.erp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class UserCreateRequest {
    private Long deptId;
    @NotBlank(message = "username required")
    private String username;
    @NotBlank(message = "password required")
    private String password;
    private String nickname;
    private String phone;
    private String email;
    private Integer status;
    private List<Long> roleIds;
}

package com.fy.erp.service;

import com.fy.erp.dto.LoginRequestDTO;
import com.fy.erp.dto.LoginResponseDTO;
import com.fy.erp.dto.SmsLoginRequestDTO;

import java.util.Map;

/**
 * 认证服务接口
 */
public interface IAuthService {

  LoginResponseDTO login(LoginRequestDTO request);

  LoginResponseDTO loginBySms(SmsLoginRequestDTO request);

  Map<String, Object> getMenusForCurrentUser();
}

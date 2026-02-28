package com.fy.erp.security;

import lombok.Data;
import java.util.List;

@Data
public class UserPrincipal {
  private Long userId;
  private String username;
  private String nickname;
  private List<String> roles;
}

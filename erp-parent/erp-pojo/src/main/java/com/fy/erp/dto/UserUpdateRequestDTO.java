package com.fy.erp.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserUpdateRequestDTO {
  private Long deptId;
  private String password;
  private String nickname;
  private String phone;
  private String email;
  private Integer status;
  private List<Long> roleIds;
}

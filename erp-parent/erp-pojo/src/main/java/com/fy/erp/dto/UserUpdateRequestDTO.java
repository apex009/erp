package com.fy.erp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserUpdateRequestDTO {
  private Long id;
  private String username;
  private Long deptId;
  private String password;
  private String nickname;
  private String phone;
  private String email;
  private Integer status;
  private List<Long> roleIds;
}

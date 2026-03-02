package com.fy.erp.controller;

import com.fy.erp.result.Result;
import com.fy.erp.service.IAuthService;
import com.fy.erp.security.UserContext;
import com.fy.erp.security.UserPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/debug")
public class DebugController {
  private final IAuthService authService;

  public DebugController(IAuthService authService) {
    this.authService = authService;
  }

  @GetMapping("/menus/fin")
  public Result<Map<String, Object>> debugFinMenus() {
    // Force UserContext to Finance User (ID 4)
    UserContext.set(new UserPrincipal(4L, "finance", "财务总监", Arrays.asList("FIN")));
    try {
      return Result.success(authService.getMenusForCurrentUser());
    } finally {
      UserContext.clear();
    }
  }
}

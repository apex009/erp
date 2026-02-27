package com.fy.erp.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.erp.entities.SysPermission;
import com.fy.erp.mapper.SysPermissionMapper;
import org.springframework.stereotype.Service;

@Service
public class SysPermissionService extends ServiceImpl<SysPermissionMapper, SysPermission> {

  @org.springframework.beans.factory.annotation.Autowired
  private org.springframework.context.ApplicationContext applicationContext;

  public void syncPermissions() {
    org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping mapping = applicationContext
        .getBean(org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping.class);

    java.util.Map<org.springframework.web.servlet.mvc.method.RequestMappingInfo, org.springframework.web.method.HandlerMethod> map = mapping
        .getHandlerMethods();

    java.util.List<SysPermission> newPermissions = new java.util.ArrayList<>();

    for (org.springframework.web.servlet.mvc.method.RequestMappingInfo info : map.keySet()) {
      java.util.Set<String> patterns = info.getPatternValues();
      if (patterns.isEmpty() && info.getPathPatternsCondition() != null) {
        patterns = info.getPathPatternsCondition().getPatternValues();
      }
      if (patterns.isEmpty())
        continue;

      String path = patterns.iterator().next();
      if (path.startsWith("/error"))
        continue;

      java.util.Set<org.springframework.web.bind.annotation.RequestMethod> methods = info.getMethodsCondition()
          .getMethods();
      String method = methods.isEmpty() ? "ALL" : methods.iterator().next().name();

      String permCode = path.replace("/", ":").substring(1) + ":" + method.toLowerCase();

      if (this.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysPermission>()
          .eq(SysPermission::getPermCode, permCode)) == 0) {
        SysPermission p = new SysPermission();
        p.setPermCode(permCode);
        p.setPermName(path + " [" + method + "]");
        p.setPath(path);
        p.setMethod(method);
        p.setType(method.equals("GET") ? "API_READ" : "API_WRITE");
        p.setStatus(1);
        newPermissions.add(p);
      }
    }

    if (!newPermissions.isEmpty()) {
      this.saveBatch(newPermissions);
    }
  }
}

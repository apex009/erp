package com.fy.erp.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.erp.dto.PermissionTreeDTO;
import com.fy.erp.entities.SysPermission;
import com.fy.erp.mapper.SysPermissionMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Set;

@Service
public class SysPermissionService extends ServiceImpl<SysPermissionMapper, SysPermission> {

  @org.springframework.beans.factory.annotation.Autowired
  private org.springframework.context.ApplicationContext applicationContext;

  public List<PermissionTreeDTO> getPermissionTree() {
    List<SysPermission> all = this.list();
    Map<String, Long> menuMap = all.stream()
        .filter(p -> p.getType() != null && "MENU".equalsIgnoreCase(p.getType()))
        .collect(Collectors.toMap(SysPermission::getPermCode, SysPermission::getId, (v1, v2) -> v1));

    Map<String, Long> macroMap = all.stream()
        .filter(p -> p.getPermCode().endsWith(":read") || p.getPermCode().endsWith(":write"))
        .collect(Collectors.toMap(SysPermission::getPermCode, SysPermission::getId, (v1, v2) -> v1));

    // Root Groups
    Map<String, List<String>> structure = new LinkedHashMap<>();
    structure.put("基础数据", Arrays.asList("customers", "products", "suppliers", "warehouses"));
    structure.put("采购管理", Arrays.asList("purchase-requests", "purchase-orders"));
    structure.put("销售管理", Arrays.asList("leads", "sales-orders"));
    structure.put("库存管理", Arrays.asList("stocks", "stock-records", "stock-checks", "stock-transfers"));
    structure.put("财务管理", Arrays.asList("receivables", "payables", "receipts", "payments"));
    structure.put("系统管理", Arrays.asList("users", "roles", "depts", "permissions"));
    structure.put("经营看板", Arrays.asList("reports", "auth"));

    Map<String, String> labelMap = new HashMap<>();
    labelMap.put("customers", "客户管理");
    labelMap.put("products", "商品管理");
    labelMap.put("suppliers", "供应商管理");
    labelMap.put("warehouses", "仓库管理");
    labelMap.put("purchase-requests", "采购申请");
    labelMap.put("purchase-orders", "采购订单");
    labelMap.put("leads", "销售线索");
    labelMap.put("sales-orders", "销售订单");
    labelMap.put("stocks", "库存查询");
    labelMap.put("stock-records", "库存流水");
    labelMap.put("stock-checks", "库存盘点");
    labelMap.put("stock-transfers", "库存调拨");
    labelMap.put("receivables", "应收管理");
    labelMap.put("payables", "应付管理");
    labelMap.put("receipts", "收款管理");
    labelMap.put("payments", "付款管理");
    labelMap.put("users", "用户管理");
    labelMap.put("roles", "角色管理");
    labelMap.put("depts", "部门管理");
    labelMap.put("permissions", "权限项");
    labelMap.put("reports", "报表分析");
    labelMap.put("auth", "个人中心");

    // Module to Menu Mapping
    Map<String, String> modToMenu = new HashMap<>();
    modToMenu.put("customers", "menu:base:customers");
    modToMenu.put("products", "menu:base:products");
    modToMenu.put("suppliers", "menu:base:suppliers");
    modToMenu.put("warehouses", "menu:base:warehouses");
    modToMenu.put("purchase-requests", "menu:purchase:requests");
    modToMenu.put("purchase-orders", "menu:purchase:orders");
    modToMenu.put("leads", "menu:sales:leads");
    modToMenu.put("sales-orders", "menu:sales:orders");
    modToMenu.put("stocks", "menu:inventory:stocks");
    modToMenu.put("stock-records", "menu:inventory:records");
    modToMenu.put("stock-checks", "menu:inventory:records"); // Fallback to records
    modToMenu.put("stock-transfers", "menu:inventory:records"); // Fallback to records
    modToMenu.put("receivables", "menu:finance:receivables");
    modToMenu.put("payables", "menu:finance:payables");
    modToMenu.put("receipts", "menu:finance:receivables"); // Map to finance group
    modToMenu.put("payments", "menu:finance:payables"); // Map to finance group
    modToMenu.put("users", "menu:system:users");
    modToMenu.put("roles", "menu:system:roles");
    modToMenu.put("depts", "menu:system:depts");
    modToMenu.put("reports", "menu:dashboard");

    // Helper Map: which modules need which lookups
    Map<String, List<String>> helperMap = new HashMap<>();
    helperMap.put("stocks", Arrays.asList("api:warehouses:read", "api:products:read"));
    helperMap.put("stock-records", Arrays.asList("api:warehouses:read", "api:products:read"));
    helperMap.put("purchase-orders", Arrays.asList("api:suppliers:read", "api:products:read"));
    helperMap.put("purchase-requests", Arrays.asList("api:suppliers:read", "api:products:read"));
    helperMap.put("sales-orders", Arrays.asList("api:customers:read", "api:products:read"));
    helperMap.put("receivables", Arrays.asList("api:customers:read"));
    helperMap.put("payables", Arrays.asList("api:suppliers:read"));

    // Macro Perm Mapping (Group Level)
    Map<String, String> modToMacroRead = new HashMap<>();
    modToMacroRead.put("stocks", "api:inventory:read");
    modToMacroRead.put("stock-records", "api:inventory:read");
    modToMacroRead.put("stock-checks", "api:inventory:read");
    modToMacroRead.put("stock-transfers", "api:inventory:read");
    modToMacroRead.put("purchase-requests", "api:purchase:read");
    modToMacroRead.put("purchase-orders", "api:purchase:read");
    modToMacroRead.put("leads", "api:sales:read");
    modToMacroRead.put("sales-orders", "api:sales:read");
    modToMacroRead.put("receivables", "api:receivables:read");
    modToMacroRead.put("payables", "api:payables:read");
    modToMacroRead.put("users", "api:system:read");
    modToMacroRead.put("roles", "api:system:read");
    modToMacroRead.put("depts", "api:system:read");

    List<PermissionTreeDTO> root = new ArrayList<>();

    for (Map.Entry<String, List<String>> group : structure.entrySet()) {
      PermissionTreeDTO groupNode = PermissionTreeDTO.builder()
          .id("group-" + group.getKey())
          .label(group.getKey())
          .children(new ArrayList<>())
          .build();

      for (String moduleKey : group.getValue()) {
        String subLabel = labelMap.getOrDefault(moduleKey, moduleKey);
        PermissionTreeDTO moduleNode = PermissionTreeDTO.builder()
            .id("mod-" + moduleKey)
            .label(subLabel)
            .children(new ArrayList<>())
            .build();

        Long menuId = menuMap.get(modToMenu.get(moduleKey));
        Long macroReadId = macroMap.get(modToMacroRead.get(moduleKey));

        // View Only
        Set<Long> readIds = all.stream()
            .filter(p -> p.getPermCode().startsWith("api:" + moduleKey) &&
                (p.getType() != null && ("API_READ".equalsIgnoreCase(p.getType()) || p.getPermCode().endsWith(":get")
                    || p.getPermCode().endsWith(":read")
                    || p.getPermCode().endsWith(":list"))))
            .map(SysPermission::getId)
            .collect(Collectors.toSet());

        if (menuId != null) {
          readIds.add(menuId);
        }
        if (macroReadId != null) {
          readIds.add(macroReadId);
        }

        // Add Helpers
        List<String> helpers = helperMap.get(moduleKey);
        if (helpers != null) {
          for (String hCode : helpers) {
            Long hId = macroMap.get(hCode);
            if (hId != null)
              readIds.add(hId);
          }
        }

        if (!readIds.isEmpty()) {
          moduleNode.addChild(PermissionTreeDTO.builder()
              .id("view-" + moduleKey)
              .label("查看权限")
              .permIds(new ArrayList<>(readIds))
              .build());
        }

        // Manage/Modify
        Set<Long> writeIds = all.stream()
            .filter(p -> p.getPermCode().startsWith("api:" + moduleKey) &&
                (p.getType() != null && ("API_WRITE".equalsIgnoreCase(p.getType())
                    || (!"API_READ".equalsIgnoreCase(p.getType()) && !"MENU".equalsIgnoreCase(p.getType())))))
            .map(SysPermission::getId)
            .collect(Collectors.toSet());

        // Exclude IDs already in read node for clarity, but menuId should stay if
        // Manage is picked alone
        writeIds.removeAll(readIds);

        if (menuId != null) {
          writeIds.add(menuId);
        }
        if (macroReadId != null) {
          writeIds.add(macroReadId);
        }

        if (!writeIds.isEmpty()) {
          moduleNode.addChild(PermissionTreeDTO.builder()
              .id("edit-" + moduleKey)
              .label("管理权限")
              .permIds(new ArrayList<>(writeIds))
              .build());
        }

        if (moduleNode.getChildren() != null && !moduleNode.getChildren().isEmpty()) {
          groupNode.addChild(moduleNode);
        }
      }

      if (groupNode.getChildren() != null && !groupNode.getChildren().isEmpty()) {
        root.add(groupNode);
      }
    }

    return root;
  }

  @org.springframework.cache.annotation.CacheEvict(value = com.fy.erp.constant.RedisKeyPrefix.AUTH_PERM, allEntries = true)
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

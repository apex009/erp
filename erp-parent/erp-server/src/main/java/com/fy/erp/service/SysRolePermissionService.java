package com.fy.erp.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.erp.entities.SysRolePermission;
import com.fy.erp.mapper.SysRolePermissionMapper;
import org.springframework.stereotype.Service;

@Service
public class SysRolePermissionService extends ServiceImpl<SysRolePermissionMapper, SysRolePermission> {
  private final RbacService rbacService;

  public SysRolePermissionService(@org.springframework.context.annotation.Lazy RbacService rbacService) {
    this.rbacService = rbacService;
  }

  @Override
  public boolean save(com.fy.erp.entities.SysRolePermission entity) {
    rbacService.evictByRole(entity.getRoleId());
    return super.save(entity);
  }

  @Override
  public boolean removeById(java.io.Serializable id) {
    com.fy.erp.entities.SysRolePermission rp = getById(id);
    if (rp != null)
      rbacService.evictByRole(rp.getRoleId());
    return super.removeById(id);
  }
}

package com.fy.erp.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.erp.entities.SysUserRole;
import com.fy.erp.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Service;

@Service
public class SysUserRoleService extends ServiceImpl<SysUserRoleMapper, SysUserRole> {
  private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

  public SysUserRoleService(org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public boolean save(com.fy.erp.entities.SysUserRole entity) {
    evict(entity.getUserId());
    return super.save(entity);
  }

  @Override
  public boolean removeById(java.io.Serializable id) {
    com.fy.erp.entities.SysUserRole ur = getById(id);
    if (ur != null)
      evict(ur.getUserId());
    return super.removeById(id);
  }

  private void evict(Long userId) {
    if (userId != null) {
      redisTemplate.delete(com.fy.erp.constant.RedisKeyPrefix.AUTH_PERM + userId);
    }
  }
}

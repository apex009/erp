package com.fy.erp.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.erp.entities.Warehouse;
import com.fy.erp.mapper.WarehouseMapper;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

@Service
public class WarehouseService extends ServiceImpl<WarehouseMapper, Warehouse> {

  @Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.DICT_WAREHOUSE, key = "'all'")
  public java.util.List<com.fy.erp.entities.Warehouse> listAll() {
    return list();
  }

  @Override
  @CacheEvict(value = com.fy.erp.constant.RedisKeyPrefix.DICT_WAREHOUSE, allEntries = true)
  public boolean save(com.fy.erp.entities.Warehouse entity) {
    return super.save(entity);
  }

  @Override
  @CacheEvict(value = com.fy.erp.constant.RedisKeyPrefix.DICT_WAREHOUSE, allEntries = true)
  public boolean updateById(com.fy.erp.entities.Warehouse entity) {
    return super.updateById(entity);
  }

  @Override
  @CacheEvict(value = com.fy.erp.constant.RedisKeyPrefix.DICT_WAREHOUSE, allEntries = true)
  public boolean removeById(java.io.Serializable id) {
    return super.removeById(id);
  }
}

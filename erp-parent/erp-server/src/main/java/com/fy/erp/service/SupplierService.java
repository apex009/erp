package com.fy.erp.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.erp.entities.Supplier;
import com.fy.erp.mapper.SupplierMapper;
import org.springframework.stereotype.Service;

@Service
public class SupplierService extends ServiceImpl<SupplierMapper, Supplier> {
  @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.DICT_SUPPLIER, key = "'all'")
  public java.util.List<com.fy.erp.entities.Supplier> listAll() {
    return list();
  }

  @Override
  @org.springframework.cache.annotation.CacheEvict(value = com.fy.erp.constant.RedisKeyPrefix.DICT_SUPPLIER, allEntries = true)
  public boolean save(com.fy.erp.entities.Supplier entity) {
    return super.save(entity);
  }

  @Override
  @org.springframework.cache.annotation.CacheEvict(value = com.fy.erp.constant.RedisKeyPrefix.DICT_SUPPLIER, allEntries = true)
  public boolean updateById(com.fy.erp.entities.Supplier entity) {
    return super.updateById(entity);
  }

  @Override
  @org.springframework.cache.annotation.CacheEvict(value = com.fy.erp.constant.RedisKeyPrefix.DICT_SUPPLIER, allEntries = true)
  public boolean removeById(java.io.Serializable id) {
    return super.removeById(id);
  }
}

package com.fy.erp.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.erp.entities.Product;
import com.fy.erp.mapper.ProductMapper;
import org.springframework.stereotype.Service;

@Service
public class ProductService extends ServiceImpl<ProductMapper, Product> {

  @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.DICT_CATEGORY, key = "'all'")
  public java.util.List<String> listCategories() {
    return this.list().stream()
        .map(com.fy.erp.entities.Product::getCategory)
        .filter(c -> c != null && !c.isBlank())
        .distinct()
        .toList();
  }

  @Override
  @org.springframework.cache.annotation.CacheEvict(value = com.fy.erp.constant.RedisKeyPrefix.DICT_CATEGORY, allEntries = true)
  public boolean save(com.fy.erp.entities.Product entity) {
    return super.save(entity);
  }

  @Override
  @org.springframework.cache.annotation.CacheEvict(value = com.fy.erp.constant.RedisKeyPrefix.DICT_CATEGORY, allEntries = true)
  public boolean updateById(com.fy.erp.entities.Product entity) {
    return super.updateById(entity);
  }

  @Override
  @org.springframework.cache.annotation.CacheEvict(value = com.fy.erp.constant.RedisKeyPrefix.DICT_CATEGORY, allEntries = true)
  public boolean removeById(java.io.Serializable id) {
    return super.removeById(id);
  }
}

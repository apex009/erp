package com.fy.erp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fy.erp.entities.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}

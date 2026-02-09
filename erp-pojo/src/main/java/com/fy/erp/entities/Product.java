package com.fy.erp.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {
    private String sku;
    private String name;
    private String category;
    private String unit;
    private String spec;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private Integer status;
}

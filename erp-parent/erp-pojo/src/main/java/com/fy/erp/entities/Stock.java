package com.fy.erp.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock")
public class Stock extends BaseEntity {
    private Long productId;
    private Long warehouseId;
    private BigDecimal quantity;
    private BigDecimal safeStock;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String productName;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String productSku;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String unit;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String warehouseName;
}

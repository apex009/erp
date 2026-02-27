package com.fy.erp.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sales_item")
public class SalesItem extends BaseEntity {
    private Long orderId;
    private Long productId;
    private Long warehouseId;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal amount;
}

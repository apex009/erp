package com.fy.erp.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_item")
public class PurchaseItem extends BaseEntity {
    private Long orderId;
    private Long productId;
    private Long warehouseId;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal amount;
}

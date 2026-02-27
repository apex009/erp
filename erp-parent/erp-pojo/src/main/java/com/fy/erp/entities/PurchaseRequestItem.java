package com.fy.erp.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_request_item")
public class PurchaseRequestItem extends BaseEntity {
    private Long requestId;
    private Long productId;
    private Long warehouseId;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal amount;
}

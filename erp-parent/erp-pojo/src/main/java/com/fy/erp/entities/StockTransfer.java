package com.fy.erp.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_transfer")
public class StockTransfer extends BaseEntity {
    private String orderNo;
    private Long fromWarehouseId;
    private Long toWarehouseId;
    private BigDecimal totalQuantity;
    private Integer status;
    private String remark;
}

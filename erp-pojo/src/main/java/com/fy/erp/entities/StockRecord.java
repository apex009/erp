package com.fy.erp.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_record")
public class StockRecord extends BaseEntity {
    private Long productId;
    private Long warehouseId;
    private BigDecimal quantity;
    private String recordType;
    private String bizType;
    private Long bizId;
    private String remark;
}

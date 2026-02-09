package com.fy.erp.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_check_item")
public class StockCheckItem extends BaseEntity {
    private Long checkId;
    private Long productId;
    private BigDecimal systemQty;
    private BigDecimal actualQty;
    private BigDecimal diffQty;
}

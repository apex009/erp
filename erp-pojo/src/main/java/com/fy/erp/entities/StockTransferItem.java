package com.fy.erp.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_transfer_item")
public class StockTransferItem extends BaseEntity {
    private Long transferId;
    private Long productId;
    private BigDecimal quantity;
}

package com.fy.erp.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_check")
public class StockCheck extends BaseEntity {
    private String orderNo;
    private Long warehouseId;
    private Integer status;
    private String remark;
}

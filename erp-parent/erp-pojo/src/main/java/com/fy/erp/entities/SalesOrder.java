package com.fy.erp.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sales_order")
public class SalesOrder extends BaseEntity {
    private String orderNo;
    private Long customerId;
    private Long ownerUserId;
    private BigDecimal totalAmount;
    private Integer status;
    private String remark;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String customerName;
}

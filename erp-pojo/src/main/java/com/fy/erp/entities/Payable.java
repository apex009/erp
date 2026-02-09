package com.fy.erp.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("payable")
public class Payable extends BaseEntity {
    private Long supplierId;
    private Long orderId;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private Integer status;
}

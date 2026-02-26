package com.fy.erp.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("receivable")
public class Receivable extends BaseEntity {
    private Long customerId;
    private Long orderId;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private Integer status;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String customerName;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String sourceOrderNo;
}

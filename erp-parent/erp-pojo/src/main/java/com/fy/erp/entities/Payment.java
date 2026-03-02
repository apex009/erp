package com.fy.erp.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fin_payment")
public class Payment extends BaseEntity {
    private Long payableId;
    private BigDecimal amount;
    private String method;
    private String remark;
}

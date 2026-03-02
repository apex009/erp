package com.fy.erp.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fin_receipt")
public class Receipt extends BaseEntity {
    private Long receivableId;
    private BigDecimal amount;
    private String method;
    private String remark;
}

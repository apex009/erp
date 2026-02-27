package com.fy.erp.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sales_lead")
public class SalesLead extends BaseEntity {
    private Long customerId;
    private String name;
    private String stage;
    private BigDecimal expectedAmount;
    private Integer probability;
    private Long ownerUserId;
    private LocalDateTime nextFollowTime;
    private String remark;
    private Integer status;
}

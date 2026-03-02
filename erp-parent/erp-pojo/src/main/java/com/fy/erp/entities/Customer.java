package com.fy.erp.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer")
public class Customer extends BaseEntity {
    private String name;
    private String contact;
    private String phone;
    private Integer level;
    private Long ownerUserId;
    private BigDecimal creditLimit;
    private Integer status;
}

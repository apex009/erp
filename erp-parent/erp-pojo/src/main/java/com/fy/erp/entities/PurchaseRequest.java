package com.fy.erp.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_request")
public class PurchaseRequest extends BaseEntity {
    private String requestNo;
    private Long supplierId;
    private BigDecimal totalAmount;
    private Integer status;
    private Long orderId;
    private String remark;
}

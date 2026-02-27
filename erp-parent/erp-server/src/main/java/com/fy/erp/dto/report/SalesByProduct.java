package com.fy.erp.dto.report;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesByProduct {
    private Long productId;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal amount;
}

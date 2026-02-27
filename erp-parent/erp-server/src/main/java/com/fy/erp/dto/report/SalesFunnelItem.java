package com.fy.erp.dto.report;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesFunnelItem {
    private String stage;
    private Long count;
    private BigDecimal amount;
}

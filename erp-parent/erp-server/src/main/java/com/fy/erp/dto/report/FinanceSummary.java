package com.fy.erp.dto.report;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FinanceSummary {
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal unpaidAmount;
}

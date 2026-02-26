package com.fy.erp.dto.report;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesAmountByDay {
    private String statDate;
    private BigDecimal amount;
}

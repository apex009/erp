package com.fy.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentCreateRequest {
    @NotNull(message = "amount required")
    private BigDecimal amount;
    private String method;
    private String remark;
}

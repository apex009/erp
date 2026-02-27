package com.fy.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseRequestItemRequest {
    @NotNull(message = "productId required")
    private Long productId;
    @NotNull(message = "warehouseId required")
    private Long warehouseId;
    @NotNull(message = "quantity required")
    private BigDecimal quantity;
    @NotNull(message = "price required")
    private BigDecimal price;
}

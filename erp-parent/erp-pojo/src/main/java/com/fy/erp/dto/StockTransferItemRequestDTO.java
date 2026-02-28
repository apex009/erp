package com.fy.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class StockTransferItemRequestDTO {
  @NotNull(message = "productId required")
  private Long productId;
  @NotNull(message = "quantity required")
  private BigDecimal quantity;
}

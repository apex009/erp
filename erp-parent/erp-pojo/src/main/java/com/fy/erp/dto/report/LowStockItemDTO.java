package com.fy.erp.dto.report;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LowStockItemDTO {
  private Long productId;
  private String productName;
  private Long warehouseId;
  private BigDecimal quantity;
  private BigDecimal safeStock;
}

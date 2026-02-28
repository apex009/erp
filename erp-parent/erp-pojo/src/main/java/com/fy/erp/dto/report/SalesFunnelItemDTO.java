package com.fy.erp.dto.report;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SalesFunnelItemDTO {
  private String stageCode;
  private String stageName;
  private Long count;
  private BigDecimal amount;
  private BigDecimal conversionRate;
}

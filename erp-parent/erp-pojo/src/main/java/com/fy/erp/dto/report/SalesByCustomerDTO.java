package com.fy.erp.dto.report;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SalesByCustomerDTO {
  private Long customerId;
  private String customerName;
  private BigDecimal amount;
}

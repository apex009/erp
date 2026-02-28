package com.fy.erp.dto.report;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SalesRankItem {
  private Long userId;
  private String nickname;
  private BigDecimal amount;
  private Integer orderCount;
}

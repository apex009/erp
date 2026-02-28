package com.fy.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class StockCheckCreateRequestDTO {
  @NotNull(message = "warehouseId required")
  private Long warehouseId;
  private String remark;
  @NotNull(message = "items required")
  private List<StockCheckItemRequestDTO> items;
}

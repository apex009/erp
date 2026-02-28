package com.fy.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class PurchaseOrderCreateRequestDTO {
  @NotNull(message = "supplierId required")
  private Long supplierId;
  private String remark;
  @NotNull(message = "items required")
  private List<PurchaseOrderItemRequestDTO> items;
}

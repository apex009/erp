package com.fy.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class StockTransferCreateRequest {
    @NotNull(message = "fromWarehouseId required")
    private Long fromWarehouseId;
    @NotNull(message = "toWarehouseId required")
    private Long toWarehouseId;
    private String remark;
    @NotNull(message = "items required")
    private List<StockTransferItemRequest> items;
}

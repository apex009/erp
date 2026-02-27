package com.fy.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SalesOrderCreateRequest {
    @NotNull(message = "customerId required")
    private Long customerId;
    private String remark;
    @NotNull(message = "items required")
    private List<SalesOrderItemRequest> items;
}

package com.fy.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fy.erp.dto.PurchaseOrderCreateRequest;
import com.fy.erp.entities.PurchaseItem;
import com.fy.erp.entities.PurchaseOrder;
import com.fy.erp.result.Result;
import com.fy.erp.service.PurchaseItemService;
import com.fy.erp.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {
    private final PurchaseOrderService orderService;
    private final PurchaseItemService itemService;

    public PurchaseOrderController(PurchaseOrderService orderService, PurchaseItemService itemService) {
        this.orderService = orderService;
        this.itemService = itemService;
    }

    @GetMapping
    public Result<Page<PurchaseOrder>> page(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) String orderNo,
                                            @RequestParam(required = false) Long supplierId,
                                            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        if (orderNo != null && !orderNo.isBlank()) {
            wrapper.like(PurchaseOrder::getOrderNo, orderNo);
        }
        if (supplierId != null) {
            wrapper.eq(PurchaseOrder::getSupplierId, supplierId);
        }
        if (status != null) {
            wrapper.eq(PurchaseOrder::getStatus, status);
        }
        return Result.success(orderService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/{id}")
    public Result<PurchaseOrder> detail(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    @GetMapping("/{id}/items")
    public Result<List<PurchaseItem>> items(@PathVariable Long id) {
        return Result.success(itemService.list(new LambdaQueryWrapper<PurchaseItem>().eq(PurchaseItem::getOrderId, id)));
    }

    @PostMapping
    public Result<PurchaseOrder> create(@Valid @RequestBody PurchaseOrderCreateRequest request) {
        return Result.success(orderService.createOrder(request));
    }

    @PostMapping("/{id}/approve")
    public Result<PurchaseOrder> approve(@PathVariable Long id) {
        return Result.success(orderService.approve(id));
    }

    @PostMapping("/{id}/cancel")
    public Result<PurchaseOrder> cancel(@PathVariable Long id) {
        return Result.success(orderService.cancel(id));
    }

    @PostMapping("/{id}/return")
    public Result<PurchaseOrder> refund(@PathVariable Long id) {
        return Result.success(orderService.refund(id));
    }
}

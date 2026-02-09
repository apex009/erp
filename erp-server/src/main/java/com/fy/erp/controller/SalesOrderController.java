package com.fy.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fy.erp.dto.SalesOrderCreateRequest;
import com.fy.erp.entities.SalesItem;
import com.fy.erp.entities.SalesOrder;
import com.fy.erp.result.Result;
import com.fy.erp.service.SalesItemService;
import com.fy.erp.service.SalesOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {
    private final SalesOrderService orderService;
    private final SalesItemService itemService;

    public SalesOrderController(SalesOrderService orderService, SalesItemService itemService) {
        this.orderService = orderService;
        this.itemService = itemService;
    }

    @GetMapping
    public Result<Page<SalesOrder>> page(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "10") long size,
                                         @RequestParam(required = false) String orderNo,
                                         @RequestParam(required = false) Long customerId,
                                         @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        if (orderNo != null && !orderNo.isBlank()) {
            wrapper.like(SalesOrder::getOrderNo, orderNo);
        }
        if (customerId != null) {
            wrapper.eq(SalesOrder::getCustomerId, customerId);
        }
        if (status != null) {
            wrapper.eq(SalesOrder::getStatus, status);
        }
        return Result.success(orderService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/{id}")
    public Result<SalesOrder> detail(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    @GetMapping("/{id}/items")
    public Result<List<SalesItem>> items(@PathVariable Long id) {
        return Result.success(itemService.list(new LambdaQueryWrapper<SalesItem>().eq(SalesItem::getOrderId, id)));
    }

    @PostMapping
    public Result<SalesOrder> create(@Valid @RequestBody SalesOrderCreateRequest request) {
        return Result.success(orderService.createOrder(request));
    }

    @PostMapping("/{id}/approve")
    public Result<SalesOrder> approve(@PathVariable Long id) {
        return Result.success(orderService.approve(id));
    }

    @PostMapping("/{id}/cancel")
    public Result<SalesOrder> cancel(@PathVariable Long id) {
        return Result.success(orderService.cancel(id));
    }

    @PostMapping("/{id}/return")
    public Result<SalesOrder> refund(@PathVariable Long id) {
        return Result.success(orderService.refund(id));
    }
}

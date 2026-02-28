package com.fy.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fy.erp.dto.PurchaseRequestCreateRequestDTO;
import com.fy.erp.entities.PurchaseRequest;
import com.fy.erp.entities.PurchaseRequestItem;
import com.fy.erp.result.Result;
import com.fy.erp.service.PurchaseRequestItemService;
import com.fy.erp.service.PurchaseRequestService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-requests")
public class PurchaseRequestController {
    private final PurchaseRequestService requestService;
    private final PurchaseRequestItemService itemService;

    public PurchaseRequestController(PurchaseRequestService requestService, PurchaseRequestItemService itemService) {
        this.requestService = requestService;
        this.itemService = itemService;
    }

    @GetMapping
    public Result<Page<PurchaseRequest>> page(@RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String requestNo,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<PurchaseRequest> wrapper = new LambdaQueryWrapper<>();
        if (requestNo != null && !requestNo.isBlank()) {
            wrapper.like(PurchaseRequest::getRequestNo, requestNo);
        }
        if (supplierId != null) {
            wrapper.eq(PurchaseRequest::getSupplierId, supplierId);
        }
        if (status != null) {
            wrapper.eq(PurchaseRequest::getStatus, status);
        }
        return Result.success(requestService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/{id}")
    public Result<PurchaseRequest> detail(@PathVariable Long id) {
        return Result.success(requestService.getById(id));
    }

    @GetMapping("/{id}/items")
    public Result<List<PurchaseRequestItem>> items(@PathVariable Long id) {
        return Result.success(itemService
                .list(new LambdaQueryWrapper<PurchaseRequestItem>().eq(PurchaseRequestItem::getRequestId, id)));
    }

    @PostMapping
    public Result<PurchaseRequest> create(@Valid @RequestBody PurchaseRequestCreateRequestDTO request) {
        return Result.success(requestService.createRequest(request));
    }

    @PostMapping("/{id}/approve")
    public Result<PurchaseRequest> approve(@PathVariable Long id) {
        return Result.success(requestService.approve(id));
    }

    @PostMapping("/{id}/reject")
    public Result<PurchaseRequest> reject(@PathVariable Long id) {
        return Result.success(requestService.reject(id));
    }

    @PostMapping("/{id}/to-order")
    public Result<PurchaseRequest> toOrder(@PathVariable Long id) {
        return Result.success(requestService.toOrder(id));
    }
}

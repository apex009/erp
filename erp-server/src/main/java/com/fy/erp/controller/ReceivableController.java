package com.fy.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fy.erp.dto.ReceiptCreateRequest;
import com.fy.erp.entities.Receivable;
import com.fy.erp.entities.Receipt;
import com.fy.erp.result.Result;
import com.fy.erp.service.ReceivableService;
import com.fy.erp.service.ReceiptService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/receivables")
public class ReceivableController {
    private final ReceivableService receivableService;
    private final ReceiptService receiptService;

    public ReceivableController(ReceivableService receivableService, ReceiptService receiptService) {
        this.receivableService = receivableService;
        this.receiptService = receiptService;
    }

    @GetMapping
    public Result<Page<Receivable>> page(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "10") long size,
                                         @RequestParam(required = false) Long customerId,
                                         @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Receivable> wrapper = new LambdaQueryWrapper<>();
        if (customerId != null) {
            wrapper.eq(Receivable::getCustomerId, customerId);
        }
        if (status != null) {
            wrapper.eq(Receivable::getStatus, status);
        }
        return Result.success(receivableService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/{id}")
    public Result<Receivable> detail(@PathVariable Long id) {
        return Result.success(receivableService.getById(id));
    }

    @GetMapping("/{id}/receipts")
    public Result<List<Receipt>> receipts(@PathVariable Long id) {
        return Result.success(receiptService.list(new LambdaQueryWrapper<Receipt>().eq(Receipt::getReceivableId, id)));
    }

    @PostMapping("/{id}/receipt")
    public Result<Receipt> receive(@PathVariable Long id, @Valid @RequestBody ReceiptCreateRequest request) {
        return Result.success(receivableService.receive(id, request.getAmount(), request.getMethod(), request.getRemark()));
    }
}

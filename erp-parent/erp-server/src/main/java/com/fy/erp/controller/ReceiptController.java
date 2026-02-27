package com.fy.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fy.erp.entities.Receipt;
import com.fy.erp.result.Result;
import com.fy.erp.service.ReceiptService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {
    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @GetMapping
    public Result<Page<Receipt>> page(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) Long receivableId) {
        LambdaQueryWrapper<Receipt> wrapper = new LambdaQueryWrapper<>();
        if (receivableId != null) {
            wrapper.eq(Receipt::getReceivableId, receivableId);
        }
        return Result.success(receiptService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/{id}")
    public Result<Receipt> detail(@PathVariable Long id) {
        return Result.success(receiptService.getById(id));
    }
}

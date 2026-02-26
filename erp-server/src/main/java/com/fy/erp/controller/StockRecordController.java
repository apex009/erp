package com.fy.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fy.erp.entities.StockRecord;
import com.fy.erp.enums.StockBizType;
import com.fy.erp.enums.StockRecordType;
import com.fy.erp.result.Result;
import com.fy.erp.service.StockRecordService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock-records")
public class StockRecordController {
    private final StockRecordService stockRecordService;

    public StockRecordController(StockRecordService stockRecordService) {
        this.stockRecordService = stockRecordService;
    }

    @GetMapping
    public Result<Page<StockRecord>> page(@RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer recordType,
            @RequestParam(required = false) Integer bizType) {
        LambdaQueryWrapper<StockRecord> wrapper = new LambdaQueryWrapper<>();
        if (productId != null) {
            wrapper.eq(StockRecord::getProductId, productId);
        }
        if (warehouseId != null) {
            wrapper.eq(StockRecord::getWarehouseId, warehouseId);
        }
        if (recordType != null) {
            wrapper.eq(StockRecord::getRecordType, recordType);
        }
        if (bizType != null) {
            wrapper.eq(StockRecord::getBizType, bizType);
        }
        return Result.success(stockRecordService.pageWithNames(new Page<>(page, size), wrapper));
    }
}

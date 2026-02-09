package com.fy.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fy.erp.entities.Stock;
import com.fy.erp.result.Result;
import com.fy.erp.service.StockService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stocks")
public class StockController {
    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public Result<Page<Stock>> page(@RequestParam(defaultValue = "1") long page,
                                    @RequestParam(defaultValue = "10") long size,
                                    @RequestParam(required = false) Long productId,
                                    @RequestParam(required = false) Long warehouseId) {
        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        if (productId != null) {
            wrapper.eq(Stock::getProductId, productId);
        }
        if (warehouseId != null) {
            wrapper.eq(Stock::getWarehouseId, warehouseId);
        }
        return Result.success(stockService.page(new Page<>(page, size), wrapper));
    }

    @PutMapping("/{id}")
    public Result<Stock> update(@PathVariable Long id, @RequestBody Stock stock) {
        stock.setId(id);
        stockService.updateById(stock);
        return Result.success(stock);
    }
}

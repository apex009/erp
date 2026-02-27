package com.fy.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fy.erp.dto.StockCheckCreateRequest;
import com.fy.erp.dto.StockCheckItemRequest;
import com.fy.erp.entities.StockCheck;
import com.fy.erp.entities.StockCheckItem;
import com.fy.erp.result.Result;
import com.fy.erp.service.StockCheckItemService;
import com.fy.erp.service.StockCheckService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock-checks")
public class StockCheckController {
    private final StockCheckService checkService;
    private final StockCheckItemService itemService;

    public StockCheckController(StockCheckService checkService, StockCheckItemService itemService) {
        this.checkService = checkService;
        this.itemService = itemService;
    }

    @GetMapping
    public Result<Page<StockCheck>> page(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "10") long size,
                                         @RequestParam(required = false) Long warehouseId) {
        LambdaQueryWrapper<StockCheck> wrapper = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            wrapper.eq(StockCheck::getWarehouseId, warehouseId);
        }
        return Result.success(checkService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/{id}")
    public Result<StockCheck> detail(@PathVariable Long id) {
        return Result.success(checkService.getById(id));
    }

    @GetMapping("/{id}/items")
    public Result<List<StockCheckItem>> items(@PathVariable Long id) {
        return Result.success(itemService.list(new LambdaQueryWrapper<StockCheckItem>().eq(StockCheckItem::getCheckId, id)));
    }

    @PostMapping
    public Result<StockCheck> create(@Valid @RequestBody StockCheckCreateRequest request) {
        List<StockCheckItem> items = request.getItems().stream().map(itemReq -> {
            StockCheckItem item = new StockCheckItem();
            item.setProductId(itemReq.getProductId());
            item.setActualQty(itemReq.getActualQty());
            return item;
        }).toList();
        return Result.success(checkService.createCheck(request.getWarehouseId(), request.getRemark(), items));
    }
}

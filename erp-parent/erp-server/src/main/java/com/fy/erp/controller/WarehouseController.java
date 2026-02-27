package com.fy.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fy.erp.entities.Warehouse;
import com.fy.erp.result.Result;
import com.fy.erp.service.WarehouseService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {
    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    public Result<Page<Warehouse>> page(@RequestParam(defaultValue = "1") long page,
                                        @RequestParam(defaultValue = "10") long size,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Warehouse::getName, keyword).or().like(Warehouse::getManager, keyword));
        }
        if (status != null) {
            wrapper.eq(Warehouse::getStatus, status);
        }
        return Result.success(warehouseService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/{id}")
    public Result<Warehouse> detail(@PathVariable Long id) {
        return Result.success(warehouseService.getById(id));
    }

    @PostMapping
    public Result<Warehouse> create(@RequestBody Warehouse warehouse) {
        warehouseService.save(warehouse);
        return Result.success(warehouse);
    }

    @PutMapping("/{id}")
    public Result<Warehouse> update(@PathVariable Long id, @RequestBody Warehouse warehouse) {
        warehouse.setId(id);
        warehouseService.updateById(warehouse);
        return Result.success(warehouse);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        warehouseService.removeById(id);
        return Result.success();
    }
}

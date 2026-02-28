package com.fy.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fy.erp.dto.StockTransferCreateRequestDTO;
import com.fy.erp.entities.StockTransfer;
import com.fy.erp.entities.StockTransferItem;
import com.fy.erp.result.Result;
import com.fy.erp.service.StockTransferItemService;
import com.fy.erp.service.StockTransferService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock-transfers")
public class StockTransferController {
    private final StockTransferService transferService;
    private final StockTransferItemService itemService;

    public StockTransferController(StockTransferService transferService, StockTransferItemService itemService) {
        this.transferService = transferService;
        this.itemService = itemService;
    }

    @GetMapping
    public Result<Page<StockTransfer>> page(@RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long fromWarehouseId,
            @RequestParam(required = false) Long toWarehouseId) {
        LambdaQueryWrapper<StockTransfer> wrapper = new LambdaQueryWrapper<>();
        if (fromWarehouseId != null) {
            wrapper.eq(StockTransfer::getFromWarehouseId, fromWarehouseId);
        }
        if (toWarehouseId != null) {
            wrapper.eq(StockTransfer::getToWarehouseId, toWarehouseId);
        }
        return Result.success(transferService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/{id}")
    public Result<StockTransfer> detail(@PathVariable Long id) {
        return Result.success(transferService.getById(id));
    }

    @GetMapping("/{id}/items")
    public Result<List<StockTransferItem>> items(@PathVariable Long id) {
        return Result.success(
                itemService.list(new LambdaQueryWrapper<StockTransferItem>().eq(StockTransferItem::getTransferId, id)));
    }

    @PostMapping
    public Result<StockTransfer> create(@Valid @RequestBody StockTransferCreateRequestDTO request) {
        List<StockTransferItem> items = request.getItems().stream().map(itemReq -> {
            StockTransferItem item = new StockTransferItem();
            item.setProductId(itemReq.getProductId());
            item.setQuantity(itemReq.getQuantity());
            return item;
        }).toList();
        return Result.success(
                transferService.createTransfer(request.getFromWarehouseId(), request.getToWarehouseId(),
                        request.getRemark(), items));
    }
}

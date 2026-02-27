package com.fy.erp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.erp.entities.Stock;
import com.fy.erp.exception.BizException;
import com.fy.erp.mapper.StockMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StockService extends ServiceImpl<StockMapper, Stock> {

    private final ProductService productService;
    private final WarehouseService warehouseService;

    public StockService(ProductService productService, WarehouseService warehouseService) {
        this.productService = productService;
        this.warehouseService = warehouseService;
    }

    public Stock getStock(Long productId, Long warehouseId) {
        return getOne(new LambdaQueryWrapper<Stock>()
                .eq(Stock::getProductId, productId)
                .eq(Stock::getWarehouseId, warehouseId));
    }

    public void addStock(Long productId, Long warehouseId, BigDecimal qty) {
        Stock stock = getStock(productId, warehouseId);
        if (stock == null) {
            stock = new Stock();
            stock.setProductId(productId);
            stock.setWarehouseId(warehouseId);
            stock.setQuantity(qty);
            stock.setSafeStock(BigDecimal.ZERO);
            save(stock);
        } else {
            stock.setQuantity(stock.getQuantity().add(qty));
            updateById(stock);
        }
    }

    public void reduceStock(Long productId, Long warehouseId, BigDecimal qty) {
        Stock stock = getStock(productId, warehouseId);
        if (stock == null) {
            throw new BizException(400, "stock not found");
        }
        BigDecimal remain = stock.getQuantity().subtract(qty);
        if (remain.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(400, "insufficient stock");
        }
        stock.setQuantity(remain);
        updateById(stock);
    }

    public void setStockQuantity(Long productId, Long warehouseId, BigDecimal qty) {
        Stock stock = getStock(productId, warehouseId);
        if (stock == null) {
            stock = new Stock();
            stock.setProductId(productId);
            stock.setWarehouseId(warehouseId);
            stock.setQuantity(qty);
            stock.setSafeStock(BigDecimal.ZERO);
            save(stock);
        } else {
            stock.setQuantity(qty);
            updateById(stock);
        }
    }

    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Stock> pageWithNames(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Stock> page, LambdaQueryWrapper<Stock> wrapper) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Stock> result = page(page, wrapper);
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            java.util.Set<Long> productIds = result.getRecords().stream().map(Stock::getProductId)
                    .collect(java.util.stream.Collectors.toSet());
            java.util.Set<Long> warehouseIds = result.getRecords().stream().map(Stock::getWarehouseId)
                    .collect(java.util.stream.Collectors.toSet());

            java.util.Map<Long, com.fy.erp.entities.Product> productMap = new java.util.HashMap<>();
            if (!productIds.isEmpty()) {
                productMap = productService.listByIds(productIds).stream()
                        .collect(java.util.stream.Collectors.toMap(com.fy.erp.entities.Product::getId, p -> p));
            }

            java.util.Map<Long, com.fy.erp.entities.Warehouse> warehouseMap = new java.util.HashMap<>();
            if (!warehouseIds.isEmpty()) {
                warehouseMap = warehouseService.listByIds(warehouseIds).stream()
                        .collect(java.util.stream.Collectors.toMap(com.fy.erp.entities.Warehouse::getId, w -> w));
            }

            for (Stock stock : result.getRecords()) {
                if (stock.getProductId() != null && productMap.containsKey(stock.getProductId())) {
                    com.fy.erp.entities.Product p = productMap.get(stock.getProductId());
                    stock.setProductName(p.getName());
                    stock.setProductSku(p.getSku());
                    stock.setUnit(p.getUnit());
                }
                if (stock.getWarehouseId() != null && warehouseMap.containsKey(stock.getWarehouseId())) {
                    stock.setWarehouseName(warehouseMap.get(stock.getWarehouseId()).getName());
                }
            }
        }
        return result;
    }
}

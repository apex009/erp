package com.fy.erp.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.erp.entities.StockRecord;
import com.fy.erp.mapper.StockRecordMapper;
import org.springframework.stereotype.Service;

@Service
public class StockRecordService extends ServiceImpl<StockRecordMapper, StockRecord> {

  private final ProductService productService;
  private final WarehouseService warehouseService;

  public StockRecordService(ProductService productService, WarehouseService warehouseService) {
    this.productService = productService;
    this.warehouseService = warehouseService;
  }

  public com.baomidou.mybatisplus.extension.plugins.pagination.Page<StockRecord> pageWithNames(
      com.baomidou.mybatisplus.extension.plugins.pagination.Page<StockRecord> page,
      com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StockRecord> wrapper) {
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<StockRecord> result = page(page, wrapper);
    if (result.getRecords() != null && !result.getRecords().isEmpty()) {
      java.util.Set<Long> productIds = result.getRecords().stream().map(StockRecord::getProductId)
          .collect(java.util.stream.Collectors.toSet());
      java.util.Set<Long> warehouseIds = result.getRecords().stream().map(StockRecord::getWarehouseId)
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

      for (StockRecord record : result.getRecords()) {
        record.setRecordTypeName(com.fy.erp.enums.StockRecordType.labelOf(record.getRecordType()));
        record.setBizTypeName(com.fy.erp.enums.StockBizType.labelOf(record.getBizType()));

        if (record.getProductId() != null && productMap.containsKey(record.getProductId())) {
          record.setProductName(productMap.get(record.getProductId()).getName());
        }
        if (record.getWarehouseId() != null && warehouseMap.containsKey(record.getWarehouseId())) {
          record.setWarehouseName(warehouseMap.get(record.getWarehouseId()).getName());
        }
      }
    }
    return result;
  }
}

package com.fy.erp.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.erp.entities.StockCheck;
import com.fy.erp.entities.StockCheckItem;
import com.fy.erp.entities.StockRecord;
import com.fy.erp.mapper.StockCheckMapper;
import com.fy.erp.util.OrderNoUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class StockCheckService extends ServiceImpl<StockCheckMapper, StockCheck> {
    private final StockCheckItemService itemService;
    private final StockService stockService;
    private final StockRecordService stockRecordService;

    public StockCheckService(StockCheckItemService itemService,
                             StockService stockService,
                             StockRecordService stockRecordService) {
        this.itemService = itemService;
        this.stockService = stockService;
        this.stockRecordService = stockRecordService;
    }

    @Transactional
    public StockCheck createCheck(Long warehouseId, String remark, List<StockCheckItem> items) {
        StockCheck check = new StockCheck();
        check.setOrderNo(OrderNoUtil.generate("CK"));
        check.setWarehouseId(warehouseId);
        check.setStatus(1);
        check.setRemark(remark);
        save(check);

        for (StockCheckItem item : items) {
            item.setCheckId(check.getId());
            BigDecimal systemQty = BigDecimal.ZERO;
            var stock = stockService.getStock(item.getProductId(), warehouseId);
            if (stock != null && stock.getQuantity() != null) {
                systemQty = stock.getQuantity();
            }
            item.setSystemQty(systemQty);
            item.setDiffQty(item.getActualQty().subtract(systemQty));
            itemService.save(item);

            stockService.setStockQuantity(item.getProductId(), warehouseId, item.getActualQty());

            StockRecord record = new StockRecord();
            record.setProductId(item.getProductId());
            record.setWarehouseId(warehouseId);
            record.setQuantity(item.getDiffQty().abs());
            record.setRecordType(item.getDiffQty().compareTo(BigDecimal.ZERO) >= 0 ? "IN" : "OUT");
            record.setBizType("STOCK_CHECK");
            record.setBizId(check.getId());
            record.setRemark("stock check adjust");
            stockRecordService.save(record);
        }
        return check;
    }
}

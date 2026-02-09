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
}

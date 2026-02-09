package com.fy.erp.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.erp.entities.StockRecord;
import com.fy.erp.entities.StockTransfer;
import com.fy.erp.entities.StockTransferItem;
import com.fy.erp.exception.BizException;
import com.fy.erp.mapper.StockTransferMapper;
import com.fy.erp.util.OrderNoUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class StockTransferService extends ServiceImpl<StockTransferMapper, StockTransfer> {
    private final StockTransferItemService itemService;
    private final StockService stockService;
    private final StockRecordService stockRecordService;

    public StockTransferService(StockTransferItemService itemService,
                                StockService stockService,
                                StockRecordService stockRecordService) {
        this.itemService = itemService;
        this.stockService = stockService;
        this.stockRecordService = stockRecordService;
    }

    @Transactional
    public StockTransfer createTransfer(Long fromWarehouseId, Long toWarehouseId, String remark, List<StockTransferItem> items) {
        if (fromWarehouseId.equals(toWarehouseId)) {
            throw new BizException(400, "fromWarehouseId equals toWarehouseId");
        }
        StockTransfer transfer = new StockTransfer();
        transfer.setOrderNo(OrderNoUtil.generate("TR"));
        transfer.setFromWarehouseId(fromWarehouseId);
        transfer.setToWarehouseId(toWarehouseId);
        transfer.setStatus(1);
        transfer.setRemark(remark);
        transfer.setTotalQuantity(BigDecimal.ZERO);
        save(transfer);

        BigDecimal totalQty = BigDecimal.ZERO;
        for (StockTransferItem item : items) {
            item.setTransferId(transfer.getId());
            itemService.save(item);
            totalQty = totalQty.add(item.getQuantity());

            stockService.reduceStock(item.getProductId(), fromWarehouseId, item.getQuantity());
            stockService.addStock(item.getProductId(), toWarehouseId, item.getQuantity());

            StockRecord out = new StockRecord();
            out.setProductId(item.getProductId());
            out.setWarehouseId(fromWarehouseId);
            out.setQuantity(item.getQuantity());
            out.setRecordType("OUT");
            out.setBizType("TRANSFER");
            out.setBizId(transfer.getId());
            out.setRemark("transfer out");
            stockRecordService.save(out);

            StockRecord in = new StockRecord();
            in.setProductId(item.getProductId());
            in.setWarehouseId(toWarehouseId);
            in.setQuantity(item.getQuantity());
            in.setRecordType("IN");
            in.setBizType("TRANSFER");
            in.setBizId(transfer.getId());
            in.setRemark("transfer in");
            stockRecordService.save(in);
        }
        transfer.setTotalQuantity(totalQty);
        updateById(transfer);
        return transfer;
    }
}

package com.fy.erp.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.erp.dto.SalesOrderCreateRequest;
import com.fy.erp.dto.SalesOrderItemRequest;
import com.fy.erp.entities.Receivable;
import com.fy.erp.entities.SalesItem;
import com.fy.erp.entities.SalesOrder;
import com.fy.erp.entities.StockRecord;
import com.fy.erp.exception.BizException;
import com.fy.erp.mapper.SalesOrderMapper;
import com.fy.erp.util.OrderNoUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class SalesOrderService extends ServiceImpl<SalesOrderMapper, SalesOrder> {
    private final SalesItemService itemService;
    private final StockService stockService;
    private final StockRecordService stockRecordService;
    private final ReceivableService receivableService;

    public SalesOrderService(SalesItemService itemService,
                             StockService stockService,
                             StockRecordService stockRecordService,
                             ReceivableService receivableService) {
        this.itemService = itemService;
        this.stockService = stockService;
        this.stockRecordService = stockRecordService;
        this.receivableService = receivableService;
    }

    @Transactional
    public SalesOrder createOrder(SalesOrderCreateRequest request) {
        SalesOrder order = new SalesOrder();
        order.setOrderNo(OrderNoUtil.generate("SO"));
        order.setCustomerId(request.getCustomerId());
        order.setRemark(request.getRemark());
        order.setStatus(0);
        order.setTotalAmount(BigDecimal.ZERO);
        save(order);

        BigDecimal total = BigDecimal.ZERO;
        for (SalesOrderItemRequest itemReq : request.getItems()) {
            BigDecimal amount = itemReq.getPrice().multiply(itemReq.getQuantity());
            total = total.add(amount);

            SalesItem item = new SalesItem();
            item.setOrderId(order.getId());
            item.setProductId(itemReq.getProductId());
            item.setWarehouseId(itemReq.getWarehouseId());
            item.setQuantity(itemReq.getQuantity());
            item.setPrice(itemReq.getPrice());
            item.setAmount(amount);
            itemService.save(item);

            stockService.reduceStock(itemReq.getProductId(), itemReq.getWarehouseId(), itemReq.getQuantity());

            StockRecord record = new StockRecord();
            record.setProductId(itemReq.getProductId());
            record.setWarehouseId(itemReq.getWarehouseId());
            record.setQuantity(itemReq.getQuantity());
            record.setRecordType("OUT");
            record.setBizType("SALE");
            record.setBizId(order.getId());
            record.setRemark("sale out");
            stockRecordService.save(record);
        }
        order.setTotalAmount(total);
        updateById(order);

        Receivable receivable = new Receivable();
        receivable.setCustomerId(order.getCustomerId());
        receivable.setOrderId(order.getId());
        receivable.setAmount(total);
        receivable.setPaidAmount(BigDecimal.ZERO);
        receivable.setStatus(0);
        receivableService.save(receivable);

        return order;
    }

    @Transactional
    public SalesOrder approve(Long id) {
        SalesOrder order = getById(id);
        if (order == null) {
            throw new BizException(404, "order not found");
        }
        if (order.getStatus() != null && order.getStatus() != 0) {
            return order;
        }
        order.setStatus(1);
        updateById(order);
        return order;
    }

    @Transactional
    public SalesOrder cancel(Long id) {
        SalesOrder order = getById(id);
        if (order == null) {
            throw new BizException(404, "order not found");
        }
        if (order.getStatus() != null && order.getStatus() == 2) {
            return order;
        }
        Receivable receivable = receivableService.getByOrderId(id);
        if (receivable != null && receivable.getPaidAmount() != null && receivable.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BizException(400, "receivable already paid");
        }
        for (SalesItem item : itemService.lambdaQuery().eq(SalesItem::getOrderId, id).list()) {
            stockService.addStock(item.getProductId(), item.getWarehouseId(), item.getQuantity());
            StockRecord record = new StockRecord();
            record.setProductId(item.getProductId());
            record.setWarehouseId(item.getWarehouseId());
            record.setQuantity(item.getQuantity());
            record.setRecordType("IN");
            record.setBizType("SALE_CANCEL");
            record.setBizId(order.getId());
            record.setRemark("sale cancel");
            stockRecordService.save(record);
        }
        order.setStatus(2);
        updateById(order);
        if (receivable != null) {
            receivable.setStatus(3);
            receivableService.updateById(receivable);
        }
        return order;
    }

    @Transactional
    public SalesOrder refund(Long id) {
        SalesOrder order = getById(id);
        if (order == null) {
            throw new BizException(404, "order not found");
        }
        if (order.getStatus() != null && order.getStatus() == 3) {
            return order;
        }
        Receivable receivable = receivableService.getByOrderId(id);
        if (receivable != null && receivable.getPaidAmount() != null && receivable.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BizException(400, "receivable already paid");
        }
        for (SalesItem item : itemService.lambdaQuery().eq(SalesItem::getOrderId, id).list()) {
            stockService.addStock(item.getProductId(), item.getWarehouseId(), item.getQuantity());
            StockRecord record = new StockRecord();
            record.setProductId(item.getProductId());
            record.setWarehouseId(item.getWarehouseId());
            record.setQuantity(item.getQuantity());
            record.setRecordType("IN");
            record.setBizType("SALE_RETURN");
            record.setBizId(order.getId());
            record.setRemark("sale return");
            stockRecordService.save(record);
        }
        order.setStatus(3);
        updateById(order);
        if (receivable != null) {
            receivable.setStatus(3);
            receivableService.updateById(receivable);
        }
        return order;
    }
}

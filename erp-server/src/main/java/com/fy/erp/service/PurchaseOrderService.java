package com.fy.erp.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.erp.dto.PurchaseOrderCreateRequest;
import com.fy.erp.dto.PurchaseOrderItemRequest;
import com.fy.erp.entities.Payable;
import com.fy.erp.entities.PurchaseItem;
import com.fy.erp.entities.PurchaseOrder;
import com.fy.erp.entities.StockRecord;
import com.fy.erp.exception.BizException;
import com.fy.erp.mapper.PurchaseOrderMapper;
import com.fy.erp.util.OrderNoUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PurchaseOrderService extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> {
    private final PurchaseItemService itemService;
    private final StockService stockService;
    private final StockRecordService stockRecordService;
    private final PayableService payableService;

    public PurchaseOrderService(PurchaseItemService itemService,
                                StockService stockService,
                                StockRecordService stockRecordService,
                                PayableService payableService) {
        this.itemService = itemService;
        this.stockService = stockService;
        this.stockRecordService = stockRecordService;
        this.payableService = payableService;
    }

    @Transactional
    public PurchaseOrder createOrder(PurchaseOrderCreateRequest request) {
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(OrderNoUtil.generate("PO"));
        order.setSupplierId(request.getSupplierId());
        order.setRemark(request.getRemark());
        order.setStatus(0);
        order.setTotalAmount(BigDecimal.ZERO);
        save(order);

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderItemRequest itemReq : request.getItems()) {
            BigDecimal amount = itemReq.getPrice().multiply(itemReq.getQuantity());
            total = total.add(amount);

            PurchaseItem item = new PurchaseItem();
            item.setOrderId(order.getId());
            item.setProductId(itemReq.getProductId());
            item.setWarehouseId(itemReq.getWarehouseId());
            item.setQuantity(itemReq.getQuantity());
            item.setPrice(itemReq.getPrice());
            item.setAmount(amount);
            itemService.save(item);

            stockService.addStock(itemReq.getProductId(), itemReq.getWarehouseId(), itemReq.getQuantity());

            StockRecord record = new StockRecord();
            record.setProductId(itemReq.getProductId());
            record.setWarehouseId(itemReq.getWarehouseId());
            record.setQuantity(itemReq.getQuantity());
            record.setRecordType("IN");
            record.setBizType("PURCHASE");
            record.setBizId(order.getId());
            record.setRemark("purchase in");
            stockRecordService.save(record);
        }
        order.setTotalAmount(total);
        updateById(order);

        Payable payable = new Payable();
        payable.setSupplierId(order.getSupplierId());
        payable.setOrderId(order.getId());
        payable.setAmount(total);
        payable.setPaidAmount(BigDecimal.ZERO);
        payable.setStatus(0);
        payableService.save(payable);

        return order;
    }

    @Transactional
    public PurchaseOrder approve(Long id) {
        PurchaseOrder order = getById(id);
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
    public PurchaseOrder cancel(Long id) {
        PurchaseOrder order = getById(id);
        if (order == null) {
            throw new BizException(404, "order not found");
        }
        if (order.getStatus() != null && order.getStatus() == 2) {
            return order;
        }
        Payable payable = payableService.getByOrderId(id);
        if (payable != null && payable.getPaidAmount() != null && payable.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BizException(400, "payable already paid");
        }
        for (PurchaseItem item : itemService.lambdaQuery().eq(PurchaseItem::getOrderId, id).list()) {
            stockService.reduceStock(item.getProductId(), item.getWarehouseId(), item.getQuantity());
            StockRecord record = new StockRecord();
            record.setProductId(item.getProductId());
            record.setWarehouseId(item.getWarehouseId());
            record.setQuantity(item.getQuantity());
            record.setRecordType("OUT");
            record.setBizType("PURCHASE_CANCEL");
            record.setBizId(order.getId());
            record.setRemark("purchase cancel");
            stockRecordService.save(record);
        }
        order.setStatus(2);
        updateById(order);
        if (payable != null) {
            payable.setStatus(3);
            payableService.updateById(payable);
        }
        return order;
    }

    @Transactional
    public PurchaseOrder refund(Long id) {
        PurchaseOrder order = getById(id);
        if (order == null) {
            throw new BizException(404, "order not found");
        }
        if (order.getStatus() != null && order.getStatus() == 3) {
            return order;
        }
        Payable payable = payableService.getByOrderId(id);
        if (payable != null && payable.getPaidAmount() != null && payable.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BizException(400, "payable already paid");
        }
        for (PurchaseItem item : itemService.lambdaQuery().eq(PurchaseItem::getOrderId, id).list()) {
            stockService.reduceStock(item.getProductId(), item.getWarehouseId(), item.getQuantity());
            StockRecord record = new StockRecord();
            record.setProductId(item.getProductId());
            record.setWarehouseId(item.getWarehouseId());
            record.setQuantity(item.getQuantity());
            record.setRecordType("OUT");
            record.setBizType("PURCHASE_RETURN");
            record.setBizId(order.getId());
            record.setRemark("purchase return");
            stockRecordService.save(record);
        }
        order.setStatus(3);
        updateById(order);
        if (payable != null) {
            payable.setStatus(3);
            payableService.updateById(payable);
        }
        return order;
    }
}

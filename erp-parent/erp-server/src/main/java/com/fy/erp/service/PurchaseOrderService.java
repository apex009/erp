package com.fy.erp.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.erp.dto.PurchaseOrderCreateRequest;
import com.fy.erp.dto.PurchaseOrderItemRequest;
import com.fy.erp.entities.Payable;
import com.fy.erp.entities.PurchaseItem;
import com.fy.erp.entities.PurchaseOrder;
import com.fy.erp.entities.StockRecord;
import com.fy.erp.enums.StockBizType;
import com.fy.erp.enums.StockRecordType;
import com.fy.erp.exception.BizException;
import com.fy.erp.mapper.PurchaseOrderMapper;
import com.fy.erp.util.OrderNoUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PurchaseOrderService extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> {
    private final PurchaseItemService itemService;
    private final StockService stockService;
    private final StockRecordService stockRecordService;
    private final PayableService payableService;
    private final SupplierService supplierService;

    public PurchaseOrderService(PurchaseItemService itemService,
            StockService stockService,
            StockRecordService stockRecordService,
            PayableService payableService,
            SupplierService supplierService) {
        this.itemService = itemService;
        this.stockService = stockService;
        this.stockRecordService = stockRecordService;
        this.payableService = payableService;
        this.supplierService = supplierService;
    }

    @Transactional
    public PurchaseOrder createOrder(PurchaseOrderCreateRequest request) {
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(OrderNoUtil.generate("PO"));
        order.setSupplierId(request.getSupplierId());
        order.setRemark(request.getRemark());
        order.setStatus(0); // 0: Pending Audit
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
        }
        order.setTotalAmount(total);
        updateById(order);

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
        if (payable != null && payable.getPaidAmount() != null
                && payable.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BizException(400, "payable already paid");
        }
        for (PurchaseItem item : itemService.lambdaQuery().eq(PurchaseItem::getOrderId, id).list()) {
            stockService.reduceStock(item.getProductId(), item.getWarehouseId(), item.getQuantity());
            StockRecord record = new StockRecord();
            record.setProductId(item.getProductId());
            record.setWarehouseId(item.getWarehouseId());
            record.setQuantity(item.getQuantity());
            record.setRecordType(StockRecordType.OUT.getCode());
            record.setBizType(StockBizType.PURCHASE.getCode());
            record.setBizId(order.getId());
            record.setRemark("采购取消出库");
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
        if (payable != null && payable.getPaidAmount() != null
                && payable.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BizException(400, "payable already paid");
        }
        for (PurchaseItem item : itemService.lambdaQuery().eq(PurchaseItem::getOrderId, id).list()) {
            stockService.reduceStock(item.getProductId(), item.getWarehouseId(), item.getQuantity());
            StockRecord record = new StockRecord();
            record.setProductId(item.getProductId());
            record.setWarehouseId(item.getWarehouseId());
            record.setQuantity(item.getQuantity());
            record.setRecordType(StockRecordType.OUT.getCode());
            record.setBizType(StockBizType.PURCHASE.getCode());
            record.setBizId(order.getId());
            record.setRemark("采购退货出库");
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

    @Transactional
    @CacheEvict(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, allEntries = true)
    public PurchaseOrder inbound(Long id) {
        PurchaseOrder order = getById(id);
        if (order == null) {
            throw new BizException(404, "order not found");
        }
        // Only allow inbound if status is 1 (Approved/Wait Inbound)
        if (order.getStatus() != 1) {
            throw new BizException(400, "Order status is not valid for inbound");
        }

        // Logic moved from createOrder: Add Stock & Create Records
        for (PurchaseItem item : itemService.lambdaQuery().eq(PurchaseItem::getOrderId, id).list()) {
            stockService.addStock(item.getProductId(), item.getWarehouseId(), item.getQuantity());

            StockRecord record = new StockRecord();
            record.setProductId(item.getProductId());
            record.setWarehouseId(item.getWarehouseId());
            record.setQuantity(item.getQuantity());
            record.setRecordType(StockRecordType.IN.getCode());
            record.setBizType(StockBizType.PURCHASE.getCode());
            record.setBizId(order.getId());
            record.setRemark("采购入库");
            stockRecordService.save(record);
        }

        // Create Payable
        Payable payable = new Payable();
        payable.setSupplierId(order.getSupplierId());
        payable.setOrderId(order.getId());
        payable.setAmount(order.getTotalAmount());
        payable.setPaidAmount(BigDecimal.ZERO);
        payable.setStatus(0);
        payableService.save(payable);

        // Update Status to 3 (Completed)
        order.setStatus(3);
        updateById(order);

        return order;
    }

    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<PurchaseOrder> pageWithSupplier(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<PurchaseOrder> page,
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PurchaseOrder> wrapper) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<PurchaseOrder> result = page(page, wrapper);
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            java.util.Set<Long> supplierIds = result.getRecords().stream()
                    .map(PurchaseOrder::getSupplierId)
                    .collect(java.util.stream.Collectors.toSet());
            if (!supplierIds.isEmpty()) {
                java.util.Map<Long, com.fy.erp.entities.Supplier> supplierMap = supplierService.listByIds(supplierIds)
                        .stream()
                        .collect(java.util.stream.Collectors.toMap(com.fy.erp.entities.Supplier::getId, s -> s));
                result.getRecords().forEach(order -> {
                    if (order.getSupplierId() != null && supplierMap.containsKey(order.getSupplierId())) {
                        order.setSupplierName(supplierMap.get(order.getSupplierId()).getName());
                    }
                });
            }
        }
        return result;
    }
}

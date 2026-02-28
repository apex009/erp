package com.fy.erp.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.erp.dto.SalesOrderCreateRequest;
import com.fy.erp.dto.SalesOrderItemRequest;
import com.fy.erp.entities.Receivable;
import com.fy.erp.entities.SalesItem;
import com.fy.erp.entities.SalesOrder;
import com.fy.erp.entities.StockRecord;
import com.fy.erp.enums.StockBizType;
import com.fy.erp.enums.StockRecordType;
import com.fy.erp.exception.BizException;
import com.fy.erp.mapper.SalesOrderMapper;
import com.fy.erp.util.OrderNoUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class SalesOrderService extends ServiceImpl<SalesOrderMapper, SalesOrder> {
    private final SalesItemService itemService;
    private final StockService stockService;
    private final StockRecordService stockRecordService;
    private final ReceivableService receivableService;
    private final CustomerService customerService;

    public SalesOrderService(SalesItemService itemService,
            StockService stockService,
            StockRecordService stockRecordService,
            ReceivableService receivableService,
            CustomerService customerService) {
        this.itemService = itemService;
        this.stockService = stockService;
        this.stockRecordService = stockRecordService;
        this.receivableService = receivableService;
        this.customerService = customerService;
    }

    @Transactional
    @CacheEvict(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, allEntries = true)
    public SalesOrder createOrder(SalesOrderCreateRequest request) {
        SalesOrder order = new SalesOrder();
        order.setOrderNo(OrderNoUtil.generate("SO"));
        order.setCustomerId(request.getCustomerId());
        order.setRemark(request.getRemark());
        order.setStatus(0); // 0: Pending Audit
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
        }
        order.setTotalAmount(total);
        updateById(order);

        return order;
    }

    @Transactional
    @CacheEvict(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, allEntries = true)
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
    @CacheEvict(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, allEntries = true)
    public SalesOrder outbound(Long id) {
        SalesOrder order = getById(id);
        if (order == null) {
            throw new BizException(404, "order not found");
        }
        if (order.getStatus() != 1) {
            throw new BizException(400, "Order status is not valid for outbound");
        }
        for (SalesItem item : itemService.lambdaQuery().eq(SalesItem::getOrderId, id).list()) {
            stockService.reduceStock(item.getProductId(), item.getWarehouseId(), item.getQuantity());
            StockRecord record = new StockRecord();
            record.setProductId(item.getProductId());
            record.setWarehouseId(item.getWarehouseId());
            record.setQuantity(item.getQuantity());
            record.setRecordType(StockRecordType.OUT.getCode());
            record.setBizType(StockBizType.SALE.getCode());
            record.setBizId(order.getId());
            record.setRemark("销售出库");
            stockRecordService.save(record);
        }
        Receivable receivable = new Receivable();
        receivable.setCustomerId(order.getCustomerId());
        receivable.setOrderId(order.getId());
        receivable.setAmount(order.getTotalAmount());
        receivable.setPaidAmount(BigDecimal.ZERO);
        receivable.setStatus(0);
        receivableService.save(receivable);
        order.setStatus(3);
        updateById(order);
        return order;
    }

    @Transactional
    @CacheEvict(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, allEntries = true)
    public SalesOrder cancel(Long id) {
        SalesOrder order = getById(id);
        if (order == null) {
            throw new BizException(404, "order not found");
        }
        if (order.getStatus() != null && order.getStatus() == 2) {
            return order;
        }
        Receivable receivable = receivableService.getByOrderId(id);
        if (receivable != null && receivable.getPaidAmount() != null
                && receivable.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BizException(400, "receivable already paid");
        }
        for (SalesItem item : itemService.lambdaQuery().eq(SalesItem::getOrderId, id).list()) {
            stockService.addStock(item.getProductId(), item.getWarehouseId(), item.getQuantity());
            StockRecord record = new StockRecord();
            record.setProductId(item.getProductId());
            record.setWarehouseId(item.getWarehouseId());
            record.setQuantity(item.getQuantity());
            record.setRecordType(StockRecordType.IN.getCode());
            record.setBizType(StockBizType.SALE.getCode());
            record.setBizId(order.getId());
            record.setRemark("销售取消入库");
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
    @CacheEvict(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, allEntries = true)
    public SalesOrder refund(Long id) {
        SalesOrder order = getById(id);
        if (order == null) {
            throw new BizException(404, "order not found");
        }
        if (order.getStatus() != null && order.getStatus() == 3) {
            return order;
        }
        Receivable receivable = receivableService.getByOrderId(id);
        if (receivable != null && receivable.getPaidAmount() != null
                && receivable.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BizException(400, "receivable already paid");
        }
        for (SalesItem item : itemService.lambdaQuery().eq(SalesItem::getOrderId, id).list()) {
            stockService.addStock(item.getProductId(), item.getWarehouseId(), item.getQuantity());
            StockRecord record = new StockRecord();
            record.setProductId(item.getProductId());
            record.setWarehouseId(item.getWarehouseId());
            record.setQuantity(item.getQuantity());
            record.setRecordType(StockRecordType.IN.getCode());
            record.setBizType(StockBizType.SALE.getCode());
            record.setBizId(order.getId());
            record.setRemark("销售退货入库");
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

    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<SalesOrder> pageWithCustomer(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<SalesOrder> page,
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SalesOrder> wrapper) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SalesOrder> result = page(page, wrapper);
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            java.util.Set<Long> customerIds = result.getRecords().stream()
                    .map(SalesOrder::getCustomerId)
                    .collect(java.util.stream.Collectors.toSet());
            if (!customerIds.isEmpty()) {
                java.util.Map<Long, com.fy.erp.entities.Customer> customerMap = customerService.listByIds(customerIds)
                        .stream()
                        .collect(java.util.stream.Collectors.toMap(com.fy.erp.entities.Customer::getId, c -> c));
                result.getRecords().forEach(order -> {
                    if (order.getCustomerId() != null && customerMap.containsKey(order.getCustomerId())) {
                        order.setCustomerName(customerMap.get(order.getCustomerId()).getName());
                    }
                });
            }
        }
        return result;
    }
}

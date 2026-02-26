package com.fy.erp.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.erp.entities.Receivable;
import com.fy.erp.entities.Receipt;
import com.fy.erp.exception.BizException;
import com.fy.erp.mapper.ReceivableMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ReceivableService extends ServiceImpl<ReceivableMapper, Receivable> {
    private final ReceiptService receiptService;
    private final CustomerService customerService;
    private final SalesOrderService salesOrderService;

    public ReceivableService(ReceiptService receiptService, CustomerService customerService,
            @org.springframework.context.annotation.Lazy SalesOrderService salesOrderService) {
        this.receiptService = receiptService;
        this.customerService = customerService;
        this.salesOrderService = salesOrderService;
    }

    @Transactional
    public Receipt receive(Long receivableId, BigDecimal amount, String method, String remark) {
        Receivable receivable = getById(receivableId);
        if (receivable == null) {
            throw new BizException(404, "receivable not found");
        }
        BigDecimal paid = receivable.getPaidAmount().add(amount);
        if (paid.compareTo(receivable.getAmount()) > 0) {
            throw new BizException(400, "amount exceeds receivable");
        }
        receivable.setPaidAmount(paid);
        receivable.setStatus(paid.compareTo(receivable.getAmount()) == 0 ? 2 : 1);
        updateById(receivable);

        Receipt receipt = new Receipt();
        receipt.setReceivableId(receivableId);
        receipt.setAmount(amount);
        receipt.setMethod(method);
        receipt.setRemark(remark);
        receiptService.save(receipt);
        return receipt;
    }

    public Receivable getByOrderId(Long orderId) {
        return lambdaQuery().eq(Receivable::getOrderId, orderId).one();
    }

    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Receivable> pageWithNames(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Receivable> page,
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Receivable> wrapper) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Receivable> result = page(page, wrapper);
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            java.util.Set<Long> customerIds = result.getRecords().stream().map(Receivable::getCustomerId)
                    .collect(java.util.stream.Collectors.toSet());
            java.util.Set<Long> orderIds = result.getRecords().stream().map(Receivable::getOrderId)
                    .collect(java.util.stream.Collectors.toSet());

            java.util.Map<Long, com.fy.erp.entities.Customer> customerMap = new java.util.HashMap<>();
            if (!customerIds.isEmpty()) {
                customerMap = customerService.listByIds(customerIds).stream()
                        .collect(java.util.stream.Collectors.toMap(com.fy.erp.entities.Customer::getId, c -> c));
            }

            java.util.Map<Long, com.fy.erp.entities.SalesOrder> orderMap = new java.util.HashMap<>();
            if (!orderIds.isEmpty()) {
                orderMap = salesOrderService.listByIds(orderIds).stream()
                        .collect(java.util.stream.Collectors.toMap(com.fy.erp.entities.SalesOrder::getId, o -> o));
            }

            for (Receivable receivable : result.getRecords()) {
                if (receivable.getCustomerId() != null && customerMap.containsKey(receivable.getCustomerId())) {
                    receivable.setCustomerName(customerMap.get(receivable.getCustomerId()).getName());
                }
                if (receivable.getOrderId() != null && orderMap.containsKey(receivable.getOrderId())) {
                    receivable.setSourceOrderNo(orderMap.get(receivable.getOrderId()).getOrderNo());
                }
            }
        }
        return result;
    }
}

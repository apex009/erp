package com.fy.erp.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.erp.entities.Payable;
import com.fy.erp.entities.Payment;
import com.fy.erp.exception.BizException;
import com.fy.erp.mapper.PayableMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PayableService extends ServiceImpl<PayableMapper, Payable> {
    private final PaymentService paymentService;
    private final SupplierService supplierService;
    private final PurchaseOrderService purchaseOrderService;

    public PayableService(PaymentService paymentService, SupplierService supplierService,
            @org.springframework.context.annotation.Lazy PurchaseOrderService purchaseOrderService) {
        this.paymentService = paymentService;
        this.supplierService = supplierService;
        this.purchaseOrderService = purchaseOrderService;
    }

    @Transactional
    public Payment pay(Long payableId, BigDecimal amount, String method, String remark) {
        Payable payable = getById(payableId);
        if (payable == null) {
            throw new BizException(404, "payable not found");
        }
        BigDecimal paid = payable.getPaidAmount().add(amount);
        if (paid.compareTo(payable.getAmount()) > 0) {
            throw new BizException(400, "amount exceeds payable");
        }
        payable.setPaidAmount(paid);
        payable.setStatus(paid.compareTo(payable.getAmount()) == 0 ? 2 : 1);
        updateById(payable);

        Payment payment = new Payment();
        payment.setPayableId(payableId);
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setRemark(remark);
        paymentService.save(payment);
        return payment;
    }

    public Payable getByOrderId(Long orderId) {
        return lambdaQuery().eq(Payable::getOrderId, orderId).one();
    }

    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Payable> pageWithNames(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Payable> page,
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Payable> wrapper) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Payable> result = page(page, wrapper);
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            java.util.Set<Long> supplierIds = result.getRecords().stream().map(Payable::getSupplierId)
                    .collect(java.util.stream.Collectors.toSet());
            java.util.Set<Long> orderIds = result.getRecords().stream().map(Payable::getOrderId)
                    .collect(java.util.stream.Collectors.toSet());

            java.util.Map<Long, com.fy.erp.entities.Supplier> supplierMap = new java.util.HashMap<>();
            if (!supplierIds.isEmpty()) {
                supplierMap = supplierService.listByIds(supplierIds).stream()
                        .collect(java.util.stream.Collectors.toMap(com.fy.erp.entities.Supplier::getId, s -> s));
            }

            java.util.Map<Long, com.fy.erp.entities.PurchaseOrder> orderMap = new java.util.HashMap<>();
            if (!orderIds.isEmpty()) {
                orderMap = purchaseOrderService.listByIds(orderIds).stream()
                        .collect(java.util.stream.Collectors.toMap(com.fy.erp.entities.PurchaseOrder::getId, o -> o));
            }

            for (Payable payable : result.getRecords()) {
                if (payable.getSupplierId() != null && supplierMap.containsKey(payable.getSupplierId())) {
                    payable.setSupplierName(supplierMap.get(payable.getSupplierId()).getName());
                }
                if (payable.getOrderId() != null && orderMap.containsKey(payable.getOrderId())) {
                    payable.setSourceOrderNo(orderMap.get(payable.getOrderId()).getOrderNo());
                }
            }
        }
        return result;
    }
}

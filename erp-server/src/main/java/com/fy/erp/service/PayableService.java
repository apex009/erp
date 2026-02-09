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

    public PayableService(PaymentService paymentService) {
        this.paymentService = paymentService;
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
}

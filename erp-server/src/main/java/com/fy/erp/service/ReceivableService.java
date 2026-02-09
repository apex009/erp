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

    public ReceivableService(ReceiptService receiptService) {
        this.receiptService = receiptService;
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
}

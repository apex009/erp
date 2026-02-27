package com.fy.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fy.erp.dto.PaymentCreateRequest;
import com.fy.erp.entities.Payable;
import com.fy.erp.entities.Payment;
import com.fy.erp.result.Result;
import com.fy.erp.service.PayableService;
import com.fy.erp.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payables")
public class PayableController {
    private final PayableService payableService;
    private final PaymentService paymentService;

    public PayableController(PayableService payableService, PaymentService paymentService) {
        this.payableService = payableService;
        this.paymentService = paymentService;
    }

    @GetMapping
    public Result<Page<Payable>> page(@RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Payable> wrapper = new LambdaQueryWrapper<>();
        if (supplierId != null) {
            wrapper.eq(Payable::getSupplierId, supplierId);
        }
        if (status != null) {
            wrapper.eq(Payable::getStatus, status);
        }
        return Result.success(payableService.pageWithNames(new Page<>(page, size), wrapper));
    }

    @GetMapping("/{id}")
    public Result<Payable> detail(@PathVariable Long id) {
        return Result.success(payableService.getById(id));
    }

    @GetMapping("/{id}/payments")
    public Result<List<Payment>> payments(@PathVariable Long id) {
        return Result.success(paymentService.list(new LambdaQueryWrapper<Payment>().eq(Payment::getPayableId, id)));
    }

    @PostMapping("/{id}/payment")
    public Result<Payment> pay(@PathVariable Long id, @Valid @RequestBody PaymentCreateRequest request) {
        return Result.success(payableService.pay(id, request.getAmount(), request.getMethod(), request.getRemark()));
    }
}

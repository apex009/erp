package com.fy.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fy.erp.entities.Payment;
import com.fy.erp.result.Result;
import com.fy.erp.service.PaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public Result<Page<Payment>> page(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) Long payableId) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        if (payableId != null) {
            wrapper.eq(Payment::getPayableId, payableId);
        }
        return Result.success(paymentService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/{id}")
    public Result<Payment> detail(@PathVariable Long id) {
        return Result.success(paymentService.getById(id));
    }
}

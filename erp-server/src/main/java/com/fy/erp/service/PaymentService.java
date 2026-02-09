package com.fy.erp.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.erp.entities.Payment;
import com.fy.erp.mapper.PaymentMapper;
import org.springframework.stereotype.Service;

@Service
public class PaymentService extends ServiceImpl<PaymentMapper, Payment> {
}

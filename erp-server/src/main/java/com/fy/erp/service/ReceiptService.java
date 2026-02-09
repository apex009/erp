package com.fy.erp.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.erp.entities.Receipt;
import com.fy.erp.mapper.ReceiptMapper;
import org.springframework.stereotype.Service;

@Service
public class ReceiptService extends ServiceImpl<ReceiptMapper, Receipt> {
}

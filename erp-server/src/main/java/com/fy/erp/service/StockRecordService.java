package com.fy.erp.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fy.erp.entities.StockRecord;
import com.fy.erp.mapper.StockRecordMapper;
import org.springframework.stereotype.Service;

@Service
public class StockRecordService extends ServiceImpl<StockRecordMapper, StockRecord> {
}

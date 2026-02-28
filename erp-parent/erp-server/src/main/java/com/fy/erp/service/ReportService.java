package com.fy.erp.service;

import com.fy.erp.dto.report.FinanceSummary;
import com.fy.erp.dto.report.LowStockItem;
import com.fy.erp.dto.report.SalesAmountByDay;
import com.fy.erp.dto.report.SalesByCustomer;
import com.fy.erp.dto.report.SalesByProduct;
import com.fy.erp.dto.report.SalesFunnelItem;
import com.fy.erp.mapper.ReportMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {
    private final ReportMapper reportMapper;

    public ReportService(ReportMapper reportMapper) {
        this.reportMapper = reportMapper;
    }

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'sales:day:' + #start + ':' + #end")
    public List<SalesAmountByDay> salesAmountByDay(String start, String end) {
        return reportMapper.salesAmountByDay(start, end);
    }

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'sales:customer:' + #start + ':' + #end")
    public List<SalesByCustomer> salesByCustomer(String start, String end) {
        return reportMapper.salesByCustomer(start, end);
    }

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'sales:product:' + #start + ':' + #end")
    public List<SalesByProduct> salesByProduct(String start, String end) {
        return reportMapper.salesByProduct(start, end);
    }

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'sales:funnel'")
    public List<SalesFunnelItem> salesFunnel() {
        return reportMapper.salesFunnel();
    }

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'inventory:low-stock'")
    public List<LowStockItem> lowStock() {
        return reportMapper.lowStock();
    }

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'finance:receivable'")
    public FinanceSummary receivableSummary() {
        return reportMapper.receivableSummary();
    }

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'finance:payable'")
    public FinanceSummary payableSummary() {
        return reportMapper.payableSummary();
    }
}

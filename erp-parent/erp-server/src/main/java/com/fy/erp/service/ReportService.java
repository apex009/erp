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

    public List<SalesAmountByDay> salesAmountByDay(String start, String end) {
        return reportMapper.salesAmountByDay(start, end);
    }

    public List<SalesByCustomer> salesByCustomer(String start, String end) {
        return reportMapper.salesByCustomer(start, end);
    }

    public List<SalesByProduct> salesByProduct(String start, String end) {
        return reportMapper.salesByProduct(start, end);
    }

    public List<SalesFunnelItem> salesFunnel() {
        return reportMapper.salesFunnel();
    }

    public List<LowStockItem> lowStock() {
        return reportMapper.lowStock();
    }

    public FinanceSummary receivableSummary() {
        return reportMapper.receivableSummary();
    }

    public FinanceSummary payableSummary() {
        return reportMapper.payableSummary();
    }
}

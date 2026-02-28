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

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'dashboard:summary'")
    public com.fy.erp.dto.report.DashboardSummary dashboardSummary() {
        com.fy.erp.dto.report.DashboardSummary summary = new com.fy.erp.dto.report.DashboardSummary();

        // 今日销售
        var sales = reportMapper.todaySalesSummary();
        if (sales != null) {
            summary.setTodaySalesAmount(sales.getTodaySalesAmount());
            summary.setTodaySalesOrderCount(sales.getTodaySalesOrderCount());
        }
        var salesOut = reportMapper.todaySalesOutbound();
        if (salesOut != null) {
            summary.setTodaySalesOutAmount(salesOut.getTodaySalesOutAmount());
            summary.setTodaySalesOutCount(salesOut.getTodaySalesOutCount());
        }

        // 采购情况
        var purchase = reportMapper.todayPurchaseSummary();
        if (purchase != null) {
            summary.setTodayPurchaseAmount(purchase.getTodayPurchaseAmount());
            summary.setTodayPurchaseOrderCount(purchase.getTodayPurchaseOrderCount());
        }
        var purchaseIn = reportMapper.todayPurchaseInbound();
        if (purchaseIn != null) {
            summary.setTodayPurchaseInAmount(purchaseIn.getTodayPurchaseInAmount());
            summary.setTodayPurchaseInCount(purchaseIn.getTodayPurchaseInCount());
        }

        // 客户
        var customers = reportMapper.todayNewCustomers();
        if (customers != null) {
            summary.setNewCustomerCount(customers.getNewCustomerCount());
        }

        // 库存
        var inventory = reportMapper.inventorySummary();
        if (inventory != null) {
            summary.setInventoryTotalAmount(inventory.getInventoryTotalAmount());
            summary.setProductTotalCount(inventory.getProductTotalCount());
        }

        // 收付款
        var receivable = reportMapper.todayReceivable();
        if (receivable != null) {
            summary.setTodayReceivableAmount(receivable.getTodayReceivableAmount());
        }
        var payable = reportMapper.todayPayable();
        if (payable != null) {
            summary.setTodayPayableAmount(payable.getTodayPayableAmount());
        }

        // 销售退货
        var salesReturn = reportMapper.todaySalesReturn();
        if (salesReturn != null) {
            summary.setTodaySalesReturnAmount(salesReturn.getTodaySalesReturnAmount());
            summary.setTodaySalesReturnCount(salesReturn.getTodaySalesReturnCount());
        }

        // 采购退货
        var purchaseReturn = reportMapper.todayPurchaseReturn();
        if (purchaseReturn != null) {
            summary.setTodayPurchaseReturnAmount(purchaseReturn.getTodayPurchaseReturnAmount());
            summary.setTodayPurchaseReturnCount(purchaseReturn.getTodayPurchaseReturnCount());
        }

        // 毛利
        var profit = reportMapper.todaySalesProfit();
        if (profit != null) {
            summary.setTodaySalesProfit(profit.getTodaySalesProfit());
        }

        // 销售目标（可配置常量，后续可改为从数据库读取）
        summary.setSalesTarget(new java.math.BigDecimal("100000"));

        // 同比/环比
        java.math.BigDecimal todayAmount = summary.getTodaySalesAmount() != null ? summary.getTodaySalesAmount()
                : java.math.BigDecimal.ZERO;
        var yesterday = reportMapper.yesterdaySalesSummary();
        if (yesterday != null && yesterday.getTodaySalesAmount() != null
                && yesterday.getTodaySalesAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            summary.setMomGrowth(todayAmount.subtract(yesterday.getTodaySalesAmount())
                    .divide(yesterday.getTodaySalesAmount(), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new java.math.BigDecimal("100")));
        } else {
            summary.setMomGrowth(java.math.BigDecimal.ZERO);
        }

        var lastYear = reportMapper.lastYearSameDaySalesSummary();
        if (lastYear != null && lastYear.getTodaySalesAmount() != null
                && lastYear.getTodaySalesAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            summary.setYoyGrowth(todayAmount.subtract(lastYear.getTodaySalesAmount())
                    .divide(lastYear.getTodaySalesAmount(), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new java.math.BigDecimal("100")));
        } else {
            summary.setYoyGrowth(java.math.BigDecimal.ZERO);
        }

        return summary;
    }
}

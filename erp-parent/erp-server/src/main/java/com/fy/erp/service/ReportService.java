package com.fy.erp.service;

import com.fy.erp.dto.report.DashboardSummary;
import com.fy.erp.dto.report.FinanceSummary;
import com.fy.erp.dto.report.LowStockItem;
import com.fy.erp.dto.report.SalesAmountByDay;
import com.fy.erp.dto.report.SalesByCustomer;
import com.fy.erp.dto.report.SalesByProduct;
import com.fy.erp.dto.report.SalesFunnelItem;
import com.fy.erp.mapper.ReportMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ReportService {
    private final ReportMapper reportMapper;

    @Value("${erp.dashboard.sales-target:100000}")
    private BigDecimal salesTarget;

    public ReportService(ReportMapper reportMapper) {
        this.reportMapper = reportMapper;
    }

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'sales:day:' + #start + ':' + #end")
    public List<SalesAmountByDay> salesAmountByDay(String start, String end) {
        // D: 日期补齐，保证包含 end 当天全部数据
        start = normalizeStartDate(start);
        end = normalizeEndDate(end);
        return reportMapper.salesAmountByDay(start, end);
    }

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'sales:customer:' + #start + ':' + #end")
    public List<SalesByCustomer> salesByCustomer(String start, String end) {
        start = normalizeStartDate(start);
        end = normalizeEndDate(end);
        return reportMapper.salesByCustomer(start, end);
    }

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'sales:product:' + #start + ':' + #end")
    public List<SalesByProduct> salesByProduct(String start, String end) {
        start = normalizeStartDate(start);
        end = normalizeEndDate(end);
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
    public DashboardSummary dashboardSummary() {
        DashboardSummary summary = new DashboardSummary();

        // 今日成交（出库口径：status=3, update_time）
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

        // 销售退货（status=2, update_time）
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

        // 毛利（出库口径）
        var profit = reportMapper.todaySalesProfit();
        if (profit != null) {
            summary.setTodaySalesProfit(profit.getTodaySalesProfit());
        }

        // C: 销售目标（来自 application.yaml 配置）
        summary.setSalesTarget(salesTarget);

        // C: 达成率
        BigDecimal todayAmount = summary.getTodaySalesAmount() != null ? summary.getTodaySalesAmount()
                : BigDecimal.ZERO;
        if (salesTarget.compareTo(BigDecimal.ZERO) > 0) {
            summary.setAchieveRate(
                    todayAmount.divide(salesTarget, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
        } else {
            summary.setAchieveRate(BigDecimal.ZERO);
        }

        // B: 环比（昨日出库口径对比）
        var yesterday = reportMapper.yesterdaySalesSummary();
        if (yesterday != null && yesterday.getTodaySalesAmount() != null
                && yesterday.getTodaySalesAmount().compareTo(BigDecimal.ZERO) > 0) {
            summary.setMomGrowth(todayAmount.subtract(yesterday.getTodaySalesAmount())
                    .divide(yesterday.getTodaySalesAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")));
        } else {
            summary.setMomGrowth(BigDecimal.ZERO);
        }

        // B: 同比（去年同日出库口径对比）
        var lastYear = reportMapper.lastYearSameDaySalesSummary();
        if (lastYear != null && lastYear.getTodaySalesAmount() != null
                && lastYear.getTodaySalesAmount().compareTo(BigDecimal.ZERO) > 0) {
            summary.setYoyGrowth(todayAmount.subtract(lastYear.getTodaySalesAmount())
                    .divide(lastYear.getTodaySalesAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")));
        } else {
            summary.setYoyGrowth(BigDecimal.ZERO);
        }

        return summary;
    }

    // === D: 日期补齐工具方法 ===

    /**
     * 若 start 为纯日期(YYYY-MM-DD)，补充为 YYYY-MM-DD 00:00:00
     */
    private String normalizeStartDate(String start) {
        if (start != null && start.length() == 10) {
            return start + " 00:00:00";
        }
        return start;
    }

    /**
     * 若 end 为纯日期(YYYY-MM-DD)，补充为 YYYY-MM-DD 23:59:59
     * 避免 create_time <= '2026-02-28' 被解释为 <= '2026-02-28 00:00:00' 从而丢失当天数据
     */
    private String normalizeEndDate(String end) {
        if (end != null && end.length() == 10) {
            return end + " 23:59:59";
        }
        return end;
    }
}

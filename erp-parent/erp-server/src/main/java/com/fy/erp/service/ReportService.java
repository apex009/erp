package com.fy.erp.service;

import com.fy.erp.dto.report.*;
import com.fy.erp.mapper.ReportMapper;
import com.fy.erp.security.UserContext;
import com.fy.erp.security.UserPrincipal;
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

    /**
     * 解析数据范围：
     * - ADMIN → null(全量)，支持可选 salesUserId 筛选
     * - SALES → 强制当前 userId
     * - FIN → null(全局财务指标)
     */
    public Long resolveScope(Long requestedUserId) {
        UserPrincipal user = UserContext.get();
        if (user == null)
            return null;
        List<String> roles = user.getRoles();
        if (roles.contains("ADMIN")) {
            // 管理员：可传指定销售员 ID，不传则看全量
            return requestedUserId;
        }
        if (roles.contains("SALES")) {
            // 销售员：强制只看自己（忽略传入参数）
            return user.getUserId();
        }
        // 财务：看全局
        return null;
    }

    // ======================== 子报表 ========================

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'sales:day:' + #start + ':' + #end + ':' + #salesUserId")
    public List<SalesAmountByDay> salesAmountByDay(String start, String end, Long salesUserId) {
        return reportMapper.salesAmountByDay(normalizeStartDate(start), normalizeEndDate(end), salesUserId);
    }

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'sales:customer:' + #start + ':' + #end + ':' + #salesUserId")
    public List<SalesByCustomer> salesByCustomer(String start, String end, Long salesUserId) {
        return reportMapper.salesByCustomer(normalizeStartDate(start), normalizeEndDate(end), salesUserId);
    }

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'sales:product:' + #start + ':' + #end + ':' + #salesUserId")
    public List<SalesByProduct> salesByProduct(String start, String end, Long salesUserId) {
        return reportMapper.salesByProduct(normalizeStartDate(start), normalizeEndDate(end), salesUserId);
    }

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'sales:funnel:' + #ownerUserId")
    public List<SalesFunnelItem> salesFunnel(Long ownerUserId) {
        return reportMapper.salesFunnel(ownerUserId);
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

    // ======================== 看板聚合 ========================

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'dashboard:summary:' + #salesUserId")
    public DashboardSummary dashboardSummary(Long salesUserId) {
        DashboardSummary summary = new DashboardSummary();

        // 今日成交（出库口径）
        var sales = reportMapper.todaySalesSummary(salesUserId);
        if (sales != null) {
            summary.setTodaySalesAmount(sales.getTodaySalesAmount());
            summary.setTodaySalesOrderCount(sales.getTodaySalesOrderCount());
        }
        var salesOut = reportMapper.todaySalesOutbound(salesUserId);
        if (salesOut != null) {
            summary.setTodaySalesOutAmount(salesOut.getTodaySalesOutAmount());
            summary.setTodaySalesOutCount(salesOut.getTodaySalesOutCount());
        }

        // 采购情况（全局，不按用户过滤）
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

        // 客户（全局）
        var customers = reportMapper.todayNewCustomers();
        if (customers != null) {
            summary.setNewCustomerCount(customers.getNewCustomerCount());
        }

        // 库存（全局）
        var inventory = reportMapper.inventorySummary();
        if (inventory != null) {
            summary.setInventoryTotalAmount(inventory.getInventoryTotalAmount());
            summary.setProductTotalCount(inventory.getProductTotalCount());
        }

        // 收付款（全局）
        var receivable = reportMapper.todayReceivable();
        if (receivable != null) {
            summary.setTodayReceivableAmount(receivable.getTodayReceivableAmount());
        }
        var payable = reportMapper.todayPayable();
        if (payable != null) {
            summary.setTodayPayableAmount(payable.getTodayPayableAmount());
        }

        // 销售退货
        var salesReturn = reportMapper.todaySalesReturn(salesUserId);
        if (salesReturn != null) {
            summary.setTodaySalesReturnAmount(salesReturn.getTodaySalesReturnAmount());
            summary.setTodaySalesReturnCount(salesReturn.getTodaySalesReturnCount());
        }

        // 采购退货（全局）
        var purchaseReturn = reportMapper.todayPurchaseReturn();
        if (purchaseReturn != null) {
            summary.setTodayPurchaseReturnAmount(purchaseReturn.getTodayPurchaseReturnAmount());
            summary.setTodayPurchaseReturnCount(purchaseReturn.getTodayPurchaseReturnCount());
        }

        // 毛利
        var profit = reportMapper.todaySalesProfit(salesUserId);
        if (profit != null) {
            summary.setTodaySalesProfit(profit.getTodaySalesProfit());
        }

        // 销售目标
        summary.setSalesTarget(salesTarget);

        // 达成率
        BigDecimal todayAmount = summary.getTodaySalesAmount() != null ? summary.getTodaySalesAmount()
                : BigDecimal.ZERO;
        if (salesTarget.compareTo(BigDecimal.ZERO) > 0) {
            summary.setAchieveRate(
                    todayAmount.divide(salesTarget, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
        } else {
            summary.setAchieveRate(BigDecimal.ZERO);
        }

        // 环比（昨日）
        var yesterday = reportMapper.yesterdaySalesSummary(salesUserId);
        if (yesterday != null && yesterday.getTodaySalesAmount() != null
                && yesterday.getTodaySalesAmount().compareTo(BigDecimal.ZERO) > 0) {
            summary.setMomGrowth(todayAmount.subtract(yesterday.getTodaySalesAmount())
                    .divide(yesterday.getTodaySalesAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")));
        } else {
            summary.setMomGrowth(BigDecimal.ZERO);
        }

        // 同比（去年同日）
        var lastYear = reportMapper.lastYearSameDaySalesSummary(salesUserId);
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

    // ======================== 排行榜 ========================

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'rank:sales:today:' + #top")
    public List<SalesRankItem> salesRank(int top) {
        return reportMapper.salesRank(top);
    }

    // ======================== 工具 ========================

    private String normalizeStartDate(String start) {
        if (start != null && start.length() == 10)
            return start + " 00:00:00";
        return start;
    }

    private String normalizeEndDate(String end) {
        if (end != null && end.length() == 10)
            return end + " 23:59:59";
        return end;
    }
}

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
    private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

    @org.springframework.beans.factory.annotation.Value("${erp.dashboard.sales-target:100000}")
    private BigDecimal defaultSalesTarget;

    public ReportService(ReportMapper reportMapper,
            org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate) {
        this.reportMapper = reportMapper;
        this.redisTemplate = redisTemplate;
    }

    public BigDecimal getDynamicSalesTarget() {
        Object target = redisTemplate.opsForValue()
                .get(com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD + "sales_target");
        if (target != null) {
            try {
                return new BigDecimal(target.toString());
            } catch (Exception e) {
            }
        }
        return defaultSalesTarget;
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
    public List<SalesAmountByDayDTO> salesAmountByDay(String start, String end, Long salesUserId) {
        return reportMapper.salesAmountByDay(normalizeStartDate(start), normalizeEndDate(end), salesUserId);
    }

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'sales:customer:' + #start + ':' + #end + ':' + #salesUserId")
    public List<SalesByCustomerDTO> salesByCustomer(String start, String end, Long salesUserId) {
        return reportMapper.salesByCustomer(normalizeStartDate(start), normalizeEndDate(end), salesUserId);
    }

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'sales:product:' + #start + ':' + #end + ':' + #salesUserId")
    public List<SalesByProductDTO> salesByProduct(String start, String end, Long salesUserId) {
        return reportMapper.salesByProduct(normalizeStartDate(start), normalizeEndDate(end), salesUserId);
    }

    /**
     * 固定阶段定义（按业务顺序）
     */
    private static final String[][] STAGES = { { "潜在客户", "潜在客户" }, { "已跟进", "已跟进" }, { "报价中", "报价中" },
            { "已成交", "已成交" } };

    /**
     * 漏斗数据（阶段固定顺序 + 缺失补0 + 转化率计算）
     * - start/end 为空则默认最近 30 天
     * - ownerUserId 来自 resolveScope()
     */
    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'sales:funnel:' + #ownerUserId + ':' + #start + ':' + #end")
    public List<SalesFunnelItemDTO> salesFunnel(Long ownerUserId, String start, String end) {
        // 默认最近 30 天
        if (start == null || start.isBlank()) {
            start = java.time.LocalDate.now().minusDays(30).toString();
        }
        if (end == null || end.isBlank()) {
            end = java.time.LocalDate.now().toString();
        }
        start = normalizeStartDate(start);
        end = normalizeEndDate(end);

        // 1. 查询数据库（可能缺失某些阶段）
        List<SalesFunnelItemDTO> dbItems = reportMapper.salesFunnel(ownerUserId, start, end);
        java.util.Map<String, SalesFunnelItemDTO> dbMap = new java.util.HashMap<>();
        for (SalesFunnelItemDTO item : dbItems) {
            dbMap.put(item.getStageCode(), item);
        }

        // 2. 固定阶段顺序 + 补齐缺失阶段
        List<SalesFunnelItemDTO> result = new java.util.ArrayList<>();
        for (String[] stage : STAGES) {
            SalesFunnelItemDTO item = dbMap.get(stage[0]);
            SalesFunnelItemDTO filled = new SalesFunnelItemDTO();
            filled.setStageCode(stage[0]);
            filled.setStageName(stage[1]);
            filled.setCount(item != null ? item.getCount() : 0L);
            filled.setAmount(item != null && item.getAmount() != null ? item.getAmount() : java.math.BigDecimal.ZERO);
            result.add(filled);
        }

        // 3. 计算转化率 = 当前阶段 count / 上一阶段 count * 100
        for (int i = 0; i < result.size(); i++) {
            if (i == 0) {
                result.get(i).setConversionRate(null); // 第一阶段没有上一阶段
            } else {
                long prevCount = result.get(i - 1).getCount();
                long curCount = result.get(i).getCount();
                if (prevCount > 0) {
                    result.get(i)
                            .setConversionRate(java.math.BigDecimal.valueOf(curCount)
                                    .divide(java.math.BigDecimal.valueOf(prevCount), 4, java.math.RoundingMode.HALF_UP)
                                    .multiply(new java.math.BigDecimal("100")));
                } else {
                    result.get(i).setConversionRate(java.math.BigDecimal.ZERO);
                }
            }
        }

        return result;
    }

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'inventory:low-stock'")
    public List<LowStockItemDTO> lowStock() {
        return reportMapper.lowStock();
    }

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'finance:receivable'")
    public FinanceSummaryDTO receivableSummary() {
        return reportMapper.receivableSummary();
    }

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'finance:payable'")
    public FinanceSummaryDTO payableSummary() {
        return reportMapper.payableSummary();
    }

    // ======================== 看板聚合 ========================

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'dashboard:summary:' + #salesUserId")
    public DashboardSummaryDTO dashboardSummary(Long salesUserId) {
        DashboardSummaryDTO summary = new DashboardSummaryDTO();

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
        BigDecimal currentSalesTarget = getDynamicSalesTarget();
        summary.setSalesTarget(currentSalesTarget);

        // 达成率
        BigDecimal todayAmount = summary.getTodaySalesAmount() != null ? summary.getTodaySalesAmount()
                : BigDecimal.ZERO;
        if (currentSalesTarget.compareTo(BigDecimal.ZERO) > 0) {
            summary.setAchieveRate(
                    todayAmount.divide(currentSalesTarget, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
        } else {
            summary.setAchieveRate(BigDecimal.ZERO);
        }

        // 环比（昨日）
        var yesterday = reportMapper.yesterdaySalesSummary(salesUserId);
        if (yesterday != null && yesterday.getTodaySalesAmount() != null
                && yesterday.getTodaySalesAmount().compareTo(BigDecimal.ZERO) > 0) {
            summary.setMomGrowth(todayAmount.subtract(yesterday.getTodaySalesAmount())
                    .divide(yesterday.getTodaySalesAmount(), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
        } else {
            summary.setMomGrowth(BigDecimal.ZERO);
        }

        // 同比（去年同日）
        var lastYear = reportMapper.lastYearSameDaySalesSummary(salesUserId);
        if (lastYear != null && lastYear.getTodaySalesAmount() != null
                && lastYear.getTodaySalesAmount().compareTo(BigDecimal.ZERO) > 0) {
            summary.setYoyGrowth(todayAmount.subtract(lastYear.getTodaySalesAmount())
                    .divide(lastYear.getTodaySalesAmount(), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
        } else {
            summary.setYoyGrowth(BigDecimal.ZERO);
        }

        return summary;
    }

    // ======================== 排行榜 ========================

    @org.springframework.cache.annotation.Cacheable(value = com.fy.erp.constant.RedisKeyPrefix.REPORT_DASHBOARD, key = "'rank:sales:today:' + #top")
    public List<SalesRankItemDTO> salesRank(int top) {
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

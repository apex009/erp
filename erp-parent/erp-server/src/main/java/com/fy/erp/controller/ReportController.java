package com.fy.erp.controller;

import com.fy.erp.dto.report.*;
import com.fy.erp.result.Result;
import com.fy.erp.service.ReportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sales/day")
    public Result<List<SalesAmountByDay>> salesByDay(@RequestParam String start, @RequestParam String end,
            @RequestParam(required = false) Long salesUserId) {
        Long scope = reportService.resolveScope(salesUserId);
        return Result.success(reportService.salesAmountByDay(start, end, scope));
    }

    @GetMapping("/sales/customer")
    public Result<List<SalesByCustomer>> salesByCustomer(@RequestParam String start, @RequestParam String end,
            @RequestParam(required = false) Long salesUserId) {
        Long scope = reportService.resolveScope(salesUserId);
        return Result.success(reportService.salesByCustomer(start, end, scope));
    }

    @GetMapping("/sales/product")
    public Result<List<SalesByProduct>> salesByProduct(@RequestParam String start, @RequestParam String end,
            @RequestParam(required = false) Long salesUserId) {
        Long scope = reportService.resolveScope(salesUserId);
        return Result.success(reportService.salesByProduct(start, end, scope));
    }

    @GetMapping("/sales/funnel")
    public Result<List<SalesFunnelItem>> salesFunnel(@RequestParam(required = false) Long salesUserId) {
        Long scope = reportService.resolveScope(salesUserId);
        return Result.success(reportService.salesFunnel(scope));
    }

    @GetMapping("/inventory/low-stock")
    public Result<List<LowStockItem>> lowStock() {
        return Result.success(reportService.lowStock());
    }

    @GetMapping("/finance/receivable")
    public Result<FinanceSummary> receivableSummary() {
        return Result.success(reportService.receivableSummary());
    }

    @GetMapping("/finance/payable")
    public Result<FinanceSummary> payableSummary() {
        return Result.success(reportService.payableSummary());
    }

    @GetMapping("/dashboard/summary")
    public Result<DashboardSummary> dashboardSummary(@RequestParam(required = false) Long salesUserId) {
        Long scope = reportService.resolveScope(salesUserId);
        return Result.success(reportService.dashboardSummary(scope));
    }

    /**
     * 成交排行榜（≤5分钟延迟，Redis 缓存）
     */
    @GetMapping("/rank/sales")
    public Result<List<SalesRankItem>> salesRank(@RequestParam(defaultValue = "10") int top) {
        return Result.success(reportService.salesRank(Math.min(top, 50)));
    }
}

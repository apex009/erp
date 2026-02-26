package com.fy.erp.controller;

import com.fy.erp.dto.report.FinanceSummary;
import com.fy.erp.dto.report.LowStockItem;
import com.fy.erp.dto.report.SalesAmountByDay;
import com.fy.erp.dto.report.SalesByCustomer;
import com.fy.erp.dto.report.SalesByProduct;
import com.fy.erp.dto.report.SalesFunnelItem;
import com.fy.erp.result.Result;
import com.fy.erp.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sales/day")
    public Result<List<SalesAmountByDay>> salesByDay(@RequestParam String start,
                                                     @RequestParam String end) {
        return Result.success(reportService.salesAmountByDay(start, end));
    }

    @GetMapping("/sales/customer")
    public Result<List<SalesByCustomer>> salesByCustomer(@RequestParam String start,
                                                         @RequestParam String end) {
        return Result.success(reportService.salesByCustomer(start, end));
    }

    @GetMapping("/sales/product")
    public Result<List<SalesByProduct>> salesByProduct(@RequestParam String start,
                                                       @RequestParam String end) {
        return Result.success(reportService.salesByProduct(start, end));
    }

    @GetMapping("/sales/funnel")
    public Result<List<SalesFunnelItem>> salesFunnel() {
        return Result.success(reportService.salesFunnel());
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
}

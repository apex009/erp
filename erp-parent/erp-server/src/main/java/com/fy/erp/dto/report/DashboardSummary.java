package com.fy.erp.dto.report;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DashboardSummary {
  // 今日销售
  private BigDecimal todaySalesAmount;
  private Integer todaySalesOrderCount;
  private BigDecimal todaySalesOutAmount;
  private Integer todaySalesOutCount;
  private BigDecimal todaySalesReturnAmount;
  private Integer todaySalesReturnCount;
  private BigDecimal todaySalesProfit;

  // 销售目标与增长
  private BigDecimal salesTarget;
  private BigDecimal yoyGrowth; // 同比增长率
  private BigDecimal momGrowth; // 环比增长率

  // 采购情况
  private BigDecimal todayPurchaseAmount;
  private Integer todayPurchaseOrderCount;
  private BigDecimal todayPurchaseInAmount;
  private Integer todayPurchaseInCount;
  private BigDecimal todayPurchaseReturnAmount;
  private Integer todayPurchaseReturnCount;

  // 客户情况
  private Integer newCustomerCount;

  // 库存情况
  private BigDecimal inventoryTotalAmount;
  private Integer productTotalCount;

  // 今日收款
  private BigDecimal todayReceivableAmount;

  // 今日付款
  private BigDecimal todayPayableAmount;
}

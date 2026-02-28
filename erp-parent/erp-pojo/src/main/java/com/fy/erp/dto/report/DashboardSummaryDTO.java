package com.fy.erp.dto.report;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DashboardSummaryDTO {
  // 今日成交（出库口径：status=3 已出库，以 update_time 为出库完成时间）
  private BigDecimal todaySalesAmount;
  private Integer todaySalesOrderCount;
  private BigDecimal todaySalesOutAmount;
  private Integer todaySalesOutCount;
  private BigDecimal todaySalesReturnAmount;
  private Integer todaySalesReturnCount;
  private BigDecimal todaySalesProfit;

  // 销售目标与增长
  private BigDecimal salesTarget;
  private BigDecimal achieveRate;
  private BigDecimal yoyGrowth;
  private BigDecimal momGrowth;

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

  // 今日收付款
  private BigDecimal todayReceivableAmount;
  private BigDecimal todayPayableAmount;
}

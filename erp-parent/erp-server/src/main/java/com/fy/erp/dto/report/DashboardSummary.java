package com.fy.erp.dto.report;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DashboardSummary {
  // 今日成交（出库口径：status=3 已出库，以 update_time 为出库完成时间）
  private BigDecimal todaySalesAmount; // 今日成交总额
  private Integer todaySalesOrderCount; // 今日成交订单数
  private BigDecimal todaySalesOutAmount; // 今日出库金额（与成交同口径）
  private Integer todaySalesOutCount; // 今日出库单数
  private BigDecimal todaySalesReturnAmount; // 今日退货金额（status=2）
  private Integer todaySalesReturnCount; // 今日退货单数
  private BigDecimal todaySalesProfit; // 今日毛利（出库口径）

  // 销售目标与增长（出库口径）
  private BigDecimal salesTarget; // 目标金额（来自 application.yaml 配置）
  private BigDecimal achieveRate; // 达成率 = todaySalesAmount / salesTarget * 100
  private BigDecimal yoyGrowth; // 同比增长率（去年同日出库额对比）
  private BigDecimal momGrowth; // 环比增长率（昨日出库额对比）

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

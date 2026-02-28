package com.fy.erp.mapper;

import com.fy.erp.dto.report.FinanceSummary;
import com.fy.erp.dto.report.LowStockItem;
import com.fy.erp.dto.report.SalesAmountByDay;
import com.fy.erp.dto.report.SalesByCustomer;
import com.fy.erp.dto.report.SalesByProduct;
import com.fy.erp.dto.report.SalesFunnelItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReportMapper {
  @Select("""
      SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS statDate,
             SUM(total_amount) AS amount
      FROM sales_order
      WHERE deleted = 0
        AND create_time >= #{start}
        AND create_time <= #{end}
      GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d')
      ORDER BY statDate
      """)
  List<SalesAmountByDay> salesAmountByDay(@Param("start") String start, @Param("end") String end);

  @Select("""
      SELECT c.id AS customerId,
             c.name AS customerName,
             SUM(o.total_amount) AS amount
      FROM sales_order o
      LEFT JOIN customer c ON o.customer_id = c.id
      WHERE o.deleted = 0
        AND o.create_time >= #{start}
        AND o.create_time <= #{end}
      GROUP BY c.id, c.name
      ORDER BY amount DESC
      """)
  List<SalesByCustomer> salesByCustomer(@Param("start") String start, @Param("end") String end);

  @Select("""
      SELECT p.id AS productId,
             p.name AS productName,
             SUM(i.quantity) AS quantity,
             SUM(i.amount) AS amount
      FROM sales_item i
      LEFT JOIN sales_order o ON i.order_id = o.id
      LEFT JOIN product p ON i.product_id = p.id
      WHERE i.deleted = 0
        AND o.deleted = 0
        AND o.create_time >= #{start}
        AND o.create_time <= #{end}
      GROUP BY p.id, p.name
      ORDER BY amount DESC
      """)
  List<SalesByProduct> salesByProduct(@Param("start") String start, @Param("end") String end);

  @Select("""
      SELECT stage AS stage,
             COUNT(*) AS count,
             IFNULL(SUM(expected_amount), 0) AS amount
      FROM sales_lead
      WHERE deleted = 0
        AND status = 1
      GROUP BY stage
      ORDER BY count DESC
      """)
  List<SalesFunnelItem> salesFunnel();

  @Select("""
      SELECT s.product_id AS productId,
             p.name AS productName,
             s.warehouse_id AS warehouseId,
             s.quantity AS quantity,
             s.safe_stock AS safeStock
      FROM stock s
      LEFT JOIN product p ON s.product_id = p.id
      WHERE s.deleted = 0
        AND s.quantity < s.safe_stock
      ORDER BY (s.safe_stock - s.quantity) DESC
      """)
  List<LowStockItem> lowStock();

  @Select("""
      SELECT IFNULL(SUM(amount), 0) AS totalAmount,
             IFNULL(SUM(paid_amount), 0) AS paidAmount,
             IFNULL(SUM(amount - paid_amount), 0) AS unpaidAmount
      FROM receivable
      WHERE deleted = 0
      """)
  FinanceSummary receivableSummary();

  @Select("""
      SELECT IFNULL(SUM(amount), 0) AS totalAmount,
             IFNULL(SUM(paid_amount), 0) AS paidAmount,
             IFNULL(SUM(amount - paid_amount), 0) AS unpaidAmount
      FROM payable
      WHERE deleted = 0
      """)
  FinanceSummary payableSummary();

  // === 经营看板聚合查询 ===

  // 今日成交（出库口径：status=3 已出库，以 update_time 为出库完成时间）
  @Select("""
      SELECT IFNULL(SUM(total_amount), 0) AS todaySalesAmount,
             COUNT(*) AS todaySalesOrderCount
      FROM sales_order
      WHERE deleted = 0
        AND status = 3
        AND DATE(update_time) = CURDATE()
      """)
  com.fy.erp.dto.report.DashboardSummary todaySalesSummary();

  @Select("""
      SELECT IFNULL(SUM(total_amount), 0) AS todaySalesOutAmount,
             COUNT(*) AS todaySalesOutCount
      FROM sales_order
      WHERE deleted = 0
        AND status = 3
        AND DATE(update_time) = CURDATE()
      """)
  com.fy.erp.dto.report.DashboardSummary todaySalesOutbound();

  @Select("""
      SELECT IFNULL(SUM(total_amount), 0) AS todayPurchaseAmount,
             COUNT(*) AS todayPurchaseOrderCount
      FROM purchase_order
      WHERE deleted = 0
        AND DATE(create_time) = CURDATE()
      """)
  com.fy.erp.dto.report.DashboardSummary todayPurchaseSummary();

  @Select("""
      SELECT IFNULL(SUM(total_amount), 0) AS todayPurchaseInAmount,
             COUNT(*) AS todayPurchaseInCount
      FROM purchase_order
      WHERE deleted = 0
        AND status = 3
        AND DATE(update_time) = CURDATE()
      """)
  com.fy.erp.dto.report.DashboardSummary todayPurchaseInbound();

  @Select("""
      SELECT COUNT(*) AS newCustomerCount
      FROM customer
      WHERE deleted = 0
        AND DATE(create_time) = CURDATE()
      """)
  com.fy.erp.dto.report.DashboardSummary todayNewCustomers();

  @Select("""
      SELECT IFNULL(SUM(s.quantity * p.sale_price), 0) AS inventoryTotalAmount,
             COUNT(DISTINCT s.product_id) AS productTotalCount
      FROM stock s
      LEFT JOIN product p ON s.product_id = p.id
      WHERE s.deleted = 0
      """)
  com.fy.erp.dto.report.DashboardSummary inventorySummary();

  @Select("""
      SELECT IFNULL(SUM(paid_amount), 0) AS todayReceivableAmount
      FROM receivable
      WHERE deleted = 0
        AND DATE(update_time) = CURDATE()
        AND paid_amount > 0
      """)
  com.fy.erp.dto.report.DashboardSummary todayReceivable();

  @Select("""
      SELECT IFNULL(SUM(paid_amount), 0) AS todayPayableAmount
      FROM payable
      WHERE deleted = 0
        AND DATE(update_time) = CURDATE()
        AND paid_amount > 0
      """)
  com.fy.erp.dto.report.DashboardSummary todayPayable();

  // 销售退货（status = 2 代表已取消/退货）
  @Select("""
      SELECT IFNULL(SUM(total_amount), 0) AS todaySalesReturnAmount,
             COUNT(*) AS todaySalesReturnCount
      FROM sales_order
      WHERE deleted = 0
        AND status = 2
        AND DATE(update_time) = CURDATE()
      """)
  com.fy.erp.dto.report.DashboardSummary todaySalesReturn();

  // 采购退货
  @Select("""
      SELECT IFNULL(SUM(total_amount), 0) AS todayPurchaseReturnAmount,
             COUNT(*) AS todayPurchaseReturnCount
      FROM purchase_order
      WHERE deleted = 0
        AND status = 2
        AND DATE(update_time) = CURDATE()
      """)
  com.fy.erp.dto.report.DashboardSummary todayPurchaseReturn();

  // 毛利 = 销售出库金额 - 对应商品的采购成本
  @Select("""
      SELECT IFNULL(SUM(i.amount - i.quantity * p.purchase_price), 0) AS todaySalesProfit
      FROM sales_item i
      LEFT JOIN sales_order o ON i.order_id = o.id
      LEFT JOIN product p ON i.product_id = p.id
      WHERE o.deleted = 0
        AND o.status = 3
        AND DATE(o.update_time) = CURDATE()
      """)
  com.fy.erp.dto.report.DashboardSummary todaySalesProfit();

  // 昨日成交额（出库口径，用于环比计算）
  @Select("""
      SELECT IFNULL(SUM(total_amount), 0) AS todaySalesAmount
      FROM sales_order
      WHERE deleted = 0
        AND status = 3
        AND DATE(update_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)
      """)
  com.fy.erp.dto.report.DashboardSummary yesterdaySalesSummary();

  // 去年同日成交额（出库口径，用于同比计算）
  @Select("""
      SELECT IFNULL(SUM(total_amount), 0) AS todaySalesAmount
      FROM sales_order
      WHERE deleted = 0
        AND status = 3
        AND DATE(update_time) = DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
      """)
  com.fy.erp.dto.report.DashboardSummary lastYearSameDaySalesSummary();
}

package com.fy.erp.mapper;

import com.fy.erp.dto.report.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReportMapper {

        // ======================== 子报表（带可选 salesUserId 过滤） ========================

        @Select("""
                        <script>
                        SELECT DATE_FORMAT(o.update_time, '%Y-%m-%d') AS statDate,
                               SUM(o.total_amount) AS amount
                        FROM sales_order o
                        WHERE o.deleted = 0 AND o.status = 3
                          AND o.update_time &gt;= #{start}
                          AND o.update_time &lt;= #{end}
                          <if test="salesUserId != null"> AND o.owner_user_id = #{salesUserId} </if>
                        GROUP BY DATE_FORMAT(o.update_time, '%Y-%m-%d')
                        ORDER BY statDate
                        </script>
                        """)
        List<SalesAmountByDayDTO> salesAmountByDay(@Param("start") String start, @Param("end") String end,
                        @Param("salesUserId") Long salesUserId);

        @Select("""
                        <script>
                        SELECT c.id AS customerId, c.name AS customerName,
                               SUM(o.total_amount) AS amount
                        FROM sales_order o LEFT JOIN customer c ON o.customer_id = c.id
                        WHERE o.deleted = 0 AND o.status = 3
                          AND o.update_time &gt;= #{start}
                          AND o.update_time &lt;= #{end}
                          <if test="salesUserId != null"> AND o.owner_user_id = #{salesUserId} </if>
                        GROUP BY c.id, c.name
                        ORDER BY amount DESC
                        </script>
                        """)
        List<SalesByCustomerDTO> salesByCustomer(@Param("start") String start, @Param("end") String end,
                        @Param("salesUserId") Long salesUserId);

        @Select("""
                        <script>
                        SELECT p.id AS productId, p.name AS productName,
                               SUM(i.quantity) AS quantity, SUM(i.amount) AS amount
                        FROM sales_item i
                        LEFT JOIN sales_order o ON i.order_id = o.id
                        LEFT JOIN product p ON i.product_id = p.id
                        WHERE i.deleted = 0 AND o.deleted = 0 AND o.status = 3
                          AND o.update_time &gt;= #{start}
                          AND o.update_time &lt;= #{end}
                          <if test="salesUserId != null"> AND o.owner_user_id = #{salesUserId} </if>
                        GROUP BY p.id, p.name
                        ORDER BY amount DESC
                        </script>
                        """)
        List<SalesByProductDTO> salesByProduct(@Param("start") String start, @Param("end") String end,
                        @Param("salesUserId") Long salesUserId);

        // ======================== 销售漏斗（带可选 ownerUserId 过滤） ========================

        @Select("""
                        <script>
                        SELECT stage AS stageCode, COUNT(*) AS count,
                               IFNULL(SUM(expected_amount), 0) AS amount
                        FROM sales_lead
                        WHERE deleted = 0 AND status = 1
                          <if test="ownerUserId != null"> AND owner_user_id = #{ownerUserId} </if>
                          <if test="start != null"> AND create_time &gt;= #{start} </if>
                          <if test="end != null"> AND create_time &lt;= #{end} </if>
                        GROUP BY stage
                        </script>
                        """)
        List<SalesFunnelItemDTO> salesFunnel(@Param("ownerUserId") Long ownerUserId,
                        @Param("start") String start,
                        @Param("end") String end);

        // ======================== 全局（不按用户过滤） ========================

        @Select("""
                        SELECT s.product_id AS productId, p.name AS productName,
                               s.warehouse_id AS warehouseId, s.quantity, s.safe_stock AS safeStock
                        FROM stock s LEFT JOIN product p ON s.product_id = p.id
                        WHERE s.deleted = 0 AND s.quantity < s.safe_stock
                        ORDER BY (s.safe_stock - s.quantity) DESC
                        """)
        List<LowStockItemDTO> lowStock();

        @Select("""
                        SELECT IFNULL(SUM(amount), 0) AS totalAmount,
                               IFNULL(SUM(paid_amount), 0) AS paidAmount,
                               IFNULL(SUM(amount - paid_amount), 0) AS unpaidAmount
                        FROM receivable WHERE deleted = 0
                        """)
        FinanceSummaryDTO receivableSummary();

        @Select("""
                        SELECT IFNULL(SUM(amount), 0) AS totalAmount,
                               IFNULL(SUM(paid_amount), 0) AS paidAmount,
                               IFNULL(SUM(amount - paid_amount), 0) AS unpaidAmount
                        FROM payable WHERE deleted = 0
                        """)
        FinanceSummaryDTO payableSummary();

        // ======================== 看板聚合（带可选 salesUserId 过滤） ========================

        @Select("""
                        <script>
                        SELECT IFNULL(SUM(total_amount), 0) AS todaySalesAmount,
                               COUNT(*) AS todaySalesOrderCount
                        FROM sales_order
                        WHERE deleted = 0 AND status = 3 AND DATE(update_time) = CURDATE()
                          <if test="salesUserId != null"> AND owner_user_id = #{salesUserId} </if>
                        </script>
                        """)
        DashboardSummaryDTO todaySalesSummary(@Param("salesUserId") Long salesUserId);

        @Select("""
                        <script>
                        SELECT IFNULL(SUM(total_amount), 0) AS todaySalesOutAmount,
                               COUNT(*) AS todaySalesOutCount
                        FROM sales_order
                        WHERE deleted = 0 AND status = 3 AND DATE(update_time) = CURDATE()
                          <if test="salesUserId != null"> AND owner_user_id = #{salesUserId} </if>
                        </script>
                        """)
        DashboardSummaryDTO todaySalesOutbound(@Param("salesUserId") Long salesUserId);

        @Select("""
                        SELECT IFNULL(SUM(total_amount), 0) AS todayPurchaseAmount,
                               COUNT(*) AS todayPurchaseOrderCount
                        FROM purchase_order WHERE deleted = 0 AND DATE(create_time) = CURDATE()
                        """)
        DashboardSummaryDTO todayPurchaseSummary();

        @Select("""
                        SELECT IFNULL(SUM(total_amount), 0) AS todayPurchaseInAmount,
                               COUNT(*) AS todayPurchaseInCount
                        FROM purchase_order WHERE deleted = 0 AND status = 3 AND DATE(update_time) = CURDATE()
                        """)
        DashboardSummaryDTO todayPurchaseInbound();

        @Select("""
                        SELECT COUNT(*) AS newCustomerCount
                        FROM customer WHERE deleted = 0 AND DATE(create_time) = CURDATE()
                        """)
        DashboardSummaryDTO todayNewCustomers();

        @Select("""
                        SELECT IFNULL(SUM(s.quantity * p.sale_price), 0) AS inventoryTotalAmount,
                               COUNT(DISTINCT s.product_id) AS productTotalCount
                        FROM stock s LEFT JOIN product p ON s.product_id = p.id
                        WHERE s.deleted = 0
                        """)
        DashboardSummaryDTO inventorySummary();

        @Select("""
                        SELECT IFNULL(SUM(paid_amount), 0) AS todayReceivableAmount
                        FROM receivable WHERE deleted = 0 AND DATE(update_time) = CURDATE() AND paid_amount > 0
                        """)
        DashboardSummaryDTO todayReceivable();

        @Select("""
                        SELECT IFNULL(SUM(paid_amount), 0) AS todayPayableAmount
                        FROM payable WHERE deleted = 0 AND DATE(update_time) = CURDATE() AND paid_amount > 0
                        """)
        DashboardSummaryDTO todayPayable();

        @Select("""
                        <script>
                        SELECT IFNULL(SUM(total_amount), 0) AS todaySalesReturnAmount,
                               COUNT(*) AS todaySalesReturnCount
                        FROM sales_order
                        WHERE deleted = 0 AND status = 2 AND DATE(update_time) = CURDATE()
                          <if test="salesUserId != null"> AND owner_user_id = #{salesUserId} </if>
                        </script>
                        """)
        DashboardSummaryDTO todaySalesReturn(@Param("salesUserId") Long salesUserId);

        @Select("""
                        SELECT IFNULL(SUM(total_amount), 0) AS todayPurchaseReturnAmount,
                               COUNT(*) AS todayPurchaseReturnCount
                        FROM purchase_order WHERE deleted = 0 AND status = 2 AND DATE(update_time) = CURDATE()
                        """)
        DashboardSummaryDTO todayPurchaseReturn();

        @Select("""
                        <script>
                        SELECT IFNULL(SUM(i.amount - i.quantity * p.purchase_price), 0) AS todaySalesProfit
                        FROM sales_item i
                        LEFT JOIN sales_order o ON i.order_id = o.id
                        LEFT JOIN product p ON i.product_id = p.id
                        WHERE o.deleted = 0 AND o.status = 3 AND DATE(o.update_time) = CURDATE()
                          <if test="salesUserId != null"> AND o.owner_user_id = #{salesUserId} </if>
                        </script>
                        """)
        DashboardSummaryDTO todaySalesProfit(@Param("salesUserId") Long salesUserId);

        @Select("""
                        <script>
                        SELECT IFNULL(SUM(total_amount), 0) AS todaySalesAmount
                        FROM sales_order
                        WHERE deleted = 0 AND status = 3 AND DATE(update_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)
                          <if test="salesUserId != null"> AND owner_user_id = #{salesUserId} </if>
                        </script>
                        """)
        DashboardSummaryDTO yesterdaySalesSummary(@Param("salesUserId") Long salesUserId);

        @Select("""
                        <script>
                        SELECT IFNULL(SUM(total_amount), 0) AS todaySalesAmount
                        FROM sales_order
                        WHERE deleted = 0 AND status = 3 AND DATE(update_time) = DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
                          <if test="salesUserId != null"> AND owner_user_id = #{salesUserId} </if>
                        </script>
                        """)
        DashboardSummaryDTO lastYearSameDaySalesSummary(@Param("salesUserId") Long salesUserId);

        // ======================== 排行榜 ========================

        @Select("""
                        SELECT o.owner_user_id AS userId, u.nickname,
                               IFNULL(SUM(o.total_amount), 0) AS amount,
                               COUNT(*) AS orderCount
                        FROM sales_order o
                        LEFT JOIN sys_user u ON o.owner_user_id = u.id
                        WHERE o.deleted = 0 AND o.status = 3 AND DATE(o.update_time) = CURDATE()
                          AND o.owner_user_id IS NOT NULL
                        GROUP BY o.owner_user_id, u.nickname
                        ORDER BY amount DESC
                        LIMIT #{top}
                        """)
        List<SalesRankItemDTO> salesRank(@Param("top") int top);
}

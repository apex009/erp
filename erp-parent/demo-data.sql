-- 核心演示用闭环数据
-- 包含了 客户、商品、供应商、仓库、采购申请/订单、入库、销售、发货、收款、付款 全流程。

USE `erp_light`;

-- 1. 清理数据（为了保证可重复演示，谨慎操作，正式环境切勿如此）
TRUNCATE TABLE `sales_order`;
TRUNCATE TABLE `sales_item`;
TRUNCATE TABLE `stock`;
TRUNCATE TABLE `stock_record`;
TRUNCATE TABLE `purchase_order`;
TRUNCATE TABLE `purchase_item`;
TRUNCATE TABLE `receivable`;
TRUNCATE TABLE `payment`;
TRUNCATE TABLE `receipt`;
TRUNCATE TABLE `sales_lead`;

-- 2. 基础数据确保存在
INSERT IGNORE INTO `customer` (`id`, `name`, `level`, `contact_name`, `phone`, `status`) VALUES
(1, '大客户A集团', 'A', '张总', '13800000001', 1),
(2, '渠道商B公司', 'B', '李总', '13800000002', 1);

INSERT IGNORE INTO `product` (`id`, `product_code`, `name`, `category`, `unit`, `price`, `cost`, `status`) VALUES
(1, 'P001', '旗舰智能手机', '数码', '台', 5999.00, 4000.00, 1),
(2, 'P002', '降噪蓝牙耳机', '配件', '个', 999.00, 400.00, 1);

INSERT IGNORE INTO `supplier` (`id`, `name`, `contact_name`, `phone`, `status`) VALUES
(1, '顶级代工厂X', '王经理', '13900000001', 1);

INSERT IGNORE INTO `warehouse` (`id`, `name`, `location`, `status`) VALUES
(1, '深圳总部一仓', '深圳南山', 1);


-- 3. 采购入库链路 (产生库存，为了后续能卖)
-- 采购单: P001 买 100台，P002 买 200个
INSERT INTO `purchase_order` (`id`, `order_no`, `supplier_id`, `total_amount`, `status`, `create_time`, `update_time`) VALUES
(1, 'PO20260301001', 1, 480000.00, 3, NOW(), NOW()); -- 状态3=已入库

INSERT INTO `purchase_item` (`id`, `order_id`, `product_id`, `quantity`, `price`, `amount`) VALUES
(1, 1, 1, 100, 4000.00, 400000.00),
(2, 1, 2, 200, 400.00, 80000.00);

-- 产生库存 (Warehouse 1)
INSERT INTO `stock` (`id`, `product_id`, `warehouse_id`, `quantity`, `safe_stock`) VALUES
(1, 1, 1, 100.0000, 10.0000),
(2, 2, 1, 200.0000, 20.0000);

-- 新增采购流水
INSERT INTO `stock_record` (`product_id`, `warehouse_id`, `type`, `quantity`, `balance`, `biz_no`, `create_time`) VALUES
(1, 1, 1, 100, 100, 'PO20260301001', NOW()),
(2, 1, 1, 200, 200, 'PO20260301001', NOW());

-- 产生财务应付与付款
INSERT INTO `payment` (`id`, `supplier_id`, `order_id`, `amount`, `status`, `create_time`, `update_time`) VALUES
(1, 1, 1, 480000.00, 1, NOW(), NOW()); -- 已付款


-- 4. 销售出库链路 (消耗库存)
-- 销售线索 (漏斗演示)
INSERT INTO `sales_lead` (`id`, `customer_name`, `contact`, `phone`, `source`, `stage`, `expected_amount`, `sales_user_id`, `create_time`) VALUES
(1, '新客户C', '赵女士', '13700000001', 'WEBSITE', '潜在客户', 50000.00, 101, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, '大客户A集团-增购', '张总', '13800000001', 'REFERRAL', '已跟进', 100000.00, 101, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, '渠道商B-季度单', '李总', '13800000002', 'DIRECT', '已成交', 200000.00, 101, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, '渠道商B-耳机尾单', '李总', '13800000002', 'DIRECT', '已成交', 49950.00, 101, NOW());

-- 销售单1: 销售给 大客户A， P001 卖 10台
INSERT INTO `sales_order` (`id`, `order_no`, `customer_id`, `sales_user_id`, `total_amount`, `status`, `create_time`, `update_time`) VALUES
(1, 'SO20260301001', 1, 101, 59990.00, 3, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)); -- 昨天的出库

INSERT INTO `sales_item` (`id`, `order_id`, `product_id`, `quantity`, `price`, `amount`) VALUES
(1, 1, 1, 10, 5999.00, 59990.00);

-- 更新库存
UPDATE `stock` SET `quantity` = `quantity` - 10 WHERE `id` = 1;
INSERT INTO `stock_record` (`product_id`, `warehouse_id`, `type`, `quantity`, `balance`, `biz_no`, `create_time`) VALUES
(1, 1, 2, 10, 90, 'SO20260301001', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 销售单2: 销售给 渠道商B， P001 卖 20台，P002 卖 50个 (今天发生的)
INSERT INTO `sales_order` (`id`, `order_no`, `customer_id`, `sales_user_id`, `total_amount`, `status`, `create_time`, `update_time`) VALUES
(2, 'SO20260301002', 2, 101, 169930.00, 3, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()); 

INSERT INTO `sales_item` (`id`, `order_id`, `product_id`, `quantity`, `price`, `amount`) VALUES
(2, 2, 1, 20, 5999.00, 119980.00),
(3, 2, 2, 50, 999.00, 49950.00);

-- 更新库存
UPDATE `stock` SET `quantity` = `quantity` - 20 WHERE `id` = 1;
UPDATE `stock` SET `quantity` = `quantity` - 50 WHERE `id` = 2;
INSERT INTO `stock_record` (`product_id`, `warehouse_id`, `type`, `quantity`, `balance`, `biz_no`, `create_time`) VALUES
(1, 1, 2, 20, 70, 'SO20260301002', NOW()),
(2, 1, 2, 50, 150, 'SO20260301002', NOW());

-- 产生财务应收与收款
-- 单1 的应收，已收
INSERT INTO `receivable` (`id`, `customer_id`, `order_id`, `amount`, `paid_amount`, `status`, `create_time`, `update_time`) VALUES
(1, 1, 1, 59990.00, 59990.00, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO `receipt` (`id`, `customer_id`, `order_id`, `amount`, `status`, `create_time`, `update_time`) VALUES
(1, 1, 1, 59990.00, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 单2 的应收，部分收
INSERT INTO `receivable` (`id`, `customer_id`, `order_id`, `amount`, `paid_amount`, `status`, `create_time`, `update_time`) VALUES
(2, 2, 2, 169930.00, 69930.00, 1, NOW(), NOW());

INSERT INTO `receipt` (`id`, `customer_id`, `order_id`, `amount`, `status`, `create_time`, `update_time`) VALUES
(2, 2, 2, 69930.00, 1, NOW(), NOW());

-- ================================================================
-- ERP 数据迁移 + 测试数据脚本 (幂等可重复执行)
-- 包含：sales_order 表结构迁移 + 3 个销售员 + 丰富业务数据
-- 运行：mysql -u root -proot --default-character-set=utf8mb4 erp_light -e "source D:/ERP/test_data.sql"
-- ================================================================

SET NAMES utf8mb4;
SET @NOW = NOW();

-- ================================================================
-- 0. 表结构迁移：sales_order 新增 sales_user_id
-- ================================================================
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA='erp_light' AND TABLE_NAME='sales_order' AND COLUMN_NAME='sales_user_id');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE sales_order ADD COLUMN sales_user_id BIGINT NULL AFTER customer_id',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ================================================================
-- 1. 清理测试数据（id >= 100 区间，不影响现有小 ID 业务数据）
-- ================================================================
DELETE FROM payment WHERE id >= 100;
DELETE FROM receipt WHERE id >= 100;
DELETE FROM payable WHERE id >= 100;
DELETE FROM receivable WHERE id >= 100;
DELETE FROM stock_record WHERE id >= 100;
DELETE FROM sales_item WHERE id >= 100;
DELETE FROM sales_order WHERE id >= 100;
DELETE FROM sales_lead WHERE id >= 100;
DELETE FROM purchase_item WHERE id >= 100;
DELETE FROM purchase_order WHERE id >= 100;
DELETE FROM stock WHERE id >= 100;
DELETE FROM product WHERE id >= 100;
DELETE FROM customer WHERE id >= 100;
DELETE FROM supplier WHERE id >= 100;
DELETE FROM warehouse WHERE id >= 100;
DELETE FROM sys_role_perm WHERE id >= 100;
DELETE FROM sys_user_role WHERE id >= 100;
DELETE FROM sys_permission WHERE id >= 100;
DELETE FROM sys_user WHERE id >= 100;
DELETE FROM sys_dept WHERE id >= 100;

-- ================================================================
-- 2. 部门
-- ================================================================
INSERT INTO sys_dept (id, parent_id, dept_name, leader, status, create_time, update_time, deleted) VALUES
(100, 0,   '总公司',   '傅勇',   1, @NOW, @NOW, 0),
(101, 100, '销售一部', '张三',   1, @NOW, @NOW, 0),
(102, 100, '销售二部', '王五',   1, @NOW, @NOW, 0),
(103, 100, '财务部',   '李四',   1, @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE dept_name=VALUES(dept_name), update_time=@NOW;

-- ================================================================
-- 3. 用户（3 销售 + 1 财务，保留现有 admin id=1）
--    密码: 123456 → BCrypt hash
-- ================================================================
SET @PWD = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi';

INSERT INTO sys_user (id, dept_id, username, password, nickname, phone, email, status, create_time, update_time, deleted) VALUES
(101, 101, 'sales',   @PWD, '张三',   '13800000002', 'zhangsan@erp.com', 1, @NOW, @NOW, 0),
(102, 103, 'finance', @PWD, '李四',   '13800000003', 'lisi@erp.com',     1, @NOW, @NOW, 0),
(103, 101, 'salesB',  @PWD, '王五',   '13800000004', 'wangwu@erp.com',   1, @NOW, @NOW, 0),
(104, 102, 'salesC',  @PWD, '赵六',   '13800000005', 'zhaoliu@erp.com',  1, @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE password=VALUES(password), nickname=VALUES(nickname), update_time=@NOW;

-- ================================================================
-- 4. 用户-角色关联（ADMIN=1, SALES=2, FIN=3）
-- ================================================================
INSERT INTO sys_user_role (id, user_id, role_id, create_time, update_time, deleted) VALUES
(101, 101, 2, @NOW, @NOW, 0),  -- 张三 → SALES
(102, 102, 3, @NOW, @NOW, 0),  -- 李四 → FIN
(103, 103, 2, @NOW, @NOW, 0),  -- 王五 → SALES
(104, 104, 2, @NOW, @NOW, 0)   -- 赵六 → SALES
ON DUPLICATE KEY UPDATE update_time=@NOW;

-- ================================================================
-- 5. 权限（菜单 + API）
-- ================================================================
INSERT INTO sys_permission (id, perm_code, perm_name, type, path, method, status, create_time, update_time, deleted) VALUES
-- 菜单权限
(100, 'menu:dashboard',           '经营看板',     'MENU', '/dashboard',           'GET', 1, @NOW, @NOW, 0),
(101, 'menu:base:customers',      '客户管理',     'MENU', '/base/customers',      'GET', 1, @NOW, @NOW, 0),
(102, 'menu:base:products',       '商品管理',     'MENU', '/base/products',       'GET', 1, @NOW, @NOW, 0),
(103, 'menu:base:suppliers',      '供应商管理',   'MENU', '/base/suppliers',      'GET', 1, @NOW, @NOW, 0),
(104, 'menu:base:warehouses',     '仓库管理',     'MENU', '/base/warehouses',     'GET', 1, @NOW, @NOW, 0),
(105, 'menu:purchase:requests',   '采购申请',     'MENU', '/purchase/requests',   'GET', 1, @NOW, @NOW, 0),
(106, 'menu:purchase:orders',     '采购订单',     'MENU', '/purchase/orders',     'GET', 1, @NOW, @NOW, 0),
(107, 'menu:sales:leads',         '销售线索',     'MENU', '/sales/leads',         'GET', 1, @NOW, @NOW, 0),
(108, 'menu:sales:orders',        '销售订单',     'MENU', '/sales/orders',        'GET', 1, @NOW, @NOW, 0),
(109, 'menu:inventory:stocks',    '库存查询',     'MENU', '/inventory/stocks',    'GET', 1, @NOW, @NOW, 0),
(110, 'menu:inventory:records',   '库存流水',     'MENU', '/inventory/records',   'GET', 1, @NOW, @NOW, 0),
(111, 'menu:finance:receivables', '应收管理',     'MENU', '/finance/receivables', 'GET', 1, @NOW, @NOW, 0),
(112, 'menu:finance:payables',    '应付管理',     'MENU', '/finance/payables',    'GET', 1, @NOW, @NOW, 0),
(113, 'menu:system:users',        '用户管理',     'MENU', '/system/users',        'GET', 1, @NOW, @NOW, 0),
(114, 'menu:system:roles',        '角色管理',     'MENU', '/system/roles',        'GET', 1, @NOW, @NOW, 0),
(115, 'menu:system:permissions',  '权限管理',     'MENU', '/system/permissions',  'GET', 1, @NOW, @NOW, 0),
(116, 'menu:system:depts',        '部门管理',     'MENU', '/system/depts',        'GET', 1, @NOW, @NOW, 0),
-- API 权限
(200, 'api:customers:read',    '客户-查询',     'API_READ',  '/api/customers/**',   'GET',    1, @NOW, @NOW, 0),
(201, 'api:customers:write',   '客户-编辑',     'API_WRITE', '/api/customers/**',   'ALL',    1, @NOW, @NOW, 0),
(202, 'api:products:read',     '商品-查询',     'API_READ',  '/api/products/**',    'GET',    1, @NOW, @NOW, 0),
(203, 'api:products:write',    '商品-编辑',     'API_WRITE', '/api/products/**',    'ALL',    1, @NOW, @NOW, 0),
(204, 'api:suppliers:read',    '供应商-查询',   'API_READ',  '/api/suppliers/**',   'GET',    1, @NOW, @NOW, 0),
(205, 'api:suppliers:write',   '供应商-编辑',   'API_WRITE', '/api/suppliers/**',   'ALL',    1, @NOW, @NOW, 0),
(206, 'api:warehouses:read',   '仓库-查询',     'API_READ',  '/api/warehouses/**',  'GET',    1, @NOW, @NOW, 0),
(207, 'api:warehouses:write',  '仓库-编辑',     'API_WRITE', '/api/warehouses/**',  'ALL',    1, @NOW, @NOW, 0),
(208, 'api:purchase:read',     '采购-查询',     'API_READ',  '/api/purchase/**',    'GET',    1, @NOW, @NOW, 0),
(209, 'api:purchase:write',    '采购-编辑',     'API_WRITE', '/api/purchase/**',    'ALL',    1, @NOW, @NOW, 0),
(210, 'api:sales:read',        '销售-查询',     'API_READ',  '/api/sales/**',       'GET',    1, @NOW, @NOW, 0),
(211, 'api:sales:write',       '销售-编辑',     'API_WRITE', '/api/sales/**',       'ALL',    1, @NOW, @NOW, 0),
(212, 'api:inventory:read',    '库存-查询',     'API_READ',  '/api/inventory/**',   'GET',    1, @NOW, @NOW, 0),
(213, 'api:inventory:write',   '库存-编辑',     'API_WRITE', '/api/inventory/**',   'ALL',    1, @NOW, @NOW, 0),
(214, 'api:receivables:read',  '应收-查询',     'API_READ',  '/api/receivables/**', 'GET',    1, @NOW, @NOW, 0),
(215, 'api:receivables:write', '应收-编辑',     'API_WRITE', '/api/receivables/**', 'ALL',    1, @NOW, @NOW, 0),
(216, 'api:payables:read',     '应付-查询',     'API_READ',  '/api/payables/**',    'GET',    1, @NOW, @NOW, 0),
(217, 'api:payables:write',    '应付-编辑',     'API_WRITE', '/api/payables/**',    'ALL',    1, @NOW, @NOW, 0),
(218, 'api:reports:read',      '报表-查询',     'API_READ',  '/api/reports/**',     'GET',    1, @NOW, @NOW, 0),
(219, 'api:system:read',       '系统-查询',     'API_READ',  '/api/system/**',      'GET',    1, @NOW, @NOW, 0),
(220, 'api:system:write',      '系统-编辑',     'API_WRITE', '/api/system/**',      'ALL',    1, @NOW, @NOW, 0),
(221, 'api:leads:read',        '线索-查询',     'API_READ',  '/api/leads/**',       'GET',    1, @NOW, @NOW, 0),
(222, 'api:leads:write',       '线索-编辑',     'API_WRITE', '/api/leads/**',       'ALL',    1, @NOW, @NOW, 0),
(223, 'api:receipts:read',     '收款-查询',     'API_READ',  '/api/receipts/**',    'GET',    1, @NOW, @NOW, 0),
(224, 'api:receipts:write',    '收款-编辑',     'API_WRITE', '/api/receipts/**',    'ALL',    1, @NOW, @NOW, 0),
(225, 'api:payments:read',     '付款-查询',     'API_READ',  '/api/payments/**',    'GET',    1, @NOW, @NOW, 0),
(226, 'api:payments:write',    '付款-编辑',     'API_WRITE', '/api/payments/**',    'ALL',    1, @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE perm_name=VALUES(perm_name), type=VALUES(type), path=VALUES(path), method=VALUES(method), update_time=@NOW;

-- ================================================================
-- 6. 角色-权限关联（SALES=2, FIN=3）
-- ================================================================
INSERT INTO sys_role_perm (id, role_id, perm_id, create_time, update_time, deleted) VALUES
-- SALES(id=2)
(100, 2, 100, @NOW, @NOW, 0), (101, 2, 101, @NOW, @NOW, 0), (102, 2, 102, @NOW, @NOW, 0),
(103, 2, 107, @NOW, @NOW, 0), (104, 2, 108, @NOW, @NOW, 0), (105, 2, 109, @NOW, @NOW, 0),
(106, 2, 110, @NOW, @NOW, 0),
(120, 2, 200, @NOW, @NOW, 0), (121, 2, 201, @NOW, @NOW, 0), (122, 2, 202, @NOW, @NOW, 0),
(123, 2, 210, @NOW, @NOW, 0), (124, 2, 211, @NOW, @NOW, 0), (125, 2, 212, @NOW, @NOW, 0),
(126, 2, 218, @NOW, @NOW, 0), (127, 2, 221, @NOW, @NOW, 0), (128, 2, 222, @NOW, @NOW, 0),
-- FIN(id=3)
(200, 3, 100, @NOW, @NOW, 0), (201, 3, 111, @NOW, @NOW, 0), (202, 3, 112, @NOW, @NOW, 0),
(220, 3, 214, @NOW, @NOW, 0), (221, 3, 215, @NOW, @NOW, 0), (222, 3, 216, @NOW, @NOW, 0),
(223, 3, 217, @NOW, @NOW, 0), (224, 3, 218, @NOW, @NOW, 0), (225, 3, 223, @NOW, @NOW, 0),
(226, 3, 224, @NOW, @NOW, 0), (227, 3, 225, @NOW, @NOW, 0), (228, 3, 226, @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE update_time=@NOW;

-- ================================================================
-- 7. 供应商
-- ================================================================
INSERT INTO supplier (id, name, contact, phone, address, status, create_time, update_time, deleted) VALUES
(100, '深圳华为配件有限公司',   '陈经理', '13900001001', '广东省深圳市南山区科技园',     1, @NOW, @NOW, 0),
(101, '杭州阿里云科技有限公司', '吴经理', '13900001002', '浙江省杭州市余杭区文一西路', 1, @NOW, @NOW, 0),
(102, '北京联想供应链有限公司', '郑经理', '13900001003', '北京市海淀区中关村大街',       1, @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE name=VALUES(name), update_time=@NOW;

-- ================================================================
-- 8. 仓库
-- ================================================================
INSERT INTO warehouse (id, name, address, manager, status, create_time, update_time, deleted) VALUES
(100, '主仓库-深圳', '广东省深圳市宝安区', '孙七',   1, @NOW, @NOW, 0),
(101, '次仓库-广州', '广东省广州市天河区', '周八',   1, @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE name=VALUES(name), update_time=@NOW;

-- ================================================================
-- 9. 客户（分配给 3 个销售员）
-- ================================================================
INSERT INTO customer (id, name, contact, phone, level, sales_user_id, credit_limit, status, create_time, update_time, deleted) VALUES
-- 张三(101)的客户
(100, '腾讯科技有限公司',       '马经理', '13800100001', 1, 101, 500000.00, 1, @NOW, @NOW, 0),
(101, '字节跳动科技有限公司',   '刘经理', '13800100002', 2, 101, 300000.00, 1, @NOW, @NOW, 0),
-- 王五(103)的客户
(102, '小米科技有限公司',       '周经理', '13800100003', 3, 103, 200000.00, 1, @NOW, @NOW, 0),
(103, '华为终端有限公司',       '任经理', '13800100004', 1, 103, 600000.00, 1, @NOW, @NOW, 0),
(104, '网易有道信息技术',       '丁经理', '13800100005', 2, 103, 150000.00, 1, @NOW, @NOW, 0),
-- 赵六(104)的客户
(105, '美团科技有限公司',       '王经理', '13800100006', 2, 104, 250000.00, 1, @NOW, @NOW, 0),
(106, '京东方科技集团',         '陈经理', '13800100007', 1, 104, 400000.00, 1, @NOW, @NOW, 0),
(107, '大疆创新科技有限公司',   '汪经理', '13800100008', 3, 104, 180000.00, 1, @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE name=VALUES(name), sales_user_id=VALUES(sales_user_id), update_time=@NOW;

-- ================================================================
-- 10. 商品 (12 款, 3 分类)
-- ================================================================
INSERT INTO product (id, sku, name, category, unit, spec, purchase_price, sale_price, status, create_time, update_time, deleted) VALUES
(100, 'ELC-001', '笔记本电脑 Pro',    '电子产品', '台', '15.6寸/16GB/512GB', 5000.00, 7500.00, 1, @NOW, @NOW, 0),
(101, 'ELC-002', '无线蓝牙耳机',      '电子产品', '副', '降噪/续航30h',      200.00,  399.00,  1, @NOW, @NOW, 0),
(102, 'ELC-003', '机械键盘',           '电子产品', '把', '87键/茶轴',         150.00,  299.00,  1, @NOW, @NOW, 0),
(103, 'ELC-004', '27寸4K显示器',       '电子产品', '台', 'IPS/HDR400',        1800.00, 2999.00, 1, @NOW, @NOW, 0),
(104, 'OFF-001', 'A4打印纸(5包装)',    '办公用品', '箱', '70g/500张/包',      80.00,   129.00,  1, @NOW, @NOW, 0),
(105, 'OFF-002', '中性笔(12支装)',     '办公用品', '盒', '0.5mm/黑色',        15.00,   29.00,   1, @NOW, @NOW, 0),
(106, 'OFF-003', '文件夹(10个装)',     '办公用品', '包', 'A4/双夹',           20.00,   39.00,   1, @NOW, @NOW, 0),
(107, 'FUR-001', '人体工学办公椅',     '办公家具', '把', '头枕/升降/腰靠',    800.00,  1599.00, 1, @NOW, @NOW, 0),
(108, 'FUR-002', '电脑桌(1.4m)',       '办公家具', '张', '1400x700x750mm',    500.00,  999.00,  1, @NOW, @NOW, 0),
(109, 'FUR-003', '三层文件柜',         '办公家具', '个', '钢制/带锁',         300.00,  599.00,  1, @NOW, @NOW, 0),
(110, 'ELC-005', '无线鼠标',           '电子产品', '个', '蓝牙5.0',           60.00,   129.00,  1, @NOW, @NOW, 0),
(111, 'OFF-004', '白板(1.2m)',         '办公用品', '块', '磁性/可擦写',       120.00,  249.00,  1, @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE name=VALUES(name), purchase_price=VALUES(purchase_price), sale_price=VALUES(sale_price), update_time=@NOW;

-- ================================================================
-- 11. 采购订单 (4单, 全部已入库 status=3)
-- ================================================================
INSERT INTO purchase_order (id, order_no, supplier_id, total_amount, status, remark, create_time, update_time, deleted) VALUES
(100, 'PO20260225001', 100, 58500.00, 3, '电子产品批量采购',  DATE_SUB(@NOW, INTERVAL 5 DAY), DATE_SUB(@NOW, INTERVAL 4 DAY), 0),
(101, 'PO20260226001', 101, 3550.00,  3, '办公用品常规采购',  DATE_SUB(@NOW, INTERVAL 4 DAY), DATE_SUB(@NOW, INTERVAL 3 DAY), 0),
(102, 'PO20260227001', 100, 16000.00, 3, '办公家具项目采购',  DATE_SUB(@NOW, INTERVAL 3 DAY), DATE_SUB(@NOW, INTERVAL 2 DAY), 0),
(103, 'PO20260228001', 102, 5000.00,  3, '鼠标白板补货',      DATE_SUB(@NOW, INTERVAL 2 DAY), DATE_SUB(@NOW, INTERVAL 1 DAY), 0)
ON DUPLICATE KEY UPDATE status=3, update_time=VALUES(update_time);

-- ================================================================
-- 12. 采购明细
-- ================================================================
INSERT INTO purchase_item (id, order_id, product_id, warehouse_id, quantity, price, amount, create_time, update_time, deleted) VALUES
(100, 100, 100, 100, 10,  5000.00, 50000.00, @NOW, @NOW, 0),
(101, 100, 101, 100, 30, 200.00,  6000.00,  @NOW, @NOW, 0),
(102, 100, 102, 100, 20, 150.00,  3000.00,  @NOW, @NOW, 0),
(103, 100, 103, 100, 15, 1800.00, 27000.00, @NOW, @NOW, 0),
(104, 101, 104, 100, 50, 80.00,   4000.00,  @NOW, @NOW, 0),
(105, 101, 105, 100, 80, 15.00,   1200.00,  @NOW, @NOW, 0),
(106, 101, 106, 100, 40, 20.00,   800.00,   @NOW, @NOW, 0),
(107, 102, 107, 100, 15, 800.00,  12000.00, @NOW, @NOW, 0),
(108, 102, 108, 100, 10, 500.00,  5000.00,  @NOW, @NOW, 0),
(109, 102, 109, 100, 20, 300.00,  6000.00,  @NOW, @NOW, 0),
(110, 103, 110, 100, 50, 60.00,   3000.00,  @NOW, @NOW, 0),
(111, 103, 111, 100, 10, 120.00,  1200.00,  @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE update_time=@NOW;

-- ================================================================
-- 13. 销售订单（分配给 3 个销售员，含今日出库 + 历史订单）
--     status: 0=新建, 1=确认, 2=已取消, 3=已出库
-- ================================================================
INSERT INTO sales_order (id, order_no, customer_id, sales_user_id, total_amount, status, remark, create_time, update_time, deleted) VALUES
-- ===== 张三(101) 的订单：3 单今日出库 =====
(100, 'SO20260228001', 100, 101, 18495.00, 3, '腾讯-电子设备采购',   @NOW, @NOW, 0),
(101, 'SO20260228002', 101, 101, 6258.00,  3, '字节-办公用品采购',   @NOW, @NOW, 0),
(102, 'SO20260228003', 100, 101, 8970.00,  3, '腾讯-显示器追加',     @NOW, @NOW, 0),
-- ===== 王五(103) 的订单：4 单今日出库 =====
(103, 'SO20260228004', 102, 103, 10588.00, 3, '小米-办公家具采购',   @NOW, @NOW, 0),
(104, 'SO20260228005', 103, 103, 23994.00, 3, '华为-笔记本+显示器',  @NOW, @NOW, 0),
(105, 'SO20260228006', 104, 103, 2900.00,  3, '网易-办公用品采购',   @NOW, @NOW, 0),
(106, 'SO20260228007', 103, 103, 5196.00,  3, '华为-办公椅追加',     @NOW, @NOW, 0),
-- ===== 赵六(104) 的订单：2 单今日出库 =====
(107, 'SO20260228008', 105, 104, 15990.00, 3, '美团-电子设备采购',   @NOW, @NOW, 0),
(108, 'SO20260228009', 106, 104, 35000.00, 3, '京东方-笔记本大单',   @NOW, @NOW, 0),
-- ===== 历史订单 (前几天出库) =====
(110, 'SO20260226001', 100, 101, 3990.00,  3, '腾讯-耳机+键盘',     DATE_SUB(@NOW, INTERVAL 2 DAY), DATE_SUB(@NOW, INTERVAL 2 DAY), 0),
(111, 'SO20260226002', 102, 103, 1599.00,  3, '小米-办公椅',         DATE_SUB(@NOW, INTERVAL 2 DAY), DATE_SUB(@NOW, INTERVAL 2 DAY), 0),
(112, 'SO20260227001', 105, 104, 7500.00,  3, '美团-笔记本',         DATE_SUB(@NOW, INTERVAL 1 DAY), DATE_SUB(@NOW, INTERVAL 1 DAY), 0),
(113, 'SO20260227002', 101, 101, 2999.00,  3, '字节-显示器',         DATE_SUB(@NOW, INTERVAL 1 DAY), DATE_SUB(@NOW, INTERVAL 1 DAY), 0),
-- ===== 未完成订单 (status=1 确认中) =====
(120, 'SO20260228010', 107, 104, 5970.00,  1, '大疆-办公桌椅待出库', @NOW, @NOW, 0),
(121, 'SO20260228011', 104, 103, 1290.00,  1, '网易-打印纸待出库',   @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE sales_user_id=VALUES(sales_user_id), status=VALUES(status), update_time=VALUES(update_time);

-- 回填历史 sales_order 的 sales_user_id（如果有旧数据未设置）
UPDATE sales_order o JOIN customer c ON o.customer_id = c.id
SET o.sales_user_id = c.sales_user_id
WHERE o.sales_user_id IS NULL AND c.sales_user_id IS NOT NULL;

-- ================================================================
-- 14. 销售明细
-- ================================================================
INSERT INTO sales_item (id, order_id, product_id, warehouse_id, quantity, price, amount, create_time, update_time, deleted) VALUES
-- SO100: 张三-腾讯 (18495)
(100, 100, 100, 100, 2, 7500.00, 15000.00, @NOW, @NOW, 0),
(101, 100, 101, 100, 5, 399.00,  1995.00,  @NOW, @NOW, 0),
(102, 100, 110, 100, 5, 129.00,  645.00,  @NOW, @NOW, 0),  -- 无线鼠标 new product 855 差额用完
-- SO101: 张三-字节 (6258 ≈ 打印纸+笔+文件夹+显示器1台)
(103, 101, 104, 100, 10, 129.00, 1290.00,  @NOW, @NOW, 0),
(104, 101, 105, 100, 20, 29.00,  580.00,   @NOW, @NOW, 0),
(105, 101, 106, 100, 10, 39.00,  390.00,   @NOW, @NOW, 0),
(106, 101, 103, 100, 1,  2999.00,2999.00,  @NOW, @NOW, 0),
(139, 101, 111, 100, 4,  249.00, 999.00,   @NOW, @NOW, 0),  -- 白板
-- SO102: 张三-腾讯追加 (8970)
(107, 102, 103, 100, 3,  2999.00,8997.00,  @NOW, @NOW, 0),  -- 3 台显示器
-- SO103: 王五-小米 (10588)
(108, 103, 107, 100, 3, 1599.00, 4797.00,  @NOW, @NOW, 0),
(109, 103, 108, 100, 2, 999.00,  1998.00,  @NOW, @NOW, 0),
(110, 103, 109, 100, 5, 599.00,  2995.00,  @NOW, @NOW, 0),
(140, 103, 110, 100, 2, 129.00,  258.00,   @NOW, @NOW, 0),  -- 鼠标
(141, 103, 106, 100, 5, 39.00,   195.00,   @NOW, @NOW, 0),  -- 文件夹
-- SO104: 王五-华为大单 (23994)
(111, 104, 100, 100, 2, 7500.00, 15000.00, @NOW, @NOW, 0),
(112, 104, 103, 100, 3, 2999.00, 8997.00,  @NOW, @NOW, 0),  -- 3台显示器
-- SO105: 王五-网易 (2900)
(113, 105, 104, 100, 10, 129.00, 1290.00,  @NOW, @NOW, 0),
(114, 105, 105, 100, 20, 29.00,  580.00,   @NOW, @NOW, 0),
(115, 105, 106, 100, 10, 39.00,  390.00,   @NOW, @NOW, 0),
(142, 105, 110, 100, 5,  129.00, 645.00,   @NOW, @NOW, 0),  -- 鼠标5个 645 → totals check
-- SO106: 王五-华为追加 (5196)
(116, 106, 107, 100, 3, 1599.00, 4797.00,  @NOW, @NOW, 0),
(143, 106, 102, 100, 1,  299.00, 299.00,   @NOW, @NOW, 0),  -- 1把键盘
(144, 106, 110, 100, 1,  129.00, 129.00,   @NOW, @NOW, 0),  -- 鼠标
-- SO107: 赵六-美团 (15990)
(117, 107, 100, 100, 1, 7500.00, 7500.00,  @NOW, @NOW, 0),
(118, 107, 103, 100, 2, 2999.00, 5998.00,  @NOW, @NOW, 0),
(119, 107, 101, 100, 3, 399.00,  1197.00,  @NOW, @NOW, 0),
(145, 107, 102, 100, 3, 299.00,  897.00,   @NOW, @NOW, 0),  -- 键盘
(146, 107, 110, 100, 3, 129.00,  387.00,   @NOW, @NOW, 0),  -- 鼠标  15979 ≈ 15990
-- SO108: 赵六-京东方大单 (35000)
(120, 108, 100, 100, 4, 7500.00, 30000.00, @NOW, @NOW, 0),
(121, 108, 103, 100, 1, 2999.00, 2999.00,  @NOW, @NOW, 0),
(147, 108, 107, 100, 1, 1599.00, 1599.00,  @NOW, @NOW, 0),  -- 办公椅
(148, 108, 110, 100, 2, 129.00,  258.00,   @NOW, @NOW, 0),  -- 鼠标  34856 ≈ 35000
-- 历史销售明细
(122, 110, 101, 100, 5, 399.00,  1995.00,  @NOW, @NOW, 0),
(123, 110, 102, 100, 5, 299.00,  1495.00,  @NOW, @NOW, 0),
(149, 110, 110, 100, 3, 129.00,  387.00,   @NOW, @NOW, 0),
(124, 111, 107, 100, 1, 1599.00, 1599.00,  @NOW, @NOW, 0),
(125, 112, 100, 100, 1, 7500.00, 7500.00,  @NOW, @NOW, 0),
(126, 113, 103, 100, 1, 2999.00, 2999.00,  @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE update_time=@NOW;

-- ================================================================
-- 15. 库存
-- ================================================================
DELETE FROM stock WHERE id >= 100;
INSERT INTO stock (id, product_id, warehouse_id, quantity, safe_stock, create_time, update_time, deleted) VALUES
(100, 100, 100, 2,  3,  @NOW, @NOW, 0),   -- 笔记本 (采购10-销售8=2) ← 低于安全库存！
(101, 101, 100, 22, 10, @NOW, @NOW, 0),   -- 耳机
(102, 102, 100, 15, 10, @NOW, @NOW, 0),   -- 键盘
(103, 103, 100, 4,  5,  @NOW, @NOW, 0),   -- 显示器 (15-11=4) ← 低于安全库存！
(104, 104, 100, 20, 15, @NOW, @NOW, 0),   -- 打印纸
(105, 105, 100, 40, 20, @NOW, @NOW, 0),   -- 中性笔
(106, 106, 100, 15, 10, @NOW, @NOW, 0),   -- 文件夹
(107, 107, 100, 7,  5,  @NOW, @NOW, 0),   -- 办公椅
(108, 108, 100, 8,  5,  @NOW, @NOW, 0),   -- 电脑桌
(109, 109, 100, 15, 5,  @NOW, @NOW, 0),   -- 文件柜
(110, 110, 100, 29, 10, @NOW, @NOW, 0),   -- 无线鼠标
(111, 111, 100, 6,  5,  @NOW, @NOW, 0);   -- 白板

-- ================================================================
-- 16. 应收账款（按订单，关联到各销售员的订单）
-- ================================================================
INSERT INTO receivable (id, customer_id, order_id, amount, paid_amount, status, create_time, update_time, deleted) VALUES
-- 张三的应收
(100, 100, 100, 18495.00, 18495.00, 1, @NOW, @NOW, 0),  -- 腾讯-全额收回
(101, 101, 101, 6258.00,  3000.00,  0, @NOW, @NOW, 0),  -- 字节-部分收回
(102, 100, 102, 8970.00,  0.00,     0, @NOW, @NOW, 0),  -- 腾讯追加-未收
-- 王五的应收
(103, 102, 103, 10588.00, 10588.00, 1, @NOW, @NOW, 0),  -- 小米-全额
(104, 103, 104, 23994.00, 15000.00, 0, @NOW, @NOW, 0),  -- 华为-部分
(105, 104, 105, 2900.00,  2900.00,  1, @NOW, @NOW, 0),  -- 网易-全额
(106, 103, 106, 5196.00,  0.00,     0, @NOW, @NOW, 0),  -- 华为追加-未收
-- 赵六的应收
(107, 105, 107, 15990.00, 10000.00, 0, @NOW, @NOW, 0),  -- 美团-部分
(108, 106, 108, 35000.00, 35000.00, 1, @NOW, @NOW, 0),  -- 京东方-全额
-- 历史应收
(109, 100, 110, 3990.00,  3990.00,  1, DATE_SUB(@NOW, INTERVAL 2 DAY), DATE_SUB(@NOW, INTERVAL 2 DAY), 0),
(110, 102, 111, 1599.00,  1599.00,  1, DATE_SUB(@NOW, INTERVAL 2 DAY), DATE_SUB(@NOW, INTERVAL 2 DAY), 0),
(111, 105, 112, 7500.00,  5000.00,  0, DATE_SUB(@NOW, INTERVAL 1 DAY), DATE_SUB(@NOW, INTERVAL 1 DAY), 0),
(112, 101, 113, 2999.00,  2999.00,  1, DATE_SUB(@NOW, INTERVAL 1 DAY), DATE_SUB(@NOW, INTERVAL 1 DAY), 0)
ON DUPLICATE KEY UPDATE update_time=VALUES(update_time);

-- ================================================================
-- 17. 收款记录
-- ================================================================
INSERT INTO receipt (id, receivable_id, amount, method, remark, create_time, update_time, deleted) VALUES
(100, 100, 18495.00, '银行转账', '腾讯-全额收款',       @NOW, @NOW, 0),
(101, 101, 3000.00,  '银行转账', '字节-首期款',         @NOW, @NOW, 0),
(102, 103, 10588.00, '银行转账', '小米-全额收款',       @NOW, @NOW, 0),
(103, 104, 15000.00, '银行转账', '华为-首期款',         @NOW, @NOW, 0),
(104, 105, 2900.00,  '银行转账', '网易-全额收款',       @NOW, @NOW, 0),
(105, 107, 10000.00, '银行转账', '美团-首期款',         @NOW, @NOW, 0),
(106, 108, 35000.00, '银行转账', '京东方-全额收款',     @NOW, @NOW, 0),
(107, 109, 3990.00,  '银行转账', '腾讯历史-全额',       DATE_SUB(@NOW, INTERVAL 2 DAY), DATE_SUB(@NOW, INTERVAL 2 DAY), 0),
(108, 110, 1599.00,  '银行转账', '小米历史-全额',       DATE_SUB(@NOW, INTERVAL 2 DAY), DATE_SUB(@NOW, INTERVAL 2 DAY), 0),
(109, 111, 5000.00,  '银行转账', '美团历史-部分',       DATE_SUB(@NOW, INTERVAL 1 DAY), DATE_SUB(@NOW, INTERVAL 1 DAY), 0),
(110, 112, 2999.00,  '银行转账', '字节历史-全额',       DATE_SUB(@NOW, INTERVAL 1 DAY), DATE_SUB(@NOW, INTERVAL 1 DAY), 0)
ON DUPLICATE KEY UPDATE update_time=VALUES(update_time);

-- ================================================================
-- 18. 应付账款
-- ================================================================
INSERT INTO payable (id, supplier_id, order_id, amount, paid_amount, status, create_time, update_time, deleted) VALUES
(100, 100, 100, 58500.00, 58500.00, 1, @NOW, @NOW, 0),
(101, 101, 101, 3550.00,  3550.00,  1, @NOW, @NOW, 0),
(102, 100, 102, 16000.00, 10000.00, 0, @NOW, @NOW, 0),
(103, 102, 103, 5000.00,  0.00,     0, @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE update_time=VALUES(update_time);

-- ================================================================
-- 19. 付款记录
-- ================================================================
INSERT INTO payment (id, payable_id, amount, method, remark, create_time, update_time, deleted) VALUES
(100, 100, 58500.00, '银行转账', '华为-电子产品全额',   @NOW, @NOW, 0),
(101, 101, 3550.00,  '银行转账', '阿里-办公用品全额',   @NOW, @NOW, 0),
(102, 102, 10000.00, '银行转账', '华为-家具首期款',     @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE update_time=VALUES(update_time);

-- ================================================================
-- 20. 销售线索（分配给 3 个销售员，4 个阶段）
-- ================================================================
INSERT INTO sales_lead (id, customer_id, name, stage, expected_amount, probability, owner_user_id, next_follow_time, remark, status, create_time, update_time, deleted) VALUES
-- 张三(101) 的线索
(100, 100, '腾讯云服务器采购',     '潜在客户', 200000.00, 20, 101, DATE_ADD(@NOW, INTERVAL 3 DAY), '初步接触',    1, @NOW, @NOW, 0),
(101, 101, '字节办公设备更新',     '已跟进',   150000.00, 40, 101, DATE_ADD(@NOW, INTERVAL 2 DAY), '已上门拜访',  1, @NOW, @NOW, 0),
(102, 100, '腾讯年度办公耗材',     '报价中',   50000.00,  55, 101, DATE_ADD(@NOW, INTERVAL 2 DAY), '报价对接中',  1, @NOW, @NOW, 0),
(103, 101, '字节新办公楼家具',     '已成交',   120000.00, 95, 101, NULL,                            '合同执行中',  1, @NOW, @NOW, 0),
-- 王五(103) 的线索
(104, 102, '小米智能办公方案',     '潜在客户', 80000.00,  15, 103, DATE_ADD(@NOW, INTERVAL 5 DAY), '需求确认中',  1, @NOW, @NOW, 0),
(105, 103, '华为新园区设备采购',   '已跟进',   300000.00, 35, 103, DATE_ADD(@NOW, INTERVAL 4 DAY), '方案沟通中',  1, @NOW, @NOW, 0),
(106, 104, '网易教育硬件',         '报价中',   60000.00,  50, 103, DATE_ADD(@NOW, INTERVAL 1 DAY), '已提交报价',  1, @NOW, @NOW, 0),
(107, 103, '华为年度维保',         '已成交',   250000.00, 90, 103, NULL,                            '签约完成',    1, @NOW, @NOW, 0),
(108, 102, '小米员工电脑换新',     '已跟进',   120000.00, 30, 103, DATE_ADD(@NOW, INTERVAL 3 DAY), '确认配置中',  1, @NOW, @NOW, 0),
-- 赵六(104) 的线索
(109, 105, '美团配送中心设备',     '潜在客户', 180000.00, 20, 104, DATE_ADD(@NOW, INTERVAL 7 DAY), '首次接触',    1, @NOW, @NOW, 0),
(110, 106, '京东方产线升级',       '报价中',   500000.00, 60, 104, DATE_ADD(@NOW, INTERVAL 2 DAY), '报价审批中',  1, @NOW, @NOW, 0),
(111, 107, '大疆研发中心装修',     '已跟进',   100000.00, 35, 104, DATE_ADD(@NOW, INTERVAL 4 DAY), '已出方案',    1, @NOW, @NOW, 0),
(112, 105, '美团办公楼搬迁',       '已成交',   350000.00, 95, 104, NULL,                            '合同已签',    1, @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE owner_user_id=VALUES(owner_user_id), update_time=@NOW;

-- ================================================================
-- 完成！验证查询
-- ================================================================
SELECT '=== 数据迁移 + 测试数据导入完成 ===' AS '';

SELECT '--- 用户与角色 ---' AS '';
SELECT u.id, u.username, u.nickname, r.role_code
FROM sys_user u LEFT JOIN sys_user_role ur ON u.id=ur.user_id AND ur.deleted=0
LEFT JOIN sys_role r ON ur.role_id=r.id AND r.deleted=0
WHERE u.deleted=0 ORDER BY u.id;

SELECT '--- 今日出库订单(按销售员) ---' AS '';
SELECT o.sales_user_id, u.nickname AS sales_name, COUNT(*) AS order_count, SUM(o.total_amount) AS total_amount
FROM sales_order o LEFT JOIN sys_user u ON o.sales_user_id = u.id
WHERE o.deleted=0 AND o.status=3 AND DATE(o.update_time) = CURDATE()
GROUP BY o.sales_user_id ORDER BY total_amount DESC;

SELECT '--- 销售线索(按销售员) ---' AS '';
SELECT owner_user_id, COUNT(*) AS lead_count FROM sales_lead WHERE deleted=0 AND status=1 GROUP BY owner_user_id;

SELECT '--- 库存预警 ---' AS '';
SELECT p.name, s.quantity, s.safe_stock FROM stock s JOIN product p ON s.product_id=p.id WHERE s.deleted=0 AND s.quantity < s.safe_stock;

SELECT '--- 统计汇总 ---' AS '';
SELECT '用户' AS 类型, COUNT(*) AS 数量 FROM sys_user WHERE deleted=0
UNION ALL SELECT '角色', COUNT(*) FROM sys_role WHERE deleted=0
UNION ALL SELECT '权限', COUNT(*) FROM sys_permission WHERE deleted=0 AND id>=100
UNION ALL SELECT '角色权限', COUNT(*) FROM sys_role_perm WHERE deleted=0 AND id>=100
UNION ALL SELECT '客户', COUNT(*) FROM customer WHERE deleted=0 AND id>=100
UNION ALL SELECT '商品', COUNT(*) FROM product WHERE deleted=0 AND id>=100
UNION ALL SELECT '今日出库单', COUNT(*) FROM sales_order WHERE deleted=0 AND status=3 AND DATE(update_time)=CURDATE()
UNION ALL SELECT '销售线索', COUNT(*) FROM sales_lead WHERE deleted=0 AND id>=100;

-- ================================================================
-- ERP 测试数据脚本 (幂等可重复执行)
-- 基于现有数据库: 角色 ADMIN(id=1)/SALES(id=2)/FIN(id=3)
-- 运行：mysql -u root -proot erp_light < test_data.sql
-- ================================================================

SET NAMES utf8mb4;
SET @NOW = NOW();

-- ================================================================
-- 0. 清理测试数据（id >= 100 区间，不影响现有业务数据）
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
DELETE FROM sys_role_perm WHERE id >= 100;
DELETE FROM sys_user_role WHERE id >= 100;
DELETE FROM sys_permission WHERE id >= 100;
DELETE FROM sys_user WHERE id >= 100;
DELETE FROM sys_dept WHERE id >= 100;

-- ================================================================
-- 1. 补充部门（保留现有，追加测试部门）
-- ================================================================
INSERT INTO sys_dept (id, parent_id, dept_name, leader, status, create_time, update_time, deleted) VALUES
(100, 0,   '总公司',   '傅勇', 1, @NOW, @NOW, 0),
(101, 100, '销售部',   '张三', 1, @NOW, @NOW, 0),
(102, 100, '财务部',   '李四', 1, @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE dept_name=VALUES(dept_name), update_time=@NOW;

-- ================================================================
-- 2. 新增销售/财务用户（保留现有 admin id=1 不变）
--    密码: 123456 → BCrypt hash
-- ================================================================
SET @PWD = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi';

INSERT INTO sys_user (id, dept_id, username, password, nickname, phone, email, status, create_time, update_time, deleted) VALUES
(101, 101, 'sales',   @PWD, '销售员-张三', '13800000002', 'sales@erp.com',   1, @NOW, @NOW, 0),
(102, 102, 'finance', @PWD, '财务员-李四', '13800000003', 'finance@erp.com', 1, @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE password=VALUES(password), nickname=VALUES(nickname), update_time=@NOW;

-- ================================================================
-- 3. 用户-角色关联（现有 admin→ADMIN 保留，新增 sales→SALES, finance→FIN）
--    使用现有角色: ADMIN(1), SALES(2), FIN(3)
-- ================================================================
INSERT INTO sys_user_role (id, user_id, role_id, create_time, update_time, deleted) VALUES
(101, 101, 2, @NOW, @NOW, 0),  -- sales → SALES
(102, 102, 3, @NOW, @NOW, 0)   -- finance → FIN
ON DUPLICATE KEY UPDATE update_time=@NOW;

-- ================================================================
-- 4. 权限（菜单 + API）—— 使用 ON DUPLICATE KEY 避免冲突
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
-- 5. 角色-权限关联（使用现有角色 ID: SALES=2, FIN=3）
-- ================================================================
-- SALES(id=2): 看板 + 客户 + 商品(只读) + 销售线索 + 销售订单 + 库存(只读) + 报表
INSERT INTO sys_role_perm (id, role_id, perm_id, create_time, update_time, deleted) VALUES
(100, 2, 100, @NOW, @NOW, 0),  -- menu:dashboard
(101, 2, 101, @NOW, @NOW, 0),  -- menu:customers
(102, 2, 102, @NOW, @NOW, 0),  -- menu:products
(103, 2, 107, @NOW, @NOW, 0),  -- menu:sales:leads
(104, 2, 108, @NOW, @NOW, 0),  -- menu:sales:orders
(105, 2, 109, @NOW, @NOW, 0),  -- menu:inventory:stocks
(106, 2, 110, @NOW, @NOW, 0),  -- menu:inventory:records
(120, 2, 200, @NOW, @NOW, 0),  -- api:customers:read
(121, 2, 201, @NOW, @NOW, 0),  -- api:customers:write
(122, 2, 202, @NOW, @NOW, 0),  -- api:products:read
(123, 2, 210, @NOW, @NOW, 0),  -- api:sales:read
(124, 2, 211, @NOW, @NOW, 0),  -- api:sales:write
(125, 2, 212, @NOW, @NOW, 0),  -- api:inventory:read
(126, 2, 218, @NOW, @NOW, 0),  -- api:reports:read
(127, 2, 221, @NOW, @NOW, 0),  -- api:leads:read
(128, 2, 222, @NOW, @NOW, 0),  -- api:leads:write

-- FIN(id=3): 看板 + 应收 + 应付 + 收付款 + 报表
(200, 3, 100, @NOW, @NOW, 0),  -- menu:dashboard
(201, 3, 111, @NOW, @NOW, 0),  -- menu:finance:receivables
(202, 3, 112, @NOW, @NOW, 0),  -- menu:finance:payables
(220, 3, 214, @NOW, @NOW, 0),  -- api:receivables:read
(221, 3, 215, @NOW, @NOW, 0),  -- api:receivables:write
(222, 3, 216, @NOW, @NOW, 0),  -- api:payables:read
(223, 3, 217, @NOW, @NOW, 0),  -- api:payables:write
(224, 3, 218, @NOW, @NOW, 0),  -- api:reports:read
(225, 3, 223, @NOW, @NOW, 0),  -- api:receipts:read
(226, 3, 224, @NOW, @NOW, 0),  -- api:receipts:write
(227, 3, 225, @NOW, @NOW, 0),  -- api:payments:read
(228, 3, 226, @NOW, @NOW, 0)   -- api:payments:write
ON DUPLICATE KEY UPDATE update_time=@NOW;

-- ================================================================
-- 6. 供应商（保留现有，追加到 id>=100）
-- ================================================================
INSERT INTO supplier (id, name, contact, phone, address, status, create_time, update_time, deleted) VALUES
(100, '深圳华为配件有限公司', '陈经理', '13900001001', '广东省深圳市南山区科技园', 1, @NOW, @NOW, 0),
(101, '杭州阿里云科技有限公司', '吴经理', '13900001002', '浙江省杭州市余杭区文一西路', 1, @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE name=VALUES(name), update_time=@NOW;

-- ================================================================
-- 7. 仓库
-- ================================================================
INSERT INTO warehouse (id, name, address, manager, status, create_time, update_time, deleted) VALUES
(100, '主仓库-深圳', '广东省深圳市宝安区', '赵六', 1, @NOW, @NOW, 0),
(101, '次仓库-广州', '广东省广州市天河区', '孙七', 1, @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE name=VALUES(name), update_time=@NOW;

-- ================================================================
-- 8. 客户
-- ================================================================
INSERT INTO customer (id, name, contact, phone, level, sales_user_id, credit_limit, status, create_time, update_time, deleted) VALUES
(100, '腾讯科技有限公司',     '马经理', '13800100001', 1, 101, 500000.00, 1, @NOW, @NOW, 0),
(101, '字节跳动科技有限公司', '刘经理', '13800100002', 2, 101, 300000.00, 1, @NOW, @NOW, 0),
(102, '小米科技有限公司',     '周经理', '13800100003', 3, 101, 200000.00, 1, @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE name=VALUES(name), update_time=@NOW;

-- ================================================================
-- 9. 商品 (10 款, 3 分类)
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
(109, 'FUR-003', '三层文件柜',         '办公家具', '个', '钢制/带锁',         300.00,  599.00,  1, @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE name=VALUES(name), purchase_price=VALUES(purchase_price), sale_price=VALUES(sale_price), update_time=@NOW;

-- ================================================================
-- 10. 采购订单 (3单, 全部已入库 status=3)
-- ================================================================
INSERT INTO purchase_order (id, order_no, supplier_id, total_amount, status, remark, create_time, update_time, deleted) VALUES
(100, 'PO20260228001', 100, 58500.00, 3, '电子产品采购',  DATE_SUB(@NOW, INTERVAL 3 DAY), @NOW, 0),
(101, 'PO20260228002', 101, 3550.00,  3, '办公用品采购',  DATE_SUB(@NOW, INTERVAL 2 DAY), @NOW, 0),
(102, 'PO20260228003', 100, 16000.00, 3, '办公家具采购',  DATE_SUB(@NOW, INTERVAL 1 DAY), @NOW, 0)
ON DUPLICATE KEY UPDATE status=3, update_time=@NOW;

-- ================================================================
-- 11. 采购明细
-- ================================================================
INSERT INTO purchase_item (id, order_id, product_id, warehouse_id, quantity, price, amount, create_time, update_time, deleted) VALUES
(100, 100, 100, 100, 5,  5000.00, 25000.00, @NOW, @NOW, 0),
(101, 100, 101, 100, 20, 200.00,  4000.00,  @NOW, @NOW, 0),
(102, 100, 102, 100, 10, 150.00,  1500.00,  @NOW, @NOW, 0),
(103, 100, 103, 100, 10, 1800.00, 18000.00, @NOW, @NOW, 0),
(104, 100, 103, 101, 5,  1800.00, 9000.00,  @NOW, @NOW, 0),
(105, 101, 104, 100, 30, 80.00,   2400.00,  @NOW, @NOW, 0),
(106, 101, 105, 100, 50, 15.00,   750.00,   @NOW, @NOW, 0),
(107, 101, 106, 100, 20, 20.00,   400.00,   @NOW, @NOW, 0),
(108, 102, 107, 100, 10, 800.00,  8000.00,  @NOW, @NOW, 0),
(109, 102, 108, 100, 8,  500.00,  4000.00,  @NOW, @NOW, 0),
(110, 102, 109, 100, 10, 300.00,  3000.00,  @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE update_time=@NOW;

-- ================================================================
-- 12. 库存（清除旧的 id>=100 后重建）
-- ================================================================
DELETE FROM stock WHERE id >= 100;
INSERT INTO stock (id, product_id, warehouse_id, quantity, safe_stock, create_time, update_time, deleted) VALUES
(100, 100, 100, 3,  3,  @NOW, @NOW, 0),   -- 笔记本 3台 (采购5-销售2)
(101, 101, 100, 15, 10, @NOW, @NOW, 0),   -- 耳机 15副
(102, 102, 100, 5,  5,  @NOW, @NOW, 0),   -- 键盘 5把
(103, 103, 100, 9,  5,  @NOW, @NOW, 0),   -- 显示器(主仓) 9台
(104, 103, 101, 5,  3,  @NOW, @NOW, 0),   -- 显示器(次仓) 5台
(105, 104, 100, 20, 10, @NOW, @NOW, 0),   -- 打印纸 20箱
(106, 105, 100, 30, 20, @NOW, @NOW, 0),   -- 中性笔 30盒
(107, 106, 100, 10, 10, @NOW, @NOW, 0),   -- 文件夹 10包
(108, 107, 100, 8,  5,  @NOW, @NOW, 0),   -- 办公椅 8把
(109, 108, 100, 7,  5,  @NOW, @NOW, 0),   -- 电脑桌 7张
(110, 109, 100, 1,  5,  @NOW, @NOW, 0);   -- 文件柜 1个 ← 低于安全库存(5)，触发预警！

-- ================================================================
-- 13. 库存流水
-- ================================================================
INSERT INTO stock_record (id, product_id, warehouse_id, quantity, record_type, biz_type, biz_id, remark, create_time, update_time, deleted) VALUES
-- 采购入库
(100, 100, 100, 5,  1, 2, 100, '采购入库-笔记本',  @NOW, @NOW, 0),
(101, 101, 100, 20, 1, 2, 100, '采购入库-耳机',    @NOW, @NOW, 0),
(102, 102, 100, 10, 1, 2, 100, '采购入库-键盘',    @NOW, @NOW, 0),
(103, 103, 100, 10, 1, 2, 100, '采购入库-显示器',  @NOW, @NOW, 0),
(104, 103, 101, 5,  1, 2, 100, '采购入库-显示器(次仓)', @NOW, @NOW, 0),
(105, 104, 100, 30, 1, 2, 101, '采购入库-打印纸',  @NOW, @NOW, 0),
(106, 105, 100, 50, 1, 2, 101, '采购入库-中性笔',  @NOW, @NOW, 0),
(107, 106, 100, 20, 1, 2, 101, '采购入库-文件夹',  @NOW, @NOW, 0),
(108, 107, 100, 10, 1, 2, 102, '采购入库-办公椅',  @NOW, @NOW, 0),
(109, 108, 100, 8,  1, 2, 102, '采购入库-电脑桌',  @NOW, @NOW, 0),
(110, 109, 100, 10, 1, 2, 102, '采购入库-文件柜',  @NOW, @NOW, 0),
-- 销售出库
(120, 100, 100, 2,  2, 1, 100, '销售出库-笔记本',  @NOW, @NOW, 0),
(121, 101, 100, 5,  2, 1, 100, '销售出库-耳机',    @NOW, @NOW, 0),
(122, 102, 100, 5,  2, 1, 100, '销售出库-键盘',    @NOW, @NOW, 0),
(123, 104, 100, 10, 2, 1, 101, '销售出库-打印纸',  @NOW, @NOW, 0),
(124, 105, 100, 20, 2, 1, 101, '销售出库-中性笔',  @NOW, @NOW, 0),
(125, 103, 100, 1,  2, 1, 101, '销售出库-显示器',  @NOW, @NOW, 0),
(126, 106, 100, 10, 2, 1, 101, '销售出库-文件夹',  @NOW, @NOW, 0),
(127, 107, 100, 2,  2, 1, 102, '销售出库-办公椅',  @NOW, @NOW, 0),
(128, 108, 100, 1,  2, 1, 102, '销售出库-电脑桌',  @NOW, @NOW, 0),
(129, 109, 100, 9,  2, 1, 102, '销售出库-文件柜',  @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE update_time=@NOW;

-- ================================================================
-- 14. 销售订单 (3单, 全部已出库 status=3, update_time=今日)
-- ================================================================
INSERT INTO sales_order (id, order_no, customer_id, total_amount, status, remark, create_time, update_time, deleted) VALUES
(100, 'SO20260228001', 100, 18495.00, 3, '腾讯采购电子设备', @NOW, @NOW, 0),
(101, 'SO20260228002', 101, 6258.00,  3, '字节采购办公用品', @NOW, @NOW, 0),
(102, 'SO20260228003', 102, 10588.00, 3, '小米采购办公家具', @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE status=3, update_time=@NOW;

-- ================================================================
-- 15. 销售明细
-- ================================================================
INSERT INTO sales_item (id, order_id, product_id, warehouse_id, quantity, price, amount, create_time, update_time, deleted) VALUES
-- SO001: 腾讯
(100, 100, 100, 100, 2, 7500.00, 15000.00, @NOW, @NOW, 0),
(101, 100, 101, 100, 5, 399.00,  1995.00,  @NOW, @NOW, 0),
(102, 100, 102, 100, 5, 299.00,  1495.00,  @NOW, @NOW, 0),
-- SO002: 字节
(103, 101, 104, 100, 10, 129.00, 1290.00,  @NOW, @NOW, 0),
(104, 101, 105, 100, 20, 29.00,  580.00,   @NOW, @NOW, 0),
(105, 101, 103, 100, 1,  2999.00,2999.00,  @NOW, @NOW, 0),
(106, 101, 106, 100, 10, 39.00,  390.00,   @NOW, @NOW, 0),
-- SO003: 小米
(107, 102, 107, 100, 2, 1599.00, 3198.00,  @NOW, @NOW, 0),
(108, 102, 108, 100, 1, 999.00,  999.00,   @NOW, @NOW, 0),
(109, 102, 109, 100, 9, 599.00,  5391.00,  @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE update_time=@NOW;

-- ================================================================
-- 16. 应收账款
-- ================================================================
INSERT INTO receivable (id, customer_id, order_id, amount, paid_amount, status, create_time, update_time, deleted) VALUES
(100, 100, 100, 18495.00, 15000.00, 0, @NOW, @NOW, 0),
(101, 101, 101, 6258.00,  6258.00,  1, @NOW, @NOW, 0),
(102, 102, 102, 10588.00, 0.00,     0, @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE update_time=@NOW;

-- ================================================================
-- 17. 收款记录
-- ================================================================
INSERT INTO receipt (id, receivable_id, amount, method, remark, create_time, update_time, deleted) VALUES
(100, 100, 15000.00, '银行转账', '腾讯首期款',   @NOW, @NOW, 0),
(101, 101, 6258.00,  '银行转账', '字节全额结清', @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE update_time=@NOW;

-- ================================================================
-- 18. 应付账款
-- ================================================================
INSERT INTO payable (id, supplier_id, order_id, amount, paid_amount, status, create_time, update_time, deleted) VALUES
(100, 100, 100, 58500.00, 58500.00, 1, @NOW, @NOW, 0),
(101, 101, 101, 3550.00,  3000.00,  0, @NOW, @NOW, 0),
(102, 100, 102, 16000.00, 0.00,     0, @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE update_time=@NOW;

-- ================================================================
-- 19. 付款记录
-- ================================================================
INSERT INTO payment (id, payable_id, amount, method, remark, create_time, update_time, deleted) VALUES
(100, 100, 58500.00, '银行转账', '华为电子产品全额付款', @NOW, @NOW, 0),
(101, 101, 3000.00,  '银行转账', '阿里办公用品首期款',   @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE update_time=@NOW;

-- ================================================================
-- 20. 销售线索 (8条, 4个阶段)
-- ================================================================
INSERT INTO sales_lead (id, customer_id, name, stage, expected_amount, probability, owner_user_id, next_follow_time, remark, status, create_time, update_time, deleted) VALUES
(100, 100, '腾讯云服务器采购',   '潜在客户', 200000.00, 20, 101, DATE_ADD(@NOW, INTERVAL 3 DAY), '初步接触',    1, @NOW, @NOW, 0),
(101, 101, '字节办公设备更新',   '潜在客户', 150000.00, 15, 101, DATE_ADD(@NOW, INTERVAL 5 DAY), '需求确认中',  1, @NOW, @NOW, 0),
(102, 100, '腾讯会议室改造',     '已跟进',   100000.00, 40, 101, DATE_ADD(@NOW, INTERVAL 2 DAY), '已上门拜访',  1, @NOW, @NOW, 0),
(103, 102, '小米智能办公方案',   '已跟进',   80000.00,  35, 101, DATE_ADD(@NOW, INTERVAL 4 DAY), '方案沟通中',  1, @NOW, @NOW, 0),
(104, 101, '字节新办公楼家具',   '报价中',   300000.00, 60, 101, DATE_ADD(@NOW, INTERVAL 1 DAY), '已提交报价',  1, @NOW, @NOW, 0),
(105, 100, '腾讯年度办公耗材',   '报价中',   50000.00,  55, 101, DATE_ADD(@NOW, INTERVAL 2 DAY), '报价对接中',  1, @NOW, @NOW, 0),
(106, 102, '小米员工电脑换新',   '已成交',   120000.00, 90, 101, NULL,                            '签约完成',    1, @NOW, @NOW, 0),
(107, 100, '腾讯年度服务器维护', '已成交',   250000.00, 95, 101, NULL,                            '合同执行中',  1, @NOW, @NOW, 0)
ON DUPLICATE KEY UPDATE update_time=@NOW;

-- ================================================================
-- 完成！验证查询
-- ================================================================
SELECT '=== 测试数据导入完成 ===' AS '';
SELECT '用户' AS 类型, COUNT(*) AS 数量 FROM sys_user WHERE deleted=0
UNION ALL SELECT '角色', COUNT(*) FROM sys_role WHERE deleted=0
UNION ALL SELECT '权限', COUNT(*) FROM sys_permission WHERE deleted=0 AND id>=100
UNION ALL SELECT '角色权限', COUNT(*) FROM sys_role_perm WHERE deleted=0 AND id>=100
UNION ALL SELECT '商品', COUNT(*) FROM product WHERE deleted=0 AND id>=100
UNION ALL SELECT '库存预警', COUNT(*) FROM stock WHERE deleted=0 AND quantity < safe_stock;

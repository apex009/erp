USE erp_light;

SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO sys_dept (id, parent_id, dept_name, leader, status, deleted)
VALUES
  (1, NULL, '系统管理', '系统', 1, 0),
  (2, 1, '销售部', '张销售', 1, 0),
  (3, 1, '财务部', '李财务', 1, 0);

INSERT INTO sys_role (id, role_code, role_name, status, remark, deleted)
VALUES
  (1, 'ADMIN', '管理员', 1, '', 0),
  (2, 'SALES', '销售', 1, '', 0),
  (3, 'FIN', '财务', 1, '', 0);

INSERT INTO customer (id, name, contact, phone, level, sales_user_id, credit_limit, status, deleted)
VALUES
  (1, '华北客户', '王强', '13800000001', 1, NULL, 5000.00, 1, 0),
  (2, '华中客户', '李敏', '13800000002', 2, NULL, 8000.00, 1, 0),
  (3, '华南客户', '赵丽', '13800000003', 1, NULL, 3000.00, 1, 0);

INSERT INTO supplier (id, name, contact, phone, address, status, deleted)
VALUES
  (1, '华北供应商', '陈伟', '13900000001', '一号路', 1, 0),
  (2, '华中供应商', '刘洋', '13900000002', '二号路', 1, 0);

INSERT INTO product (id, sku, name, category, unit, spec, purchase_price, sale_price, status, deleted)
VALUES
  (1, 'P-001', 'A4复印纸', '办公', '包', '500张', 10.00, 15.00, 1, 0),
  (2, 'P-002', '打印墨盒', '办公', '盒', '黑色', 20.00, 30.00, 1, 0),
  (3, 'P-003', 'USB数据线', '配件', '条', '1米', 5.00, 9.00, 1, 0),
  (4, 'P-004', '无线鼠标', '配件', '个', '2.4G', 25.00, 40.00, 1, 0);

INSERT INTO warehouse (id, name, address, manager, status, deleted)
VALUES
  (1, '主仓库', '仓储路1号', '王仓库', 1, 0),
  (2, '备仓库', '仓储路2号', '李仓库', 1, 0);

INSERT INTO stock (id, product_id, warehouse_id, quantity, safe_stock, deleted)
VALUES
  (1, 1, 1, 100.0000, 20.0000, 0),
  (2, 2, 1, 50.0000, 10.0000, 0),
  (3, 3, 1, 200.0000, 30.0000, 0),
  (4, 4, 2, 30.0000, 5.0000, 0);

INSERT INTO purchase_order (id, order_no, supplier_id, total_amount, status, remark, deleted)
VALUES
  (1, 'PO202602010001', 1, 200.00, 0, '示例采购', 0);

INSERT INTO purchase_item (id, order_id, product_id, warehouse_id, quantity, price, amount, deleted)
VALUES
  (1, 1, 1, 1, 10.0000, 10.00, 100.00, 0),
  (2, 1, 2, 1, 5.0000, 20.00, 100.00, 0);

INSERT INTO sales_order (id, order_no, customer_id, total_amount, status, remark, deleted)
VALUES
  (1, 'SO202602010001', 1, 63.00, 0, '示例销售', 0);

INSERT INTO sales_item (id, order_id, product_id, warehouse_id, quantity, price, amount, deleted)
VALUES
  (1, 1, 1, 1, 3.0000, 15.00, 45.00, 0),
  (2, 1, 3, 1, 2.0000, 9.00, 18.00, 0);

INSERT INTO receivable (id, customer_id, order_id, amount, paid_amount, status, deleted)
VALUES
  (1, 1, 1, 63.00, 20.00, 1, 0);

INSERT INTO payable (id, supplier_id, order_id, amount, paid_amount, status, deleted)
VALUES
  (1, 1, 1, 200.00, 50.00, 1, 0);

INSERT INTO receipt (id, receivable_id, amount, method, remark, deleted)
VALUES
  (1, 1, 20.00, 'bank', '示例收款', 0);

INSERT INTO payment (id, payable_id, amount, method, remark, deleted)
VALUES
  (1, 1, 50.00, 'cash', '示例付款', 0);

INSERT INTO stock_record (id, product_id, warehouse_id, quantity, record_type, biz_type, biz_id, remark, deleted)
VALUES
  (1, 1, 1, 10.0000, 'IN', 'PURCHASE', 1, '示例采购入库', 0),
  (2, 2, 1, 5.0000, 'IN', 'PURCHASE', 1, '示例采购入库', 0),
  (3, 1, 1, 3.0000, 'OUT', 'SALE', 1, '示例销售出库', 0),
  (4, 3, 1, 2.0000, 'OUT', 'SALE', 1, '示例销售出库', 0);

SET FOREIGN_KEY_CHECKS = 1;

USE erp_light;

SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO sys_dept (id, parent_id, dept_name, leader, status, deleted)
VALUES
  (1, NULL, 'Admin', 'System', 1, 0),
  (2, 1, 'Sales', 'Alice', 1, 0),
  (3, 1, 'Finance', 'Bob', 1, 0);

INSERT INTO sys_role (id, role_code, role_name, status, remark, deleted)
VALUES
  (1, 'ADMIN', 'Administrator', 1, '', 0),
  (2, 'SALES', 'Sales', 1, '', 0),
  (3, 'FIN', 'Finance', 1, '', 0);

INSERT INTO customer (id, name, contact, phone, level, sales_user_id, credit_limit, status, deleted)
VALUES
  (1, 'Customer A', 'Tom', '13800000001', 1, NULL, 5000.00, 1, 0),
  (2, 'Customer B', 'Jerry', '13800000002', 2, NULL, 8000.00, 1, 0),
  (3, 'Customer C', 'Kate', '13800000003', 1, NULL, 3000.00, 1, 0);

INSERT INTO supplier (id, name, contact, phone, address, status, deleted)
VALUES
  (1, 'Supplier A', 'Sam', '13900000001', 'No.1 Road', 1, 0),
  (2, 'Supplier B', 'Lily', '13900000002', 'No.2 Road', 1, 0);

INSERT INTO product (id, sku, name, category, unit, spec, purchase_price, sale_price, status, deleted)
VALUES
  (1, 'P-001', 'Paper A4', 'Office', 'pack', '500 sheets', 10.00, 15.00, 1, 0),
  (2, 'P-002', 'Ink', 'Office', 'box', 'Black', 20.00, 30.00, 1, 0),
  (3, 'P-003', 'USB Cable', 'Accessory', 'pcs', '1m', 5.00, 9.00, 1, 0),
  (4, 'P-004', 'Mouse', 'Accessory', 'pcs', 'Wireless', 25.00, 40.00, 1, 0);

INSERT INTO warehouse (id, name, address, manager, status, deleted)
VALUES
  (1, 'Main Warehouse', 'Warehouse Road 1', 'Mike', 1, 0),
  (2, 'Backup Warehouse', 'Warehouse Road 2', 'Jane', 1, 0);

INSERT INTO stock (id, product_id, warehouse_id, quantity, safe_stock, deleted)
VALUES
  (1, 1, 1, 100.0000, 20.0000, 0),
  (2, 2, 1, 50.0000, 10.0000, 0),
  (3, 3, 1, 200.0000, 30.0000, 0),
  (4, 4, 2, 30.0000, 5.0000, 0);

INSERT INTO purchase_order (id, order_no, supplier_id, total_amount, status, remark, deleted)
VALUES
  (1, 'PO202602010001', 1, 200.00, 0, 'seed purchase', 0);

INSERT INTO purchase_item (id, order_id, product_id, warehouse_id, quantity, price, amount, deleted)
VALUES
  (1, 1, 1, 1, 10.0000, 10.00, 100.00, 0),
  (2, 1, 2, 1, 5.0000, 20.00, 100.00, 0);

INSERT INTO sales_order (id, order_no, customer_id, total_amount, status, remark, deleted)
VALUES
  (1, 'SO202602010001', 1, 63.00, 0, 'seed sales', 0);

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
  (1, 1, 20.00, 'bank', 'seed receipt', 0);

INSERT INTO payment (id, payable_id, amount, method, remark, deleted)
VALUES
  (1, 1, 50.00, 'cash', 'seed payment', 0);

INSERT INTO stock_record (id, product_id, warehouse_id, quantity, record_type, biz_type, biz_id, remark, deleted)
VALUES
  (1, 1, 1, 10.0000, 'IN', 'PURCHASE', 1, 'seed purchase in', 0),
  (2, 2, 1, 5.0000, 'IN', 'PURCHASE', 1, 'seed purchase in', 0),
  (3, 1, 1, 3.0000, 'OUT', 'SALE', 1, 'seed sale out', 0),
  (4, 3, 1, 2.0000, 'OUT', 'SALE', 1, 'seed sale out', 0);

SET FOREIGN_KEY_CHECKS = 1;

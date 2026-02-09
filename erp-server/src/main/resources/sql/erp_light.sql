CREATE DATABASE IF NOT EXISTS erp_light DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE erp_light;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS sys_role_perm;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_dept;

DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS receipt;
DROP TABLE IF EXISTS payable;
DROP TABLE IF EXISTS receivable;

DROP TABLE IF EXISTS sales_item;
DROP TABLE IF EXISTS sales_order;
DROP TABLE IF EXISTS purchase_item;
DROP TABLE IF EXISTS purchase_order;

DROP TABLE IF EXISTS stock_record;
DROP TABLE IF EXISTS stock;
DROP TABLE IF EXISTS warehouse;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS supplier;
DROP TABLE IF EXISTS customer;

CREATE TABLE sys_dept (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT NULL,
    dept_name VARCHAR(50) NOT NULL,
    leader VARCHAR(50) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dept_id BIGINT DEFAULT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    email VARCHAR(100) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(50) NOT NULL,
    status TINYINT DEFAULT 1,
    remark VARCHAR(255) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    KEY idx_user_role_user (user_id),
    KEY idx_user_role_role (role_id)
);

CREATE TABLE sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    perm_code VARCHAR(100) NOT NULL UNIQUE,
    perm_name VARCHAR(100) NOT NULL,
    type VARCHAR(20) DEFAULT NULL,
    path VARCHAR(200) DEFAULT NULL,
    method VARCHAR(20) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE TABLE sys_role_perm (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    perm_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    KEY idx_role_perm_role (role_id),
    KEY idx_role_perm_perm (perm_id)
);

CREATE TABLE customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    contact VARCHAR(50) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    level TINYINT DEFAULT 1,
    sales_user_id BIGINT DEFAULT NULL,
    credit_limit DECIMAL(18,2) DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE TABLE supplier (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    contact VARCHAR(50) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    address VARCHAR(200) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE TABLE product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sku VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) DEFAULT NULL,
    unit VARCHAR(20) DEFAULT NULL,
    spec VARCHAR(100) DEFAULT NULL,
    purchase_price DECIMAL(18,2) DEFAULT 0,
    sale_price DECIMAL(18,2) DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE TABLE warehouse (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(200) DEFAULT NULL,
    manager VARCHAR(50) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE TABLE stock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    quantity DECIMAL(18,4) DEFAULT 0,
    safe_stock DECIMAL(18,4) DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_stock_prod_wh (product_id, warehouse_id)
);

CREATE TABLE stock_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    quantity DECIMAL(18,4) NOT NULL,
    record_type VARCHAR(10) NOT NULL,
    biz_type VARCHAR(20) NOT NULL,
    biz_id BIGINT DEFAULT NULL,
    remark VARCHAR(200) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    KEY idx_stock_record_prod (product_id),
    KEY idx_stock_record_wh (warehouse_id)
);

CREATE TABLE purchase_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    supplier_id BIGINT NOT NULL,
    total_amount DECIMAL(18,2) DEFAULT 0,
    status TINYINT DEFAULT 0,
    remark VARCHAR(200) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE TABLE purchase_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    quantity DECIMAL(18,4) NOT NULL,
    price DECIMAL(18,2) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    KEY idx_purchase_item_order (order_id)
);

CREATE TABLE sales_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    total_amount DECIMAL(18,2) DEFAULT 0,
    status TINYINT DEFAULT 0,
    remark VARCHAR(200) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE TABLE sales_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    quantity DECIMAL(18,4) NOT NULL,
    price DECIMAL(18,2) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    KEY idx_sales_item_order (order_id)
);

CREATE TABLE receivable (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    paid_amount DECIMAL(18,2) DEFAULT 0,
    status TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE TABLE payable (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    supplier_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    paid_amount DECIMAL(18,2) DEFAULT 0,
    status TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE TABLE receipt (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    receivable_id BIGINT NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    method VARCHAR(50) DEFAULT NULL,
    remark VARCHAR(200) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    KEY idx_receipt_receivable (receivable_id)
);

CREATE TABLE payment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payable_id BIGINT NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    method VARCHAR(50) DEFAULT NULL,
    remark VARCHAR(200) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    KEY idx_payment_payable (payable_id)
);

SET FOREIGN_KEY_CHECKS = 1;



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


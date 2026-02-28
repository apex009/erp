
# 中小企业轻量化 ERP 系统

📌 基于 Spring Boot + Vue 的轻量级 ERP（企业资源计划）系统  
⚙️ 完整支持客户、商品、采购、库存、销售、财务、权限等模块  
📦 支持 Redis 缓存优化、RBAC 权限控制、JWT 鉴权机制  
📍 适合作为毕业设计、企业中小型业务管理系统

---

## 🧠 项目简介

本项目是一个面向中小企业的轻量化 ERP 系统，通过 **Spring Boot + MyBatis + Vue3 + Pinia + Element Plus** 等主流技术栈构建，覆盖了企业从进货、库存到销售、财务核算的完整业务流程，并实现了权限管理、报表统计等核心能力。

该系统定位轻量化、低门槛易用、便于扩展，适合作为教学演示、实践作品或真实业务使用。

---

## 🏗 总体架构

```text
Frontend (Vue3 + Pinia + Element Plus)
                |
              API (JWT 鉴权)
                |
Backend (Spring Boot + MyBatis + Redis 缓存)
                |
          MySQL 数据库 + Redis
````

系统采用前后端分离架构：

* 前端使用 Vue3 + Pinia + Router + Element Plus
* 后端使用 Spring Boot + MyBatis-Plus
* 使用 JWT 实现登录鉴权
* 使用 Redis 实现热点缓存优化

---

## 🛠 技术栈

| 层   | 技术                                                 |
| --- | -------------------------------------------------- |
| 前端  | Vue3 / Pinia / Vue Router / Element Plus           |
| 后端  | Spring Boot / MyBatis-Plus / JWT / Spring Security |
| 缓存  | Redis                                              |
| 数据库 | MySQL                                              |
| 构建  | Maven (后端) + npm/yarn (前端)                         |

---

## 📂 项目结构

```
erp/
├── backend/erp-parent                  # 后端父工程
│   ├── erp-service-system              # 核心业务服务
│   ├── erp-service-auth                # 权限与登录服务
│   ├── erp-common                      # 通用工具库
│   └── erp-admin                       # 后端启动模块
└── frontend/                           # 前端 Vue 项目
    ├── public/
    └── src/
        ├── api/                        # API 请求接口
        ├── views/                      # 视图页面
        ├── store/                      # Pinia 状态管理
        ├── router/                     # 路由配置
        └── components/                 # UI 组件
```

---

## 🔐 权限与鉴权

系统使用 **RBAC（基于角色的访问控制）**：

* 用户 → 角色 → 权限 三级结构管理
* 登录使用 **JWT Token**
* 后端 Api 请求自动从 Redis 缓存权限信息，提高性能

权限类型包括：

* 菜单权限（前端可见）
* 按钮/操作权限（控制按钮显示）
* API 接口权限（后台服务控制）

---

## 🚀 核心功能模块

| 模块   | 功能说明           |
| ---- | -------------- |
| 客户管理 | 管理企业客户数据与客户分组  |
| 商品管理 | 商品档案、分类、库存基础信息 |
| 采购管理 | 采购订单、供应商、采购入库  |
| 库存管理 | 库存流水、库存预警      |
| 销售管理 | 销售订单、销售出库      |
| 财务管理 | 收支记录、账务分析      |
| 报表分析 | 仪表盘统计、图表展示     |
| 权限管理 | 用户/角色/权限分配     |

---

## 🔥 Redis 优化说明（已支持）

✔ 登录验证码存储（TTL 有效期控制）
✔ 权限数据缓存（减少频繁 DB 调用）
✔ 字典数据缓存（仓库、供应商等）
✔ 报表统计缓存（提升性能、减少重复计算）

所有缓存 key 命名规范统一管理，并支持变更失效策略。

---

## 🗄 数据库设计与初始化

数据库使用 MySQL，表结构设计参考：

| 表名                             | 功能     |
| ------------------------------ | ------ |
| sys_user                       | 用户表    |
| sys_role                       | 角色表    |
| sys_permission                 | 权限表    |
| customer                       | 客户信息   |
| product                        | 商品信息   |
| purchase_order / purchase_item | 采购订单   |
| sales_order / sales_item       | 销售订单   |
| inventory                      | 库存记录   |
| finance_record                 | 财务流水记录 |

初次使用建议导入 `docs/db-init.sql`（可视需求调整）。

---

## 🧩 项目部署指南

### ✔ 准备环境

* JDK 17+
* Node.js 16+
* MySQL 8+
* Redis 6+

---

### 📍 后端启动流程

1. 克隆代码

```bash
git clone https://github.com/apex009/erp.git
cd erp/backend/erp-parent
```

2. 修改数据库配置
   编辑 `application-dev.yml`，设置自己的 MySQL/Redis 配置

3. 启动服务

```bash
mvn clean install
mvn spring-boot:run
```

---

### 📍 前端启动流程

进入前端目录：

```bash
cd erp/frontend
npm install
npm run dev
```

---

### 📍 访问方式

前端默认访问：

```
http://localhost:3000
```

API 默认后端地址：

```
http://localhost:8080
```

---

## 🖼 系统截图（示例）

📌 欢迎页 / 登录 / 权限管理 / 仪表盘… *(截图占位，可自行补充)*

---

## 📌 贡献与许可

本项目采用 MIT 许可证
欢迎 Fork 与共同完善！

---

## 📜 未来优化方向

✔ 完善 API 文档（OpenAPI/Swagger）
✔ 增加单元测试覆盖
✔ 支持多租户模式
✔ 支持工作流审批引擎

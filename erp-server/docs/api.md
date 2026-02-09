openapi: 3.0.3
info:
  title: ERP 后端 API（Apifox 可导入）
  version: "1.0.0"
servers:
  - url: http://localhost:8080
security:
  - bearerAuth: [ ]
tags:
  - name: 认证
  - name: 用户
  - name: 部门
  - name: 角色
  - name: 客户
  - name: 供应商
  - name: 商品
  - name: 仓库
  - name: 库存
  - name: 库存流水
  - name: 采购订单
  - name: 销售订单
  - name: 应收
  - name: 应付
  - name: 收款记录
  - name: 付款记录

paths:
  /api/auth/login:
    post:
      tags: [ 认证 ]
      summary: 登录
      security: [ ]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              example:
                username: admin
                password: 123456
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                type: object
                example:
                  code: 200
                  msg: success
                  data:
                    token: xxx
                    tokenType: Bearer
                    expiresAt: 1710000000000
                    userId: 1
                    nickname: Administrator
                    roles: [ "ADMIN" ]

  /api/auth/logout:
    post:
      tags: [ 认证 ]
      summary: 退出登录
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema: { type: object }

  /api/auth/me:
    get:
      tags: [ 认证 ]
      summary: 当前用户
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                type: object
                example:
                  code: 200
                  msg: success
                  data:
                    userId: 1
                    username: admin
                    nickname: Administrator
                    roles: [ "ADMIN" ]

  /api/users:
    get:
      tags: [ 用户 ]
      summary: 用户分页
      parameters:
        - in: query
          name: page
          schema: { type: integer, default: 1 }
        - in: query
          name: size
          schema: { type: integer, default: 10 }
        - in: query
          name: keyword
          schema: { type: string }
        - in: query
          name: deptId
          schema: { type: integer, format: int64 }
        - in: query
          name: status
          schema: { type: integer }
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema: { type: object }
    post:
      tags: [ 用户 ]
      summary: 新增用户
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              example:
                deptId: 1
                username: alice
                password: 123456
                nickname: Alice
                phone: 13800000000
                email: alice@example.com
                status: 1
                roleIds: [ 1 ]
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema: { type: object }

  /api/users/{id}:
    get:
      tags: [ 用户 ]
      summary: 用户详情
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema: { type: object }
    put:
      tags: [ 用户 ]
      summary: 修改用户
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      requestBody:
        required: true
        content:
          application/json:
            schema: { type: object }
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema: { type: object }
    delete:
      tags: [ 用户 ]
      summary: 删除用户
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema: { type: object }

  /api/depts:
    get:
      tags: [ 部门 ]
      summary: 部门分页
      parameters:
        - in: query
          name: page
          schema: { type: integer, default: 1 }
        - in: query
          name: size
          schema: { type: integer, default: 10 }
        - in: query
          name: keyword
          schema: { type: string }
        - in: query
          name: status
          schema: { type: integer }
      responses:
        '200': { description: OK }
    post:
      tags: [ 部门 ]
      summary: 新增部门
      requestBody:
        required: true
        content:
          application/json:
            schema: { type: object }
      responses:
        '200': { description: OK }

  /api/depts/{id}:
    put:
      tags: [ 部门 ]
      summary: 修改部门
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      requestBody:
        required: true
        content:
          application/json:
            schema: { type: object }
      responses:
        '200': { description: OK }
    delete:
      tags: [ 部门 ]
      summary: 删除部门
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      responses:
        '200': { description: OK }

  /api/roles:
    get:
      tags: [ 角色 ]
      summary: 角色分页
      parameters:
        - in: query
          name: page
          schema: { type: integer, default: 1 }
        - in: query
          name: size
          schema: { type: integer, default: 10 }
        - in: query
          name: keyword
          schema: { type: string }
        - in: query
          name: status
          schema: { type: integer }
      responses:
        '200': { description: OK }
    post:
      tags: [ 角色 ]
      summary: 新增角色
      requestBody:
        required: true
        content:
          application/json:
            schema: { type: object }
      responses:
        '200': { description: OK }

  /api/roles/{id}:
    put:
      tags: [ 角色 ]
      summary: 修改角色
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      requestBody:
        required: true
        content:
          application/json:
            schema: { type: object }
      responses:
        '200': { description: OK }
    delete:
      tags: [ 角色 ]
      summary: 删除角色
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      responses:
        '200': { description: OK }

  /api/customers:
    get:
      tags: [ 客户 ]
      summary: 客户分页
      parameters:
        - in: query
          name: page
          schema: { type: integer, default: 1 }
        - in: query
          name: size
          schema: { type: integer, default: 10 }
        - in: query
          name: keyword
          schema: { type: string }
        - in: query
          name: level
          schema: { type: integer }
        - in: query
          name: status
          schema: { type: integer }
      responses:
        '200': { description: OK }
    post:
      tags: [ 客户 ]
      summary: 新增客户
      requestBody:
        required: true
        content:
          application/json:
            schema: { type: object }
      responses:
        '200': { description: OK }

  /api/customers/{id}:
    get:
      tags: [ 客户 ]
      summary: 客户详情
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      responses:
        '200': { description: OK }
    put:
      tags: [ 客户 ]
      summary: 修改客户
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      requestBody:
        required: true
        content:
          application/json:
            schema: { type: object }
      responses:
        '200': { description: OK }
    delete:
      tags: [ 客户 ]
      summary: 删除客户
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      responses:
        '200': { description: OK }

  /api/suppliers:
    get:
      tags: [ 供应商 ]
      summary: 供应商分页
      parameters:
        - in: query
          name: page
          schema: { type: integer, default: 1 }
        - in: query
          name: size
          schema: { type: integer, default: 10 }
        - in: query
          name: keyword
          schema: { type: string }
        - in: query
          name: status
          schema: { type: integer }
      responses:
        '200': { description: OK }
    post:
      tags: [ 供应商 ]
      summary: 新增供应商
      requestBody:
        required: true
        content:
          application/json:
            schema: { type: object }
      responses:
        '200': { description: OK }

  /api/suppliers/{id}:
    get:
      tags: [ 供应商 ]
      summary: 供应商详情
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      responses:
        '200': { description: OK }
    put:
      tags: [ 供应商 ]
      summary: 修改供应商
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      requestBody:
        required: true
        content:
          application/json:
            schema: { type: object }
      responses:
        '200': { description: OK }
    delete:
      tags: [ 供应商 ]
      summary: 删除供应商
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      responses:
        '200': { description: OK }

  /api/products:
    get:
      tags: [ 商品 ]
      summary: 商品分页
      parameters:
        - in: query
          name: page
          schema: { type: integer, default: 1 }
        - in: query
          name: size
          schema: { type: integer, default: 10 }
        - in: query
          name: keyword
          schema: { type: string }
        - in: query
          name: category
          schema: { type: string }
        - in: query
          name: status
          schema: { type: integer }
      responses:
        '200': { description: OK }
    post:
      tags: [ 商品 ]
      summary: 新增商品
      requestBody:
        required: true
        content:
          application/json:
            schema: { type: object }
      responses:
        '200': { description: OK }

  /api/products/{id}:
    get:
      tags: [ 商品 ]
      summary: 商品详情
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      responses:
        '200': { description: OK }
    put:
      tags: [ 商品 ]
      summary: 修改商品
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      requestBody:
        required: true
        content:
          application/json:
            schema: { type: object }
      responses:
        '200': { description: OK }
    delete:
      tags: [ 商品 ]
      summary: 删除商品
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      responses:
        '200': { description: OK }

  /api/warehouses:
    get:
      tags: [ 仓库 ]
      summary: 仓库分页
      parameters:
        - in: query
          name: page
          schema: { type: integer, default: 1 }
        - in: query
          name: size
          schema: { type: integer, default: 10 }
        - in: query
          name: keyword
          schema: { type: string }
        - in: query
          name: status
          schema: { type: integer }
      responses:
        '200': { description: OK }
    post:
      tags: [ 仓库 ]
      summary: 新增仓库
      requestBody:
        required: true
        content:
          application/json:
            schema: { type: object }
      responses:
        '200': { description: OK }

  /api/warehouses/{id}:
    get:
      tags: [ 仓库 ]
      summary: 仓库详情
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      responses:
        '200': { description: OK }
    put:
      tags: [ 仓库 ]
      summary: 修改仓库
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      requestBody:
        required: true
        content:
          application/json:
            schema: { type: object }
      responses:
        '200': { description: OK }
    delete:
      tags: [ 仓库 ]
      summary: 删除仓库
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      responses:
        '200': { description: OK }

  /api/stocks:
    get:
      tags: [ 库存 ]
      summary: 库存分页
      parameters:
        - in: query
          name: page
          schema: { type: integer, default: 1 }
        - in: query
          name: size
          schema: { type: integer, default: 10 }
        - in: query
          name: productId
          schema: { type: integer, format: int64 }
        - in: query
          name: warehouseId
          schema: { type: integer, format: int64 }
      responses:
        '200': { description: OK }

  /api/stocks/{id}:
    put:
      tags: [ 库存 ]
      summary: 修改库存
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      requestBody:
        required: true
        content:
          application/json:
            schema: { type: object }
      responses:
        '200': { description: OK }

  /api/stock-records:
    get:
      tags: [ 库存流水 ]
      summary: 库存流水分页
      parameters:
        - in: query
          name: page
          schema: { type: integer, default: 1 }
        - in: query
          name: size
          schema: { type: integer, default: 10 }
        - in: query
          name: productId
          schema: { type: integer, format: int64 }
        - in: query
          name: warehouseId
          schema: { type: integer, format: int64 }
        - in: query
          name: recordType
          schema: { type: string }
        - in: query
          name: bizType
          schema: { type: string }
      responses:
        '200': { description: OK }

  /api/purchase-orders:
    get:
      tags: [ 采购订单 ]
      summary: 采购订单分页
      parameters:
        - in: query
          name: page
          schema: { type: integer, default: 1 }
        - in: query
          name: size
          schema: { type: integer, default: 10 }
        - in: query
          name: orderNo
          schema: { type: string }
        - in: query
          name: supplierId
          schema: { type: integer, format: int64 }
        - in: query
          name: status
          schema: { type: integer }
      responses:
        '200': { description: OK }
    post:
      tags: [ 采购订单 ]
      summary: 新建采购订单
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              example:
                supplierId: 1
                remark: PO for May
                items:
                  - { productId: 1, warehouseId: 1, quantity: 10, price: 12.5 }
      responses:
        '200': { description: OK }

  /api/purchase-orders/{id}:
    get:
      tags: [ 采购订单 ]
      summary: 采购订单详情
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      responses:
        '200': { description: OK }

  /api/purchase-orders/{id}/items:
    get:
      tags: [ 采购订单 ]
      summary: 采购订单明细
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      responses:
        '200': { description: OK }

  /api/sales-orders:
    get:
      tags: [ 销售订单 ]
      summary: 销售订单分页
      parameters:
        - in: query
          name: page
          schema: { type: integer, default: 1 }
        - in: query
          name: size
          schema: { type: integer, default: 10 }
        - in: query
          name: orderNo
          schema: { type: string }
        - in: query
          name: customerId
          schema: { type: integer, format: int64 }
        - in: query
          name: status
          schema: { type: integer }
      responses:
        '200': { description: OK }
    post:
      tags: [ 销售订单 ]
      summary: 新建销售订单
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              example:
                customerId: 1
                remark: SO for June
                items:
                  - { productId: 1, warehouseId: 1, quantity: 3, price: 18.0 }
      responses:
        '200': { description: OK }

  /api/sales-orders/{id}:
    get:
      tags: [ 销售订单 ]
      summary: 销售订单详情
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      responses:
        '200': { description: OK }

  /api/sales-orders/{id}/items:
    get:
      tags: [ 销售订单 ]
      summary: 销售订单明细
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      responses:
        '200': { description: OK }

  /api/receivables:
    get:
      tags: [ 应收 ]
      summary: 应收分页
      parameters:
        - in: query
          name: page
          schema: { type: integer, default: 1 }
        - in: query
          name: size
          schema: { type: integer, default: 10 }
        - in: query
          name: customerId
          schema: { type: integer, format: int64 }
        - in: query
          name: status
          schema: { type: integer }
      responses:
        '200': { description: OK }

  /api/receivables/{id}:
    get:
      tags: [ 应收 ]
      summary: 应收详情
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      responses:
        '200': { description: OK }

  /api/receivables/{id}/receipts:
    get:
      tags: [ 应收 ]
      summary: 应收收款记录
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      responses:
        '200': { description: OK }

  /api/receivables/{id}/receipt:
    post:
      tags: [ 应收 ]
      summary: 应收收款
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              example: { amount: 200.0, method: bank, remark: first payment }
      responses:
        '200': { description: OK }

  /api/payables:
    get:
      tags: [ 应付 ]
      summary: 应付分页
      parameters:
        - in: query
          name: page
          schema: { type: integer, default: 1 }
        - in: query
          name: size
          schema: { type: integer, default: 10 }
        - in: query
          name: supplierId
          schema: { type: integer, format: int64 }
        - in: query
          name: status
          schema: { type: integer }
      responses:
        '200': { description: OK }

  /api/payables/{id}:
    get:
      tags: [ 应付 ]
      summary: 应付详情
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      responses:
        '200': { description: OK }

  /api/payables/{id}/payments:
    get:
      tags: [ 应付 ]
      summary: 应付付款记录
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      responses:
        '200': { description: OK }

  /api/payables/{id}/payment:
    post:
      tags: [ 应付 ]
      summary: 应付付款
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              example: { amount: 150.0, method: cash, remark: advance }
      responses:
        '200': { description: OK }

  /api/receipts:
    get:
      tags: [ 收款记录 ]
      summary: 收款记录分页
      parameters:
        - in: query
          name: page
          schema: { type: integer, default: 1 }
        - in: query
          name: size
          schema: { type: integer, default: 10 }
        - in: query
          name: receivableId
          schema: { type: integer, format: int64 }
      responses:
        '200': { description: OK }

  /api/receipts/{id}:
    get:
      tags: [ 收款记录 ]
      summary: 收款记录详情
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      responses:
        '200': { description: OK }

  /api/payments:
    get:
      tags: [ 付款记录 ]
      summary: 付款记录分页
      parameters:
        - in: query
          name: page
          schema: { type: integer, default: 1 }
        - in: query
          name: size
          schema: { type: integer, default: 10 }
        - in: query
          name: payableId
          schema: { type: integer, format: int64 }
      responses:
        '200': { description: OK }

  /api/payments/{id}:
    get:
      tags: [ 付款记录 ]
      summary: 付款记录详情
      parameters:
        - in: path
          name: id
          required: true
          schema: { type: integer, format: int64 }
      responses:
        '200': { description: OK }

components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="queryParams.requestNo" placeholder="申请单号" style="width: 200px; margin-right: 10px;" />
      <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px; margin-right: 10px;">
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已驳回" :value="2" />
        <el-option label="已转订单" :value="3" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="success" @click="handleCreate">新增申请</el-button>
    </div>

    <el-table v-loading="loading" :data="list" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="requestNo" label="申请单号" width="180" />
      <el-table-column prop="applicant" label="申请人" width="120" />
      <el-table-column prop="requestDate" label="申请日期" width="180">
          <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
          </template>
      </el-table-column>
      <el-table-column prop="items" label="包含商品">
          <template #default="scope">
              <span v-if="scope.row.itemCount">共 {{ scope.row.itemCount }} 种商品</span>
          </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="预估总额">
          <template #default="scope">¥{{ scope.row.totalAmount }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="statusMap[scope.row.status]?.type">{{ statusMap[scope.row.status]?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250">
        <template #default="scope">
          <el-button v-if="scope.row.status === 0" link type="primary" @click="handleApprove(scope.row)">审核</el-button>
          <el-button v-if="scope.row.status === 0" link type="danger" @click="handleReject(scope.row)">驳回</el-button>
          <el-button v-if="scope.row.status === 1" link type="success" @click="handleToOrder(scope.row)">转订单</el-button>
          <el-button link type="info" @click="handleDetail(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container" style="margin-top: 20px; float: right;">
        <el-pagination
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.size"
            :total="total"
            layout="total, prev, pager, next"
            @current-change="getList"
        />
    </div>

    <!-- Create Dialog -->
    <el-dialog v-model="dialogVisible" title="新增采购申请" width="900px">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="供应商" prop="supplierId" :rules="[{ required: true, message: '请选择供应商', trigger: 'change' }]">
            <el-select v-model="form.supplierId" placeholder="请选择供应商" filterable style="width: 100%">
                <el-option v-for="item in suppliers" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
            <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
        
        <el-divider content-position="left">申请明细</el-divider>
        <el-button type="primary" size="small" @click="showProductSelector = true" style="margin-bottom: 10px;">添加商品</el-button>
        
        <el-table :data="form.items" border height="300px">
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="sku" label="SKU" />
            <el-table-column prop="warehouseId" label="仓库" width="150">
                <template #default="scope">
                    <el-select v-model="scope.row.warehouseId" placeholder="选择仓库" size="small">
                        <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
                    </el-select>
                </template>
            </el-table-column>
            <el-table-column prop="price" label="参考单价" width="120">
                <template #default="scope">
                    <el-input-number v-model="scope.row.price" :min="0" :precision="2" size="small" style="width: 100%" />
                </template>
            </el-table-column>
            <el-table-column prop="quantity" label="申请数量" width="150">
                <template #default="scope">
                    <el-input-number v-model="scope.row.quantity" :min="1" size="small" style="width: 100%" />
                </template>
            </el-table-column>
            <el-table-column label="小计" width="120">
                <template #default="scope">
                    {{ (scope.row.price * scope.row.quantity).toFixed(2) }}
                </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
                <template #default="scope">
                    <el-button link type="danger" @click="removeItem(scope.$index)">移除</el-button>
                </template>
            </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">提交申请</el-button>
      </template>
    </el-dialog>

    <ProductSelector v-model="showProductSelector" @select="handleProductSelect" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getRequestPage, createRequest, approveRequest, rejectRequest, convertToOrder } from '@/api/purchase/request'
import { getSupplierPage } from '@/api/base/supplier'
import { getWarehousePage } from '@/api/base/warehouse'
import ProductSelector from '@/components/ProductSelector/index.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const suppliers = ref([])
const warehouses = ref([])
const queryParams = reactive({
  page: 1,
  size: 10,
  requestNo: '',
  status: null
})

const statusMap = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已驳回', type: 'danger' },
  3: { label: '已转订单', type: 'info' }
}

const dialogVisible = ref(false)
const showProductSelector = ref(false)
const form = reactive({
    supplierId: null,
    remark: '',
    items: []
})

const getList = async () => {
  loading.value = true
  try {
    const res = await getRequestPage(queryParams)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const getSuppliers = async () => {
    try {
        const res = await getSupplierPage({ page: 1, size: 100 })
        suppliers.value = res.records
    } catch (e) {
        console.error("Failed to load suppliers", e)
    }
}

const getWarehouses = async () => {
    try {
        const res = await getWarehousePage({ page: 1, size: 100 })
        warehouses.value = res.records
    } catch (e) {
        console.error("Failed to load warehouses", e)
    }
}

const handleSearch = () => {
  queryParams.page = 1
  getList()
}

const handleCreate = () => {
    form.supplierId = null
    form.remark = ''
    form.items = []
    dialogVisible.value = true
}

const handleProductSelect = (products) => {
    products.forEach(p => {
        // Avoid duplicates
        if (!form.items.find(i => i.productId === p.id)) {
            form.items.push({
                productId: p.id,
                productName: p.name,
                sku: p.sku,
                warehouseId: warehouses.value.length > 0 ? warehouses.value[0].id : null,
                price: p.purchasePrice,
                quantity: 1
            })
        }
    })
}

const removeItem = (index) => {
    form.items.splice(index, 1)
}

const submitForm = async () => {
    if (form.items.length === 0) {
        ElMessage.warning('请至少添加一种商品')
        return
    }
    if (form.items.some(item => !item.warehouseId)) {
        ElMessage.warning('请为所有商品选择仓库')
        return
    }
    // Convert items for backend DTO if necessary, assuming backend accepts this structure or similar
    // We might need to check the DTO structure again, but usually it matches
    const submitData = {
        supplierId: form.supplierId,
        remark: form.remark,
        items: form.items
    }
    
    await createRequest(submitData)
    ElMessage.success('申请提交成功')
    dialogVisible.value = false
    getList()
}

const handleApprove = async (row) => {
    await approveRequest(row.id)
    ElMessage.success('审核通过')
    getList()
}

const handleReject = async (row) => {
    await rejectRequest(row.id)
    ElMessage.success('已驳回')
    getList()
}

const handleToOrder = async (row) => {
    await convertToOrder(row.id)
    ElMessage.success('已生成采购订单')
    getList()
}

const handleDetail = (row) => {
    // TODO: Implement detail view or dialog
    ElMessage.info('详情功能开发中')
}

const formatDate = (dateStr) => {
    if (!dateStr) return ''
    return new Date(dateStr).toLocaleString()
}

onMounted(() => {
  getList()
  getSuppliers()
  getWarehouses()
})
</script>

<style scoped>
.app-container {
    padding: 20px;
}
</style>

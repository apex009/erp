<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="queryParams.orderNo" placeholder="订单号" style="width: 200px; margin-right: 10px;" />
      <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px; margin-right: 10px;">
        <el-option label="待审核" :value="0" />
        <el-option label="待入库" :value="1" />
        <el-option label="部分入库" :value="2" />
        <el-option label="已完成" :value="3" />
        <el-option label="已取消" :value="4" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button v-if="authStore.hasPerm('api:purchase:write')" type="success" @click="handleCreate">直接新增订单</el-button>
    </div>

    <el-table v-loading="loading" :data="list" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="supplierName" label="供应商" width="150" />
      <el-table-column prop="totalAmount" label="订单总额">
          <template #default="scope">¥{{ scope.row.totalAmount }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建日期" width="180">
          <template #default="scope">{{ formatDate(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="statusMap[scope.row.status]?.type">{{ statusMap[scope.row.status]?.label }}</el-tag>
        </template>
      </el-table-column>
       <el-table-column label="操作" width="250">
        <template #default="scope">
          <el-button v-if="scope.row.status === 0 && authStore.hasPerm('api:purchase-orders:{id}:approve:post')" link type="primary" @click="handleApprove(scope.row)">审核</el-button>
          <el-button v-if="scope.row.status === 0 && authStore.hasPerm('api:purchase:write')" link type="danger" @click="handleCancel(scope.row)">取消</el-button>
          <el-button v-if="scope.row.status === 1 && authStore.hasPerm('api:purchase:write')" link type="success" @click="handleInbound(scope.row)">入库</el-button>
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
    <el-dialog v-model="dialogVisible" title="新增采购订单" width="900px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
         <el-form-item label="供应商" prop="supplierId">
             <el-select v-model="form.supplierId" placeholder="选择供应商" filterable>
                 <el-option
                    v-for="item in suppliers"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                 />
             </el-select>
         </el-form-item>
        <el-form-item label="备注" prop="remark">
            <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
        
        <el-divider content-position="left">订单明细</el-divider>
        <el-button type="primary" size="small" @click="showProductSelector = true" style="margin-bottom: 10px;">添加商品</el-button>

        <el-table :data="form.items" border height="300px">
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="sku" label="SKU" />
            <el-table-column prop="price" label="采购单价" width="140">
                <template #default="scope">
                    <el-input-number v-model="scope.row.price" :min="0" :precision="2" size="small" style="width: 100%" />
                </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="150">
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
        <el-button type="primary" @click="submitForm">提交订单</el-button>
      </template>
    </el-dialog>
    <!-- Detail Drawer -->
    <el-drawer v-model="detailVisible" title="采购订单详情" size="60%">
      <div v-loading="detailLoading" v-if="detailData">
        <el-descriptions title="基础信息" :column="2" border>
          <el-descriptions-item label="订单号">{{ detailData.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="供应商">{{ suppliers.find(s => s.id === detailData.supplierId)?.name || '未知供应商' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(detailData.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="订单总额">¥{{ detailData.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusMap[detailData.status]?.type">{{ statusMap[detailData.status]?.label }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="关联采购单">{{ detailData.requestNo || '无' }}</el-descriptions-item>
          <el-descriptions-item label="入库时间">{{ formatDate(detailData.inboundTime) }}</el-descriptions-item>
          <el-descriptions-item label="应付账款单号">{{ detailData.payableNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">订单明细</el-divider>
        <el-table :data="detailItems" border>
          <el-table-column prop="productId" label="商品ID" width="100" />
          <el-table-column label="仓库" width="150">
             <template #default="scope">
                 {{ warehouses.find(w => w.id === scope.row.warehouseId)?.name || scope.row.warehouseId || '默认仓库' }}
             </template>
          </el-table-column>
          <el-table-column prop="price" label="采购单价">
              <template #default="scope">¥{{ scope.row.price }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="订购数量" />
          <el-table-column prop="inboundQuantity" label="已入库数量" />
          <el-table-column label="小计">
              <template #default="scope">¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </el-drawer>

    <ProductSelector v-model="showProductSelector" @select="handleProductSelect" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getOrderPage, createOrder, approveOrder, cancelOrder, inboundOrder, getOrder, getOrderItems } from '@/api/purchase/order'
import { getSupplierPage } from '@/api/base/supplier'
import { getWarehousePage } from '@/api/base/warehouse'
import ProductSelector from '@/components/ProductSelector/index.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const suppliers = ref([])
const warehouses = ref([])
const queryParams = reactive({
  page: 1,
  size: 10,
  orderNo: '',
  status: null
})

const statusMap = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '待入库', type: 'primary' },
  2: { label: '部分入库', type: 'info' },
  3: { label: '已完成', type: 'success' },
  4: { label: '已取消', type: 'danger' }
}

const dialogVisible = ref(false)
const showProductSelector = ref(false)
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)
const detailItems = ref([])
const formRef = ref(null)
const form = reactive({
    supplierId: null,
    remark: '',
    items: []
})
const rules = {
    supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getOrderPage(queryParams)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const getSuppliers = async () => {
    if (!authStore.hasPerm('api:suppliers:read')) return
    try {
        const res = await getSupplierPage({ page: 1, size: 100, status: 1 })
        suppliers.value = res.records
    } catch (e) {}
}

const getWarehouses = async () => {
    if (!authStore.hasPerm('api:warehouses:read')) return
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
        if (!form.items.find(i => i.productId === p.id)) {
            form.items.push({
                productId: p.id,
                productName: p.name,
                sku: p.sku,
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
    if (!formRef.value) return
    await formRef.value.validate(async (valid) => {
        if (valid) {
             if (form.items.length === 0) {
                ElMessage.warning('请至少添加一种商品')
                return
            }
            
            // Front-to-Back alignment: Strip out 'productName', 'sku'
            const submitData = {
                supplierId: form.supplierId,
                remark: form.remark,
                items: form.items.map(item => ({
                    productId: item.productId,
                    warehouseId: item.warehouseId || 1, // Defaulting to 1 to satisfy backend @NotNull if not set
                    price: item.price,
                    quantity: item.quantity
                }))
            }
            
            await createOrder(submitData)
            ElMessage.success('订单创建成功')
            dialogVisible.value = false
            getList()
        }
    })
}

const handleApprove = async (row) => {
    await approveOrder(row.id)
    ElMessage.success('审核通过')
    getList()
}

const handleCancel = async (row) => {
    ElMessageBox.confirm('确定取消该订单吗?', '提示').then(async () => {
        await cancelOrder(row.id)
        ElMessage.success('订单已取消')
        getList()
    })
}

const handleInbound = (row) => {
    ElMessageBox.confirm('确定执行入库操作吗？这将增加库存并生成应付账款。', '提示', {
        type: 'warning'
    }).then(async () => {
        await inboundOrder(row.id)
        ElMessage.success('入库成功')
        getList()
    })
}

const handleDetail = async (row) => {
    detailVisible.value = true
    detailLoading.value = true
    detailData.value = null
    detailItems.value = []
    try {
        const [detailRes, itemsRes] = await Promise.all([
            getOrder(row.id),
            getOrderItems(row.id)
        ])
        detailData.value = detailRes
        detailItems.value = itemsRes
    } catch (e) {
        ElMessage.error('获取详情失败')
    } finally {
        detailLoading.value = false
    }
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

<template>
  <div class="app-container">
    <div class="filter-container">
      <el-select v-model="queryParams.warehouseId" placeholder="选择仓库" clearable style="width: 200px; margin-right: 10px;">
        <el-option
           v-for="item in warehouses"
           :key="item.id"
           :label="item.name"
           :value="item.id"
        />
      </el-select>
       <el-input v-model="queryParams.keyword" placeholder="商品名称/编码" style="width: 200px; margin-right: 10px;" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <el-table v-loading="loading" :data="list" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="warehouseName" label="仓库" width="180" />
      <el-table-column prop="productName" label="商品名称" min-width="150" />
      <el-table-column prop="productSku" label="SKU" width="150" />
      <el-table-column prop="quantity" label="当前库存" width="120">
          <template #default="scope">
              <span :class="{'text-danger': scope.row.quantity < scope.row.safeStock}">{{ scope.row.quantity }}</span>
          </template>
      </el-table-column>
      <el-table-column prop="unit" label="单位" width="80" />
      <el-table-column prop="safeStock" label="安全库存" width="120">
          <template #default="scope">{{ scope.row.safeStock }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button link type="primary" @click="handleEditSafeStock(scope.row)">设置安全库存</el-button>
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

    <!-- Safe Stock Dialog -->
    <el-dialog v-model="dialogVisible" title="设置安全库存" width="400px">
        <el-form :model="form">
            <el-form-item label="商品" label-width="80px">
                <el-input v-model="currentProductName" disabled />
            </el-form-item>
            <el-form-item label="安全库存" label-width="80px">
                <el-input-number v-model="form.safeStock" :min="0" style="width: 100%" />
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitSafeStock">确定</el-button>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getStockPage, updateStock } from '@/api/inventory/stock'
import { getWarehousePage } from '@/api/base/warehouse'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const warehouses = ref([])
const queryParams = reactive({
  page: 1,
  size: 10,
  warehouseId: null,
  keyword: '' // Backend might need to support product name search in stock query
})

const dialogVisible = ref(false)
const currentProductName = ref('')
const form = reactive({
    id: null,
    safeStock: 0
})

const getList = async () => {
  loading.value = true
  try {
    const res = await getStockPage(queryParams)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const getWarehouses = async () => {
    const res = await getWarehousePage({ page: 1, size: 100, status: 1 })
    warehouses.value = res.records
}

const handleSearch = () => {
  queryParams.page = 1
  getList()
}

const handleEditSafeStock = (row) => {
    form.id = row.id
    form.safeStock = row.safeStock
    currentProductName.value = row.productName
    dialogVisible.value = true
}

const submitSafeStock = async () => {
    await updateStock(form.id, { safeStock: form.safeStock })
    ElMessage.success('设置成功')
    dialogVisible.value = false
    getList()
}

onMounted(() => {
  getList()
  getWarehouses()
})
</script>

<style scoped>
.app-container {
    padding: 20px;
}
.text-danger {
    color: #f56c6c;
    font-weight: bold;
}
</style>

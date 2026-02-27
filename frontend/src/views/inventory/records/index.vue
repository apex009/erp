<template>
  <div class="app-container">
    <div class="filter-container">
        <el-select v-model="queryParams.warehouseId" placeholder="仓库" clearable style="width: 150px; margin-right: 10px;">
        <el-option
           v-for="item in warehouses"
           :key="item.id"
           :label="item.name"
           :value="item.id"
        />
      </el-select>
      <el-select v-model="queryParams.recordType" placeholder="类型" clearable style="width: 120px; margin-right: 10px;">
        <el-option label="入库" value="IN" />
        <el-option label="出库" value="OUT" />
      </el-select>
      <el-select v-model="queryParams.bizType" placeholder="业务类型" clearable style="width: 150px; margin-right: 10px;">
        <el-option label="采购入库" value="PURCHASE" />
        <el-option label="销售出库" value="SALES" />
        <el-option label="盘点" value="CHECK" />
        <el-option label="调拨" value="TRANSFER" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <el-table v-loading="loading" :data="list" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="createTime" label="时间" width="180">
          <template #default="scope">{{ formatDate(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column prop="warehouseName" label="仓库" width="150" />
      <el-table-column prop="productName" label="商品" min-width="150" />
      <el-table-column prop="recordType" label="方向" width="80">
          <template #default="scope">
              <el-tag :type="scope.row.recordType === 'IN' ? 'success' : 'danger'">
                  {{ scope.row.recordType === 'IN' ? '入库' : '出库' }}
              </el-tag>
          </template>
      </el-table-column>
      <el-table-column prop="bizType" label="业务类型" width="120">
          <template #default="scope">{{ formatBizType(scope.row.bizType) }}</template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="100">
          <template #default="scope">
              {{ scope.row.recordType === 'OUT' ? '-' : '+' }}{{ scope.row.quantity }}
          </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" show-overflow-tooltip />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getStockRecordPage } from '@/api/inventory/record'
import { getWarehousePage } from '@/api/base/warehouse'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const warehouses = ref([])
const queryParams = reactive({
  page: 1,
  size: 10,
  warehouseId: null,
  recordType: '',
  bizType: ''
})

const getList = async () => {
  loading.value = true
  try {
    const res = await getStockRecordPage(queryParams)
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

const formatDate = (dateStr) => {
    if (!dateStr) return ''
    return new Date(dateStr).toLocaleString()
}

const formatBizType = (type) => {
    const map = {
        'PURCHASE': '采购入库',
        'SALES': '销售出库',
        'CHECK': '盘点',
        'TRANSFER': '调拨'
    }
    return map[type] || type
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
</style>

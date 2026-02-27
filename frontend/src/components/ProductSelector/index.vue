<template>
  <el-dialog
    v-model="visible"
    title="选择商品"
    width="800px"
    @close="handleClose"
  >
    <div class="filter-container" style="margin-bottom: 20px;">
      <el-input
        v-model="queryParams.keyword"
        placeholder="商品名称/编码"
        style="width: 250px; margin-right: 10px;"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="productList"
      border
      highlight-current-row
      @selection-change="handleSelectionChange"
      style="width: 100%"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="name" label="商品名称" />
      <el-table-column prop="sku" label="SKU" />
      <el-table-column prop="spec" label="规格" />
      <el-table-column prop="unit" label="单位" width="80" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="purchasePrice" label="采购价" width="100" />
      <el-table-column prop="stock" label="当前库存" width="100">
         <template #default="scope">
             {{ scope.row.stock || 0 }}
         </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container" style="margin-top: 20px; text-align: right;">
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="getList"
      />
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleConfirm">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { getProductPage } from '@/api/base/product'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'select'])

const visible = ref(false)
const loading = ref(false)
const productList = ref([])
const total = ref(0)
const selectedRows = ref([])

const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: 1 // Only show active products
})

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    getList()
  }
})

const getList = async () => {
  loading.value = true
  try {
    const res = await getProductPage(queryParams)
    productList.value = res.records
    total.value = res.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
  getList()
}

const handleSelectionChange = (val) => {
  selectedRows.value = val
}

const handleClose = () => {
  visible.value = false
  emit('update:modelValue', false)
}

const handleConfirm = () => {
  emit('select', selectedRows.value)
  handleClose()
}
</script>

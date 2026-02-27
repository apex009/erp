<template>
  <div class="product-container">
    <div class="header-actions">
      <el-input 
        v-model="queryParams.keyword" 
        placeholder="搜索名称/编码" 
        style="width: 200px" 
        @keyup.enter="handleSearch"
      />
      <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="正常" :value="1" />
        <el-option label="下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="success" @click="handleCreate">新增商品</el-button>
    </div>

    <el-table :data="productList" v-loading="loading" style="width: 100%; margin-top: 20px;" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="商品名称" />
      <el-table-column prop="sku" label="SKU / 编码" />
      <el-table-column prop="category" label="分类" />
      <el-table-column prop="unit" label="单位" width="80" />
      <el-table-column prop="spec" label="规格" />
      <el-table-column label="价格信息" width="200">
        <template #default="scope">
            <div>采购价: ¥{{ scope.row.purchasePrice }}</div>
            <div>销售价: ¥{{ scope.row.salePrice }}</div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ scope.row.status === 1 ? '正常' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="getList"
        @current-change="getList"
      />
    </div>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="SKU/编码" prop="sku">
          <el-input v-model="form.sku" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="form.category" placeholder="例如: 电子产品" />
        </el-form-item>
        <el-row :gutter="20">
            <el-col :span="12">
                <el-form-item label="单位" prop="unit">
                  <el-input v-model="form.unit" placeholder="个/台" />
                </el-form-item>
            </el-col>
            <el-col :span="12">
                <el-form-item label="规格" prop="spec">
                  <el-input v-model="form.spec" />
                </el-form-item>
            </el-col>
        </el-row>
        <el-row :gutter="20">
            <el-col :span="12">
                <el-form-item label="采购价" prop="purchasePrice">
                  <el-input-number v-model="form.purchasePrice" :precision="2" :step="0.1" :min="0" style="width: 100%" />
                </el-form-item>
            </el-col>
            <el-col :span="12">
                <el-form-item label="销售价" prop="salePrice">
                  <el-input-number v-model="form.salePrice" :precision="2" :step="0.1" :min="0" style="width: 100%" />
                </el-form-item>
            </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getProductPage, createProduct, updateProduct, deleteProduct } from '@/api/base/product'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const productList = ref([])
const total = ref(0)
const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: null
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const form = reactive({
  id: null,
  name: '',
  sku: '',
  category: '',
  unit: '',
  spec: '',
  purchasePrice: 0,
  salePrice: 0,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  sku: [{ required: true, message: '请输入SKU编码', trigger: 'blur' }],
  purchasePrice: [{ required: true, message: '请输入采购价', trigger: 'blur' }],
  salePrice: [{ required: true, message: '请输入销售价', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getProductPage(queryParams)
    productList.value = res.records
    total.value = res.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
  getList()
}

const handleCreate = () => {
  dialogTitle.value = '新增商品'
  form.id = null
  Object.assign(form, {
      name: '',
      sku: '',
      category: '',
      unit: '',
      spec: '',
      purchasePrice: 0,
      salePrice: 0,
      status: 1
  })
  if (formRef.value) formRef.value.clearValidate()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑商品'
  Object.assign(form, row)
  if (formRef.value) formRef.value.clearValidate()
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该商品吗?', '警告', {
    type: 'warning'
  }).then(async () => {
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    getList()
  })
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (form.id) {
        await updateProduct(form.id, form)
        ElMessage.success('更新成功')
      } else {
        await createProduct(form)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      getList()
    }
  })
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.product-container {
  padding: 20px;
}
.header-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

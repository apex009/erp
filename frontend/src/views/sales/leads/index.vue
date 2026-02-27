<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="queryParams.keyword" placeholder="线索名称/备注" style="width: 200px; margin-right: 10px;" />
      <el-select v-model="queryParams.stage" placeholder="阶段" clearable style="width: 120px; margin-right: 10px;">
        <el-option label="初步接触" value="初步接触" />
        <el-option label="需求分析" value="需求分析" />
        <el-option label="方案报价" value="方案报价" />
        <el-option label="商务谈判" value="商务谈判" />
        <el-option label="赢单" value="赢单" />
        <el-option label="输单" value="输单" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="success" @click="handleCreate">新增线索</el-button>
    </div>

    <el-table v-loading="loading" :data="list" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="name" label="线索名称" min-width="150" />
      <el-table-column prop="stage" label="阶段" width="120">
        <template #default="scope">
          <el-tag :type="getStageType(scope.row.stage)">{{ scope.row.stage }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="expectedAmount" label="预计金额" width="120">
        <template #default="scope">¥{{ scope.row.expectedAmount }}</template>
      </el-table-column>
      <el-table-column prop="probability" label="赢单率" width="100">
        <template #default="scope">{{ scope.row.probability }}%</template>
      </el-table-column>
      <el-table-column prop="nextFollowTime" label="下次跟进" width="180">
        <template #default="scope">{{ formatDate(scope.row.nextFollowTime) }}</template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          <el-button v-if="scope.row.stage === '赢单'" link type="success" @click="handleConvert(scope.row)">转订单</el-button>
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

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="线索名称" prop="name">
          <el-input v-model="form.name" placeholder="例如: XX公司采购意向" />
        </el-form-item>
        <el-form-item label="关联客户" prop="customerId">
           <el-select v-model="form.customerId" placeholder="选择客户" filterable clearable style="width: 100%">
             <el-option
                v-for="item in customers"
                :key="item.id"
                :label="item.name"
                :value="item.id"
             />
           </el-select>
        </el-form-item>
        <el-form-item label="阶段" prop="stage">
          <el-select v-model="form.stage" style="width: 100%">
            <el-option label="初步接触" value="初步接触" />
            <el-option label="需求分析" value="需求分析" />
            <el-option label="方案报价" value="方案报价" />
            <el-option label="商务谈判" value="商务谈判" />
            <el-option label="赢单" value="赢单" />
            <el-option label="输单" value="输单" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
            <el-col :span="12">
                <el-form-item label="预计金额" prop="expectedAmount">
                  <el-input-number v-model="form.expectedAmount" :min="0" :precision="2" style="width: 100%" />
                </el-form-item>
            </el-col>
            <el-col :span="12">
                <el-form-item label="赢单率(%)" prop="probability">
                  <el-input-number v-model="form.probability" :min="0" :max="100" style="width: 100%" />
                </el-form-item>
            </el-col>
        </el-row>
        <el-form-item label="下次跟进" prop="nextFollowTime">
            <el-date-picker v-model="form.nextFollowTime" type="datetime" placeholder="选择日期时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getLeadPage, createLead, updateLead, deleteLead } from '@/api/sales/lead'
import { getCustomerPage } from '@/api/base/customer'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const customers = ref([])
const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  stage: '',
  status: 1
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const form = reactive({
  id: null,
  name: '',
  customerId: null,
  stage: '初步接触',
  expectedAmount: 0,
  probability: 10,
  nextFollowTime: null,
  remark: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入线索名称', trigger: 'blur' }],
  stage: [{ required: true, message: '请选择阶段', trigger: 'change' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getLeadPage(queryParams)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const getCustomers = async () => {
    const res = await getCustomerPage({ page: 1, size: 100, status: 1 })
    customers.value = res.records
}

const handleSearch = () => {
  queryParams.page = 1
  getList()
}

const handleCreate = () => {
  dialogTitle.value = '新增线索'
  form.id = null
  Object.assign(form, {
      name: '',
      customerId: null,
      stage: '初步接触',
      expectedAmount: 0,
      probability: 10,
      nextFollowTime: null,
      remark: '',
      status: 1
  })
  if (formRef.value) formRef.value.clearValidate()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑线索'
  Object.assign(form, row)
  if (formRef.value) formRef.value.clearValidate()
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该线索吗?', '警告').then(async () => {
    await deleteLead(row.id)
    ElMessage.success('删除成功')
    getList()
  })
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (form.id) {
        await updateLead(form.id, form)
        ElMessage.success('更新成功')
      } else {
        await createLead(form)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      getList()
    }
  })
}

const handleConvert = (row) => {
    // Navigate to Sales Order creation with lead info
    router.push({ path: '/sales/orders', query: { customerId: row.customerId } })
    ElMessage.info('请并在订单页面完成下单')
}

const getStageType = (stage) => {
    if (stage === '赢单') return 'success'
    if (stage === '输单') return 'info'
    if (stage === '商务谈判') return 'warning'
    return 'primary'
}

const formatDate = (dateStr) => {
    if (!dateStr) return ''
    return new Date(dateStr).toLocaleString()
}

onMounted(() => {
  getList()
  getCustomers()
})
</script>

<style scoped>
.app-container {
    padding: 20px;
}
</style>

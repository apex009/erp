<template>
  <div class="app-container">
    <div class="filter-container">
      <el-select v-model="queryParams.customerId" placeholder="客户" clearable filterable style="width: 200px; margin-right: 10px;">
        <el-option
           v-for="item in customers"
           :key="item.id"
           :label="item.name"
           :value="item.id"
        />
      </el-select>
      <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px; margin-right: 10px;">
        <el-option label="待收款" :value="0" />
        <el-option label="部分收款" :value="1" />
        <el-option label="已结清" :value="2" />
        <el-option label="坏账" :value="3" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <el-table v-loading="loading" :data="list" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="variableNo" label="应收单号" width="180" />
      <el-table-column prop="sourceOrderNo" label="源单号" width="180" />
      <el-table-column prop="customerName" label="客户" min-width="150" />
      <el-table-column prop="totalAmount" label="应收总额" width="120">
          <template #default="scope">¥{{ scope.row.totalAmount }}</template>
      </el-table-column>
      <el-table-column prop="receivedAmount" label="已收金额" width="120">
          <template #default="scope">¥{{ scope.row.receivedAmount }}</template>
      </el-table-column>
      <el-table-column label="欠款" width="120">
          <template #default="scope">
              <span class="text-danger">¥{{ (scope.row.totalAmount - scope.row.receivedAmount).toFixed(2) }}</span>
          </template>
      </el-table-column>
      <el-table-column prop="dueDate" label="到期日" width="120">
          <template #default="scope">{{ formatDate(scope.row.dueDate) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="statusMap[scope.row.status]?.type">{{ statusMap[scope.row.status]?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button v-if="scope.row.status !== 2" link type="success" @click="handleReceive(scope.row)">收款</el-button>
          <el-button link type="info" @click="handleDetail(scope.row)">明细</el-button>
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

    <!-- Receipt Dialog -->
    <el-dialog v-model="dialogVisible" title="收款登记" width="500px">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
            <el-form-item label="本次收款" prop="amount">
                <el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
            <el-form-item label="支付方式" prop="method">
                <el-select v-model="form.method" style="width: 100%">
                    <el-option label="银行转账" value="BANK" />
                    <el-option label="支付宝" value="ALIPAY" />
                    <el-option label="微信支付" value="WECHAT" />
                    <el-option label="现金" value="CASH" />
                    <el-option label="支票" value="CHECK" />
                </el-select>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
                <el-input v-model="form.remark" type="textarea" />
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitReceipt">确定收款</el-button>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getReceivablePage, createReceipt } from '@/api/finance/receivable'
import { getCustomerPage } from '@/api/base/customer'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const customers = ref([])
const queryParams = reactive({
  page: 1,
  size: 10,
  customerId: null,
  status: null
})

const statusMap = {
  0: { label: '待收款', type: 'danger' },
  1: { label: '部分收款', type: 'warning' },
  2: { label: '已结清', type: 'success' },
  3: { label: '坏账', type: 'info' }
}

const dialogVisible = ref(false)
const formRef = ref(null)
const form = reactive({
    id: null,
    amount: 0,
    method: 'BANK',
    remark: ''
})
const rules = {
    amount: [{ required: true, message: '请输入收款金额', trigger: 'blur' }],
    method: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getReceivablePage(queryParams)
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

const handleReceive = (row) => {
    form.id = row.id
    form.amount = (row.totalAmount - row.receivedAmount) // Default to full remaining info
    form.method = 'BANK'
    form.remark = ''
    dialogVisible.value = true
}

const submitReceipt = async () => {
    if (!formRef.value) return
    await formRef.value.validate(async (valid) => {
        if (valid) {
            await createReceipt(form.id, {
                amount: form.amount,
                method: form.method,
                remark: form.remark
            })
            ElMessage.success('收款登记成功')
            dialogVisible.value = false
            getList()
        }
    })
}

const handleDetail = (row) => {
    // TODO: Show receipt history dialog
    ElMessage.info('查看明细功能开发中')
}

const formatDate = (dateStr) => {
    if (!dateStr) return '-'
    return new Date(dateStr).toLocaleDateString()
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
.text-danger {
    color: #f56c6c;
}
</style>

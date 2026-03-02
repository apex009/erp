<template>
  <div class="app-container">
    <div class="filter-container">
      <el-select v-model="queryParams.supplierId" placeholder="供应商" clearable filterable style="width: 200px; margin-right: 10px;">
        <el-option
           v-for="item in suppliers"
           :key="item.id"
           :label="item.name"
           :value="item.id"
        />
      </el-select>
      <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px; margin-right: 10px;">
        <el-option label="待付款" :value="0" />
        <el-option label="部分付款" :value="1" />
        <el-option label="已结清" :value="2" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <el-table v-loading="loading" :data="list" border style="width: 100%; margin-top: 20px;">
      <el-table-column prop="payableNo" label="应付单号" width="180" />
      <el-table-column prop="sourceOrderNo" label="源单号" width="180" />
      <el-table-column prop="supplierName" label="供应商" min-width="150" />
      <el-table-column prop="totalAmount" label="应付总额" width="120">
          <template #default="scope">¥{{ scope.row.totalAmount }}</template>
      </el-table-column>
      <el-table-column prop="paidAmount" label="已付金额" width="120">
          <template #default="scope">¥{{ scope.row.paidAmount }}</template>
      </el-table-column>
      <el-table-column label="欠款" width="120">
          <template #default="scope">
              <span class="text-danger">¥{{ (scope.row.totalAmount - scope.row.paidAmount).toFixed(2) }}</span>
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
          <el-button v-if="scope.row.status !== 2 && authStore.hasPerm('api:payables:write')" link type="success" @click="handlePay(scope.row)">付款</el-button>
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

    <!-- Payment Dialog -->
    <el-dialog v-model="dialogVisible" title="付款登记" width="500px">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
            <el-form-item label="本次付款" prop="amount">
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
            <el-button type="primary" @click="submitPayment">确定付款</el-button>
        </template>
    </el-dialog>

    <!-- Detail Drawer -->
    <el-drawer v-model="detailVisible" title="应付明细记录" size="50%">
      <div v-loading="detailLoading" v-if="detailData">
        <el-descriptions title="基础信息" :column="2" border>
          <el-descriptions-item label="应付单号">{{ detailData.payableNo }}</el-descriptions-item>
          <el-descriptions-item label="源单号">{{ detailData.sourceOrderNo }}</el-descriptions-item>
          <el-descriptions-item label="供应商">{{ suppliers.find(s => s.id === detailData.supplierId)?.name || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="应付总额">¥{{ detailData.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="已付金额">¥{{ detailData.paidAmount }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusMap[detailData.status]?.type">{{ statusMap[detailData.status]?.label }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="到期日">{{ formatDate(detailData.dueDate) }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">付款流水记录</el-divider>
        <el-table :data="detailPayments" border>
          <el-table-column prop="paymentNo" label="付款流水号" width="180" />
          <el-table-column prop="amount" label="付款金额">
              <template #default="scope">¥{{ scope.row.amount }}</template>
          </el-table-column>
          <el-table-column prop="method" label="付款方式">
              <template #default="scope">
                  <el-tag size="small">{{ scope.row.method }}</el-tag>
              </template>
          </el-table-column>
          <el-table-column prop="paymentTime" label="付款时间" width="160">
              <template #default="scope">{{ new Date(scope.row.paymentTime).toLocaleString() }}</template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" />
        </el-table>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getPayablePage, createPayment, getPayable, getPayments } from '@/api/finance/payable'
import { getSupplierPage } from '@/api/base/supplier'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const suppliers = ref([])
const queryParams = reactive({
  page: 1,
  size: 10,
  supplierId: null,
  status: null
})

const statusMap = {
  0: { label: '待付款', type: 'danger' },
  1: { label: '部分付款', type: 'warning' },
  2: { label: '已结清', type: 'success' }
}

const dialogVisible = ref(false)
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)
const detailPayments = ref([])
const formRef = ref(null)
const form = reactive({
    id: null,
    amount: 0,
    method: 'BANK',
    remark: ''
})
const rules = {
    amount: [{ required: true, message: '请输入付款金额', trigger: 'blur' }],
    method: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getPayablePage(queryParams)
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
const handleSearch = () => {
  queryParams.page = 1
  getList()
}

const handlePay = (row) => {
    form.id = row.id
    form.amount = (row.totalAmount - row.paidAmount) // Default to full remaining info
    form.method = 'BANK'
    form.remark = ''
    dialogVisible.value = true
}

const submitPayment = async () => {
    if (!formRef.value) return
    await formRef.value.validate(async (valid) => {
        if (valid) {
            await createPayment(form.id, {
                amount: form.amount,
                method: form.method,
                remark: form.remark
            })
            ElMessage.success('付款登记成功')
            dialogVisible.value = false
            getList()
        }
    })
}

const handleDetail = async (row) => {
    detailVisible.value = true
    detailLoading.value = true
    detailData.value = null
    detailPayments.value = []
    try {
        const [detailRes, paymentsRes] = await Promise.all([
            getPayable(row.id),
            getPayments(row.id)
        ])
        detailData.value = detailRes
        detailPayments.value = paymentsRes
    } catch (e) {
        ElMessage.error('获取明细失败')
    } finally {
        detailLoading.value = false
    }
}

const formatDate = (dateStr) => {
    if (!dateStr) return '-'
    return new Date(dateStr).toLocaleDateString()
}

onMounted(() => {
  getList()
  getSuppliers()
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

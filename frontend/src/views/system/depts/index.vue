<template>
  <div class="dept-container">
    <div class="header-actions">
      <el-input 
        v-model="queryParams.keyword" 
        placeholder="搜索部门名称" 
        style="width: 200px" 
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="success" @click="handleCreate">新增部门</el-button>
    </div>

    <el-table :data="deptList" v-loading="loading" style="width: 100%; margin-top: 20px;" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="deptName" label="部门名称" />
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '正常' : '禁用' }}
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle">
      <el-form ref="deptFormRef" :model="deptForm" :rules="deptRules" label-width="100px">
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="deptForm.deptName" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="deptForm.status" :active-value="1" :inactive-value="0" />
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
import { getDeptPage, createDept, updateDept, deleteDept } from '@/api/dept'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const deptList = ref([])
const total = ref(0)
const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const deptFormRef = ref(null)
const deptForm = reactive({
  id: null,
  deptName: '',
  status: 1
})

const deptRules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getDeptPage(queryParams)
    deptList.value = res.records
    total.value = res.total
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
  getList()
}

const handleCreate = () => {
  dialogTitle.value = '新增部门'
  deptForm.id = null
  deptForm.deptName = ''
  deptForm.status = 1
  
  if (deptFormRef.value) deptFormRef.value.clearValidate()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑部门'
  Object.assign(deptForm, row)
  if (deptFormRef.value) deptFormRef.value.clearValidate()
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该部门吗?', '警告', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    await deleteDept(row.id)
    ElMessage.success('删除成功')
    getList()
  })
}

const submitForm = async () => {
  if (!deptFormRef.value) return
  
  await deptFormRef.value.validate(async (valid) => {
    if (valid) {
      if (deptForm.id) {
        await updateDept(deptForm.id, deptForm)
        ElMessage.success('更新成功')
      } else {
        await createDept(deptForm)
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
.dept-container {
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

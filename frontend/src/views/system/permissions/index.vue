<template>
  <div class="permission-container">
    <div class="header-actions">
      <el-input 
        v-model="queryParams.keyword" 
        placeholder="搜索名称/编码/路径" 
        style="width: 250px" 
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="success" @click="handleCreate">新增权限</el-button>
      <el-button type="warning" @click="handleSync">一键同步权限</el-button>
    </div>

    <el-table :data="permList" v-loading="loading" style="width: 100%; margin-top: 20px;" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="permName" label="权限名称" />
      <el-table-column prop="permCode" label="权限编码" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="scope">
           <el-tag :type="scope.row.type === 'menu' ? '' : 'warning'">{{ scope.row.type === 'menu' ? '菜单' : '按钮/API' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="path" label="路径" />
      <el-table-column prop="method" label="方法" width="100" />
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
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="getList"
        @current-change="getList"
      />
    </div>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle">
      <el-form ref="permFormRef" :model="permForm" :rules="permRules" label-width="120px">
        <el-form-item label="权限名称" prop="permName">
          <el-input v-model="permForm.permName" />
        </el-form-item>
        <el-form-item label="权限编码" prop="permCode">
          <el-input v-model="permForm.permCode" placeholder="例如: user:create" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="permForm.type" placeholder="选择类型">
            <el-option label="菜单" value="menu" />
            <el-option label="按钮/API" value="button" />
          </el-select>
        </el-form-item>
        <el-form-item label="路径" prop="path">
          <el-input v-model="permForm.path" placeholder="/api/users" />
        </el-form-item>
        <el-form-item label="方法" prop="method">
            <el-select v-model="permForm.method" placeholder="选择方法" clearable>
                <el-option label="GET" value="GET" />
                <el-option label="POST" value="POST" />
                <el-option label="PUT" value="PUT" />
                <el-option label="DELETE" value="DELETE" />
            </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="permForm.status" :active-value="1" :inactive-value="0" />
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
import { getPermissionPage, createPermission, updatePermission, deletePermission, syncPermissions } from '@/api/permission'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const permList = ref([])
const total = ref(0)
const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const permFormRef = ref(null)
const permForm = reactive({
  id: null,
  permName: '',
  permCode: '',
  type: 'button',
  path: '',
  method: '',
  status: 1
})

const permRules = {
  permName: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  permCode: [{ required: true, message: '请输入权限编码', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getPermissionPage(queryParams)
    permList.value = res.records
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

const handleSync = async () => {
    try {
        loading.value = true
        await syncPermissions()
        ElMessage.success('权限同步成功')
        getList()
    } catch (err) {
        console.error(err)
    } finally {
        loading.value = false
    }
}

const handleCreate = () => {
  dialogTitle.value = '新增权限'
  permForm.id = null
  permForm.permName = ''
  permForm.permCode = ''
  permForm.type = 'button'
  permForm.path = ''
  permForm.method = ''
  permForm.status = 1
  
  if (permFormRef.value) permFormRef.value.clearValidate()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑权限'
  Object.assign(permForm, row)
  if (permFormRef.value) permFormRef.value.clearValidate()
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该权限吗?', '警告', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    await deletePermission(row.id)
    ElMessage.success('删除成功')
    getList()
  })
}

const submitForm = async () => {
  if (!permFormRef.value) return
  
  await permFormRef.value.validate(async (valid) => {
    if (valid) {
      if (permForm.id) {
        await updatePermission(permForm.id, permForm)
        ElMessage.success('更新成功')
      } else {
        await createPermission(permForm)
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
.permission-container {
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

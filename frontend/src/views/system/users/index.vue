<template>
  <div class="user-container">
    <div class="header-actions">
      <el-input 
        v-model="queryParams.keyword" 
        placeholder="搜索用户名/昵称/手机号" 
        style="width: 250px" 
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="success" @click="handleCreate">新增用户</el-button>
    </div>

    <el-table :data="userList" v-loading="loading" style="width: 100%; margin-top: 20px;" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="email" label="邮箱" />
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
      <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="!!userForm.id" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname" />
        </el-form-item>
        <el-form-item label="所属部门" prop="deptId">
           <el-select v-model="userForm.deptId" placeholder="选择部门" clearable style="width: 100%">
              <el-option 
                v-for="dept in deptList" 
                :key="dept.id" 
                :label="dept.deptName" 
                :value="dept.id" 
              />
           </el-select>
        </el-form-item>
        <el-form-item label="关联角色" prop="roleIds">
           <el-select v-model="userForm.roleIds" multiple placeholder="选择角色" style="width: 100%">
              <el-option 
                v-for="role in roleList" 
                :key="role.id" 
                :label="role.roleName" 
                :value="role.id" 
              />
           </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!userForm.id">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="userForm.status" :active-value="1" :inactive-value="0" />
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
import { getUserPage, createUser, updateUser, deleteUser } from '@/api/user'
import { getDeptPage } from '@/api/dept'
import { getRolePage } from '@/api/role'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const userList = ref([])
const total = ref(0)
// For selects
const deptList = ref([])
const roleList = ref([])

const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const userFormRef = ref(null)
const userForm = reactive({
  id: null,
  username: '',
  nickname: '',
  deptId: null,
  roleIds: [],
  phone: '',
  email: '',
  password: '',
  status: 1
})

const userRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getUserPage(queryParams)
    userList.value = res.records
    total.value = res.total
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const initSelectData = async () => {
    try {
        // Fetch all depts/roles (assuming 100 is enough for dropdowns for now)
        const deptRes = await getDeptPage({ page: 1, size: 100 })
        deptList.value = deptRes.records
        const roleRes = await getRolePage({ page: 1, size: 100 })
        roleList.value = roleRes.records
    } catch (e) {
        console.error(e)
    }
}

const handleSearch = () => {
  queryParams.page = 1
  getList()
}

const handleCreate = () => {
  dialogTitle.value = '新增用户'
  userForm.id = null
  userForm.username = ''
  userForm.nickname = ''
  userForm.deptId = null
  userForm.roleIds = []
  userForm.phone = ''
  userForm.email = ''
  userForm.password = ''
  userForm.status = 1
  
  if (userFormRef.value) userFormRef.value.clearValidate()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  // Careful with object assignment, we need to handle existing roleIds if the backend sends them with the user object
  // If backend doesn't send roleIds in user detail, we might need a separate call. 
  // For now assuming user object has what we need or we edit basic info.
  // NOTE: The current backend User entity doesn't seem to return roleIds in the list API directly, 
  // but let's map what we have.
  Object.assign(userForm, row)
  userForm.password = '' // Don't show hash
  // Start with empty roles if not present
  if (!userForm.roleIds) userForm.roleIds = []
  
  if (userFormRef.value) userFormRef.value.clearValidate()
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该用户吗?', '警告', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    getList()
  })
}

const submitForm = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      if (userForm.id) {
        await updateUser(userForm.id, userForm)
        ElMessage.success('更新成功')
      } else {
        await createUser(userForm)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      getList()
    }
  })
}

onMounted(() => {
  getList()
  initSelectData()
})
</script>

<style scoped>
.user-container {
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

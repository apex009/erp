<template>
  <div class="role-container">
    <div class="header-actions">
      <el-input 
        v-model="queryParams.keyword" 
        placeholder="搜索角色名称/编码" 
        style="width: 200px" 
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="success" @click="handleCreate">新增角色</el-button>
    </div>

    <el-table :data="roleList" v-loading="loading" style="width: 100%; margin-top: 20px;" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="roleName" label="角色名称" />
      <el-table-column prop="roleCode" label="角色编码" />
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300">
        <template #default="scope">
          <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button link type="primary" @click="handlePerms(scope.row)">权限配置</el-button>
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

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle">
      <el-form ref="roleFormRef" :model="roleForm" :rules="roleRules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="roleForm.roleCode" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="roleForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Permission Config Dialog -->
    <el-dialog v-model="permDialogVisible" title="配置权限" width="500px">
      <div v-loading="treeLoading" style="max-height: 500px; overflow-y: auto;">
        <el-tree
          ref="treeRef"
          :data="allPerms"
          show-checkbox
          node-key="id"
          default-expand-all
          :props="{ label: 'label', children: 'children' }"
        />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="permDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPerms">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getRolePage, createRole, updateRole, deleteRole, getRolePermissions, updateRolePermissions } from '@/api/role'
import { getPermissionTree } from '@/api/permission'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const roleList = ref([])
const total = ref(0)
const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const roleFormRef = ref(null)
const roleForm = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  status: 1
})

// Permission Dialog Logic
const permDialogVisible = ref(false)
const treeLoading = ref(false)
const treeRef = ref(null)
const currentRoleId = ref(null)
const allPerms = ref([])

const roleRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getRolePage(queryParams)
    roleList.value = res.records
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
  dialogTitle.value = '新增角色'
  roleForm.id = null
  roleForm.roleName = ''
  roleForm.roleCode = ''
  roleForm.status = 1
  
  if (roleFormRef.value) roleFormRef.value.clearValidate()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑角色'
  Object.assign(roleForm, row)
  if (roleFormRef.value) roleFormRef.value.clearValidate()
  dialogVisible.value = true
}

// Open Permission Dialog
const handlePerms = async (row) => {
    currentRoleId.value = row.id
    permDialogVisible.value = true
    treeLoading.value = true
    
    try {
        // 1. Get all permissions as tree (if not loaded)
        if (allPerms.value.length === 0) {
            allPerms.value = await getPermissionTree()
        }
        
        // 2. Get current role's permissions (flat IDs)
        const rolePerms = await getRolePermissions(row.id)
        const rolePermIds = new Set(rolePerms.map(item => item.permId))

        // 3. Determine which tree nodes to check
        // A node (View/Manage) is checked if any of its permIds are present
        const checkedKeys = []
        
        const traverse = (nodes) => {
            nodes.forEach(node => {
                if (node.permIds && node.permIds.length > 0) {
                    // It's a leaf node (查看/管理)
                    // If at least one perm is assigned, consider it checked for the simplified view
                    const isChecked = node.permIds.some(id => rolePermIds.has(id))
                    if (isChecked) {
                        checkedKeys.push(node.id)
                    }
                }
                if (node.children) {
                    traverse(node.children)
                }
            })
        }
        traverse(allPerms.value)
        
        // Wait for next tick to ensure tree is rendered
        setTimeout(() => {
            if (treeRef.value) {
                treeRef.value.setCheckedKeys(checkedKeys)
            }
        }, 10)

    } catch (e) {
        console.error(e)
    } finally {
        treeLoading.value = false
    }
}

const submitPerms = async () => {
    try {
        const checkedNodes = treeRef.value.getCheckedNodes(false, true)
        const allPermIds = new Set()
        
        checkedNodes.forEach(node => {
            if (node.permIds) {
                node.permIds.forEach(id => allPermIds.add(id))
            }
        })

        await updateRolePermissions(currentRoleId.value, Array.from(allPermIds))
        ElMessage.success('权限更新成功')
        permDialogVisible.value = false
    } catch (e) {
        console.error(e)
    }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该角色吗?', '警告', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    getList()
  })
}

const submitForm = async () => {
  if (!roleFormRef.value) return
  
  await roleFormRef.value.validate(async (valid) => {
    if (valid) {
      if (roleForm.id) {
        await updateRole(roleForm.id, roleForm)
        ElMessage.success('更新成功')
      } else {
        await createRole(roleForm)
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
.role-container {
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

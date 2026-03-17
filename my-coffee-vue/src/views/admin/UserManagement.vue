<template>
  <div class="user-management">
    <div class="page-header">
      <h1>用户管理</h1>
      <el-button type="primary" @click="openAddUserDialog">
        <el-icon><Plus /></el-icon>
        添加用户
      </el-button>
    </div>
    
    <el-card class="filter-card" shadow="hover">
      <div class="filter-bar">
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索用户名或邮箱" 
          style="width: 300px;"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select 
          v-model="roleFilter" 
          placeholder="筛选角色" 
          style="width: 150px;"
          clearable
        >
          <el-option label="全部角色" value="" />
          <el-option label="管理员" value="admin" />
          <el-option label="商家" value="merchant" />
          <el-option label="普通用户" value="user" />
        </el-select>
        
        <el-select 
          v-model="statusFilter" 
          placeholder="筛选状态" 
          style="width: 150px;"
          clearable
        >
          <el-option label="全部状态" value="" />
          <el-option label="激活" value="active" />
          <el-option label="冻结" value="frozen" />
        </el-select>
        
        <el-button type="primary" @click="searchUsers">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        
        <el-button @click="resetFilters">
          <el-icon><RefreshRight /></el-icon>
          重置
        </el-button>
      </div>
    </el-card>
    
    <el-card class="table-card" shadow="hover">
      <el-table 
        :data="users" 
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column label="用户信息" min-width="200">
          <template #default="scope">
            <div class="user-info">
              <el-avatar :size="40">
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="user-details">
                <div class="username">{{ scope.row.username }}</div>
                <div class="email">{{ scope.row.email || '未设置' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="phone" label="电话" width="130">
          <template #default="scope">
            {{ scope.row.phone || '未设置' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag :type="getRoleType(scope.row.role)" size="small">
              {{ getRoleText(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-switch 
              v-model="scope.row.status" 
              :active-value="'active'" 
              :inactive-value="'frozen'" 
              @change="handleStatusChange(scope.row)"
              active-text="激活" 
              inactive-text="冻结"
            />
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="editUser(scope.row)" link>
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button size="small" type="warning" @click="resetUserPassword(scope.row)" link>
              <el-icon><Key /></el-icon>
              重置密码
            </el-button>
            <el-button size="small" type="danger" @click="deleteUser(scope.row)" link>
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="totalUsers"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 添加/编辑用户对话框 -->
    <el-dialog 
      :title="editingUser ? '编辑用户' : '添加用户'" 
      v-model="dialogVisible" 
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        
        <el-form-item label="电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入电话" />
        </el-form-item>
        
        <el-form-item label="密码" prop="password" v-if="!editingUser">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        
        <el-form-item label="角色" prop="role">
          <el-select v-model="formData.role" style="width: 100%;">
            <el-option label="管理员" value="admin" />
            <el-option label="商家" value="merchant" />
            <el-option label="普通用户" value="user" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio label="active">激活</el-radio>
            <el-radio label="frozen">冻结</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser" :loading="saving">
          {{ saving ? '保存中...' : '保存' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Search, RefreshRight, User, Edit, Key, Delete 
} from '@element-plus/icons-vue'
import { getUserPage, updateStatus, updateUser, resetPassword, addUser } from '@/api/user'
import type { UserResponseDTO } from '@/api/user'

interface User {
  id: number
  username: string
  email: string
  phone: string
  role: string
  status: string
  createdAt: string
}

interface FormData {
  username: string
  email: string
  phone: string
  password?: string
  role: string
  status: string
}

const searchQuery = ref('')
const roleFilter = ref('')
const statusFilter = ref('')
const users = ref<User[]>([])
const totalUsers = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const dialogVisible = ref(false)
const saving = ref(false)
const editingUser = ref<User | null>(null)
const formRef = ref()

const formData = ref<FormData>({
  username: '',
  email: '',
  phone: '',
  password: '',
  role: 'user',
  status: 'active'
})

const rules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
})

const getRoleType = (role: string) => {
  const typeMap: Record<string, any> = {
    admin: 'danger',
    merchant: 'warning',
    user: 'success'
  }
  return typeMap[role] || 'info'
}

const getRoleText = (role: string) => {
  const textMap: Record<string, string> = {
    admin: '管理员',
    merchant: '商家',
    user: '普通用户'
  }
  return textMap[role] || role
}

const loadUsers = async () => {
  loading.value = true
  try {
    const response = await getUserPage(currentPage.value, pageSize.value)
    if (response && response.records) {
      users.value = response.records.map((user: UserResponseDTO) => ({
        id: user.id,
        username: user.username,
        email: user.email || '',
        phone: user.phone || '',
        role: user.role,
        status: user.status,
        createdAt: user.createdAt
      }))
      totalUsers.value = response.total || 0
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const searchUsers = async () => {
  loading.value = true
  try {
    const response = await getUserPage(1, pageSize.value)
    if (response && response.records) {
      let filteredUsers = response.records
      
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filteredUsers = filteredUsers.filter(user => 
          user.username.toLowerCase().includes(query) || 
          (user.email && user.email.toLowerCase().includes(query))
        )
      }
      
      if (roleFilter.value) {
        filteredUsers = filteredUsers.filter(user => user.role === roleFilter.value)
      }
      
      if (statusFilter.value) {
        filteredUsers = filteredUsers.filter(user => user.status === statusFilter.value)
      }
      
      users.value = filteredUsers.map((user: UserResponseDTO) => ({
        id: user.id,
        username: user.username,
        email: user.email || '',
        phone: user.phone || '',
        role: user.role,
        status: user.status,
        createdAt: user.createdAt
      }))
      totalUsers.value = filteredUsers.length
      currentPage.value = 1
    }
  } catch (error) {
    console.error('搜索用户失败:', error)
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  searchQuery.value = ''
  roleFilter.value = ''
  statusFilter.value = ''
  currentPage.value = 1
  loadUsers()
}

const openAddUserDialog = () => {
  editingUser.value = null
  formData.value = {
    username: '',
    email: '',
    phone: '',
    password: '',
    role: 'user',
    status: 'active'
  }
  dialogVisible.value = true
}

const editUser = (user: User) => {
  editingUser.value = user
  formData.value = {
    username: user.username,
    email: user.email,
    phone: user.phone,
    role: user.role,
    status: user.status
  }
  dialogVisible.value = true
}

const handleStatusChange = async (user: User) => {
  try {
    await updateStatus(user.id, user.status as 'active' | 'frozen' | 'deleted')
    ElMessage.success('用户状态已更新')
  } catch (error) {
    console.error('更新用户状态失败:', error)
    user.status = user.status === 'active' ? 'frozen' : 'active'
    ElMessage.error('更新状态失败')
  }
}

const resetUserPassword = async (user: User) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户 ${user.username} 的密码吗？`,
      '重置密码',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await resetPassword(user.id)
    ElMessage.success(`密码已重置为：${user.email}123`)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重置密码失败')
    }
  }
}

const deleteUser = async (user: User) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 ${user.username} 吗？此操作不可恢复！`,
      '删除用户',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    await updateStatus(user.id, 'deleted')
    ElMessage.success('用户已删除')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除用户失败')
    }
  }
}

const saveUser = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    saving.value = true
    try {
      if (editingUser.value) {
        const response = await updateUser(editingUser.value.id, formData.value)
        if (response.data.code === 200) {
          ElMessage.success('用户信息更新成功')
          dialogVisible.value = false
          loadUsers()
        } else {
          ElMessage.error(response.data.message || '更新失败')
        }
      } else {
        const response = await addUser(formData.value)
        if (response.data.code === 200) {
          ElMessage.success('用户添加成功')
          dialogVisible.value = false
          loadUsers()
        } else {
          ElMessage.error(response.data.message || '添加失败')
        }
      }
    } catch (error) {
      console.error('保存用户失败:', error)
      ElMessage.error('保存失败')
    } finally {
      saving.value = false
    }
  })
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  loadUsers()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadUsers()
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-management {
  animation: fadeIn 0.5s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.table-card {
  border-radius: 12px;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-details {
  flex: 1;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.email {
  font-size: 12px;
  color: #909399;
}

.pagination-container {
  padding: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
  }
  
  .filter-bar > * {
    width: 100% !important;
  }
}
</style>

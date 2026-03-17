<template>
  <div class="profile-page">
    <div class="profile-header">
      <h1>个人中心</h1>
    </div>
    
    <el-row :gutter="20">
      <!-- 左侧菜单 -->
      <el-col :xs="24" :sm="6">
        <el-card class="menu-card" shadow="hover">
          <el-menu :default-active="activeTab" @select="handleMenuSelect">
            <el-menu-item index="info">
              <el-icon><User /></el-icon>
              <span>个人信息</span>
            </el-menu-item>
            <el-menu-item index="password">
              <el-icon><Lock /></el-icon>
              <span>修改密码</span>
            </el-menu-item>
            <el-menu-item index="address">
              <el-icon><Location /></el-icon>
              <span>地址管理</span>
            </el-menu-item>
            <el-menu-item index="orders">
              <el-icon><ShoppingBag /></el-icon>
              <span>我的订单</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>
      
      <!-- 右侧内容 -->
      <el-col :xs="24" :sm="18">
        <!-- 个人信息 -->
        <el-card v-show="activeTab === 'info'" class="content-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
            </div>
          </template>
          
          <div class="profile-info">
            <div class="avatar-section">
              <el-avatar :size="100" :src="userAvatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <el-button type="primary" size="small" style="margin-top: 16px;">
                更换头像
              </el-button>
            </div>
            
            <el-form :model="userForm" :rules="userRules" ref="userFormRef" label-width="100px" class="info-form">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="userForm.username" disabled />
              </el-form-item>
              
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="userForm.email" />
              </el-form-item>
              
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="userForm.phone" />
              </el-form-item>
              
              <el-form-item label="角色">
                <el-tag :type="getRoleType(userForm.role)">{{ getRoleText(userForm.role) }}</el-tag>
              </el-form-item>
              
              <el-form-item label="注册时间">
                <span>{{ userForm.createdAt }}</span>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="updateUserInfo" :loading="saving">
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
        
        <!-- 修改密码 -->
        <el-card v-show="activeTab === 'password'" class="content-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>修改密码</span>
            </div>
          </template>
          
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="120px" style="max-width: 500px;">
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="updatePassword" :loading="saving">
                修改密码
              </el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        
        <!-- 地址管理 -->
        <el-card v-show="activeTab === 'address'" class="content-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>地址管理</span>
              <el-button type="primary" size="small" @click="openAddressDialog()">
                <el-icon><Plus /></el-icon>
                添加地址
              </el-button>
            </div>
          </template>
          
          <div v-loading="loadingAddress">
            <el-empty v-if="addresses.length === 0" description="暂无收货地址" />
            
            <div v-else class="address-list">
              <div 
                v-for="addr in addresses" 
                :key="addr.id" 
                class="address-item"
                :class="{ 'is-default': addr.isDefault }"
              >
                <div class="address-content">
                  <div class="address-header">
                    <span class="recipient">{{ addr.recipientName }}</span>
                    <span class="phone">{{ addr.phone }}</span>
                    <el-tag v-if="addr.isDefault" type="success" size="small">默认</el-tag>
                  </div>
                  <div class="address-detail">
                    {{ addr.fullAddress }}
                  </div>
                </div>
                <div class="address-actions">
                  <el-button size="small" link @click="openAddressDialog(addr)">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-button>
                  <el-button v-if="!addr.isDefault" size="small" link @click="setDefaultAddress(addr.id)">
                    <el-icon><Star /></el-icon>
                    设为默认
                  </el-button>
                  <el-button size="small" link type="danger" @click="deleteAddress(addr.id)">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
        
        <!-- 我的订单 -->
        <el-card v-show="activeTab === 'orders'" class="content-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>我的订单</span>
            </div>
          </template>
          
          <el-table :data="orders" v-loading="loadingOrders" stripe>
            <el-table-column prop="id" label="订单ID" width="100" />
            <el-table-column label="商品" min-width="200">
              <template #default="scope">
                <div v-for="item in scope.row.items?.slice(0, 2)" :key="item.id" class="order-item">
                  {{ item.productName }} × {{ item.quantity }}
                </div>
                <div v-if="scope.row.items?.length > 2" class="more-items">
                  +{{ scope.row.items.length - 2 }} 件商品
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="totalPrice" label="金额" width="120">
              <template #default="scope">
                <span class="price">¥{{ scope.row.totalPrice.toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getOrderStatusType(scope.row.status)" size="small">
                  {{ getOrderStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="下单时间" width="180" />
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button size="small" link @click="viewOrderDetail(scope.row.id)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="orderPage"
              v-model:page-size="orderPageSize"
              :total="totalOrders"
              layout="total, prev, pager, next"
              @current-change="loadOrders"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 地址编辑对话框 -->
    <el-dialog 
      :title="editingAddress ? '编辑地址' : '添加地址'" 
      v-model="addressDialogVisible" 
      width="500px"
    >
      <el-form :model="addressForm" :rules="addressRules" ref="addressFormRef" label-width="100px">
        <el-form-item label="收货人" prop="recipientName">
          <el-input v-model="addressForm.recipientName" placeholder="请输入收货人姓名" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        
        <el-form-item label="详细地址" prop="fullAddress">
          <el-input 
            v-model="addressForm.fullAddress" 
            type="textarea" 
            :rows="3"
            placeholder="请输入详细地址"
          />
        </el-form-item>
        
        <el-form-item label="设为默认">
          <el-switch v-model="addressForm.isDefault" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAddress" :loading="saving">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  User, Lock, Location, ShoppingBag, Plus, Edit, Delete, Star 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { updateUser } from '@/api/user'
import { 
  getUserAddresses, addAddress, updateAddress, 
  deleteAddress as deleteAddressApi, setDefaultAddress as setDefaultAddressApi 
} from '@/api/address'
import { getUserOrders } from '@/api/order'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('info')
const userAvatar = ref('')
const saving = ref(false)
const loadingAddress = ref(false)
const loadingOrders = ref(false)

// 用户信息表单
const userFormRef = ref()
const userForm = ref({
  username: '',
  email: '',
  phone: '',
  role: '',
  createdAt: ''
})

const userRules = reactive({
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
})

// 修改密码表单
const passwordFormRef = ref()
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = reactive({
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
})

// 地址管理
const addresses = ref<any[]>([])
const addressDialogVisible = ref(false)
const editingAddress = ref<any>(null)
const addressFormRef = ref()
const addressForm = ref({
  recipientName: '',
  phone: '',
  fullAddress: '',
  isDefault: false
})

const addressRules = reactive({
  recipientName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  fullAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
})

// 订单列表
const orders = ref<any[]>([])
const orderPage = ref(1)
const orderPageSize = ref(5)
const totalOrders = ref(0)

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

const getOrderStatusType = (status: string) => {
  const typeMap: Record<string, any> = {
    pending: 'warning',
    paid: 'success',
    shipped: 'info',
    completed: 'success',
    cancelled: 'danger'
  }
  return typeMap[status] || 'info'
}

const getOrderStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    pending: '待支付',
    paid: '已支付',
    shipped: '已发货',
    completed: '已完成',
    cancelled: '已取消'
  }
  return textMap[status] || status
}

const handleMenuSelect = (index: string) => {
  activeTab.value = index
  
  if (index === 'address' && addresses.value.length === 0) {
    loadAddresses()
  } else if (index === 'orders' && orders.value.length === 0) {
    loadOrders()
  }
}

const loadUserInfo = () => {
  const user = userStore.userInfo
  if (user) {
    userForm.value = {
      username: user.username,
      email: user.email || '',
      phone: user.phone || '',
      role: user.role,
      createdAt: user.createdAt || ''
    }
  }
}

const updateUserInfo = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    saving.value = true
    try {
      const userId = userStore.userInfo?.id
      if (!userId) return
      
      const response = await updateUser(userId, {
        email: userForm.value.email,
        phone: userForm.value.phone
      })
      
      if (response.data.code === 200) {
        ElMessage.success('个人信息更新成功')
        // 更新本地用户信息
        if (userStore.userInfo) {
          userStore.userInfo.email = userForm.value.email
          userStore.userInfo.phone = userForm.value.phone
        }
      } else {
        ElMessage.error(response.data.message || '更新失败')
      }
    } catch (error) {
      ElMessage.error('更新失败')
    } finally {
      saving.value = false
    }
  })
}

const updatePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    saving.value = true
    try {
      // TODO: 调用修改密码API
      ElMessage.success('密码修改成功，请重新登录')
      setTimeout(() => {
        userStore.logout()
        router.push('/login')
      }, 1500)
    } catch (error) {
      ElMessage.error('修改密码失败')
    } finally {
      saving.value = false
    }
  })
}

const resetPasswordForm = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordFormRef.value?.clearValidate()
}

const loadAddresses = async () => {
  loadingAddress.value = true
  try {
    const userId = userStore.userInfo?.id
    if (!userId) return
    
    const response = await getUserAddresses(userId)
    if (response.data.code === 200) {
      addresses.value = response.data.data || []
    }
  } catch (error) {
    ElMessage.error('加载地址失败')
  } finally {
    loadingAddress.value = false
  }
}

const openAddressDialog = (address?: any) => {
  editingAddress.value = address
  if (address) {
    addressForm.value = {
      recipientName: address.recipientName,
      phone: address.phone,
      fullAddress: address.fullAddress,
      isDefault: address.isDefault
    }
  } else {
    addressForm.value = {
      recipientName: '',
      phone: '',
      fullAddress: '',
      isDefault: false
    }
  }
  addressDialogVisible.value = true
}

const saveAddress = async () => {
  if (!addressFormRef.value) return
  
  await addressFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    saving.value = true
    try {
      const userId = userStore.userInfo?.id
      if (!userId) return
      
      const data = {
        ...addressForm.value,
        userId
      }
      
      let response
      if (editingAddress.value) {
        response = await updateAddress(editingAddress.value.id, data)
      } else {
        response = await addAddress(data)
      }
      
      if (response.data.code === 200) {
        ElMessage.success(editingAddress.value ? '地址更新成功' : '地址添加成功')
        addressDialogVisible.value = false
        loadAddresses()
      } else {
        ElMessage.error(response.data.message || '操作失败')
      }
    } catch (error) {
      ElMessage.error('操作失败')
    } finally {
      saving.value = false
    }
  })
}

const setDefaultAddress = async (id: number) => {
  try {
    const response = await setDefaultAddressApi(id)
    if (response.data.code === 200) {
      ElMessage.success('设置默认地址成功')
      loadAddresses()
    } else {
      ElMessage.error(response.data.message || '设置失败')
    }
  } catch (error) {
    ElMessage.error('设置失败')
  }
}

const deleteAddress = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteAddressApi(id)
    if (response.data.code === 200) {
      ElMessage.success('地址删除成功')
      loadAddresses()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const loadOrders = async () => {
  loadingOrders.value = true
  try {
    const userId = userStore.userInfo?.id
    if (!userId) return
    
    const response = await getUserOrders(orderPage.value, orderPageSize.value, userId)
    if (response && response.records) {
      orders.value = response.records
      totalOrders.value = response.total || 0
    }
  } catch (error) {
    ElMessage.error('加载订单失败')
  } finally {
    loadingOrders.value = false
  }
}

const viewOrderDetail = (orderId: number) => {
  router.push(`/order/${orderId}`)
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-page {
  padding: 20px;
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

.profile-header {
  margin-bottom: 20px;
}

.profile-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.menu-card {
  border-radius: 12px;
}

.menu-card :deep(.el-card__body) {
  padding: 0;
}

.menu-card :deep(.el-menu) {
  border: none;
}

.content-card {
  border-radius: 12px;
  min-height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

.profile-info {
  display: flex;
  gap: 40px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.info-form {
  flex: 1;
}

.address-list {
  display: grid;
  gap: 16px;
}

.address-item {
  padding: 20px;
  border: 1px solid #e8eaed;
  border-radius: 8px;
  transition: all 0.3s;
}

.address-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
}

.address-item.is-default {
  border-color: #67c23a;
  background: #f0f9ff;
}

.address-content {
  margin-bottom: 12px;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.recipient {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.phone {
  color: #606266;
}

.address-detail {
  color: #606266;
  line-height: 1.6;
}

.address-actions {
  display: flex;
  gap: 8px;
}

.order-item {
  font-size: 13px;
  color: #606266;
  margin-bottom: 4px;
}

.more-items {
  font-size: 12px;
  color: #909399;
}

.price {
  font-size: 14px;
  font-weight: 600;
  color: #f56c6c;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .profile-info {
    flex-direction: column;
    gap: 20px;
  }
}
</style>

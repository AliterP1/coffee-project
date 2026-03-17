<template>
  <div class="merchant-settings">
    <h1>店铺设置</h1>
    
    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="basicForm" label-width="120px" style="max-width: 600px;">
            <el-form-item label="店铺名称">
              <el-input v-model="basicForm.shopName" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="basicForm.phone" />
            </el-form-item>
            <el-form-item label="联系邮箱">
              <el-input v-model="basicForm.email" />
            </el-form-item>
            <el-form-item label="店铺地址">
              <el-input v-model="basicForm.address" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="店铺简介">
              <el-input v-model="basicForm.description" type="textarea" :rows="5" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveBasicInfo">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="账户安全" name="security">
          <el-form :model="securityForm" label-width="120px" style="max-width: 600px;">
            <el-form-item label="当前密码">
              <el-input v-model="securityForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="securityForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input v-model="securityForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="通知设置" name="notification">
          <el-form label-width="120px" style="max-width: 600px;">
            <el-form-item label="新订单通知">
              <el-switch v-model="notificationForm.newOrder" />
            </el-form-item>
            <el-form-item label="评价通知">
              <el-switch v-model="notificationForm.newReview" />
            </el-form-item>
            <el-form-item label="库存预警">
              <el-switch v-model="notificationForm.stockAlert" />
            </el-form-item>
            <el-form-item label="系统消息">
              <el-switch v-model="notificationForm.systemMessage" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveNotificationSettings">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { changePassword as apiChangePassword } from '@/api/user'

const userStore = useUserStore()
const activeTab = ref('basic')

const basicForm = ref({
  shopName: userStore.userInfo?.username || '',
  phone: userStore.userInfo?.phone || '',
  email: userStore.userInfo?.email || '',
  address: '',
  description: ''
})

const securityForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const notificationForm = ref({
  newOrder: true,
  newReview: true,
  stockAlert: true,
  systemMessage: true
})

const saveBasicInfo = () => {
  ElMessage.success('基本信息保存成功')
}

const changePassword = async () => {
  if (!securityForm.value.oldPassword || !securityForm.value.newPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  if (securityForm.value.newPassword !== securityForm.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  
  try {
    const userId = userStore.userInfo?.id
    if (userId) {
      await apiChangePassword(userId, securityForm.value.oldPassword, securityForm.value.newPassword)
      ElMessage.success('密码修改成功')
      securityForm.value = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    }
  } catch (error) {
    ElMessage.error('密码修改失败')
  }
}

const saveNotificationSettings = () => {
  ElMessage.success('通知设置保存成功')
}
</script>

<style scoped>
.merchant-settings {
  padding: 20px;
}

.merchant-settings h1 {
  margin: 0 0 20px 0;
  font-size: 24px;
  color: #333;
}
</style>

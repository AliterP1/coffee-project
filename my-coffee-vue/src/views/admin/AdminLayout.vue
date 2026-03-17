<template>
  <div class="admin-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="isCollapse ? '64px' : '240px'" class="admin-sidebar">
        <div class="logo">
          <transition name="fade">
            <h2 v-if="!isCollapse">☕ Coffee Admin</h2>
            <h2 v-else class="logo-mini">☕</h2>
          </transition>
        </div>
        
        <el-menu 
          :default-active="activeMenu" 
          :collapse="isCollapse"
          mode="vertical" 
          router
          background-color="#1a1d2e"
          text-color="#a0a4b8"
          active-text-color="#ffffff"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据概览</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/products">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/orders">
            <el-icon><ShoppingCart /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
        </el-menu>
        
        <div class="sidebar-footer">
          <el-button 
            :icon="isCollapse ? Expand : Fold" 
            @click="toggleCollapse"
            text
            class="collapse-btn"
          >
            <span v-if="!isCollapse">收起</span>
          </el-button>
        </div>
      </el-aside>
      
      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部导航 -->
        <el-header class="admin-header">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="currentPageName">{{ currentPageName }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <div class="user-info">
                <el-avatar :size="36" :src="userAvatar">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <span class="username">{{ userStore.userInfo?.username || '管理员' }}</span>
                <el-icon class="arrow"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="settings">
                    <el-icon><Setting /></el-icon>
                    系统设置
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <!-- 主要内容 -->
        <el-main class="admin-main">
          <transition name="fade-transform" mode="out-in">
            <router-view />
          </transition>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { 
  DataAnalysis, User, Goods, ShoppingCart, 
  Expand, Fold, ArrowDown, Setting, SwitchButton 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const userAvatar = ref('')

const activeMenu = computed(() => route.path)

const currentPageName = computed(() => {
  const pathMap: Record<string, string> = {
    '/admin/dashboard': '数据概览',
    '/admin/users': '用户管理',
    '/admin/products': '商品管理',
    '/admin/orders': '订单管理'
  }
  return pathMap[route.path] || ''
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      ElMessage.info('系统设置功能开发中')
      break
    case 'logout':
      await userStore.logout()
      ElMessage.success('退出成功')
      router.push('/login')
      break
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  overflow: hidden;
}

.admin-sidebar {
  background: linear-gradient(180deg, #1a1d2e 0%, #16192b 100%);
  color: white;
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 20px;
}

.logo h2 {
  margin: 0;
  color: white;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 1px;
}

.logo-mini {
  font-size: 24px;
}

.el-menu {
  border: none;
  flex: 1;
}

.el-menu-item {
  margin: 4px 12px;
  border-radius: 8px;
  transition: all 0.3s;
}

.el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.1) !important;
}

.el-menu-item.is-active {
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%) !important;
  color: white !important;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.collapse-btn {
  width: 100%;
  color: #a0a4b8;
}

.collapse-btn:hover {
  color: white;
}

.admin-header {
  background: white;
  border-bottom: 1px solid #e8eaed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.3s;
}

.user-info:hover {
  background: #f5f7fa;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.arrow {
  color: #909399;
  transition: transform 0.3s;
}

.user-info:hover .arrow {
  transform: rotate(180deg);
}

.admin-main {
  background: #f5f7fa;
  padding: 24px;
  overflow-y: auto;
  height: calc(100vh - 64px);
}

/* 动画效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

/* 滚动条样式 */
.admin-main::-webkit-scrollbar {
  width: 6px;
}

.admin-main::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.admin-main::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}
</style>

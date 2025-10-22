<template>
  <div class="account-sidebar">
    <ul class="account-nav">
      <li :class="['nav-item', { active: activeRoute === 'profile' }]" @click="$emit('navigate', 'profile')">
        <el-icon><User /></el-icon>
        <span>个人信息</span>
      </li>
      <li :class="['nav-item', { active: activeRoute === 'orders' }]" @click="$emit('navigate', 'orders')">
        <el-icon><ShoppingBag /></el-icon>
        <span>我的订单</span>
      </li>
      <li :class="['nav-item', { active: activeRoute === 'favorites' }]" @click="$emit('navigate', 'favorites')">
        <el-icon><Collection /></el-icon>
        <span>我的收藏</span>
      </li>
      <li :class="['nav-item', { active: activeRoute === 'addresses' }]" @click="$emit('navigate', 'addresses')">
        <el-icon><MapLocation /></el-icon>
        <span>收货地址</span>
      </li>
      <li :class="['nav-item', { active: activeRoute === 'settings' }]" @click="$emit('navigate', 'settings')">
        <el-icon><Setting /></el-icon>
        <span>账户设置</span>
      </li>
    </ul>
    
    <el-button type="primary" @click="$emit('logout')" class="logout-btn">
      <el-icon><SwitchButton /></el-icon>
      退出登录
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { User, ShoppingBag, MapLocation, Setting,Collection,SwitchButton } from '@element-plus/icons-vue';
import { computed } from 'vue';
import { useRoute } from 'vue-router';

// 获取当前路由
const route = useRoute();

// 计算当前激活的导航项
const activeRoute = computed(() => {
  // 根据当前路径判断激活的导航项
  const path = route.path;
  if (path.includes('account')) return 'profile';
  if (path.includes('orders')) return 'orders';
  if (path.includes('favorites')) return 'favorites';
  if (path.includes('address')) return 'addresses';
  if (path.includes('settings')) return 'settings';
  return '';
});

// 定义emits
defineEmits<{
  navigate: [route: string];
  logout: [];
}>();
</script>

<style scoped>
.account-sidebar {
 width: 250px;
  background: #fff;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  height: fit-content;
}

.account-nav {
  list-style: none;
  padding: 0;
  margin: 0 0 30px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  padding: 0.8rem 1rem;
  margin-bottom: 0.5rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #6e4a2e;
}

.nav-item:hover {
  background-color: #f8f5f2;
  color: #8c5e46;
}

.nav-item.active {
  background-color: #f0e6e0;
  color: #a87b5e;
  font-weight: 500;
}

.nav-item span {
  margin-left: 10px;
  font-size: 14px;
}

.logout-btn {
  color: #a87b5e;
  width: 100%;
  background-color: #f8f5f2;
  justify-content: center;
  border: none;
  outline: none;
}
</style>
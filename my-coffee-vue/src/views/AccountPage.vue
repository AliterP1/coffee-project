<template>
  <div class="account-container">
    <Header />
    <div class="account-content">
      <div class="account-header">
        <h2>个人中心</h2>
        <p class="welcome-message">欢迎回来，{{ userInfo?.username || '用户' }}！</p>
      </div>
      
      <div class="account-main">
        <!-- 左侧导航 -->
        <AccountSidebar 
          @navigate="handleNavigation"
          @logout="handleLogout"
        />
        <!-- 右侧内容 -->
        
        <!-- 右侧内容 -->
        <div class="account-panel">
          <div class="panel-section">
            <h3>基本信息</h3>
            <div class="info-grid">
              <div class="info-item">
                <label>用户名：</label>
                <span>{{ userInfo?.username }}</span>
              </div>
              <div class="info-item">
                <label>邮箱：</label>
                <span>{{ userInfo?.email || '-' }}</span>
              </div>
              <div class="info-item">
                <label>注册时间：</label>
                <span>{{ formatDate(userInfo?.createdAt) || '-' }}</span>
              </div>
              <div class="info-item">
                <label>上次登录：</label>
                <span>{{ formatDate(userInfo?.lastLoginTime) || '-' }}</span>
              </div>
            </div>
          </div>
          
          <div class="panel-section">
            <h3>订单概览</h3>
            <div class="order-stats">
              <div class="stat-item">
                <div class="stat-number">{{ orderStats.pending }}</div>
                <div class="stat-label">待付款</div>
              </div>
              <div class="stat-item">
                <div class="stat-number">{{ orderStats.paid }}</div>
                <div class="stat-label">待发货</div>
              </div>
              <div class="stat-item">
                <div class="stat-number">{{ orderStats.shipped }}</div>
                <div class="stat-label">待收货</div>
              </div>
              <div class="stat-item">
                <div class="stat-number">{{ orderStats.completed }}</div>
                <div class="stat-label">已完成</div>
              </div>
            </div>
          </div>
          
          <!-- 订单入口 -->
          <div class="panel-section">
            <h3>我的订单</h3>
            <div class="order-entrance">
              <el-button type="primary" @click="goToOrders" class="view-all-orders-btn">
                查看全部订单
              </el-button>
            </div>
          </div>
          
          <div class="panel-section">
            <h3>最近浏览</h3>
            <div class="recent-products">
              <div v-if="recentProducts.length === 0" class="empty-message">
                <el-empty description="暂无浏览记录" />
              </div>
              <!-- <div v-else class="product-list">
                <div class="product-item" v-for="product in recentProducts" :key="product.id">
                  <img :src="product.images[0]" :alt="product.name" class="product-image" />
                  <div class="product-info">
                    <div class="product-name">{{ product.name }}</div>
                    <div class="product-price">¥{{ product.price }}</div>
                  </div>
                </div>
              </div> -->
            </div>
          </div>
        </div>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { getUserOrders } from '@/api/order';
import type { OrderResponseDTO } from '@/api/order';
// import { User, ShoppingBag, Heart, MapLocation, Setting, Logout } from '@element-plus/icons-vue';
import Header from '@/components/Header.vue';
import Footer from '@/components/Footer.vue';
import AccountSidebar from '@/components/AccountSidebar.vue';

const router = useRouter();
const userStore = useUserStore();

// 订单状态数据
const orderStats = ref({
  pending: 0,
  paid: 0,
  shipped: 0,
  completed: 0
});

// 订单列表数据
const orders = ref<OrderResponseDTO[]>([]);
const totalOrders = ref(0);
const currentPage = ref(1);
const pageSize = ref(5);
const loadingOrders = ref(false);

// 最近浏览数据
const recentProducts = ref([
  // 模拟数据，实际项目中应从API获取
]);

// 格式化日期时间
const formatDate = (date?: string) => {
  if (!date) return '';
  const d = new Date(date);
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};
// 加载用户订单数据
  const loadUserOrders = async (page = 1, size = 99999) => {
    if (!userStore.userInfo?.id) return;
    
    loadingOrders.value = true;
    try {
      const res = await getUserOrders(page, size, userStore.userInfo.id);
      if (res) {
        // 统计订单状态数量
        updateOrderStats(res.records);
        // 过滤只显示未支付订单 (pending/待付款)
        orders.value = (res.records || []).filter(order => 
          order.status === 'pending' || order.status === '待付款'
        );
        // 更新订单总数为过滤后的数量
        totalOrders.value = orders.value.length;
        currentPage.value = res.current || 1;
        pageSize.value = res.size || 10;    
      }
    } catch (error) {
      console.error('加载订单失败:', error);
    } finally {
      loadingOrders.value = false;
    }
  };

// 更新订单状态统计
const updateOrderStats = (orderList: OrderResponseDTO[]) => {
  const stats = {
    pending: 0,
    paid: 0,
    shipped: 0,
    completed: 0
  };
  
  orderList.forEach(order => {
    switch (order.status) {
      case 'pending':
      case '待付款':
        stats.pending++;
        break;
      case 'shipped':
      case '待收货':
        stats.shipped++;
        break;
      case 'paid':
      case '待发货':
        stats.paid++;
        break;
      case 'completed':
      case '已完成':
        stats.completed++;
        break;
    }
  });
  
  orderStats.value = stats;
};

// 处理导航事件
const handleNavigation = (route: string) => {
  switch(route) {
    case 'orders':
      router.push('/orders');
      break;
    case 'profile':
      // 已经在个人信息页面，不需要跳转
      break;
    // 其他导航项的处理可以在这里添加
    default:
      break;
  }
};

// 跳转到订单页面
const goToOrders = () => {
  router.push('/orders');
};


// 获取用户信息
const userInfo = computed(() => userStore.userInfo);
// 退出登录
const handleLogout = async () => {
  try {
    await userStore.logout();
    router.push('/login');
  } catch (err) {
    console.error('退出登录失败:', err);
  }
};

// 初始化页面数据
onMounted(async () => {
  // 检查用户是否登录
  if (!userStore.isLogin) {
    router.push('/login');
    return;
  }
  
  // 初始化用户信息（页面刷新后重新获取）
  await userStore.initUserInfo();
  
  // 加载用户订单数据
  loadUserOrders();
  
  // 这里可以添加加载最近浏览记录的逻辑
});
</script>

<style scoped>
.account-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #faf7f4;
}

.account-content {
  flex: 1;
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.account-header {
  margin-bottom: 2rem;
}

.account-header h2 {
  color: #4b2e1e;
  font-size: 1.8rem;
  margin-bottom: 0.5rem;
}

.welcome-message {
  color: #6e4a2e;
  font-size: 1.1rem;
}

.account-main {
  display: flex;
  gap: 2rem;
}

.account-panel {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.panel-section {
  margin-bottom: 2rem;
}

.panel-section h3 {
  color: #4b2e1e;
  font-size: 1.3rem;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #f0e6e0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.8rem;
  background-color: #f8f5f2;
  border-radius: 6px;
}

.info-item label {
  color: #6e4a2e;
  font-weight: 500;
  min-width: 80px;
}

.info-item span {
  color: #4b2e1e;
}

.order-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
}

.stat-item {
  text-align: center;
  padding: 1rem;
  background-color: #f8f5f2;
  border-radius: 8px;
}

.stat-number {
  font-size: 1.8rem;
  font-weight: bold;
  color: #a87b5e;
  margin-bottom: 0.3rem;
}

.stat-label {
  color: #6e4a2e;
  font-size: 0.9rem;
}

.product-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  padding: 1rem;
  background-color: #f8f5f2;
  border-radius: 6px;
}

.product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.product-info {
  flex: 1;
}

.product-name {
  color: #4b2e1e;
  font-weight: 500;
  margin-bottom: 0.3rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  color: #a87b5e;
  font-weight: bold;
}

.empty-message {
  text-align: center;
  padding: 2rem;
  color: #999;
}

/* 订单列表样式 */
.order-list {
  margin-top: 1rem;
}

.order-item {
  border: 1px solid #f0e6e0;
  border-radius: 8px;
  margin-bottom: 1rem;
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background-color: #f8f5f2;
}

.order-info {
  display: flex;
  gap: 1.5rem;
}

.order-id,
.order-date {
  color: #6e4a2e;
  font-size: 0.9rem;
}

.order-status {
  display: flex;
  align-items: center;
}

.status-badge {
  padding: 0.3rem 0.8rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
}

.status-pending,
.status-待付款 {
  background-color: #fff2e8;
  color: #ff9d42;
}

.status-shipping,
.status-待发货 {
  background-color: #e8f5ff;
  color: #429dff;
}

.status-delivered,
.status-待收货 {
  background-color: #e8fff2;
  color: #42ff9d;
}

.status-completed,
.status-已完成 {
  background-color: #f2e8ff;
  color: #9d42ff;
}

.status-cancelled,
.status-已取消 {
  background-color: #f5f5f5;
  color: #999;
}

.status-expired,
.status-超时 {
  background-color: #ffe8e8;
  color: #ff4242;
}

.order-items {
  padding: 1rem 1.5rem;
}

.order-product {
  padding: 0.8rem 0;
  border-bottom: 1px solid #f0e6e0;
}

.order-product:last-child {
  border-bottom: none;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.product-name {
  flex: 1;
  color: #4b2e1e;
}

.product-price {
  color: #a87b5e;
  font-weight: 500;
}

.product-quantity {
  color: #6e4a2e;
  font-size: 0.9rem;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background-color: #f8f5f2;
}

.order-total {
  color: #a87b5e;
  font-weight: 500;
}

.order-actions {
  display: flex;
  gap: 0.5rem;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #f0e6e0;
}

/* 加载状态样式 */
.loading-container {
  text-align: center;
  padding: 3rem;
  color: #6e4a2e;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .account-main {
    flex-direction: column;
  }
  
  .account-sidebar {
    width: 100%;
  }
  
  .account-nav {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 0.5rem;
  }
  
  .order-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
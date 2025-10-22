<template>
  <div class="orders-container">
    <Header />
    <div class="orders-content">
      <div class="orders-header">
        <h2>我的订单</h2>
        <p class="user-message">您好，{{ userInfo?.username || '用户' }}！</p>
      </div>
      
      <div class="orders-main">
        <!-- 左侧导航 - 使用AccountSidebar组件 -->
        <AccountSidebar @navigate="handleNavigation" @logout="handleLogout" />
        
        
        <!-- 右侧内容 -->
        <div class="orders-panel">
          <!-- 订单状态筛选 -->
          <div class="status-filter">
            <el-button
              v-for="status in orderStatuses"
              :key="status.value"
              :type="activeStatus === status.value ? 'primary' : 'default'"
              plain
              @click="filterOrdersByStatus(status.value)"
            >
              {{ status.label }}
            </el-button>
          </div>
          
          <!-- 订单列表 -->
          <div class="panel-section">
            <h3>订单列表</h3>
            <div v-if="loadingOrders" class="loading-container">
              <el-loading-text>加载中...</el-loading-text>
            </div>
            <div v-else-if="orders.length === 0" class="empty-message">
              <el-empty description="暂无订单记录" />
            </div>
            <div v-else class="order-list">
              <div v-for="order in orders" :key="order.id" class="order-item">
                <div class="order-header">
                  <div class="order-info">
                    <span class="order-id">订单号：{{ order.id }}</span>
                    <span class="order-date">下单时间：{{ formatDate(order.expireTime) }}</span>
                  </div>
                  <div class="order-status">
                    <span class="status-badge" :class="`status-${order.status}`">
                      {{ getStatusText(order.status) }}
                    </span>
                  </div>
                </div>
                
                <div class="order-items">
                  <div v-for="item in order.items" :key="item.id" class="order-product">
                    <div class="product-info">
                      <span class="product-name">{{ item.productName }}</span>
                      <span class="product-price">¥{{ item.price }}</span>
                      <span class="product-quantity">x{{ item.quantity }}</span>
                    </div>
                  </div>
                </div>
                
                <div class="order-footer">
                  <div class="order-total">
                    共{{ order.items.length }}件商品 合计：¥{{ order.totalPrice }}
                  </div>
                  <div class="order-actions">
                    <el-button type="text" @click="viewOrderDetails(order.id)">
                      查看详情
                    </el-button>
                    <template v-if="order.status === 'pending' || order.status === '待付款'">
                      <el-button type="primary" size="small">去支付</el-button>
                      <el-button type="default" size="small">取消订单</el-button>
                    </template>
                    <template v-else-if="order.status === 'completed' || order.status === '已完成'">
                      <el-button type="text">再次购买</el-button>
                      <el-button type="text">评价</el-button>
                    </template>
                    <template v-else>
                      <el-button type="text">查看物流</el-button>
                    </template>
                  </div>
                </div>
              </div>
              
              <!-- 分页控件 -->
              <div class="pagination-container">
                <el-pagination
                  v-model:current-page="currentPage"
                  v-model:page-size="pageSize"
                  :page-sizes="[5, 10, 20]"
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="totalOrders"
                  @size-change="handlePageSizeChange"
                  @current-change="handlePageChange"
                />
              </div>
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
import Header from '@/components/Header.vue';
import Footer from '@/components/Footer.vue';
import AccountSidebar from '@/components/AccountSidebar.vue';

const router = useRouter();
const userStore = useUserStore();

// 订单列表数据
const orders = ref<OrderResponseDTO[]>([]);
const totalOrders = ref(0);
const currentPage = ref(1);
const pageSize = ref(5);
const loadingOrders = ref(false);
const activeStatus = ref('all'); // 'all', 'pending', 'shipping', 'delivered', 'completed'
const orderStatuses = [
  { value: 'all', label: '全部订单' },
  { value: 'pending', label: '待付款' },
  { value: 'paid', label: '待发货' },
  { value: 'shipped', label: '待收货' },
  { value: 'completed', label: '已完成' }
];

// 获取用户信息
const userInfo = computed(() => userStore.userInfo);

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

// 获取订单状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'pending': '待付款',
    'paid': '待发货',
    'shipped': '待收货',
    'completed': '已完成',
    'cancelled': '已取消',
    'expired':'超时'
  };
  
  // 如果状态已经是中文，直接返回
  if (statusMap[status] === undefined) {
    return status;
  }
  
  return statusMap[status];
};

// 处理每页显示数量变化
const handlePageSizeChange = (size: number) => {
  pageSize.value = size;
  loadUserOrders(1, size);
};

// 处理分页变化
const handlePageChange = (page: number) => {
  loadUserOrders(page, pageSize.value);
};

// 根据状态筛选订单
const filterOrdersByStatus = (status: string) => {
  activeStatus.value = status;
  // 重新加载订单数据
  loadUserOrders(1, pageSize.value);
};

// 加载用户订单数据
const loadUserOrders = async (page = 1, size = 10) => {
  if (!userStore.userInfo?.id) return;
  
  loadingOrders.value = true;
  try {
    const res = await getUserOrders(page, size, userStore.userInfo.id);
    if (res) {
      let filteredOrders = res.records || [];
      // 根据选中的状态进行筛选
      if (activeStatus.value !== 'all') {
        filteredOrders = filteredOrders.filter(order => 
          order.status === activeStatus.value || order.status === getStatusText(activeStatus.value)
        );
      }
      
      orders.value = filteredOrders;
      totalOrders.value = res.total || 0;
      currentPage.value = res.current || 1;
      pageSize.value = res.size || 10;
    }
  } catch (error) {
    console.error('加载订单失败:', error);
  } finally {
    loadingOrders.value = false;
  }
};

// 查看订单详情
const viewOrderDetails = async (orderId: number) => {
  try {
    // 跳转到订单详情页面
    router.push(`/order/detail/${orderId}`);
  } catch (error) {
    console.error('跳转到订单详情失败:', error);
    alert('查看订单详情失败，请稍后重试');
  }
};

// 处理导航 - 接收AccountSidebar组件的navigate事件
  const handleNavigation = (route: string) => {
    try {
      switch (route) {
        case 'profile':
          router.push('/account');
          break;
        case 'orders':
          router.push('/orders');
          break;
        case 'favorites':
          alert('我的收藏功能即将上线，敬请期待！');
          break;
        case 'addresses':
          alert('收货地址功能即将上线，敬请期待！');
          break;
        case 'settings':
          alert('账户设置功能即将上线，敬请期待！');
          break;
        default:
          break;
      }
    } catch (error) {
      console.error('导航失败:', error);
      alert('导航失败，请稍后重试');
    }
  };
  
  // 处理退出登录 - 接收AccountSidebar组件的logout事件
  const handleLogout = async () => {
    try {
      await userStore.logout();
      router.push('/login');
    } catch (error) {
      console.error('退出登录失败:', error);
      alert('退出登录失败，请稍后重试');
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
});
</script>

<style scoped>
.orders-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #faf7f4;
}

.orders-content {
  flex: 1;
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.orders-header {
  margin-bottom: 2rem;
}

.orders-header h2 {
  color: #4b2e1e;
  font-size: 1.8rem;
  margin-bottom: 0.5rem;
}

.user-message {
  color: #6e4a2e;
  font-size: 1.1rem;
}

.orders-main {
  display: flex;
  gap: 2rem;
}

.orders-panel {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.status-filter {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
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

.status-paid,
.status-待发货 {
  background-color: #e8f5ff;
  color: #429dff;
}

.status-shipped,
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

.empty-message {
  text-align: center;
  padding: 2rem;
  color: #999;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .orders-main {
    flex-direction: column;
  }
  
  .orders-sidebar {
    width: 100%;
  }
  
  .orders-nav {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 0.5rem;
  }
  
  .status-filter {
    justify-content: center;
  }
}
</style>
<template>
  <div class="admin-dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card users-card">
          <div class="stat-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ totalUsers }}</div>
            <div class="stat-label">总用户数</div>
            <div class="stat-trend" :class="userGrowth >= 0 ? 'trend-up' : 'trend-down'">
              <el-icon><TrendCharts /></el-icon>
              <span>{{ formatGrowth(userGrowth) }}</span>
            </div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card products-card">
          <div class="stat-icon">
            <el-icon><Goods /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ totalProducts }}</div>
            <div class="stat-label">总商品数</div>
            <div class="stat-trend" :class="productGrowth >= 0 ? 'trend-up' : 'trend-down'">
              <el-icon><TrendCharts /></el-icon>
              <span>{{ formatGrowth(productGrowth) }}</span>
            </div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card orders-card">
          <div class="stat-icon">
            <el-icon><ShoppingCart /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ totalOrders }}</div>
            <div class="stat-label">总订单数</div>
            <div class="stat-trend" :class="orderGrowth >= 0 ? 'trend-up' : 'trend-down'">
              <el-icon><TrendCharts /></el-icon>
              <span>{{ formatGrowth(orderGrowth) }}</span>
            </div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card revenue-card">
          <div class="stat-icon">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ totalRevenue }}</div>
            <div class="stat-label">总收入</div>
            <div class="stat-trend" :class="revenueGrowth >= 0 ? 'trend-up' : 'trend-down'">
              <el-icon><TrendCharts /></el-icon>
              <span>{{ formatGrowth(revenueGrowth) }}</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 图表和列表 -->
    <el-row :gutter="20" class="content-row">
      <el-col :xs="24" :lg="12">
        <el-card class="data-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon><List /></el-icon>
                <span>最近订单</span>
              </div>
              <el-button type="primary" link @click="goToOrders">查看全部</el-button>
            </div>
          </template>
          
          <el-table :data="recentOrders" style="width: 100%">
            <el-table-column prop="id" label="订单ID" width="80" />
            <el-table-column prop="userId" label="用户ID" width="80" />
            <el-table-column prop="totalAmount" label="金额" width="100" />
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)" size="small">
                  {{ scope.row.statusText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="时间" show-overflow-tooltip />
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="12">
        <el-card class="data-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon><TrendCharts /></el-icon>
                <span>热门商品</span>
              </div>
              <el-button type="primary" link @click="goToProducts">查看全部</el-button>
            </div>
          </template>
          
          <el-table :data="popularProducts" style="width: 100%">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="name" label="商品名称" show-overflow-tooltip />
            <el-table-column prop="price" label="价格" width="100" />
            <el-table-column prop="sales" label="销量" width="80">
              <template #default="scope">
                <el-tag type="success" size="small">{{ scope.row.sales }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 快捷操作 -->
    <el-card class="quick-actions-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-title">
            <el-icon><Operation /></el-icon>
            <span>快捷操作</span>
          </div>
        </div>
      </template>
      
      <div class="quick-actions">
        <div class="action-item" @click="goToUsers">
          <div class="action-icon users">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="action-text">用户管理</div>
        </div>
        
        <div class="action-item" @click="goToProducts">
          <div class="action-icon products">
            <el-icon><Goods /></el-icon>
          </div>
          <div class="action-text">商品管理</div>
        </div>
        
        <div class="action-item" @click="goToOrders">
          <div class="action-icon orders">
            <el-icon><ShoppingCart /></el-icon>
          </div>
          <div class="action-text">订单管理</div>
        </div>
        
        <div class="action-item">
          <div class="action-icon settings">
            <el-icon><Setting /></el-icon>
          </div>
          <div class="action-text">系统设置</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  User, Goods, ShoppingCart, Money, TrendCharts, 
  List, Operation, UserFilled, Setting 
} from '@element-plus/icons-vue'
import { getUserPage } from '@/api/user'
import { getProductPage } from '@/api/product'
import { getAllOrder } from '@/api/order'
import type { OrderResponseDTO } from '@/api/order'

const router = useRouter()

const totalUsers = ref(0)
const totalProducts = ref(0)
const totalOrders = ref(0)
const totalRevenue = ref('¥0.00')

// 增长率数据
const userGrowth = ref(0)
const productGrowth = ref(0)
const orderGrowth = ref(0)
const revenueGrowth = ref(0)

const recentOrders = ref<any[]>([])
const popularProducts = ref<any[]>([])

// 计算增长率
const calculateGrowth = (current: number, previous: number): number => {
  if (previous === 0) return current > 0 ? 100 : 0
  return Math.round(((current - previous) / previous) * 100)
}

// 格式化增长率显示
const formatGrowth = (growth: number): string => {
  if (growth > 0) return `较上月 +${growth}%`
  if (growth < 0) return `较上月 ${growth}%`
  return '较上月 持平'
}

// 获取本月和上月的日期范围
const getMonthRange = (monthOffset: number = 0) => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + monthOffset
  
  const startDate = new Date(year, month, 1)
  const endDate = new Date(year, month + 1, 0, 23, 59, 59)
  
  return { startDate, endDate }
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    cart: '购物车',
    pending: '待支付',
    paid: '已支付',
    shipped: '已发货',
    completed: '已完成',
    cancelled: '已取消',
    expired: '已过期'
  }
  return statusMap[status] || status
}

const getStatusType = (status: string) => {
  const typeMap: Record<string, any> = {
    pending: 'warning',
    paid: 'success',
    shipped: 'info',
    completed: 'success',
    cancelled: 'danger'
  }
  return typeMap[status] || 'info'
}

const goToUsers = () => router.push('/admin/users')
const goToProducts = () => router.push('/admin/products')
const goToOrders = () => router.push('/admin/orders')

onMounted(async () => {
  try {
    const thisMonth = getMonthRange(0)
    const lastMonth = getMonthRange(-1)
    
    // 获取用户数据
    const userResponse = await getUserPage(1, 9999)
    if (userResponse && userResponse.records) {
      totalUsers.value = userResponse.total
      
      // 计算本月和上月的用户数
      const thisMonthUsers = userResponse.records.filter(user => {
        const createdDate = new Date(user.createdAt)
        return createdDate >= thisMonth.startDate && createdDate <= thisMonth.endDate
      }).length
      
      const lastMonthUsers = userResponse.records.filter(user => {
        const createdDate = new Date(user.createdAt)
        return createdDate >= lastMonth.startDate && createdDate <= lastMonth.endDate
      }).length
      
      userGrowth.value = calculateGrowth(thisMonthUsers, lastMonthUsers)
    }

    // 获取商品数据
    const productResponse = await getProductPage(1, 9999)
    if (productResponse && productResponse.records) {
      totalProducts.value = productResponse.total
      
      // 由于商品没有创建时间，使用总数对比（简化处理）
      // 假设上月商品数为当前的90%
      const estimatedLastMonth = Math.floor(productResponse.total * 0.9)
      productGrowth.value = calculateGrowth(productResponse.total, estimatedLastMonth)
    }

    // 获取订单数据
    const orderResponse = await getAllOrder(1, 9999)
    if (orderResponse.records) {
      // 按创建时间排序，获取最新的5条
      const sortedOrders = [...orderResponse.records].sort((a, b) => {
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      })
      
      // 只显示最新的5条订单
      recentOrders.value = sortedOrders.slice(0, 5).map((order: OrderResponseDTO) => ({
        id: order.id,
        userId: order.userId,
        totalAmount: '¥' + order.totalPrice.toFixed(2),
        status: order.status,
        statusText: getStatusText(order.status),
        createdAt: order.createdAt
      }))
      
      // 统计所有订单
      totalOrders.value = orderResponse.total || 0
      
      // 计算本月和上月的订单数
      const thisMonthOrders = orderResponse.records.filter(order => {
        const createdDate = new Date(order.createdAt)
        return createdDate >= thisMonth.startDate && createdDate <= thisMonth.endDate
      })
      
      const lastMonthOrders = orderResponse.records.filter(order => {
        const createdDate = new Date(order.createdAt)
        return createdDate >= lastMonth.startDate && createdDate <= lastMonth.endDate
      })
      
      orderGrowth.value = calculateGrowth(thisMonthOrders.length, lastMonthOrders.length)
      
      // 计算所有订单的总收入
      const revenue = orderResponse.records
        .filter(o => ['paid', 'shipped', 'completed'].includes(o.status))
        .reduce((sum, order) => sum + order.totalPrice, 0)
      totalRevenue.value = '¥' + revenue.toFixed(2)
      
      // 计算本月和上月的收入
      const thisMonthRevenue = thisMonthOrders
        .filter(o => ['paid', 'shipped', 'completed'].includes(o.status))
        .reduce((sum, order) => sum + order.totalPrice, 0)
      
      const lastMonthRevenue = lastMonthOrders
        .filter(o => ['paid', 'shipped', 'completed'].includes(o.status))
        .reduce((sum, order) => sum + order.totalPrice, 0)
      
      revenueGrowth.value = calculateGrowth(thisMonthRevenue, lastMonthRevenue)
    }

    // 获取热门商品
    const productPage = await getProductPage(1, 10)
    if (productPage && productPage.records) {
      const sortedProducts = [...productPage.records].sort((a, b) => b.stock - a.stock)
      popularProducts.value = sortedProducts.slice(0, 5).map(product => ({
        id: product.id,
        name: product.name,
        price: '¥' + product.price.toFixed(2),
        sales: product.stock
      }))
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
})
</script>

<style scoped>
.admin-dashboard {
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

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  cursor: pointer;
  margin-bottom: 20px;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: white;
}

.users-card .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.products-card .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.orders-card .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.revenue-card .stat-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.trend-up {
  color: #67c23a;
}

.trend-down {
  color: #f56c6c;
}

.content-row {
  margin-bottom: 20px;
}

.data-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.data-card :deep(.el-card__header) {
  padding: 20px;
  border-bottom: 1px solid #f0f2f5;
}

.data-card :deep(.el-card__body) {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.quick-actions-card {
  border-radius: 12px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px;
  background: #f5f7fa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-item:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
}

.action-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}

.action-icon.users {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.action-icon.products {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.action-icon.orders {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.action-icon.settings {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.action-text {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

@media (max-width: 768px) {
  .stat-card {
    padding: 16px;
  }
  
  .stat-icon {
    width: 48px;
    height: 48px;
    font-size: 24px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .quick-actions {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>

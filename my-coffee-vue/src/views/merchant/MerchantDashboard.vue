<template>
  <div class="merchant-dashboard">
    <h1>数据概览</h1>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon products">
            <el-icon><Goods /></el-icon>
          </div>
          <div class="stat-info">
            <h3>我的商品</h3>
            <p class="stat-number">{{ stats.totalProducts }}</p>
            
            <div class="stat-trend" :class="productGrowth >= 0 ? 'trend-up' : 'trend-down'">
              <el-icon><TrendCharts /></el-icon>
              <span>{{ formatGrowth(productGrowth) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon orders">
            <el-icon><ShoppingCart /></el-icon>
          </div>
          <div class="stat-info">
            <h3>订单数量</h3>
            <p class="stat-number">{{ stats.totalOrders }}</p>
            
            <div class="stat-trend" :class="orderGrowth >= 0 ? 'trend-up' : 'trend-down'">
              <el-icon><TrendCharts /></el-icon>
              <span>{{ formatGrowth(orderGrowth) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon revenue">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-info">
            <h3>我的收入</h3>
            <p class="stat-number">{{ stats.totalRevenue }}</p>
            
            <div class="stat-trend" :class="revenueGrowth >= 0 ? 'trend-up' : 'trend-down'">
              <el-icon><TrendCharts /></el-icon>
              <span>{{ formatGrowth(revenueGrowth) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon pending">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <h3>待处理</h3>
            <p class="stat-number">{{ stats.pendingOrders }}</p>
            <span class="stat-label">待发货订单</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 图表和列表 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近订单</span>
              <el-button type="text" @click="goToOrders">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentOrders" border stripe>
            <el-table-column prop="merchantOrderId" label="子订单ID" width="100" />
            <el-table-column prop="totalAmount" label="我的收入" width="100" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)" size="small">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="时间" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>热销商品</span>
              <el-button type="text" @click="goToProducts">查看全部</el-button>
            </div>
          </template>
          <el-table :data="topProducts" border stripe>
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="price" label="价格" width="100" />
            <el-table-column prop="stock" label="库存" width="80" />
            <el-table-column prop="sales" label="销量" width="80" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 快捷操作 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <span>快捷操作</span>
      </template>
      <div class="quick-actions">
        <el-button type="primary" @click="goToAddProduct">
          <el-icon><Plus /></el-icon>
          添加商品
        </el-button>
        <el-button type="success" @click="goToOrders">
          <el-icon><List /></el-icon>
          查看订单
        </el-button>
        <el-button type="warning" @click="goToReviews">
          <el-icon><ChatDotRound /></el-icon>
          查看评价
        </el-button>
        <el-button type="info" @click="goToSettings">
          <el-icon><Setting /></el-icon>
          店铺设置
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Goods, ShoppingCart, Money, Clock, Plus, List, ChatDotRound, Setting, TrendCharts } from '@element-plus/icons-vue'
import { getMerchantProductPage } from '@/api/product'
import { getMerchantOrder } from '@/api/order'

const router = useRouter()
const userStore = useUserStore()

const stats = ref({
  totalProducts: 0,
  totalOrders: 0,
  totalRevenue: '¥0.00',
  pendingOrders: 0
})

// 增长率数据
const productGrowth = ref(0)
const orderGrowth = ref(0)
const revenueGrowth = ref(0)

const recentOrders = ref<any[]>([])
const topProducts = ref<any[]>([])

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

const getStatusType = (status: string) => {
  const types: Record<string, any> = {
    pending: 'warning',
    paid: 'success',
    shipped: 'info',
    completed: 'success',
    cancelled: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    pending: '待支付',
    paid: '已支付',
    shipped: '已发货',
    completed: '已完成',
    cancelled: '已取消',
    expired: '超时'
  }
  return texts[status] || status
}

const goToOrders = () => router.push('/merchant/orders')
const goToProducts = () => router.push('/merchant/products')
const goToAddProduct = () => router.push('/merchant/products?action=add')
const goToReviews = () => router.push('/merchant/reviews')
const goToSettings = () => router.push('/merchant/settings')

// 计算订单中商品的总价
const calculateOrderTotal = (items: any[]) => {
  if (!items || items.length === 0) return 0
  return items.reduce((sum, item) => sum + (item.price * item.quantity), 0)
}

// 从items中提取商家子订单信息
const extractMerchantOrderInfo = (order: any) => {
  const firstItem = order.items && order.items.length > 0 ? order.items[0] : null
  
  return {
    merchantOrderId: firstItem?.merchantOrderId || order.id,
    mainOrderId: order.id,
    status: firstItem?.status || order.status,
    createdAt: order.createdAt,
    myTotalPrice: calculateOrderTotal(order.items)
  }
}

onMounted(async () => {
  try {
    const merchantId = userStore.userInfo?.id
    if (!merchantId) {
      console.error('商家ID不存在')
      return
    }
    
    const thisMonth = getMonthRange(0)
    const lastMonth = getMonthRange(-1)
    
    // 使用新接口获取商家商品数据
    const productRes = await getMerchantProductPage(1, 9999, merchantId.toString())
    if (productRes && productRes.records) {
      stats.value.totalProducts = productRes.total || productRes.records.length
      
      // 由于商品没有创建时间，使用估算方式
      const estimatedLastMonth = Math.floor(productRes.records.length * 0.9)
      productGrowth.value = calculateGrowth(productRes.records.length, estimatedLastMonth)
      
      // 热销商品（按库存排序，模拟销量）
      topProducts.value = productRes.records
        .sort((a, b) => b.stock - a.stock)
        .slice(0, 5)
        .map(p => ({
          name: p.name,
          price: `¥${p.price.toFixed(2)}`,
          stock: p.stock,
          sales: Math.floor(Math.random() * 100)
        }))
    }
    
    // 使用新接口获取商家订单数据
    const orderRes = await getMerchantOrder(1, 9999, merchantId)
    if (orderRes && orderRes.records) {
      // 提取所有商家子订单信息
      const merchantOrders = orderRes.records.map(order => extractMerchantOrderInfo(order))
      
      stats.value.totalOrders = merchantOrders.length
      
      // 统计待发货订单（商家子订单状态为paid）
      stats.value.pendingOrders = merchantOrders.filter(o => o.status === 'paid').length
      
      // 计算本月和上月的订单数
      const thisMonthOrders = merchantOrders.filter(order => {
        const createdDate = new Date(order.createdAt)
        return createdDate >= thisMonth.startDate && createdDate <= thisMonth.endDate
      })
      
      const lastMonthOrders = merchantOrders.filter(order => {
        const createdDate = new Date(order.createdAt)
        return createdDate >= lastMonth.startDate && createdDate <= lastMonth.endDate
      })
      
      orderGrowth.value = calculateGrowth(thisMonthOrders.length, lastMonthOrders.length)
      
      // 计算总收入（基于items计算）
      let totalRevenue = 0
      merchantOrders
        .filter(o => ['paid', 'shipped', 'completed'].includes(o.status))
        .forEach(order => {
          totalRevenue += order.myTotalPrice
        })
      
      stats.value.totalRevenue = `¥${totalRevenue.toFixed(2)}`
      
      // 计算本月和上月的收入
      const thisMonthRevenue = thisMonthOrders
        .filter(o => ['paid', 'shipped', 'completed'].includes(o.status))
        .reduce((sum, order) => sum + order.myTotalPrice, 0)
      
      const lastMonthRevenue = lastMonthOrders
        .filter(o => ['paid', 'shipped', 'completed'].includes(o.status))
        .reduce((sum, order) => sum + order.myTotalPrice, 0)
      
      revenueGrowth.value = calculateGrowth(thisMonthRevenue, lastMonthRevenue)
      
      // 最近订单（取前5条，显示商家子订单信息）
      recentOrders.value = merchantOrders.slice(0, 5).map(o => ({
        merchantOrderId: o.merchantOrderId,
        totalAmount: `¥${o.myTotalPrice.toFixed(2)}`,
        status: o.status,
        createdAt: o.createdAt
      }))
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
})
</script>

<style scoped>
.merchant-dashboard {
  padding: 20px;
}

.merchant-dashboard h1 {
  margin: 0 0 20px 0;
  font-size: 24px;
  color: #333;
}

.stat-card {
  height: 140px;
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 20px;
  height: 100%;
}

.stat-icon {
  font-size: 48px;
  color: white;
  padding: 20px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.products {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.orders {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.revenue {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.pending {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-info {
  flex: 1;
}

.stat-info h3 {
  margin: 0 0 8px 0;
  color: #666;
  font-size: 14px;
  font-weight: normal;
}

.stat-number {
  margin: 0;
  color: #333;
  font-size: 32px;
  font-weight: bold;
  line-height: 1;
}

.stat-label {
  color: #999;
  font-size: 12px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  margin-top: 8px;
}

.trend-up {
  color: #67c23a;
}

.trend-down {
  color: #f56c6c;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-actions {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.quick-actions .el-button {
  flex: 1;
  min-width: 150px;
}
</style>

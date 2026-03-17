<template>
  <div class="order-management">
    <div class="page-header">
      <h1>订单管理</h1>
      <div class="header-stats">
        <el-statistic title="总订单" :value="totalOrders" />
        <el-statistic title="待支付" :value="pendingCount" />
        <el-statistic title="已完成" :value="completedCount" />
      </div>
    </div>
    
    <el-card class="filter-card" shadow="hover">
      <div class="filter-bar">
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索订单ID或用户ID" 
          style="width: 300px;"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select 
          v-model="statusFilter" 
          placeholder="筛选状态" 
          style="width: 150px;"
          clearable
        >
          <el-option label="全部状态" value="" />
          <el-option label="待支付" value="pending" />
          <el-option label="已支付" value="paid" />
          <el-option label="已发货" value="shipped" />
          <el-option label="已完成" value="completed" />
          <el-option label="已取消" value="cancelled" />
        </el-select>
        
        <el-date-picker
          v-model="dateFilter"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 300px;"
        />
        
        <el-button type="primary" @click="searchOrders">
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
        :data="orders" 
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="订单ID" width="100" />
        
        <el-table-column prop="userId" label="用户ID" width="100" />
        
        <el-table-column label="订单商品" min-width="200">
          <template #default="scope">
            <div class="order-items">
              <div v-for="(item, index) in scope.row.items?.slice(0, 2)" :key="index" class="item-row">
                {{ item.productName }} × {{ item.quantity }}
              </div>
              <div v-if="scope.row.items?.length > 2" class="more-items">
                +{{ scope.row.items.length - 2 }} 件商品
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="totalAmount" label="订单金额" width="120">
          <template #default="scope">
            <span class="amount">{{ scope.row.totalAmount }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getTagType(scope.row.status)" size="small">
              {{ scope.row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="paymentMethod" label="支付方式" width="100" />
        
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewOrder(scope.row)" link>
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button 
              v-if="scope.row.status === 'pending'" 
              size="small" 
              type="danger" 
              @click="cancelOrderID(scope.row)" 
              link
            >
              <el-icon><Close /></el-icon>
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="totalOrders"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 订单详情对话框 -->
    <el-dialog 
      title="订单详情" 
      v-model="detailDialogVisible" 
      width="900px"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单ID">{{ orderDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ orderDetail.userId }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">
          <span class="amount-large">{{ orderDetail.totalAmount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ orderDetail.paymentMethod }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getTagType(orderDetail.status)">{{ orderDetail.statusText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ orderDetail.createdAt }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider content-position="left">收货信息</el-divider>
      <div v-if="orderDetail.address && orderDetail.address.length" class="address-info">
        <div v-for="(addr, index) in orderDetail.address" :key="index">
          <p><strong>收货人：</strong>{{ addr.recipientName }}</p>
          <p><strong>电话：</strong>{{ addr.phone }}</p>
          <p><strong>地址：</strong>{{ addr.fullAddress }}</p>
        </div>
      </div>
      <el-empty v-else description="暂无收货信息" :image-size="60" />
      
      <el-divider content-position="left">订单商品</el-divider>
      <el-table :data="orderDetail.items" border stripe>
        <el-table-column label="商品图片" width="80">
          <template #default="scope">
            <el-image
              v-if="scope.row.productImages?.length"
              :src="baseUrl + scope.row.productImages[0]"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 4px;"
              :preview-src-list="scope.row.productImages.map((img: string) => baseUrl + img)"
              preview-teleported
            />
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="price" label="单价" width="100" />
        <el-table-column prop="total" label="小计" width="100" />
      </el-table>
      
      <div class="order-total">
        <span>订单总计：</span>
        <span class="total-amount">{{ orderDetail.totalAmount }}</span>
      </div>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button 
          v-if="orderDetail.status === 'pending'" 
          type="danger" 
          @click="cancelCurrentOrder"
        >
          取消订单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, RefreshRight, View, Close 
} from '@element-plus/icons-vue'
import { getAllOrder, cancelOrder } from '@/api/order'

const baseUrl = 'http://localhost:8081'

const searchQuery = ref('')
const statusFilter = ref('')
const dateFilter = ref<Date[]>([])
const orders = ref<any[]>([])
const totalOrders = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const detailDialogVisible = ref(false)
const orderDetail = ref<any>({
  id: 0,
  userId: 0,
  totalAmount: '',
  paymentMethod: '',
  status: '',
  statusText: '',
  createdAt: '',
  address: [],
  items: []
})

const pendingCount = computed(() => 
  orders.value.filter(o => o.status === 'pending').length
)

const completedCount = computed(() => 
  orders.value.filter(o => o.status === 'completed').length
)

const getTagType = (status: string) => {
  const typeMap: Record<string, any> = {
    pending: 'warning',
    paid: 'success',
    shipped: 'info',
    completed: 'success',
    cancelled: 'danger',
    cart: 'info'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    pending: '待支付',
    paid: '已支付',
    shipped: '已发货',
    completed: '已完成',
    cancelled: '已取消',
    expired: '已过期',
    cart: '购物车'
  }
  return textMap[status] || status
}

const loadOrders = async () => {
  loading.value = true
  try {
    const response = await getAllOrder(currentPage.value, pageSize.value)
    orders.value = response.records.map(order => ({
      id: order.id,
      userId: order.userId,
      totalAmount: `¥${order.totalPrice.toFixed(2)}`,
      status: order.status,
      statusText: getStatusText(order.status),
      paymentMethod: '支付宝',
      createdAt: order.createdAt,
      items: order.items,
      address: order.address
    }))
    totalOrders.value = response.total
  } catch (error) {
    console.error('加载订单失败:', error)
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

const searchOrders = async () => {
  loading.value = true
  try {
    const response = await getAllOrder(1, pageSize.value)
    let filteredOrders = response.records
    
    if (searchQuery.value) {
      const query = searchQuery.value.toLowerCase()
      filteredOrders = filteredOrders.filter(order => 
        order.id.toString().includes(query) || 
        order.userId.toString().includes(query)
      )
    }
    
    if (statusFilter.value) {
      filteredOrders = filteredOrders.filter(order => 
        order.status === statusFilter.value
      )
    }
    
    orders.value = filteredOrders.map(order => ({
      id: order.id,
      userId: order.userId,
      totalAmount: `¥${order.totalPrice.toFixed(2)}`,
      status: order.status,
      statusText: getStatusText(order.status),
      paymentMethod: '支付宝',
      createdAt: order.createdAt,
      items: order.items,
      address: order.address
    }))
    totalOrders.value = filteredOrders.length
    currentPage.value = 1
  } catch (error) {
    console.error('搜索订单失败:', error)
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  searchQuery.value = ''
  statusFilter.value = ''
  dateFilter.value = []
  currentPage.value = 1
  loadOrders()
}

const viewOrder = (order: any) => {
  const fullOrder = orders.value.find(o => o.id === order.id)
  if (fullOrder) {
    orderDetail.value = {
      id: fullOrder.id,
      userId: fullOrder.userId,
      totalAmount: fullOrder.totalAmount,
      paymentMethod: fullOrder.paymentMethod,
      status: fullOrder.status,
      statusText: fullOrder.statusText,
      createdAt: fullOrder.createdAt,
      address: fullOrder.address || [],
      items: fullOrder.items ? fullOrder.items.map((item: any) => ({
        productName: item.productName,
        quantity: item.quantity,
        price: `¥${item.price.toFixed(2)}`,
        total: `¥${(item.price * item.quantity).toFixed(2)}`,
        productImages: item.productImages
      })) : []
    }
  }
  detailDialogVisible.value = true
}

const cancelOrderID = async (order: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消订单 ${order.id} 吗？`,
      '取消订单',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await cancelOrder(order.id)
    if (response.data.code === 200) {
      ElMessage.success('订单已取消')
      loadOrders()
    } else {
      ElMessage.error(response.data.message || '取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消订单失败')
    }
  }
}

const cancelCurrentOrder = async () => {
  detailDialogVisible.value = false
  await cancelOrderID({ id: orderDetail.value.id })
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  loadOrders()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadOrders()
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-management {
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

.header-stats {
  display: flex;
  gap: 40px;
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

.order-items {
  padding: 4px 0;
}

.item-row {
  font-size: 13px;
  color: #606266;
  margin-bottom: 4px;
}

.more-items {
  font-size: 12px;
  color: #909399;
}

.amount {
  font-size: 14px;
  font-weight: 600;
  color: #f56c6c;
}

.amount-large {
  font-size: 18px;
  font-weight: 700;
  color: #f56c6c;
}

.pagination-container {
  padding: 20px;
  display: flex;
  justify-content: flex-end;
}

.address-info {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.address-info p {
  margin: 8px 0;
  color: #606266;
}

.order-total {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20px;
  margin-top: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
}

.total-amount {
  font-size: 24px;
  color: #f56c6c;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .filter-bar {
    flex-direction: column;
  }
  
  .filter-bar > * {
    width: 100% !important;
  }
}
</style>

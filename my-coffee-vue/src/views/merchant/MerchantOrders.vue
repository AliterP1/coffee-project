<template>
  <div class="merchant-orders">
    <h1>订单管理</h1>
    
    <el-card>
      <div class="filter-bar">
        <el-input v-model="searchQuery" placeholder="搜索订单ID" style="width: 300px;">
          <template #append>
            <el-button @click="loadOrders"><el-icon><Search /></el-icon></el-button>
          </template>
        </el-input>
        <el-select v-model="statusFilter" placeholder="筛选状态" style="width: 150px;" @change="loadOrders">
          <el-option label="全部" value="" />
          <el-option label="待支付" value="pending" />
          <el-option label="已支付" value="paid" />
          <el-option label="已发货" value="shipped" />
          <el-option label="已完成" value="completed" />
          <el-option label="已取消" value="cancelled" />
          <el-option label="超时" value="expired" />
        </el-select>
      </div>
      
      <el-table :data="orders" border stripe v-loading="loading">
        <el-table-column prop="merchantOrderId" label="子订单ID" width="100" />
        <el-table-column prop="mainOrderId" label="主订单ID" width="100" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column label="我的商品" min-width="200">
          <template #default="scope">
            <div v-for="item in scope.row.items" :key="item.productId" class="order-item">
              {{ item.productName }} x {{ item.quantity }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="myTotalPrice" label="我的收入" width="120">
          <template #default="scope">
            <span class="my-price">¥{{ scope.row.myTotalPrice.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">查看详情</el-button>
            <el-button 
              v-if="scope.row.status === 'paid'" 
              size="small" 
              type="success" 
              @click="shipOrder(scope.row)"
            >
              发货
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadOrders"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>
    
    <!-- 订单详情对话框 -->
    <el-dialog title="订单详情" v-model="detailVisible" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="子订单ID">{{ currentOrder.merchantOrderId }}</el-descriptions-item>
        <el-descriptions-item label="主订单ID">{{ currentOrder.mainOrderId }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentOrder.userId }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentOrder.status)">{{ getStatusText(currentOrder.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="我的收入" :span="2" class-name="my-revenue">
          <span class="my-price-large">¥{{ currentOrder.myTotalPrice?.toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentOrder.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="收货信息" :span="2">
          <div v-if="currentOrder.address && currentOrder.address.length > 0">
            <p>收货人：{{ currentOrder.address[0].recipientName }}</p>
            <p>电话：{{ currentOrder.address[0].phone }}</p>
            <p>地址：{{ currentOrder.address[0].fullAddress }}</p>
          </div>
          <span v-else>暂无收货信息</span>
        </el-descriptions-item>
      </el-descriptions>
      
      <h3 style="margin: 20px 0 10px 0;">我的商品</h3>
      <el-table :data="currentOrder.items" border>
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="单价" width="100">
          <template #default="scope">
            ¥{{ scope.row.price.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="小计" width="100">
          <template #default="scope">
            <span class="my-price">¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}</span>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="order-summary">
        <span>合计：</span>
        <span class="my-price-large">¥{{ currentOrder.myTotalPrice?.toFixed(2) }}</span>
      </div>
      
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getMerchantOrder, shipMerchantOrder } from '@/api/order'

const userStore = useUserStore()

const searchQuery = ref('')
const statusFilter = ref('')
const orders = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const detailVisible = ref(false)
const currentOrder = ref<any>({})

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

// 计算订单中商品的总价
const calculateOrderTotal = (items: any[]) => {
  if (!items || items.length === 0) return 0
  return items.reduce((sum, item) => sum + (item.price * item.quantity), 0)
}

// 从items中提取商家子订单信息
const extractMerchantOrderInfo = (order: any) => {
  // items中的第一个商品包含merchantOrderId和status
  const firstItem = order.items && order.items.length > 0 ? order.items[0] : null
  
  return {
    merchantOrderId: firstItem?.merchantOrderId || order.id, // 商家子订单ID
    mainOrderId: order.id, // 主订单ID
    status: firstItem?.status || order.status, // 商家子订单状态
    userId: order.userId,
    createdAt: order.createdAt,
    address: order.address,
    items: order.items,
    myTotalPrice: calculateOrderTotal(order.items)
  }
}

const loadOrders = async () => {
  loading.value = true
  try {
    const merchantId = userStore.userInfo?.id
    if (!merchantId) {
      ElMessage.error('获取商家信息失败')
      return
    }

    // 使用商家订单接口，返回的items已经是该商家的商品
    const res = await getMerchantOrder(currentPage.value, pageSize.value, merchantId)
    
    if (res && res.records) {
      let filtered = res.records
      
      // 应用搜索过滤（可以搜索主订单ID或子订单ID）
      if (searchQuery.value) {
        filtered = filtered.filter(o => {
          const merchantOrderId = o.items?.[0]?.merchantOrderId?.toString() || ''
          const mainOrderId = o.id.toString()
          return merchantOrderId.includes(searchQuery.value) || mainOrderId.includes(searchQuery.value)
        })
      }
      
      // 应用状态过滤（使用商家子订单状态）
      if (statusFilter.value) {
        filtered = filtered.filter(o => {
          const merchantOrderStatus = o.items?.[0]?.status || o.status
          return merchantOrderStatus === statusFilter.value
        })
      }
      
      // 提取商家子订单信息
      orders.value = filtered.map(order => extractMerchantOrderInfo(order))
      
      total.value = res.total || filtered.length
    }
  } catch (error) {
    console.error('加载订单失败:', error)
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = (order: any) => {
  currentOrder.value = order
  detailVisible.value = true
}

const shipOrder = async (order: any) => {
  try {
    await ElMessageBox.confirm('确认发货该订单？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    // 使用商家子订单ID发货
    const response = await shipMerchantOrder(order.merchantOrderId)
    
    if (response.data.code === 200) {
      ElMessage.success('发货成功')
      loadOrders()
    } else {
      ElMessage.error(response.data.message || '发货失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('发货失败:', error)
      ElMessage.error('发货失败')
    }
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.merchant-orders {
  padding: 20px;
}

.merchant-orders h1 {
  margin: 0 0 20px 0;
  font-size: 24px;
  color: #333;
}

.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.order-item {
  padding: 2px 0;
  font-size: 13px;
}

.my-price {
  color: #67c23a;
  font-weight: 600;
}

.my-price-large {
  color: #67c23a;
  font-weight: 700;
  font-size: 20px;
}

.my-revenue :deep(.el-descriptions__label) {
  font-weight: 600;
}

.order-summary {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20px;
  margin-top: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
}
</style>

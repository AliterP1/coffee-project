<template>
  <div class="order-detail-container">
    <Header />
    <div class="order-detail-content">
      <div class="order-detail-header">
        <h2>订单详情</h2>
        <el-button type="text" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
      </div>
      
      <div v-if="loading" class="loading-container">
        <el-loading-text>加载中...</el-loading-text>
      </div>
      
      <div v-else-if="orderDetail" class="order-detail-main">
        <!-- 订单基本信息 -->
        <div class="detail-section">
          <h3>订单信息</h3>
          <div class="detail-info">
            <div class="info-row">
              <label>订单编号：</label>
              <span>{{ orderDetail?.id || '-' }}</span>
            </div>
            <div class="info-row">
              <label>订单状态：</label>
              <span class="status-badge" :class="`status-${orderDetail?.status || 'unknown'}`">
                {{ getStatusText(orderDetail?.status || '') }}
              </span>
            </div>
            <div class="info-row">
              <label>下单时间：</label>
              <span>{{ orderDetail?.expireTime ? formatDate(orderDetail.expireTime) : '-' }}</span>
            </div>
            <div class="info-row">
              <label>更新时间：</label>
              <span>{{ orderDetail?.updatedAt ? formatDate(orderDetail.updatedAt) : '-' }}</span>
            </div>
          </div>
        </div>
        
        <!-- 收货信息 -->
        <div class="detail-section">
          <h3>收货信息</h3>
          <div class="detail-info">
            <div class="info-row">
              <label>收货人：</label>
              <span>{{ orderDetail?.address?.[0]?.recipientName || '-' }}</span>
            </div>
            <div class="info-row">
              <label>联系电话：</label>
              <span>{{ orderDetail?.address?.[0]?.phone || '-' }}</span>  
            </div>
            <div class="info-row">
              <label>收货地址：</label>
              <span>{{ orderDetail?.address?.[0]?.fullAddress || '-' }}</span>
            </div>
          </div>
        </div>
        
        <!-- 商品列表 -->
        <div class="detail-section">
          <h3>商品信息</h3>
          <div class="product-list">
            <div class="product-header">
              <div class="product-col col-name">商品名称</div>
              <div class="product-col col-price">单价</div>
              <div class="product-col col-quantity">数量</div>
              <div class="product-col col-subtotal">小计</div>
            </div>
            <div v-for="item in orderDetail.items" :key="item.id" class="product-item">
              <div class="product-col col-name">
                <div class="product-image">
                  <img v-if="item.productImage" :src="'http://localhost:8081' + item.productImage" :alt="item.productName" />
                  <img v-else src="/src/assets/coffeeImage/coffee-1576256_1920.jpg" alt="默认图片" />
                </div>
                <div class="product-name">{{ item.productName }}</div>
              </div>
              <div class="product-col col-price">¥{{ item.price?.toFixed(2) || '0.00' }}</div>
              <div class="product-col col-quantity">x{{ item.quantity || 0 }}</div>
              <div class="product-col col-subtotal">¥{{ orderDetail.totalPrice.toFixed(2) || '0.00' }}</div>
            </div>
          </div>
        </div>
        
        <!-- 支付信息 -->
        <div class="detail-section">
          <h3>支付信息</h3>
          <div class="detail-info">
            <div class="info-row">
              <label>支付方式：</label>
              <span>{{ (orderDetail?.status === 'completed' || orderDetail?.status === '已完成') ? '微信支付' : '未支付' }}</span>
            </div>
            <div class="info-row">
              <label>支付状态：</label>
              <span>{{ (orderDetail?.status === 'completed' || orderDetail?.status === '已完成') ? '支付成功' : '未支付' }}</span>
            </div>
            <div class="info-row">
              <label>支付时间：</label>
              <span>{{ (orderDetail?.status === 'completed' || orderDetail?.status === '已完成') && orderDetail?.updatedAt ? formatDate(orderDetail.updatedAt) : '暂无' }}</span>
            </div>
          </div>
        </div>
        
        <!-- 订单总计 -->
        <div class="detail-section total-section">
          <div class="total-info">
            <div class="total-item">
              <label>商品总价：</label>
              <span>¥{{ orderDetail?.totalPrice?.toFixed(2) || '0.00' }}</span>
            </div>
            <div class="total-item">
              <label>运费：</label>
              <span>¥0.00</span>
            </div>
            <div class="total-item total-amount">
              <label>实付金额：</label>
              <span>¥{{ orderDetail?.totalPrice?.toFixed(2) || '0.00' }}</span>
            </div>
          </div>
        </div>
        
        <!-- 订单操作 -->
        <div class="order-actions">
          <el-button type="primary" v-if="orderDetail?.status === 'pending' || orderDetail?.status === '待付款'" @click="handlePay">
            立即支付
          </el-button>
          <el-button v-if="orderDetail?.status === 'pending' || orderDetail?.status === '待付款'" @click="handleCancel">
            取消订单
          </el-button>
          <el-button v-if="orderDetail?.status === 'shipping' || orderDetail?.status === '待发货'" @click="trackLogistics">
            查看物流
          </el-button>
          <el-button v-if="orderDetail?.status === 'delivered' || orderDetail?.status === '待收货'" @click="confirmReceipt">
            确认收货
          </el-button>
          <el-button type="text" v-if="orderDetail?.status === 'completed' || orderDetail?.status === '已完成'" @click="reviewProduct">
            评价商品
          </el-button>
        </div>
      </div>
      
      <div v-else class="error-message">
        <el-empty description="订单不存在或已被删除" />
        <el-button type="primary" @click="goBack" class="go-back-btn">返回上一页</el-button>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getOrderDetails, cancelOrder } from '@/api/order';
import type { OrderResponseDTO } from '@/api/order';
import Header from '@/components/Header.vue';
import Footer from '@/components/Footer.vue';
// import { ArrowLeft } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();

// 订单详情数据
const orderDetail = ref<OrderResponseDTO | null>(null);
const loading = ref(false);

// 获取订单ID
const orderId = parseInt(route.params.id as string, 10);

// 加载订单详情
const loadOrderDetail = async () => {
  if (!orderId) return;
  
  loading.value = true;
  try {
    const res = await getOrderDetails(orderId);
    if (res && res.data && res.data.data) {
      orderDetail.value = res.data.data;
      console.log('订单详情数据:', orderDetail.value);
    } else {
      console.error('获取订单详情数据结构异常:', res);
      alert('获取订单详情数据异常');
    }
  } catch (error) {
    console.error('获取订单详情失败:', error);
    alert('获取订单详情失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

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
    'shipping': '待发货',
    'delivered': '待收货',
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

// 返回上一页
const goBack = () => {
  router.go(-1);
};

// 处理支付
const handlePay = () => {
  alert('跳转到支付页面');
  // 实际项目中应该跳转到支付页面
};

// 取消订单
const handleCancel = async () => {
  if (!confirm('确定要取消该订单吗？')) return;
  
  try {
    await cancelOrder(orderId);
    alert('订单已取消');
    loadOrderDetail(); // 重新加载订单详情
  } catch (error) {
    console.error('取消订单失败:', error);
    alert('取消订单失败，请稍后重试');
  }
};

// 查看物流
const trackLogistics = () => {
  alert('查看物流信息');
  // 实际项目中应该跳转到物流信息页面
};

// 确认收货
const confirmReceipt = async () => {
  if (!confirm('确认已收到商品吗？')) return;
  
  alert('确认收货成功');
  // 实际项目中应该调用确认收货的API
};

// 评价商品
const reviewProduct = () => {
  alert('评价商品');
  // 实际项目中应该跳转到评价页面
};

// 初始化页面
onMounted(() => {
  loadOrderDetail();
});
</script>

<style scoped>
.order-detail-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #faf7f4;
}

.order-detail-content {
  flex: 1;
  padding: 2rem;
  max-width: 1000px;
  margin: 0 auto;
  width: 100%;
}

.order-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.order-detail-header h2 {
  color: #4b2e1e;
  font-size: 1.8rem;
  margin: 0;
}

.order-detail-main {
  background: #fff;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.detail-section {
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #f0e6e0;
}

.detail-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.detail-section h3 {
  color: #4b2e1e;
  font-size: 1.3rem;
  margin-bottom: 1rem;
}

.detail-info {
  display: grid;
  gap: 0.8rem;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.info-row label {
  color: #6e4a2e;
  font-weight: 500;
  min-width: 100px;
}

.info-row span {
  color: #4b2e1e;
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

/* 商品列表样式 */
.product-list {
  border: 1px solid #f0e6e0;
  border-radius: 6px;
  overflow: hidden;
}

.product-header {
  display: flex;
  background-color: #f8f5f2;
  padding: 1rem;
  font-weight: 500;
  color: #6e4a2e;
}

.product-col {
  display: flex;
  align-items: center;
}

.col-name {
  flex: 2;
}

.col-price,
.col-quantity,
.col-subtotal {
  width: 150px;
  justify-content: center;
}

.product-item {
  display: flex;
  padding: 1rem;
  border-top: 1px solid #f0e6e0;
}

.product-item .col-name {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-name {
  color: #4b2e1e;
  font-weight: 500;
}

.product-item .col-price {
  color: #6e4a2e;
}

.product-item .col-subtotal {
  color: #a87b5e;
  font-weight: 600;
}

/* 总计样式 */
.total-section {
  background-color: #f8f5f2;
  padding: 1.5rem;
  border-radius: 6px;
  margin-top: 1rem;
}

.total-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.8rem;
}

.total-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  color: #6e4a2e;
}

.total-item.total-amount {
  font-size: 1.2rem;
  font-weight: 600;
  margin-top: 0.5rem;
}

.total-item.total-amount span {
  color: #a87b5e;
  font-size: 1.4rem;
}

/* 订单操作样式 */
.order-actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid #f0e6e0;
}

/* 加载状态样式 */
.loading-container {
  text-align: center;
  padding: 3rem;
  color: #6e4a2e;
}

/* 错误信息样式 */
.error-message {
  text-align: center;
  padding: 3rem;
}

.go-back-btn {
  margin-top: 1.5rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .order-detail-content {
    padding: 1rem;
  }
  
  .order-detail-main {
    padding: 1.5rem;
  }
  
  .product-header {
    font-size: 0.9rem;
  }
  
  .product-col {
    font-size: 0.9rem;
  }
  
  .col-price,
  .col-quantity,
  .col-subtotal {
    width: 100px;
  }
  
  .order-actions {
    flex-wrap: wrap;
  }
}
</style>
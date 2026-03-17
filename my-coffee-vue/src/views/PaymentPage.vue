<template>
  <div class="payment-page">
    <div class="payment-container">
      <!-- 支付头部 -->
      <div class="payment-header">
        <div class="header-content">
          <el-icon :size="32" class="payment-icon"><CreditCard /></el-icon>
          <div class="header-text">
            <h1>订单支付</h1>
            <p>请在 {{ formatTime(remainingTime) }} 内完成支付</p>
          </div>
        </div>
        <div class="order-number">
          订单号：{{ orderId }}
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <!-- 支付内容 -->
      <div v-else class="payment-content">
        <!-- 订单信息 -->
        <div class="order-info-section">
          <h2>订单信息</h2>
          <div class="order-details">
            <div class="detail-item">
              <span class="label">订单金额</span>
              <span class="value amount">¥{{ orderAmount.toFixed(2) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">订单状态</span>
              <el-tag :type="getStatusType(orderStatus)">{{ getStatusText(orderStatus) }}</el-tag>
            </div>
            <div class="detail-item">
              <span class="label">创建时间</span>
              <span class="value">{{ orderCreateTime }}</span>
            </div>
            <div class="detail-item" v-if="orderExpireTime">
              <span class="label">过期时间</span>
              <span class="value expire-time">{{ orderExpireTime }}</span>
            </div>
          </div>

          <!-- 商品列表 -->
          <div class="order-items">
            <h3>商品清单</h3>
            <div class="items-list">
              <div v-for="item in orderItems" :key="item.productId" class="order-item">
                <div class="item-image">
                  <img :src="item.image || '/src/assets/coffeeImage/coffee-1576256_1920.jpg'" :alt="item.name" />
                </div>
                <div class="item-info">
                  <div class="item-name">{{ item.name }}</div>
                  <div class="item-price">¥{{ item.price.toFixed(2) }} × {{ item.quantity }}</div>
                </div>
                <div class="item-subtotal">
                  ¥{{ (item.price * item.quantity).toFixed(2) }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 支付方式 -->
        <div class="payment-method-section">
          <h2>支付方式</h2>
          <div class="payment-methods">
            <div class="payment-method active">
              <div class="method-icon">
                <svg t="1772551370387" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4723" width="40" height="40"><path d="M1024.0512 701.0304V196.864A196.9664 196.9664 0 0 0 827.136 0H196.864A196.9664 196.9664 0 0 0 0 196.864v630.272A196.9152 196.9152 0 0 0 196.864 1024h630.272a197.12 197.12 0 0 0 193.8432-162.0992c-52.224-22.6304-278.528-120.32-396.4416-176.64-89.7024 108.6976-183.7056 173.9264-325.3248 173.9264s-236.1856-87.2448-224.8192-194.048c7.4752-70.0416 55.552-184.576 264.2944-164.9664 110.08 10.3424 160.4096 30.8736 250.1632 60.5184 23.1936-42.5984 42.496-89.4464 57.1392-139.264H248.064v-39.424h196.9152V311.1424H204.8V267.776h240.128V165.632s2.1504-15.9744 19.8144-15.9744h98.4576V267.776h256v43.4176h-256V381.952h208.8448a805.9904 805.9904 0 0 1-84.8384 212.6848c60.672 22.016 336.7936 106.3936 336.7936 106.3936zM283.5456 791.6032c-149.6576 0-173.312-94.464-165.376-133.9392 7.8336-39.3216 51.2-90.624 134.4-90.624 95.5904 0 181.248 24.4736 284.0576 74.5472-72.192 94.0032-160.9216 150.016-253.0816 150.016z" fill="#009FE8" p-id="4724"></path></svg>
              </div>
              <div class="method-info">
                <div class="method-name">支付宝</div>
                <div class="method-desc">推荐使用支付宝扫码支付</div>
              </div>
              <el-icon class="method-check"><CircleCheck /></el-icon>
            </div>
          </div>

          <!-- 二维码区域 -->
          <div class="qrcode-section">
            <div class="qrcode-container">
              <div v-if="qrcodeLoading" class="qrcode-loading">
                <el-icon class="is-loading" :size="40"><Loading /></el-icon>
                <p>正在生成支付二维码...</p>
              </div>
              <div v-else-if="qrcodeUrl" class="qrcode-content">
                <img :src="qrcodeUrl" alt="支付二维码" class="qrcode-image" />
                <p class="qrcode-tip">
                  <el-icon><Iphone /></el-icon>
                  请使用支付宝扫描二维码完成支付
                </p>
              </div>
              <div v-else class="qrcode-error">
                <el-icon :size="40"><WarningFilled /></el-icon>
                <p>二维码生成失败</p>
                <el-button @click="generateQRCode" type="primary" size="small">重新生成</el-button>
              </div>
            </div>

            <!-- 支付金额显示 -->
            <div class="payment-amount">
              <div class="amount-label">需支付</div>
              <div class="amount-value">¥{{ orderAmount.toFixed(2) }}</div>
            </div>

            <!-- 支付按钮 -->
            <div class="payment-actions">
              <el-button 
                type="success" 
                size="large" 
                @click="confirmPayment"
                :loading="paymentLoading"
                class="confirm-payment-btn"
              >
                <el-icon v-if="!paymentLoading"><Select /></el-icon>
                {{ paymentLoading ? '处理中...' : '我已完成支付' }}
              </el-button>
              <el-button size="large" @click="cancelPayment" class="cancel-payment-btn">
                取消支付
              </el-button>
            </div>

            <!-- 支付提示 -->
            <div class="payment-tips">
              <el-alert
                title="支付提示"
                type="info"
                :closable="false"
                show-icon
              >
                <template #default>
                  <ul>
                    <li>请在规定时间内完成支付，超时订单将自动取消</li>
                    <li>支付完成后请点击"我已完成支付"按钮</li>
                    <li>如遇到问题，请联系客服</li>
                  </ul>
                </template>
              </el-alert>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  CreditCard, CircleCheck, Loading, Iphone, WarningFilled, 
  Select 
} from '@element-plus/icons-vue'
import { getOrderDetails } from '@/api/order'
import { pay, paid } from '@/api/pay'

const route = useRoute()
const router = useRouter()

const orderId = ref(route.params.id as string)
const loading = ref(false)
const qrcodeLoading = ref(false)
const paymentLoading = ref(false)

const orderAmount = ref(0)
const orderStatus = ref('pending')
const orderCreateTime = ref('')
const orderExpireTime = ref('') // 订单过期时间
const orderItems = ref<any[]>([])
const qrcodeUrl = ref('')
const remainingTime = ref(1800) // 剩余时间（秒）

let timer: any = null

// 计算剩余时间
const calculateRemainingTime = (expireTime: string) => {
  const now = new Date().getTime()
  const expire = new Date(expireTime).getTime()
  const diff = Math.floor((expire - now) / 1000)
  return diff > 0 ? diff : 0
}

// 格式化时间
const formatTime = (seconds: number) => {
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 获取状态类型
const getStatusType = (status: string) => {
  const types: Record<string, any> = {
    pending: 'warning',
    paid: 'success',
    cancelled: 'danger',
    expired: 'info'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    pending: '待支付',
    paid: '已支付',
    cancelled: '已取消',
    expired: '已过期'
  }
  return texts[status] || status
}

// 加载订单详情
const loadOrderDetails = async () => {
  loading.value = true
  try {
    const response = await getOrderDetails(Number(orderId.value))
    const order = response.data.data
    
    console.log('订单详情:', order)
    
    orderAmount.value = order.totalPrice
    orderStatus.value = order.status
    orderCreateTime.value = order.createdAt
    orderExpireTime.value = order.expireTime || '' // 获取过期时间
    
    // 如果有过期时间，计算剩余时间
    if (order.expireTime) {
      remainingTime.value = calculateRemainingTime(order.expireTime)
      console.log('剩余时间（秒）:', remainingTime.value)
    }
    
    orderItems.value = order.items.map((item: any) => ({
      productId: item.productId,
      name: item.productName,
      price: item.price,
      quantity: item.quantity,
      image: item.productImages?.[0] ? `http://localhost:8081${item.productImages[0]}` : ''
    }))

    // 如果订单已支付，跳转到订单详情
    if (order.status === 'paid') {
      ElMessage.success('订单已支付')
      router.push(`/order/detail/${orderId.value}`)
      return
    }

    // 如果后端已经生成了二维码，直接使用
    if (order.image) {
      qrcodeUrl.value = `http://localhost:8081${order.image}`
      console.log('使用后端二维码:', qrcodeUrl.value)
    } else {
      // 否则调用生成二维码接口
      await generateQRCode()
    }
  } catch (error) {
    console.error('加载订单失败:', error)
    ElMessage.error('加载订单信息失败')
  } finally {
    loading.value = false
  }
}

// 生成支付二维码
const generateQRCode = async () => {
  qrcodeLoading.value = true
  try {
    const response = await pay(orderId.value)
    console.log('支付接口响应:', response)
    
    // 从后端获取二维码图片路径
    if (response.data.data) {
      qrcodeUrl.value = `http://localhost:8081${response.data.data}`
      console.log('二维码URL:', qrcodeUrl.value)
    } else {
      throw new Error('二维码生成失败')
    }
  } catch (error) {
    console.error('生成二维码失败:', error)
    ElMessage.error('生成支付二维码失败')
  } finally {
    qrcodeLoading.value = false
  }
}

// 确认支付
const confirmPayment = async () => {
  paymentLoading.value = true
  try {
    // 调用已支付接口
    const response = await paid(orderId.value)
    
    if (response.data) {
      ElMessage.success('支付成功！')
      // 跳转到订单详情页
      setTimeout(() => {
        router.push(`/order/detail/${orderId.value}`)
      }, 1000)
    } else {
      ElMessage.warning('支付确认失败，请稍后重试')
    }
  } catch (error) {
    console.error('确认支付失败:', error)
    ElMessage.error('支付确认失败')
  } finally {
    paymentLoading.value = false
  }
}

// 取消支付
const cancelPayment = async () => {
  try {
    await ElMessageBox.confirm('确定要取消支付吗？订单将被取消', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '继续支付',
      type: 'warning'
    })
    
    router.push('/cart')
  } catch (error) {
    // 用户点击了继续支付
  }
}

// 倒计时
const startCountdown = () => {
  timer = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--
    } else {
      clearInterval(timer)
      ElMessage.warning('支付超时，订单已取消')
      router.push('/orders')
    }
  }, 1000)
}

onMounted(() => {
  loadOrderDetails()
  startCountdown()
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.payment-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  padding: 40px 20px;
}

.payment-container {
  max-width: 1200px;
  margin: 0 auto;
}

.payment-header {
  background: white;
  padding: 32px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.payment-icon {
  color: #a87b5e;
}

.header-text h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
}

.header-text p {
  margin: 0;
  color: #e74c3c;
  font-size: 14px;
  font-weight: 600;
}

.order-number {
  color: #7f8c8d;
  font-size: 14px;
}

.loading-container {
  background: white;
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.payment-content {
  display: grid;
  grid-template-columns: 1fr 480px;
  gap: 24px;
}

.order-info-section,
.payment-method-section {
  background: white;
  padding: 32px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.order-info-section h2,
.payment-method-section h2 {
  margin: 0 0 24px 0;
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  padding-bottom: 16px;
  border-bottom: 2px solid #ecf0f1;
}

.order-details {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 32px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
}

.detail-item .label {
  color: #7f8c8d;
  font-size: 14px;
}

.detail-item .value {
  color: #2c3e50;
  font-weight: 600;
}

.detail-item .amount {
  font-size: 24px;
  color: #e74c3c;
}

.detail-item .expire-time {
  color: #e74c3c;
  font-size: 14px;
}

.order-items h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
}

.item-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.item-price {
  font-size: 13px;
  color: #7f8c8d;
}

.item-subtotal {
  font-size: 16px;
  font-weight: 600;
  color: #a87b5e;
}

.payment-methods {
  margin-bottom: 32px;
}

.payment-method {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border: 2px solid #ecf0f1;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.payment-method.active {
  border-color: #a87b5e;
  background: #faf7f4;
}

.method-icon img {
  width: 48px;
  height: 48px;
}

.method-info {
  flex: 1;
}

.method-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.method-desc {
  font-size: 13px;
  color: #7f8c8d;
}

.method-check {
  color: #a87b5e;
  font-size: 24px;
}

.qrcode-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.qrcode-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
  background: #f8f9fa;
  border-radius: 12px;
  padding: 32px;
}

.qrcode-loading,
.qrcode-error {
  text-align: center;
  color: #7f8c8d;
}

.qrcode-content {
  text-align: center;
}

.qrcode-image {
  width: 240px;
  height: 240px;
  border: 4px solid white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.qrcode-tip {
  margin-top: 16px;
  color: #7f8c8d;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.payment-amount {
  text-align: center;
  padding: 24px;
  background: linear-gradient(135deg, #a87b5e 0%, #8c5e46 100%);
  border-radius: 12px;
  color: white;
}

.amount-label {
  font-size: 14px;
  margin-bottom: 8px;
  opacity: 0.9;
}

.amount-value {
  font-size: 36px;
  font-weight: 700;
}

.payment-actions {
  display: flex;
  gap: 12px;
}

.confirm-payment-btn,
.cancel-payment-btn {
  flex: 1;
  padding: 16px;
  font-size: 16px;
  font-weight: 600;
}

.confirm-payment-btn {
  background: linear-gradient(135deg, #27ae60 0%, #229954 100%);
  border: none;
}

.confirm-payment-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #229954 0%, #1e8449 100%);
}

.payment-tips {
  margin-top: 24px;
}

.payment-tips ul {
  margin: 0;
  padding-left: 20px;
}

.payment-tips li {
  margin: 8px 0;
  font-size: 13px;
  color: #7f8c8d;
}

@media (max-width: 1024px) {
  .payment-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .payment-page {
    padding: 20px 12px;
  }
  
  .payment-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .header-text h1 {
    font-size: 22px;
  }
  
  .payment-actions {
    flex-direction: column;
  }
}
</style>

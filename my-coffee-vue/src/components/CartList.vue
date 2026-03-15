<template>
  <div class="cart-page">
    <div class="cart-container">
      <!-- 购物车标题 -->
      <div class="cart-header">
        <h1>购物车</h1>
        <el-button type="text" @click="handleClearCart" class="clear-cart-btn" v-if="cartItems.length > 0">
          <el-icon><Delete /></el-icon>
          清空购物车
        </el-button>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>

      <!-- 购物车为空 -->
      <div v-else-if="cartItems.length === 0" class="empty-cart">
        <div class="empty-icon">
          <el-icon :size="80"><ShoppingCartFull /></el-icon>
        </div>
        <h3>购物车还是空的</h3>
        <p>快去挑选心仪的咖啡商品吧~</p>
        <el-button type="primary" @click="goToProducts" class="go-shopping-btn">
          <el-icon><Shop /></el-icon>
          去购物
        </el-button>
      </div>

      <!-- 购物车内容 -->
      <div v-else class="cart-content">
        <!-- 商品列表 -->
        <div class="cart-items-section">
          <div class="section-header">
            <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
            <span class="item-count">共 {{ cartItems.length }} 件商品</span>
          </div>

          <div class="cart-items">
            <div v-for="item in cartItems" :key="item.productId" class="cart-item">
              <el-checkbox v-model="item.selected" @change="handleItemSelect" class="item-checkbox" />
              
              <div class="item-image">
                <img
                  v-if="item.productImages && item.productImages.length > 0"
                  :src="'/api' + item.productImages[0]"
                  :alt="item.productName"
                />
                <img v-else src="/src/assets/coffeeImage/coffee-1576256_1920.jpg" alt="默认图片" />
              </div>

              <div class="item-info">
                <h3 class="item-name">{{ item.productName }}</h3>
                <p class="item-category">{{ item.category || '咖啡商品' }}</p>
              </div>

              <div class="item-price">
                <span class="price-label">单价</span>
                <span class="price-value">¥{{ item.price.toFixed(2) }}</span>
              </div>

              <div class="item-quantity">
                <el-input-number
                  v-model="item.quantity"
                  :min="1"
                  :max="99"
                  @change="handleQuantityChange(item)"
                  size="default"
                />
              </div>

              <div class="item-subtotal">
                <span class="subtotal-label">小计</span>
                <span class="subtotal-value">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
              </div>

              <div class="item-actions">
                <el-button type="text" @click="handleRemoveItem(item.productId)" class="remove-btn">
                  <el-icon><Delete /></el-icon>
                  移除
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 结算区域 -->
        <div class="checkout-section">
          <div class="checkout-card">
            <h3>订单摘要</h3>
            
            <div class="summary-item">
              <span>已选商品</span>
              <span>{{ selectedCount }} 件</span>
            </div>
            
            <div class="summary-item">
              <span>商品总价</span>
              <span>¥{{ totalPrice.toFixed(2) }}</span>
            </div>
            
            <div class="summary-divider"></div>
            
            <div class="summary-total">
              <span>合计</span>
              <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
            </div>

            <el-button 
              type="primary" 
              @click="checkout" 
              :disabled="selectedCount === 0"
              class="checkout-btn"
              size="large"
            >
              <el-icon><CreditCard /></el-icon>
              去结算
            </el-button>

            <div class="checkout-tips">
              <el-icon><InfoFilled /></el-icon>
              <span>支持支付宝在线支付</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 地址选择对话框 -->
    <el-dialog 
      v-model="addressDialogVisible" 
      title="选择收货地址" 
      width="700px"
      :close-on-click-modal="false"
    >
      <div class="address-dialog-content">
        <!-- 地址列表 -->
        <div v-if="addresses.length > 0" class="address-list">
          <div 
            v-for="addr in addresses" 
            :key="addr.id" 
            class="address-item"
            :class="{ 'selected': selectedAddressId === addr.id }"
            @click="selectAddress(addr.id)"
          >
            <el-radio :model-value="selectedAddressId" :label="addr.id" @change="selectAddress(addr.id)">
              <div class="address-content">
                <div class="address-header">
                  <span class="recipient-name">{{ addr.recipientName }}</span>
                  <span class="recipient-phone">{{ addr.phone }}</span>
                  <el-tag v-if="addr.isDefault" type="success" size="small">默认</el-tag>
                </div>
                <div class="address-detail">
                  {{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detailAddress }}
                </div>
              </div>
            </el-radio>
          </div>
        </div>

        <!-- 无地址提示 -->
        <el-empty v-else description="暂无收货地址" />

        <!-- 添加新地址按钮 -->
        <el-button 
          type="primary" 
          @click="showAddAddressForm" 
          class="add-address-btn"
          plain
        >
          <el-icon><Plus /></el-icon>
          添加新地址
        </el-button>

        <!-- 添加地址表单 -->
        <div v-if="showAddressForm" class="add-address-form">
          <el-form :model="newAddress" label-width="100px">
            <el-form-item label="收货人" required>
              <el-input v-model="newAddress.recipientName" placeholder="请输入收货人姓名" />
            </el-form-item>
            <el-form-item label="手机号" required>
              <el-input v-model="newAddress.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="省份" required>
              <el-input v-model="newAddress.province" placeholder="请输入省份" />
            </el-form-item>
            <el-form-item label="城市" required>
              <el-input v-model="newAddress.city" placeholder="请输入城市" />
            </el-form-item>
            <el-form-item label="区/县" required>
              <el-input v-model="newAddress.district" placeholder="请输入区/县" />
            </el-form-item>
            <el-form-item label="详细地址" required>
              <el-input 
                v-model="newAddress.detailAddress" 
                type="textarea" 
                :rows="3"
                placeholder="请输入详细地址"
              />
            </el-form-item>
            <el-form-item label="设为默认">
              <el-switch v-model="newAddress.isDefault" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveNewAddress" :loading="savingAddress">保存地址</el-button>
              <el-button @click="showAddressForm = false">取消</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="confirmCheckout" 
          :disabled="!selectedAddressId"
          :loading="checkoutLoading"
        >
          确认下单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, ShoppingCartFull, Shop, CreditCard, InfoFilled, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getUserCart, updateCartItem, removeFromCart, clearCart, type CartProduct } from '@/api/cart'
import { createOrder } from '@/api/order'
import { getUserAddresses, addAddress, type Address } from '@/api/address'

const router = useRouter()
const userStore = useUserStore()

interface CartItemWithProduct extends CartProduct {
  selected: boolean
}

const cartItems = ref<CartItemWithProduct[]>([])
const loading = ref(false)
const checkoutLoading = ref(false)
const selectAll = ref(false)

// 地址相关
const addressDialogVisible = ref(false)
const addresses = ref<Address[]>([])
const selectedAddressId = ref<number | null>(null)
const showAddressForm = ref(false)
const savingAddress = ref(false)
const newAddress = ref<Address>({
  recipientName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: false
})

// 加载购物车
const loadCartItems = async () => {
  loading.value = true
  try {
    if (!userStore.isLogin || !userStore.userInfo) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }
    const userId = userStore.userInfo.id.toString()
    const res = await getUserCart(userId)
    const items = res.data.data.items
    cartItems.value = Array.isArray(items) ? items.map(item => ({ ...item, selected: true })) : []
    updateSelectAllStatus()
  } catch (err: any) {
    console.error(err)
    ElMessage.error('加载购物车失败')
  } finally {
    loading.value = false
  }
}

// 加载地址列表
const loadAddresses = async () => {
  try {
    if (!userStore.userInfo) return
    const res = await getUserAddresses(userStore.userInfo.id)
    addresses.value = res.data.data || []
    
    // 自动选择默认地址
    const defaultAddr = addresses.value.find(addr => addr.isDefault)
    if (defaultAddr) {
      selectedAddressId.value = defaultAddr.id || null
    } else if (addresses.value.length > 0) {
      selectedAddressId.value = addresses.value[0].id || null
    }
  } catch (error) {
    console.error('加载地址失败:', error)
  }
}

// 选择地址
const selectAddress = (id: number | undefined) => {
  if (id) {
    selectedAddressId.value = id
  }
}

// 显示添加地址表单
const showAddAddressForm = () => {
  showAddressForm.value = true
  newAddress.value = {
    recipientName: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detailAddress: '',
    isDefault: false
  }
}

// 保存新地址
const saveNewAddress = async () => {
  if (!newAddress.value.recipientName || !newAddress.value.phone || 
      !newAddress.value.province || !newAddress.value.city || 
      !newAddress.value.district || !newAddress.value.detailAddress) {
    ElMessage.warning('请填写完整的地址信息')
    return
  }

  savingAddress.value = true
  try {
    if (!userStore.userInfo) return
    const addressData = {
      ...newAddress.value,
      userId: userStore.userInfo.id
    }
    await addAddress(addressData)
    ElMessage.success('地址添加成功')
    showAddressForm.value = false
    await loadAddresses()
  } catch (error) {
    ElMessage.error('添加地址失败')
  } finally {
    savingAddress.value = false
  }
}

// 更新全选状态
const updateSelectAllStatus = () => {
  selectAll.value = cartItems.value.length > 0 && cartItems.value.every(i => i.selected)
}

// 全选操作
const handleSelectAll = (value: boolean) => {
  cartItems.value.forEach(item => (item.selected = value))
}

// 单个选择
const handleItemSelect = () => updateSelectAllStatus()

// 数量变化
const handleQuantityChange = async (item: CartItemWithProduct) => {
  try {
    if (!userStore.userInfo) return
    const userId = userStore.userInfo.id.toString()
    await updateCartItem(userId, item.productId, item.quantity)
    ElMessage.success('数量已更新')
  } catch (err) {
    console.error(err)
    ElMessage.error('更新数量失败')
  }
}

// 移除商品
const handleRemoveItem = async (productId: number) => {
  try {
    await ElMessageBox.confirm('确定要移除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    if (!userStore.userInfo) return
    const userId = userStore.userInfo.id.toString()
    await removeFromCart(userId, productId)
    cartItems.value = cartItems.value.filter(i => i.productId !== productId)
    updateSelectAllStatus()
    ElMessage.success('已移除')
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('移除失败')
    }
  }
}

// 清空购物车
const handleClearCart = async () => {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    if (!userStore.userInfo) return
    const userId = userStore.userInfo.id.toString()
    await clearCart(userId)
    cartItems.value = []
    selectAll.value = false
    ElMessage.success('购物车已清空')
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('清空失败')
    }
  }
}

// 前往商品页
const goToProducts = () => router.push('/products')

// 计算选中数量
const selectedCount = computed(() =>
  cartItems.value.filter(i => i.selected).reduce((sum, i) => sum + i.quantity, 0)
)

// 计算总价
const totalPrice = computed(() =>
  cartItems.value.filter(i => i.selected).reduce((sum, i) => sum + i.price * i.quantity, 0)
)

// 结算 - 打开地址选择对话框
const checkout = async () => {
  if (selectedCount.value === 0) {
    ElMessage.warning('请至少选择一件商品')
    return
  }

  // 加载地址列表
  await loadAddresses()
  addressDialogVisible.value = true
}

// 确认下单
const confirmCheckout = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }

  checkoutLoading.value = true
  try {
    if (!userStore.userInfo) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }

    // 获取选中的商品
    const selectedItems = cartItems.value.filter(i => i.selected)
    
    if (selectedItems.length === 0) {
      ElMessage.warning('请至少选择一件商品')
      checkoutLoading.value = false
      return
    }
    
    // 创建订单
    const orderData = {
      userId: userStore.userInfo.id,
      addressId: selectedAddressId.value,
      items: selectedItems.map(item => ({
        productId: item.productId,
        quantity: item.quantity
      }))
    }

    console.log('创建订单数据:', orderData)
    const response = await createOrder(orderData)
    console.log('订单创建响应:', response)
    
    // 处理不同的响应格式
    let orderId: number | undefined
    
    // 格式1: response.data.data.id
    if (response.data?.data?.id) {
      orderId = response.data.data.id
    }
    // 格式2: response.data.id
    else if (response.data?.id) {
      orderId = response.data.id
    }
    // 格式3: response.id (直接返回Order对象)
    else if ((response as any).id) {
      orderId = (response as any).id
    }
    
    if (orderId) {
      ElMessage.success('订单创建成功')
      addressDialogVisible.value = false
      
      // 清空已选中的购物车商品
      const selectedProductIds = selectedItems.map(item => item.productId)
      cartItems.value = cartItems.value.filter(item => !selectedProductIds.includes(item.productId))
      
      // 跳转到支付页面
      console.log('跳转到支付页面:', `/payment/${orderId}`)
      await router.push(`/payment/${orderId}`)
    } else {
      console.error('无法获取订单ID，响应:', response)
      ElMessage.error('创建订单失败：无法获取订单ID')
    }
  } catch (err: any) {
    console.error('创建订单错误:', err)
    ElMessage.error('创建订单失败：' + (err.response?.data?.message || err.message || '未知错误'))
  } finally {
    checkoutLoading.value = false
  }
}

onMounted(() => loadCartItems())
</script>

<style scoped>
.cart-page {
  min-height: calc(100vh - 200px);
  background: linear-gradient(135deg, #faf7f4 0%, #f5f0eb 100%);
  padding: 40px 20px;
}

.cart-container {
  max-width: 1400px;
  margin: 0 auto;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.cart-header h1 {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: #4b2e1e;
  font-family: 'Playfair Display', serif;
}

.clear-cart-btn {
  color: #c0392b;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.clear-cart-btn:hover {
  color: #e74c3c;
}

.loading-container {
  background: white;
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.empty-cart {
  text-align: center;
  padding: 80px 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.empty-icon {
  color: #d4c4b0;
  margin-bottom: 24px;
}

.empty-cart h3 {
  font-size: 24px;
  color: #4b2e1e;
  margin: 0 0 12px 0;
}

.empty-cart p {
  color: #8c6e58;
  margin: 0 0 32px 0;
}

.go-shopping-btn {
  background: linear-gradient(135deg, #a87b5e 0%, #8c5e46 100%);
  border: none;
  padding: 12px 32px;
  font-size: 16px;
}

.go-shopping-btn:hover {
  background: linear-gradient(135deg, #8c5e46 0%, #6e4a2e 100%);
}

.cart-content {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
}

.cart-items-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0e8e0;
  margin-bottom: 20px;
}

.item-count {
  color: #8c6e58;
  font-size: 14px;
}

.cart-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cart-item {
  display: grid;
  grid-template-columns: 40px 100px 1fr 120px 140px 120px 80px;
  gap: 16px;
  align-items: center;
  padding: 20px;
  background: #faf7f4;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.cart-item:hover {
  background: #f5f0eb;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.item-checkbox {
  display: flex;
  align-items: center;
}

.item-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.cart-item:hover .item-image img {
  transform: scale(1.05);
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.item-name {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #4b2e1e;
  line-height: 1.4;
}

.item-category {
  margin: 0;
  font-size: 13px;
  color: #8c6e58;
}

.item-price,
.item-subtotal {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.price-label,
.subtotal-label {
  font-size: 12px;
  color: #8c6e58;
}

.price-value {
  font-size: 16px;
  font-weight: 600;
  color: #6e4a2e;
}

.subtotal-value {
  font-size: 18px;
  font-weight: 700;
  color: #a87b5e;
}

.item-actions {
  display: flex;
  justify-content: center;
}

.remove-btn {
  color: #c0392b;
  display: flex;
  align-items: center;
  gap: 4px;
}

.remove-btn:hover {
  color: #e74c3c;
}

.checkout-section {
  position: sticky;
  top: 80px;
  height: fit-content;
}

.checkout-card {
  background: white;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.checkout-card h3 {
  margin: 0 0 24px 0;
  font-size: 20px;
  font-weight: 600;
  color: #4b2e1e;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  color: #6e4a2e;
  font-size: 14px;
}

.summary-divider {
  height: 1px;
  background: #e0d7ce;
  margin: 16px 0;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #4b2e1e;
}

.total-price {
  font-size: 28px;
  font-weight: 700;
  color: #a87b5e;
}

.checkout-btn {
  width: 100%;
  margin-top: 24px;
  padding: 16px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #a87b5e 0%, #8c5e46 100%);
  border: none;
}

.checkout-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #8c5e46 0%, #6e4a2e 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(168, 123, 94, 0.3);
}

.checkout-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 12px;
  background: #f8f5f2;
  border-radius: 8px;
  color: #8c6e58;
  font-size: 13px;
}

/* 地址对话框样式 */
.address-dialog-content {
  max-height: 500px;
  overflow-y: auto;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.address-item {
  padding: 16px;
  border: 2px solid #e0d7ce;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.address-item:hover {
  border-color: #a87b5e;
  background: #faf7f4;
}

.address-item.selected {
  border-color: #a87b5e;
  background: #faf7f4;
}

.address-content {
  margin-left: 8px;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.recipient-name {
  font-size: 16px;
  font-weight: 600;
  color: #4b2e1e;
}

.recipient-phone {
  font-size: 14px;
  color: #8c6e58;
}

.address-detail {
  font-size: 14px;
  color: #6e4a2e;
  line-height: 1.6;
}

.add-address-btn {
  width: 100%;
  margin-bottom: 16px;
}

.add-address-form {
  margin-top: 16px;
  padding: 20px;
  background: #f8f5f2;
  border-radius: 12px;
}

@media (max-width: 1200px) {
  .cart-content {
    grid-template-columns: 1fr;
  }
  
  .checkout-section {
    position: static;
  }
}

@media (max-width: 768px) {
  .cart-page {
    padding: 20px 12px;
  }
  
  .cart-header h1 {
    font-size: 24px;
  }
  
  .cart-item {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .item-checkbox {
    grid-column: 1;
  }
  
  .item-image {
    width: 80px;
    height: 80px;
  }
}
</style>

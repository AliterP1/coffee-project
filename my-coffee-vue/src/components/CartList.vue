<template>
  <div class="cart-list">
    <!-- 购物车标题 -->
    <div class="cart-header">
      <h2>我的购物车</h2>
      <el-button type="text" @click="handleClearCart" class="clear-cart-btn">清空购物车</el-button>
    </div>

    <!-- 购物车为空 -->
    <div v-if="loading && cartItems.length === 0" class="loading">
      <el-loading v-model="loading" text="加载中..." />
    </div>
    <div v-else-if="cartItems.length === 0" class="empty-cart">
      <el-empty description="购物车还是空的，快去选购吧~" />
      <el-button type="primary" @click="goToProducts" class="go-shopping-btn">去购物</el-button>
    </div>

    <!-- 商品列表 -->
    <div v-else class="cart-items-container">
      <!-- 表头 -->
      <div class="cart-items-header">
        <div class="cart-item-col cart-item-select">
          <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
        </div>
        <div class="cart-item-col cart-item-info">商品信息</div>
        <div class="cart-item-col cart-item-price">单价</div>
        <div class="cart-item-col cart-item-quantity">数量</div>
        <div class="cart-item-col cart-item-subtotal">小计</div>
        <div class="cart-item-col cart-item-action">操作</div>
      </div>

      <!-- 商品列表 -->
      <div class="cart-items">
        <div v-for="item in cartItems" :key="item.productId" class="cart-item">
          <div class="cart-item-col cart-item-select">
            <el-checkbox v-model="item.selected" @change="handleItemSelect" />
          </div>
          <div class="cart-item-col cart-item-info">
            <div class="cart-item-image">
              <img
                v-if="item.productImages && item.productImages.length > 0"
                :src="'/api' + item.productImages[0]"
                :alt="item.productName"
              />
              <img v-else src="/src/assets/coffeeImage/coffee-1576256_1920.jpg" alt="默认图片" />
            </div>
            <div class="cart-item-details">
              <div class="cart-item-name">{{ item.productName }}</div>
            </div>
          </div>
          <div class="cart-item-col cart-item-price">¥{{ item.price.toFixed(2) }}</div>
          <div class="cart-item-col cart-item-quantity">
            <el-input-number
              v-model="item.quantity"
              :min="1"
              @change="handleQuantityChange(item)"
              size="small"
            />
          </div>
          <div class="cart-item-col cart-item-subtotal">
            ¥{{ (item.price * item.quantity).toFixed(2) }}
          </div>
          <div class="cart-item-col cart-item-action">
            <el-button type="text" @click="handleRemoveItem(item.productId)" class="remove-btn">移除</el-button>
          </div>
        </div>
      </div>

      <!-- 购物车底部 -->
      <div class="cart-footer">
        <div class="cart-footer-left">
          <span class="selected-count">已选择 {{ selectedCount }} 件商品</span>
        </div>
        <div class="cart-footer-right">
          <div class="total-price">
            合计：<span class="price">¥{{ totalPrice.toFixed(2) }}</span>
          </div>
          <el-button type="primary" @click="checkout" :disabled="selectedCount === 0" class="checkout-btn">
            结算
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserCart, updateCartItem, removeFromCart, clearCart,type CartProduct } from '@/api/cart'

const router = useRouter()
const userStore = useUserStore()

// 购物车商品类型
interface CartItemWithProduct extends CartProduct {
  selected: boolean
}

const cartItems = ref<CartItemWithProduct[]>([])
const loading = ref(false)
const selectAll = ref(false)

// 加载购物车
const loadCartItems = async () => {
  loading.value = true
  try {
    if (!userStore.isLogin || !userStore.userInfo) {
      throw new Error('用户未登录')
    }
    const userId = userStore.userInfo.id.toString()
    const res = await getUserCart(userId)
    const items = res.data.data.items
    console.log(items)
    cartItems.value = Array.isArray(items) ? items.map(item => ({ ...item, selected: true })) : []
    updateSelectAllStatus()
  } catch (err: any) {
    console.error(err)
    alert('加载购物车失败：' + (err.message || '未登录或登录状态失效'))
  } finally {
    loading.value = false
  }
}

// 全选状态
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
    if (!userStore.isLogin || !userStore.userInfo) {
      throw new Error('用户未登录')
    }
    const userId = userStore.userInfo.id.toString()
    await updateCartItem(userId, item.productId, item.quantity)
  } catch (err) {
    console.error(err)
    alert('更新数量失败')
  }
}

// 移除商品
const handleRemoveItem = async (productId: number) => {
  try {
    if (!userStore.isLogin || !userStore.userInfo) {
      throw new Error('用户未登录')
    }
    const userId = userStore.userInfo.id.toString()
    await removeFromCart(userId, productId)
    cartItems.value = cartItems.value.filter(i => i.productId !== productId)
    updateSelectAllStatus()
  } catch (err) {
    console.error(err)
    alert('移除失败')
  }
}

// 清空购物车
const handleClearCart = async () => {
  if (!cartItems.value.length) return alert('购物车已为空')
  if (!confirm('确定清空购物车吗？')) return
  try {
    if (!userStore.isLogin || !userStore.userInfo) {
      throw new Error('用户未登录')
    }
    const userId = userStore.userInfo.id.toString()
    await clearCart(userId)
    cartItems.value = []
    selectAll.value = false
  } catch (err) {
    console.error(err)
    alert('清空失败')
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

// 结算
const checkout = () => {
  if (selectedCount.value === 0) return alert('请至少选择一件商品')
  alert(`已选择 ${selectedCount.value} 件商品，总价 ¥${totalPrice.value.toFixed(2)}`)
}

onMounted(() => loadCartItems())
</script>

<style scoped>
.cart-list {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  background: #faf7f4;
  min-height: calc(100vh - 200px);
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}

.cart-header h2 {
  margin: 0;
  color: #4b2e1e;
  font-family: "Work Sans", sans-serif;
  font-size: 24px;
}

.clear-cart-btn {
  color: #a87b5e;
}

.clear-cart-btn:hover {
  color: #8c5e46;
}

.loading {
  padding: 60px 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.empty-cart {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}

.go-shopping-btn {
  margin-top: 24px;
  background: linear-gradient(135deg, #a87b5e 0%, #8c5e46 100%);
  border: none;
}

.go-shopping-btn:hover {
  background: linear-gradient(135deg, #8c5e46 0%, #6e4a2e 100%);
}

.cart-items-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}

.cart-items-header {
  display: flex;
  padding: 20px;
  background: #f8f5f2;
  border-bottom: 1px solid #e0d7ce;
  font-weight: 600;
  color: #4b2e1e;
}

.cart-item-col {
  display: flex;
  align-items: center;
}

.cart-item-select {
  width: 80px;
}

.cart-item-info {
  flex: 1;
  min-width: 300px;
}

.cart-item-price,
.cart-item-quantity,
.cart-item-subtotal {
  width: 120px;
  justify-content: center;
}

.cart-item-action {
  width: 80px;
  justify-content: center;
}

.cart-items {
  max-height: 600px;
  overflow-y: auto;
}

.cart-item {
  display: flex;
  padding: 20px;
  border-bottom: 1px solid #f0e8e0;
  transition: background-color 0.3s ease;
}

.cart-item:hover {
  background-color: #faf7f4;
}

.cart-item:last-child {
  border-bottom: none;
}

.cart-item-image {
  width: 80px;
  height: 80px;
  overflow: hidden;
  margin-right: 16px;
  border-radius: 8px;
}

.cart-item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cart-item-details {
  flex: 1;
}

.cart-item-name {
  font-size: 16px;
  font-weight: 500;
  color: #4b2e1e;
  margin-bottom: 8px;
  line-height: 1.4;
}

.cart-item-category {
  font-size: 12px;
  color: #8c6e58;
}

.cart-item-price {
  color: #6e4a2e;
  font-weight: 500;
}

.cart-item-subtotal {
  color: #a87b5e;
  font-weight: 600;
  font-size: 16px;
}

.remove-btn {
  color: #c0392b;
}

.remove-btn:hover {
  color: #e74c3c;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f8f5f2;
  border-top: 1px solid #e0d7ce;
}

.cart-footer-left {
  flex: 1;
}

.selected-count {
  color: #6e4a2e;
  font-size: 14px;
}

.cart-footer-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.total-price {
  font-size: 18px;
  font-weight: 600;
  color: #4b2e1e;
}

.total-price .price {
  color: #a87b5e;
  font-size: 24px;
  margin-left: 8px;
}

.checkout-btn {
  padding: 12px 36px;
  font-size: 16px;
  background: linear-gradient(135deg, #a87b5e 0%, #8c5e46 100%);
  border: none;
}

.checkout-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #8c5e46 0%, #6e4a2e 100%);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .cart-list {
    padding: 16px;
  }
  
  .cart-header {
    padding: 16px;
  }
  
  .cart-header h2 {
    font-size: 20px;
  }
  
  .cart-items-header {
    padding: 16px;
    font-size: 14px;
  }
  
  .cart-item-select {
    width: 60px;
  }
  
  .cart-item-price,
  .cart-item-quantity,
  .cart-item-subtotal,
  .cart-item-action {
    width: 80px;
  }
  
  .cart-item {
    padding: 16px;
  }
  
  .cart-item-image {
    width: 60px;
    height: 60px;
  }
  
  .cart-item-name {
    font-size: 14px;
  }
  
  .cart-footer {
    padding: 16px;
    flex-direction: column;
    gap: 16px;
  }
  
  .cart-footer-right {
    flex-direction: column;
    gap: 16px;
    width: 100%;
  }
  
  .total-price {
    width: 100%;
    text-align: center;
  }
  
  .checkout-btn {
    width: 100%;
  }
}
</style>
<template>
  <div class="product-detail-page">
  <Header />
  <div class="product-detail">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading">
      <el-loading v-loading="loading" element-loading-text="加载中..." />
    </div>
    
    <!-- 商品详情内容 -->
    <div v-else-if="product" class="product-content">
      <!-- 面包屑导航 -->
      <div class="breadcrumb">
        <el-breadcrumb separator="/">{{ product.category }}</el-breadcrumb>
      </div>
      
      <!-- 商品主信息区域 -->
      <div class="product-main">
        <!-- 商品图片轮播 -->
        <div class="product-images">
          <div class="main-image">
            <img :src="'/api' + product.images[0]" :alt="product.name" />
          </div>
          <div v-if="product.images.length > 1" class="thumbnail-list">
            <div v-for="(image, index) in product.images" :key="index" class="thumbnail-item" @click="currentImageIndex = index">
              <img :src="'/api' + image" :alt="`${product.name} - 图片${index+1}`" />
            </div>
          </div>
        </div>
        
        <!-- 商品信息 -->
        <div class="product-info">
          <h1 class="product-name">{{ product.name }}</h1>
          <div class="product-price">¥{{ product.price.toFixed(2) }}</div>
          <div class="product-category">分类：{{ product.category }}</div>
          <div class="product-stock" v-if="product.stock > 0">库存：{{ product.stock }} 件</div>
          <div class="product-stock out-of-stock" v-else>库存：缺货</div>
          
          <div class="product-description">
            {{ product.description }}
          </div>
          
          <!-- 购买区域 -->
          <div class="purchase-area">
            <div class="quantity-selector">
              <span>数量：</span>
              <el-input-number
                v-model="quantity"
                :min="1"
                :max="product.stock"
                size="small"
              />
            </div>
            
            <div class="action-buttons">
              <el-button
                type="primary"
                class="add-to-cart-btn"
                @click="addToUserCart"
                :disabled="product.stock <= 0"
              >
                添加到购物车
              </el-button>
              <el-button
                type="info"
                class="buy-now-btn"
                @click="buyNow"
                :disabled="product.stock <= 0"
              >
                立即购买
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 商品详情和评论标签切换 -->
      <div class="tab-container">
        <el-tabs v-model="activeTab" type="border-card" class="product-tabs">
          <!-- 商品详情标签页 -->
          <el-tab-pane label="商品详情" name="detail">
            <div class="product-details-section">
              <div class="details-content">
                <!-- 这里可以添加更详细的商品信息，如规格参数、使用说明等 -->
                <p>{{ product.description }}</p>
              </div>
            </div>
          </el-tab-pane>
          
          <!-- 商品评论标签页 -->
          <el-tab-pane label="商品评论" name="reviews">
            <div class="product-reviews">
              <!-- 评论统计 -->
              <div class="review-stats">
                <div class="total-reviews">{{ reviews.length }} 条评价</div>
                <div v-if="reviews.length > 0" class="average-rating">
                  <span class="rating-score">{{ (reviews.reduce((sum, r) => sum + r.rating, 0) / reviews.length).toFixed(1) }}</span>
                  <div class="stars">
                    <span v-for="star in 5" :key="star" :class="{ 'star-active': star <= Math.round(reviews.reduce((sum, r) => sum + r.rating, 0) / reviews.length) }">★</span>
                  </div>
                </div>
              </div>
              
              <!-- 评论列表 -->
              <div class="review-list">
                <div v-if="reviewLoading" class="loading-reviews">
                  <el-loading v-loading="reviewLoading" element-loading-text="加载评论中..." />
                </div>
                <div v-else-if="reviews.length === 0" class="no-reviews">
                  <p>暂无评价，快来做第一个评价的人吧！</p>
                </div>
                <div v-for="review in reviews" :key="review.id" class="review-item">
                  <div class="review-header">
                    <img :src="review.avatarUrl || '/api/assets/default-avatar.png'"  class="user-avatar" />
                    <div class="user-info">
                      <div class="username">{{ review.username }}</div>
                      <div class="review-rating">
                        <span v-for="star in 5" :key="star" :class="{ 'star-active': star <= review.rating }">★</span>
                      </div>
                    </div>
                  </div>
                  <div class="review-content">{{ review.comment }}</div>
                </div>
              </div>
              
              <!-- 发表评论按钮 -->
              <div class="add-review-section">
                <el-button type="primary" @click="showReviewForm = !showReviewForm">
                  {{ showReviewForm ? '取消评论' : '发表评价' }}
                </el-button>
                
                <!-- 评论表单（点击按钮后展开） -->
                <div v-if="showReviewForm" class="review-form">
                  <div class="form-group">
                    <label>评分：</label>
                    <div class="rating-selector">
                      <span v-for="star in 5" :key="star" :class="{ 'star-active': star <= tempRating }" @click="tempRating = star">★</span>
                    </div>
                  </div>
                  <div class="form-group">
                    <label>评价内容：</label>
                    <el-input type="textarea" v-model="tempComment" :rows="4" placeholder="请输入您的评价内容..." />
                  </div>
                  <div class="form-actions">
                    <el-button @click="showReviewForm = false">取消</el-button>
                    <el-button type="primary" @click="handleSubmitReview">提交评价</el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
      
      <!-- 推荐商品 -->
      <div class="recommended-products">
        <h2>推荐商品</h2>
        <div class="products-grid">
          <div v-for="recProduct in recommendedProducts" :key="recProduct.id" class="product-card" @click="navigateToProduct(recProduct.id)">
            <div class="product-image">
              <img :src="'/api' + recProduct.images[0]" :alt="recProduct.name" />
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ recProduct.name }}</h3>
              <div class="product-price">¥{{ recProduct.price.toFixed(2) }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 商品不存在提示 -->
    <div v-else class="product-not-found">
      <p>商品不存在或已下架</p>
      <el-button @click="goBack">返回上一页</el-button>
    </div>
  </div>
  </div>
  <Footer />
</template>



<script setup lang="ts">
import Header from '@/components/Header.vue';
import Footer from '@/components/Footer.vue';
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserInfo } from '@/utils/auth'
import { getProductPage } from '@/api/product'
import { addToCart } from '@/api/cart'
import { getProductReviews, addReview } from '@/api/review'
import type { ProductResponseDTO } from '@/api/product'
import type { ReviewResponseDTO, ReviewRequestDTO } from '@/api/review'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

// 获取商品ID
const productId = route.params.productId as string

// 状态管理
const product = ref<ProductResponseDTO | null>(null)
const recommendedProducts = ref<ProductResponseDTO[]>([])
const loading = ref(false)
const quantity = ref(1)
const currentImageIndex = ref(0)
const activeTab = ref('detail') // 活动标签页
const reviews = ref<ReviewResponseDTO[]>([])
const reviewLoading = ref(false)
const showReviewForm = ref(false) // 控制评论表单显示/隐藏
const tempRating = ref(5) // 临时存储评分
const tempComment = ref('') // 临时存储评论内容

// 获取商品详情
const fetchProductDetail = async () => {
  loading.value = true
  try {
    // 由于接口可能不可用，这里先获取所有商品，然后查找匹配的商品
    const response = await getProductPage(1, 99)
    const foundProduct = response.records.find(p => p.id === parseInt(productId))
    
    if (foundProduct) {
      product.value = foundProduct
      
      // 获取推荐商品（同一分类的其他商品）
      recommendedProducts.value = response.records
        .filter(p => p.category === foundProduct.category && p.id !== parseInt(productId))
        .slice(0, 4)
    } else {
      // 如果没有找到商品，可以尝试直接调用详情接口
      try {
        const detailRes = await request.get(`/product/${productId}`)
        product.value = detailRes.data
      } catch (error) {
        console.error('获取商品详情失败:', error)
      }
    }
  } catch (error) {
    console.error('加载商品信息失败:', error)
  } finally {
    loading.value = false
  }
}

// 添加到购物车
const addToUserCart = async () => {
  if (!product.value) return
  
  try {
    // 获取当前登录用户信息
    const userInfo = getUserInfo()
    const userId = userInfo?.id 
    
    // 检查是否已登录
    if (!userId) {
      alert('未登录，请先登录再购买')
      // 未登录，跳转到登录页面
      router.push({
        name: 'Login',
        query: {
          redirect: router.currentRoute.value.fullPath,
          productId: product.value.id
        }
      })
      return
    }
    
    await addToCart(userId, product.value.id, quantity.value)
    alert(`已将"${product.value.name}"添加到购物车`)
  } catch (error) {
    alert('添加到购物车失败，请稍后再试')
  }
}

// 立即购买
const buyNow = async () => {
  if (!product.value) return
  
  // 类似添加到购物车的逻辑，但直接跳转到订单确认页面
  try {
    const userInfo = getUserInfo()
    const userId = userInfo?.id 
    
    if (!userId) {
      alert('未登录，请先登录再购买')
      router.push({
        name: 'Login',
        query: {
          redirect: router.currentRoute.value.fullPath,
          productId: product.value.id
        }
      })
      return
    }
    
    // 先添加到购物车，然后跳转到购物车页面
    await addToCart(userId, product.value.id, quantity.value)
    router.push('/cart')
  } catch (error) {
    console.error('立即购买失败:', error)
    alert('操作失败，请稍后再试')
  }
}

// 跳转到其他商品
const navigateToProduct = (id: number) => {
  router.push({
    name: 'ProductDetail',
    params: {
      productId: id
    }
  })
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 获取商品评论
const fetchProductReviews = async () => {
  if (!product.value) return
  reviewLoading.value = true
  try {
    const response = await getProductReviews(product.value.id)
    // 处理可能的二维数组情况
    reviews.value = Array.isArray(response.records[0]) ? 
      (response.records as any[]).flat() : 
      response.records
  } catch (error) {
    console.error('获取商品评论失败:', error)
  } finally {
    reviewLoading.value = false
  }
}

// 提交评论
const submitReview = async (rating: number, comment: string) => {
  const userStore = useUserStore()
  if (!userStore.isLogin) {
    router.push({ name: 'Login', query: { redirect: encodeURIComponent(router.currentRoute.value.fullPath) } })
    return
  }
  
  try {
    // 使用正确的参数格式调用addReview函数
    const reviewData: ReviewRequestDTO = {
      userId: userStore.userInfo?.id || 0,
      productId: product.value?.id || 0,
      orderId: 0, // 示例订单ID，实际应该从用户订单中获取
      rating,
      comment,
      images: []
    }
    const response=await addReview(reviewData)
    if(response.data.code===200){
      ElMessage.success('评论提交成功')
    }if(response.data.code!==200){
      ElMessage.error(response.data.message || '评论提交失败')
    }
    // 提交成功后刷新评论
    await fetchProductReviews()
  } catch (error) {
    console.error('提交评论失败', error)
    ElMessage.error('提交评论失败，请稍后重试')
  }
}

// 处理表单提交
const handleSubmitReview = async () => {
  if (!tempComment.value.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }
  
  await submitReview(tempRating.value, tempComment.value)
  
  // 提交成功后重置表单
  tempComment.value = ''
  tempRating.value = 5
  showReviewForm.value = false
}

// 初始化时加载商品详情
onMounted(() => {
  fetchProductDetail().then(() => {
    if (product.value) {
      fetchProductReviews()
    }
  })
})
</script>

<style scoped>
.product-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  background: #faf7f4;
  min-height: calc(100vh - 200px);
}

.loading {
  min-height: 60vh;
  display: flex;
  justify-content: center;
  align-items: center;
}

.product-content {
  background: white;
  border-radius: 12px;
  padding: 40px;
}

.breadcrumb {
  margin-bottom: 24px;
  color: #8c6e58;
}

.product-main {
  display: flex;
  gap: 40px;
  margin-bottom: 60px;
}

.product-images {
  flex: 1;
  max-width: 500px;
}

.main-image {
  border: 1px solid #e0d7ce;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 16px;
}

.main-image img {
  width: 100%;
  height: auto;
  object-fit: cover;
}

.thumbnail-list {
  display: flex;
  gap: 12px;
}

.thumbnail-item {
  width: 80px;
  height: 80px;
  border: 2px solid transparent;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.3s ease;
}

.thumbnail-item:hover,
.thumbnail-item.active {
  border-color: #a87b5e;
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 24px 0;
  color: #4b2e1e;
  line-height: 1.4;
}

.product-price {
  font-size: 32px;
  font-weight: 700;
  color: #a87b5e;
  margin: 0 0 16px 0;
}

.product-category {
  font-size: 14px;
  color: #8c6e58;
  margin: 0 0 16px 0;
}

.product-stock {
  font-size: 14px;
  color: #67c23a;
  margin: 0 0 24px 0;
}

.product-stock.out-of-stock {
  color: #f56c6c;
}

.product-description {
  font-size: 16px;
  line-height: 1.8;
  color: #6e4a2e;
  margin: 0 0 32px 0;
  padding: 20px;
  background: #f8f5f2;
  border-radius: 8px;
}

.purchase-area {
  border-top: 1px solid #e0d7ce;
  padding-top: 24px;
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  color: #4b2e1e;
}

.action-buttons {
  display: flex;
  gap: 16px;
}

.add-to-cart-btn {
  flex: 1;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #a87b5e 0%, #8c5e46 100%) !important;
  border: none !important;
}

.add-to-cart-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #8c5e46 0%, #6e4a2e 100%) !important;
}

.buy-now-btn {
  flex: 1;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
}

.tab-container {
  margin-bottom: 60px;
}

.product-tabs {
  border: 1px solid #e0d7ce;
  border-radius: 8px;
  overflow: hidden;
}

.product-tabs .el-tabs__header {
  border-bottom: 1px solid #e0d7ce;
  margin: 0;
}

.product-tabs .el-tabs__nav {
  background-color: #f8f5f2;
  padding-left: 20px;
}

.product-tabs .el-tabs__item {
  padding: 16px 32px;
  font-size: 16px;
  color: #6e4a2e;
}

.product-tabs .el-tabs__item.is-active {
  color: #a87b5e;
  font-weight: 500;
}

.product-tabs .el-tabs__content {
  padding: 24px;
  background-color: white;
}

.product-details-section {
  margin-bottom: 0;
}

.details-content {
  font-size: 16px;
  line-height: 1.8;
  color: #6e4a2e;
}

/* 评论相关样式 */
.product-reviews {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.review-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #e0d7ce;
}

.total-reviews {
  font-size: 18px;
  font-weight: 500;
  color: #4b2e1e;
}

.average-rating {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rating-score {
  font-size: 32px;
  font-weight: 700;
  color: #a87b5e;
}

.stars {
  display: flex;
  gap: 4px;
  font-size: 20px;
  color: #ddd;
}

.star-active {
  color: #f59e0b;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.loading-reviews {
  height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.no-reviews {
  text-align: center;
  padding: 60px 20px;
  color: #8c6e58;
}

.review-item {
  padding: 20px;
  background-color: #f8f5f2;
  border-radius: 8px;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.user-info {
  flex: 1;
}

.username {
  font-weight: 500;
  color: #4b2e1e;
  margin-bottom: 4px;
}

.review-rating {
  display: flex;
  gap: 2px;
  font-size: 14px;
  color: #ddd;
}

.review-content {
  font-size: 16px;
  line-height: 1.6;
  color: #6e4a2e;
}

.add-review-section {
  padding-top: 20px;
  border-top: 1px solid #e0d7ce;
}

.review-form {
  margin-top: 20px;
  padding: 20px;
  background-color: #f8f5f2;
  border-radius: 8px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #4b2e1e;
}

.rating-selector {
  display: flex;
  gap: 8px;
  font-size: 24px;
  cursor: pointer;
  color: #ddd;
}

.rating-selector .star-active {
  color: #f59e0b;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
}

.recommended-products {
  margin-top: 60px;
}

.recommended-products h2 {
  font-size: 24px;
  font-weight: 600;
  color: #4b2e1e;
  margin: 0 0 24px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #a87b5e;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.product-card {
  border: 1px solid #e0d7ce;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(168, 123, 94, 0.1);
}

.product-image {
  width: 100%;
  height: 160px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.product-card .product-info {
  padding: 16px;
}

.product-card .product-name {
  font-size: 16px;
  font-weight: 500;
  margin: 0 0 8px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-card .product-price {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.product-not-found {
  text-align: center;
  padding: 100px 20px;
  background: white;
  border-radius: 12px;
  color: #8c6e58;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-detail {
    padding: 20px 16px;
  }
  
  .product-content {
    padding: 20px;
  }
  
  .product-main {
    flex-direction: column;
    gap: 24px;
  }
  
  .product-images {
    max-width: 100%;
  }
  
  .product-name {
    font-size: 24px;
  }
  
  .product-price {
    font-size: 28px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .products-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
}
</style>
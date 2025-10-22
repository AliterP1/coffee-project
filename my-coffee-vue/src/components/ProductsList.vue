<template>
  <div class="products-list">
    <!-- 商品筛选和搜索 -->
    <div class="filter-section">
      <el-input
        v-model="searchQuery"
        placeholder="搜索商品"
        prefix-icon="Search"
        class="search-input"
      />
      <el-select
        v-model="category"
        placeholder="选择分类"
        class="category-select"
      >
        <el-option label="全部" value="" />
        <el-option label="咖啡豆" value="咖啡豆" />
        <el-option label="咖啡器具" value="咖啡器具" />
        <el-option label="咖啡饮品" value="咖啡饮品" />
      </el-select>
    </div>

    <!-- 商品列表 -->
    <div class="products-grid">
      <div v-for="product in filteredProducts" :key="product.id" class="product-card" @click="navigateToProductDetail(product)">
        <div class="product-image">
          <img :src="'/api'+product.images[0]" :alt="product.name" />
        </div>
        <div class="product-info">
          <h3 class="product-name">{{ product.name }}</h3>
          <p class="product-description">{{ product.description }}</p>
          <div class="product-category">{{ product.category }}</div>
          <div class="product-price">¥{{ product.price.toFixed(2) }}</div>
          <div class="product-stock">库存: {{ product.stock }}</div>
          <el-button
            type="primary"
            class="add-to-cart-btn"
            @click.stop="addProductToCart(product)"
            :disabled="product.stock <= 0"
          >
            {{ product.stock > 0 ? '加入购物车' : '缺货' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 无商品提示 -->
    <div v-if="filteredProducts.length === 0" class="no-products">
      <p>没有找到匹配的商品</p>
    </div>

    <!-- 分页 -->
    <div class="pagination-section">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalProducts"
        :page-sizes="[12, 24, 99]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import type { ProductResponseDTO } from '@/api/product'
import { getProductPage } from '@/api/product'
import { addToCart } from '@/api/cart'
import { getUserInfo } from '@/utils/auth'
import router from '@/router'



// 商品列表数据
const products = ref<ProductResponseDTO[]>([])
const loading = ref(false)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(12)
const totalProducts = ref(0)

// 筛选参数
const searchQuery = ref('')
const category = ref('')

// 过滤后的商品
const filteredProducts = computed(() => {
  return products.value.filter(product => {
    const matchesSearch = product.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
                         product.description.toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchesCategory = !category.value || product.category === category.value
    return matchesSearch && matchesCategory
  })
})

// 加载商品数据
const loadProducts = async () => {
  loading.value = true
  try {
    // 由于后端接口可能不可用，这里提供模拟数据
    // 在实际环境中，会调用真实的API
    const response = await getProductPage(currentPage.value, pageSize.value)
    products.value = response.records
    totalProducts.value = response.total
  } catch (err: any) {
    alert(err.message || '加载商品失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadProducts()
}

// 页码变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadProducts()
}

// 跳转到商品详情页
const navigateToProductDetail = (product: ProductResponseDTO) => {
  router.push({
    name: 'ProductDetail',
    params: {
      productId: product.id
    }
  })
}

// 添加到购物车
const addProductToCart = async (product: ProductResponseDTO) => {
  console.log('添加到购物车:', product)
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
          productId: product.id
        }
      })
      return
    }
    
    const quantity = 1
    await addToCart(userId, product.id, quantity)
    alert(`已将"${product.name}"添加到购物车`)
  } catch (error) {
    console.error('添加到购物车失败:', error)
    alert('添加到购物车失败，请稍后再试')
  }
}

// 初始化时加载商品
onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.products-list {
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
  background: #faf7f4; /* 与Footer浅色背景协调 */
  min-height: calc(100vh - 200px); /* 确保页面有足够高度 */
}

.filter-section {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
  align-items: center;
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}

.search-input {
  flex: 1;
  max-width: 400px;
}

.category-select {
  width: 180px;
}

/* 搜索框和选择框样式与Header保持一致 */
.filter-section :deep(.el-input__inner) {
  border-color: #e0d7ce;
  transition: border-color 0.3s ease;
}

.filter-section :deep(.el-input__inner:focus) {
  border-color: #a87b5e;
}

.filter-section :deep(.el-select .el-input__inner) {
  border-color: #e0d7ce;
}

.filter-section :deep(.el-select .el-input__inner:focus) {
  border-color: #a87b5e;
}

.filter-section :deep(.el-select .el-input__suffix .el-icon) {
  color: #a87b5e;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}

.product-card {
  border: 1px solid #e0d7ce;
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  background: white;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(168, 123, 94, 0.1);
}

.product-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-bottom: 1px solid #f0e8e0;
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

.product-info {
  padding: 20px;
}

.product-name {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: #4b2e1e; /* 与Footer文字颜色一致 */
  font-family: "Work Sans", sans-serif;
}

.product-description {
  font-size: 14px;
  color: #6e4a2e; /* 调整为棕色系列 */
  margin: 0 0 16px 0;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-category {
  font-size: 12px;
  color: #8c6e58; /* 调整为浅棕色 */
  margin: 0 0 12px 0;
  text-transform: capitalize;
}

.product-price {
  font-size: 22px;
  font-weight: 600;
  color: #a87b5e; /* 使用Footer中的棕色 */
  margin: 0 0 8px 0;
}

.product-stock {
  font-size: 13px;
  color: #67c23a;
  margin: 0 0 16px 0;
}

/* 加入购物车按钮使用与Footer一致的棕色渐变 */
.add-to-cart-btn {
  width: 100%;
  background: linear-gradient(135deg, #a87b5e 0%, #8c5e46 100%) !important;
  border: none !important;
  height: 40px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.add-to-cart-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #8c5e46 0%, #6e4a2e 100%) !important;
  transform: translateY(-1px);
}

.add-to-cart-btn:disabled {
  background: #ccc !important;
  color: #fff;
  cursor: not-allowed;
}

.no-products {
  text-align: center;
  padding: 80px 20px;
  color: #8c6e58;
  background: white;
  border-radius: 12px;
  margin-bottom: 40px;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}

/* 分页控件样式调整 */
.pagination-section :deep(.el-pagination__sizes .el-input .el-input__inner) {
  border-color: #e0d7ce;
}

.pagination-section :deep(.el-pagination__sizes .el-input .el-input__inner:focus) {
  border-color: #a87b5e;
}

.pagination-section :deep(.el-pager li) {
  color: #6e4a2e;
}

.pagination-section :deep(.el-pager li:hover) {
  color: #a87b5e;
}

.pagination-section :deep(.el-pager li.active) {
  color: #a87b5e;
  font-weight: 600;
}

.pagination-section :deep(.el-pagination__prev:hover),
.pagination-section :deep(.el-pagination__next:hover) {
  color: #a87b5e;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .products-list {
    padding: 20px 16px;
  }
  
  .filter-section {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
    padding: 16px;
  }
  
  .search-input {
    max-width: 100%;
  }
  
  .category-select {
    width: 100%;
  }
  
  .products-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .product-card {
    border-radius: 8px;
  }
  
  .product-info {
    padding: 16px;
  }
  
  .product-name {
    font-size: 16px;
  }
  
  .product-price {
    font-size: 20px;
  }
}
</style>
<template>
  <div class="search-page">
    <Header />
    
    <!-- 搜索框区域 -->
    <section class="search-header">
      <div class="container">
        <h1>搜索</h1>
        <div class="search-bar">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索咖啡产品或新闻内容..."
            class="search-input"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
            </template>
          </el-input>
        </div>
        <div v-if="searchKeyword" class="search-query">
          搜索结果："{{ searchKeyword }}"
        </div>
      </div>
    </section>
    
    <!-- 搜索结果区域 -->
    <main class="search-results">
      <div class="container">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-spinner size="large" />
          <p>正在搜索中...</p>
        </div>
        
        <!-- 搜索结果标签页 -->
        <div v-else-if="searchResults.length > 0">
          <el-tabs v-model="activeTab" class="search-tabs">
            <el-tab-pane label="全部" name="all" />
            <el-tab-pane label="商品" name="product" />
            <el-tab-pane label="新闻" name="content" />
          </el-tabs>
          
          <!-- 搜索结果列表 -->
          <div class="search-results-list">
            <div 
              v-for="result in filteredResults" 
              :key="result.id"
              :class="['result-item', `result-${result.type.toLowerCase()}`]"
              @click="navigateToDetail(result)"
            >
              <!-- 图片 -->
              <div v-if="result.images && result.images.length > 0" class="result-image">
                <img 
                  :src="result.images && result.images[0] ? (result.images[0].startsWith('http') ? result.images[0] : '/api' + result.images[0]) : ''"
                  :alt="result.title"
                  class="result-image__img"
                />
              </div>
              
              <!-- 内容 -->
              <div class="result-content">
                <h3 class="result-title">{{ result.title }}</h3>
                <p class="result-excerpt">{{ truncateText(result.content, 150) }}</p>
                <div class="result-meta">
                  <span class="result-type">{{ result.type === 'PRODUCT' ? '商品' : '新闻' }}</span>
                  <span class="result-time">{{ formatDate(result.createdAt) }}</span>
                  <span v-if="result.price" class="result-price">¥{{ result.price }}</span>
                  <span v-if="result.authorName" class="result-author">作者：{{ result.authorName }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="totalResults"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
        
        <!-- 空状态 -->
        <div v-else-if="!loading && searchKeyword" class="empty-container">
          <el-empty description="没有找到相关内容" />
        </div>
        
        <!-- 初始状态 -->
        <div v-else class="initial-state">
          <div class="initial-content">
            <el-icon class="search-icon-large"><Search /></el-icon>
            <p>请在上方输入关键词进行搜索</p>
          </div>
        </div>
      </div>
    </main>
    
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'
import type { SearchResultDTO } from '@/api/search'
import { searchCoffeeContent } from '@/api/search'

// 路由和参数
const router = useRouter()
const route = useRoute()

// 搜索关键词
const searchKeyword = ref('')

// 搜索结果
const searchResults = ref<SearchResultDTO[]>([])
const totalResults = ref(0)
const loading = ref(false)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)

// 当前激活的标签页
const activeTab = ref('all')

// 过滤后的搜索结果
const filteredResults = computed(() => {
  if (activeTab.value === 'all') {
    return searchResults.value
  } else if (activeTab.value === 'product') {
    return searchResults.value.filter(item => item.type === 'PRODUCT')
  } else if (activeTab.value === 'content') {
    return searchResults.value.filter(item => item.type === 'CONTENT')
  }
  return searchResults.value
})

// 执行搜索
const performSearch = async () => {
  if (!searchKeyword.value.trim()) {
    return
  }
  
  loading.value = true
  
  try {
    const response = await searchCoffeeContent(
      searchKeyword.value.trim(), 
      currentPage.value, 
      pageSize.value
    )
    searchResults.value = response.records
    totalResults.value = response.total
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 根据结果类型跳转到详情页
const navigateToDetail = (result: SearchResultDTO) => {
  if (result.type === 'PRODUCT') {
    // 跳转到商品详情页
    router.push({
      name: 'ProductDetail',
      params: {
        productId: result.id
      }
    })
  } else if (result.type === 'CONTENT') {
    // 跳转到内容详情页
    router.push({
      name: 'ContentDetail',
      params: {
        contentId: result.id
      }
    })
  }
}

// 截断文本
const truncateText = (text: string, length: number): string => {
  // 去除HTML标签
  const plainText = text.replace(/<[^>]*>/g, '')
  if (plainText.length <= length) return plainText
  return plainText.slice(0, length) + '...'
}

// 格式化日期
const formatDate = (dateString: string): string => {
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    })
  } catch {
    return dateString
  }
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  performSearch()
}

const handleCurrentChange = (current: number) => {
  currentPage.value = current
  performSearch()
}

// 初始化时检查URL参数
onMounted(() => {
  // 从URL参数中获取搜索关键词
  const searchParam = route.query.q as string
  if (searchParam) {
    searchKeyword.value = searchParam
    currentPage.value = 1 // 从URL加载时重置为第一页
    performSearch()
  }
})

// 添加一个新函数用于手动触发搜索（如点击搜索按钮或按回车键）
const handleSearch = () => {
  currentPage.value = 1 // 手动搜索时重置为第一页
  performSearch()
}
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.search-header {
  background-color: #faf7f4;
  padding: 40px 0;
  border-bottom: 1px solid #e0d7ce;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.search-header h1 {
  font-size: 2.5rem;
  color: #4b2e1e;
  margin-bottom: 24px;
  text-align: center;
  font-family: "Work Sans", sans-serif;
}

.search-bar {
  max-width: 600px;
  margin: 0 auto;
}

.search-input {
  height: 50px;
  font-size: 16px;
}

.search-input :deep(.el-input__inner) {
  height: 50px;
  font-size: 16px;
  border-color: #a87b5e;
}

.search-input :deep(.el-input__inner:focus) {
  border-color: #8c5e46;
}

.search-query {
  text-align: center;
  margin-top: 16px;
  color: #6e4a2e;
  font-size: 14px;
}

.search-results {
  flex: 1;
  padding: 40px 0;
  background-color: white;
}

.loading-container, .empty-container, .initial-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #8c6e58;
  min-height: 400px;
}

.loading-container p {
  margin-top: 16px;
  color: #8c6e58;
}

.initial-content {
  text-align: center;
}

.search-icon-large {
  font-size: 64px;
  color: #e0d7ce;
  margin-bottom: 16px;
}

.initial-state p {
  color: #8c6e58;
  font-size: 16px;
}

.search-tabs {
  margin-bottom: 24px;
}

.search-tabs :deep(.el-tabs__nav) {
  border-bottom: 2px solid #e0d7ce;
}

.search-tabs :deep(.el-tabs__item) {
  color: #6e4a2e;
  font-size: 16px;
  padding: 12px 24px;
}

.search-tabs :deep(.el-tabs__item.is-active) {
  color: #a87b5e;
  font-weight: 600;
}

.search-tabs :deep(.el-tabs__active-bar) {
  background-color: #a87b5e;
  height: 3px;
}

.search-results-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100%, 1fr));
  gap: 16px;
}

.result-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border: 1px solid #e0d7ce;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: #faf7f4;
}

.result-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(168, 123, 94, 0.1);
  border-color: #a87b5e;
}

.result-image {
  width: 120px;
  height: 120px;
  margin-right: 20px;
  overflow: hidden;
  border-radius: 4px;
  flex-shrink: 0;
}

.result-image__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.result-item:hover .result-image__img {
  transform: scale(1.05);
}

.result-content {
  flex: 1;
  min-width: 0;
}

.result-title {
  font-size: 18px;
  font-weight: 600;
  color: #4b2e1e;
  margin-bottom: 8px;
  line-height: 1.4;
  font-family: "Work Sans", sans-serif;
}

.result-excerpt {
  font-size: 14px;
  line-height: 1.6;
  color: #6e4a2e;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.result-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
}

.result-type {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
}

.result-item.result-product .result-type {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.result-item.result-content .result-type {
  background-color: #e3f2fd;
  color: #1565c0;
}

.result-time, .result-author {
  font-size: 12px;
  color: #8c6e58;
}

.result-price {
  font-size: 16px;
  font-weight: 600;
  color: #a87b5e;
  margin-left: auto;
}

.pagination {
  margin-top: 32px;
  display: flex;
  justify-content: center;
  padding: 20px;
  background-color: #faf7f4;
  border-radius: 8px;
}

.pagination :deep(.el-pagination__sizes .el-input .el-input__inner) {
  border-color: #e0d7ce;
}

.pagination :deep(.el-pagination__sizes .el-input .el-input__inner:focus) {
  border-color: #a87b5e;
}

.pagination :deep(.el-pager li) {
  color: #6e4a2e;
}

.pagination :deep(.el-pager li:hover) {
  color: #a87b5e;
}

.pagination :deep(.el-pager li.active) {
  color: #a87b5e;
  font-weight: 600;
}

.pagination :deep(.el-pagination__prev:hover),
.pagination :deep(.el-pagination__next:hover) {
  color: #a87b5e;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-header {
    padding: 20px 0;
  }
  
  .search-header h1 {
    font-size: 2rem;
  }
  
  .search-bar {
    padding: 0 16px;
  }
  
  .search-results {
    padding: 20px 0;
  }
  
  .result-item {
    flex-direction: column;
    text-align: center;
    padding: 16px;
  }
  
  .result-image {
    width: 100%;
    height: 180px;
    margin-right: 0;
    margin-bottom: 16px;
  }
  
  .result-meta {
    justify-content: center;
  }
  
  .result-price {
    margin-left: 0;
  }
}
</style>
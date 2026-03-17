<template>
  <div class="media-list">
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
    <div class="media-grid">
      <div v-for="item in filteredContents" :key="item.id" class="media-item">
        <div class="media-item__content">
          <h2 class="media-item__title">{{ item.title }}</h2>
          <div class="media-item__meta">
            <span class="media-item__author">作者：{{ item.authorName }}</span>
          </div>
          <div class="media-item__excerpt">
            {{ truncateText(item.content, 150) }}
          </div>
          <el-button type="primary" size="small" class="media-item__button">
            阅读全文
          </el-button>
        </div>
        
        <!-- 图片展示 -->
        <div v-if="item.images && item.images.length > 0" class="media-item__image">
          <img :src="'/api'+item.images[0]" :alt="item.title" />
        </div>
      </div> 
    </div>

    <!-- 空状态 -->
    <div v-if="filteredContents.length === 0" class="empty-container">
      <el-empty description="暂无新闻内容" />
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
import type { CoffeeContentResponseDTO } from '@/api/coffee'
import { getCoffeeContent } from '@/api/coffee'

// 新闻列表数据
const coffeeContents = ref<CoffeeContentResponseDTO[]>([])
const loading = ref(false)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(12)
const totalProducts = ref(0)

// 筛选参数
const searchQuery = ref('')
const category = ref('')

// 过滤后的商品
const filteredContents = computed(() => {
  return coffeeContents.value.filter(item => {
    const matchesSearch = item.title.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
                         item.content.toLowerCase().includes(searchQuery.value.toLowerCase())
    return matchesSearch
  })
})

// 获取咖啡内容数据
const fetchCoffeeContent = async () => {
  loading.value = true
  try {
    const response = await getCoffeeContent(currentPage.value, pageSize.value)
    coffeeContents.value = response.records
    totalProducts.value = response.total
  } catch (err: any) {
    console.error('获取新闻内容失败:', err)
    // 实际环境中可以添加错误提示
  } finally {
    loading.value = false
  }
}

// 截断文本
const truncateText = (text: string, length: number): string => {
  if (text.length <= length) return text
  return text.slice(0, length) + '...'
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchCoffeeContent()
}

const handleCurrentChange = (current: number) => {
  currentPage.value = current
  fetchCoffeeContent()
}

// 页面加载时获取数据
onMounted(() => {
  fetchCoffeeContent()
})
</script>

<style scoped>
.media-list {
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

.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}

.media-item {
  background-color: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  display: flex;
  flex-direction: column;
  height: 100%;
  border: 1px solid #e0d7ce;
}

.media-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(168, 123, 94, 0.1);
}

.media-item__content {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.media-item__title {
  font-size: 18px;
  font-weight: 600;
  color: #4b2e1e;
  margin-bottom: 12px;
  line-height: 1.4;
  font-family: "Work Sans", sans-serif;
}

.media-item__meta {
  margin-bottom: 16px;
  font-size: 14px;
  color: #8c6e58;
}

.media-item__excerpt {
  font-size: 14px;
  line-height: 1.6;
  color: #6e4a2e;
  margin-bottom: 16px;
  flex: 1;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 使用与Footer一致的棕色渐变按钮 */
.media-item__button {
  align-self: flex-start;
  background: linear-gradient(135deg, #a87b5e 0%, #8c5e46 100%) !important;
  border: none !important;
  height: 36px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.media-item__button:hover {
  background: linear-gradient(135deg, #8c5e46 0%, #6e4a2e 100%) !important;
  transform: translateY(-1px);
}

.media-item__image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-bottom: 1px solid #f0e8e0;
}

.media-item__image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.media-item:hover .media-item__image img {
  transform: scale(1.05);
}

.pagination-section {
  margin-top: 40px;
  display: flex;
  justify-content: center;
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
  .media-list {
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
  
  .media-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .media-item {
    border-radius: 8px;
  }
  
  .media-item__content {
    padding: 16px;
  }
  
  .media-item__title {
    font-size: 16px;
  }
  
  .pagination-section {
    padding: 16px;
  }
}
</style>
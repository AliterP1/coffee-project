<template>
  <div class="media-page">
    <Header />
    
    <!-- 页面标题 -->
    <section class="page-header">
      <div class="container">
        <h1>新闻 & 媒体</h1>
        <p>了解咖啡行业的最新动态、趋势和故事</p>
      </div>
    </section>
    
    <!-- 内容区域 -->
    <main class="media-content">
      <div class="container">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-spinner size="large" />
          <p>加载中...</p>
        </div>
        
        <!-- 新闻列表 -->
        <div v-else class="media-list">
          <div v-for="item in coffeeContents" :key="item.id" class="media-item">
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
          
          <!-- 空状态 -->
          <div v-if="coffeeContents.length === 0" class="empty-container">
            <el-empty description="暂无新闻内容" />
          </div>
          
          <!-- 分页 -->
          <div v-if="coffeeContents.length > 0" class="pagination">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[12, 24, 99]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="totalProducts"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </div>
    </main>
    
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'
import type { CoffeeContentResponseDTO } from '@/api/coffee'
import { getCoffeeContent } from '@/api/coffee'

// 新闻列表数据
const coffeeContents = ref<CoffeeContentResponseDTO[]>([])
const loading = ref(false)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(12)
const totalProducts = ref(0)

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
.media-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.page-header {
  text-align: center;
  padding: 40px 20px;
  height: 150px;
  background-image: url("@/assets/coffeeImage/coffee-2139592_1920.jpg");
  background-size: cover;
  background-position: center;
  color: white;
}

.page-header h1 {
  font-size: 2.5rem;
  color: #ffffff;
  margin-bottom: 16px;
  font-weight: 600;
  font-family: "Work Sans", sans-serif;
}

.page-header p {
  font-size: 1.1rem;
  color: #ffffff;
  max-width: 600px;
  margin: 0 auto;
}

.media-content {
  flex: 1;
  padding: 40px 20px;
  background-color: #faf7f4;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.loading-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #8c6e58;
  background: white;
  border-radius: 12px;
  margin-bottom: 40px;
}

.loading-container p {
  margin-top: 16px;
  color: #8c6e58;
}

.media-list {
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

.pagination {
  margin-top: 40px;
  display: flex;
  justify-content: center;
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}

/* 分页控件样式调整 */
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
  .page-header {
    padding: 40px 0;
  }
  
  .page-header h1 {
    font-size: 2rem;
  }
  
  .media-content {
    padding: 20px 16px;
  }
  
  .media-list {
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
  
  .pagination {
    padding: 16px;
  }
}
</style>
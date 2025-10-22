<template>
  <div class="content-detail-page">
    <Header />
    
    <!-- 页面内容 -->
    <main class="content-detail">
      <div class="container">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-spinner size="large" />
          <p>加载中...</p>
        </div>
        
        <!-- 内容详情 -->
        <div v-else-if="content" class="content-wrapper">
          <!-- 返回按钮 -->
          <el-button 
            type="default" 
            icon="el-icon-arrow-left"
            class="back-button"
            @click="goBack"
          >
            返回
          </el-button>
          
          <!-- 内容主体 -->
          <article class="content-article">
            <!-- 标题和元信息 -->
            <header class="content-header">
              <h1 class="content-title">{{ content.title }}</h1>
              <div class="content-meta">
                <span class="content-author">作者：{{ content.authorName }}</span>
                <span class="content-date">{{ formatDate(content.createdAt) }}</span>
              </div>
            </header>
            
            <!-- 内容图片 -->
            <div v-if="content.images && content.images.length > 0" class="content-images">
              <img 
                v-for="(image, index) in content.images" 
                :key="index"
                :src="image.startsWith('http') ? image : '/api' + image"
                :alt="content.title + ' - 图片' + (index + 1)"
                class="content-image"
              />
            </div>
            
            <!-- 内容正文 -->
            <div class="content-body" v-html="content.content"></div>
          </article>
          
          <!-- 相关内容 -->
          <div v-if="relatedContents.length > 0" class="related-contents">
            <h3 class="related-title">相关文章</h3>
            <div class="related-list">
              <div 
                v-for="item in relatedContents" 
                :key="item.id"
                class="related-item"
                @click="navigateToDetail(item.id)"
              >
                <div v-if="item.images && item.images.length > 0" class="related-image">
                  <img 
                    :src="item.images && item.images[0] ? (item.images[0].startsWith('http') ? item.images[0] : '/api' + item.images[0]) : ''"
                    :alt="item.title"
                  />
                </div>
                <div class="related-content">
                  <h4 class="related-item-title">{{ item.title }}</h4>
                  <p class="related-excerpt">{{ truncateText(item.content, 80) }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 内容不存在 -->
        <div v-else class="not-found-container">
          <el-empty description="该内容不存在或已被删除" />
          <el-button type="primary" @click="goBack">返回上一页</el-button>
        </div>
      </div>
    </main>
    
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'
import type { CoffeeContentResponseDTO } from '@/api/coffee'
import { getCoffeeContent } from '@/api/coffee'

// 路由和参数
const router = useRouter()
const route = useRoute()

// 内容数据
const content = ref<CoffeeContentResponseDTO | null>(null)
const relatedContents = ref<CoffeeContentResponseDTO[]>([])
const loading = ref(false)

// 获取内容详情
const fetchContentDetail = async () => {
  const contentId = route.params.contentId as string
  if (!contentId) {
    return
  }
  
  loading.value = true
  try {
    // 由于没有单独的内容详情接口，这里先获取所有内容，然后查找匹配的内容
    const response = await getCoffeeContent(1, 99)
    const foundContent = response.records.find(item => item.id === parseInt(contentId))
    
    if (foundContent) {
      content.value = foundContent
      
      // 获取相关内容（排除当前内容）
      relatedContents.value = response.records
        .filter(item => item.id !== parseInt(contentId))
        .slice(0, 3)
    }
  } catch (error) {
    console.error('获取内容详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 跳转到其他内容详情页
const navigateToDetail = (id: number) => {
  router.push({
    name: 'ContentDetail',
    params: {
      contentId: id
    }
  })
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

// 页面加载时获取数据
onMounted(() => {
  fetchContentDetail()
})
</script>

<style scoped>
.content-detail-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.content-detail {
  flex: 1;
  padding: 40px 0;
  background-color: white;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

.loading-container, .not-found-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 20px;
  color: #8c6e58;
  min-height: 400px;
}

.loading-container p {
  margin-top: 16px;
  color: #8c6e58;
}

.not-found-container button {
  margin-top: 24px;
}

.content-wrapper {
  background-color: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.back-button {
  margin-bottom: 24px;
  background-color: #faf7f4;
  color: #6e4a2e;
  border-color: #e0d7ce;
}

.back-button:hover {
  background-color: #e0d7ce !important;
  border-color: #a87b5e !important;
  color: #4b2e1e !important;
}

.content-article {
  max-width: 800px;
  margin: 0 auto;
}

.content-header {
  text-align: center;
  margin-bottom: 32px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e0d7ce;
}

.content-title {
  font-size: 2.5rem;
  color: #4b2e1e;
  margin-bottom: 16px;
  line-height: 1.3;
  font-family: "Work Sans", sans-serif;
}

.content-meta {
  display: flex;
  justify-content: center;
  gap: 24px;
  font-size: 14px;
  color: #8c6e58;
}

.content-images {
  margin-bottom: 32px;
}

.content-image {
  width: 100%;
  height: auto;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.content-body {
  font-size: 16px;
  line-height: 1.8;
  color: #4b2e1e;
  margin-bottom: 32px;
}

.content-body p {
  margin-bottom: 20px;
}

.content-body h2 {
  font-size: 2rem;
  color: #4b2e1e;
  margin: 32px 0 16px 0;
  font-family: "Work Sans", sans-serif;
}

.content-body h3 {
  font-size: 1.5rem;
  color: #4b2e1e;
  margin: 24px 0 12px 0;
  font-family: "Work Sans", sans-serif;
}

.content-body img {
  max-width: 100%;
  height: auto;
  margin: 16px 0;
}

.content-body ul, .content-body ol {
  margin: 16px 0 16px 24px;
}

.content-body li {
  margin-bottom: 8px;
}

.related-contents {
  margin-top: 48px;
  padding-top: 32px;
  border-top: 1px solid #e0d7ce;
}

.related-title {
  font-size: 1.5rem;
  color: #4b2e1e;
  margin-bottom: 24px;
  font-family: "Work Sans", sans-serif;
}

.related-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.related-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #e0d7ce;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: #faf7f4;
}

.related-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(168, 123, 94, 0.1);
  border-color: #a87b5e;
}

.related-image {
  width: 80px;
  height: 80px;
  margin-right: 16px;
  overflow: hidden;
  border-radius: 4px;
  flex-shrink: 0;
}

.related-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.related-item:hover .related-image img {
  transform: scale(1.05);
}

.related-content {
  flex: 1;
  min-width: 0;
}

.related-item-title {
  font-size: 14px;
  font-weight: 600;
  color: #4b2e1e;
  margin-bottom: 4px;
  line-height: 1.4;
}

.related-excerpt {
  font-size: 12px;
  line-height: 1.4;
  color: #6e4a2e;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .content-detail {
    padding: 20px 0;
  }
  
  .content-wrapper {
    padding: 16px;
  }
  
  .content-title {
    font-size: 2rem;
  }
  
  .content-meta {
    flex-direction: column;
    gap: 8px;
  }
  
  .content-body {
    font-size: 16px;
  }
  
  .related-list {
    grid-template-columns: 1fr;
  }
  
  .related-item {
    flex-direction: column;
    text-align: center;
  }
  
  .related-image {
    width: 100%;
    height: 150px;
    margin-right: 0;
    margin-bottom: 12px;
  }
}
</style>
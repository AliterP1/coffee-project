<template>
  <div class="merchant-reviews">
    <h1>评价管理</h1>
    
    <el-card>
      <div class="filter-bar">
        <el-input v-model="searchQuery" placeholder="搜索商品名称" style="width: 300px;">
          <template #append>
            <el-button @click="loadReviews"><el-icon><Search /></el-icon></el-button>
          </template>
        </el-input>
        <el-select v-model="ratingFilter" placeholder="筛选评分" style="width: 150px;" @change="loadReviews">
          <el-option label="全部" value="" />
          <el-option label="5星" :value="5" />
          <el-option label="4星" :value="4" />
          <el-option label="3星" :value="3" />
          <el-option label="2星" :value="2" />
          <el-option label="1星" :value="1" />
        </el-select>
      </div>
      
      <el-table :data="reviews" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column prop="rating" label="评分" width="120">
          <template #default="scope">
            <el-rate v-model="scope.row.rating" disabled show-score />
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评价内容" min-width="200" />
        <el-table-column prop="createdAt" label="评价时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button size="small" @click="viewReview(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadReviews"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>
    
    <!-- 评价详情对话框 -->
    <el-dialog title="评价详情" v-model="detailVisible" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="商品名称">{{ currentReview.productName }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ currentReview.userName }}</el-descriptions-item>
        <el-descriptions-item label="评分">
          <el-rate v-model="currentReview.rating" disabled show-score />
        </el-descriptions-item>
        <el-descriptions-item label="评价内容">{{ currentReview.comment }}</el-descriptions-item>
        <el-descriptions-item label="评价时间">{{ currentReview.createdAt }}</el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getProductReviews } from '@/api/review'
import { getProductPage } from '@/api/product'

const userStore = useUserStore()

const searchQuery = ref('')
const ratingFilter = ref<number | ''>('')
const reviews = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const detailVisible = ref(false)
const currentReview = ref<any>({})

const loadReviews = async () => {
  loading.value = true
  try {
    // 先获取商家的商品列表
    const productRes = await getProductPage(1, 100)
    const merchantId = userStore.userInfo?.id
    const merchantProducts = productRes.records.filter((p: any) => p.merchantId === merchantId)
    
    // 为每个商品获取评价
    let allReviews: any[] = []
    for (const product of merchantProducts) {
      try {
        const reviewRes = await getProductReviews(product.id, currentPage.value, pageSize.value)
        if (reviewRes && reviewRes.records) {
          // 为每条评价添加商品名称
          const reviewsWithProductName = reviewRes.records.map((r: any) => ({
            ...r,
            productName: product.name,
            userName: r.username || `用户${r.userId}`
          }))
          allReviews = allReviews.concat(reviewsWithProductName)
        }
      } catch (error) {
        console.error(`获取商品 ${product.id} 的评价失败:`, error)
      }
    }
    
    // 应用筛选条件
    let filtered = allReviews
    if (searchQuery.value) {
      filtered = filtered.filter((r: any) => 
        r.productName?.includes(searchQuery.value)
      )
    }
    if (ratingFilter.value) {
      filtered = filtered.filter((r: any) => r.rating === ratingFilter.value)
    }
    
    reviews.value = filtered
    total.value = filtered.length
  } catch (error) {
    ElMessage.error('加载评价失败')
  } finally {
    loading.value = false
  }
}

const viewReview = (review: any) => {
  currentReview.value = review
  detailVisible.value = true
}

onMounted(() => {
  loadReviews()
})
</script>

<style scoped>
.merchant-reviews {
  padding: 20px;
}

.merchant-reviews h1 {
  margin: 0 0 20px 0;
  font-size: 24px;
  color: #333;
}

.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
</style>

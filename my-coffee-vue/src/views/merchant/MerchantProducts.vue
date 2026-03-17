<template>
  <div class="merchant-products">
    <div class="page-header">
      <h1>我的商品</h1>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        添加商品
      </el-button>
    </div>
    
    <el-card>
      <div class="filter-bar">
        <el-input v-model="searchQuery" placeholder="搜索商品名称" style="width: 300px;">
          <template #append>
            <el-button @click="loadProducts"><el-icon><Search /></el-icon></el-button>
          </template>
        </el-input>
        <el-select v-model="categoryFilter" placeholder="筛选分类" style="width: 150px;" @change="loadProducts">
          <el-option label="全部" value="" />
          <el-option label="咖啡豆" value="咖啡豆" />
          <el-option label="咖啡器具" value="咖啡器具" />
          <el-option label="咖啡饮品" value="咖啡饮品" />
        </el-select>
      </div>
      
      <el-table :data="products" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="图片" width="100">
          <template #default="scope">
            <el-image
              v-if="scope.row.images?.length"
              :src="baseUrl + scope.row.images[0]"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 4px;"
              :preview-src-list="scope.row.images.map((img: string) => baseUrl + img)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="scope">
            <el-tag type="info" size="small">{{ scope.row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100" />
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isActive ? 'success' : 'danger'" size="small">
              {{ scope.row.isActive ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" type="primary" @click="editProduct(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteProductItem(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadProducts"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog :title="isEdit ? '编辑商品' : '添加商品'" v-model="dialogVisible" width="600px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="商品名称" required>
          <el-input v-model="formData.name" />
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="formData.category" style="width: 100%;">
            <el-option label="咖啡豆" value="咖啡豆" />
            <el-option label="咖啡器具" value="咖啡器具" />
            <el-option label="咖啡饮品" value="咖啡饮品" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" required>
          <el-input-number v-model="formData.price" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="库存" required>
          <el-input-number v-model="formData.stock" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="商品图片">
          <el-upload
            :http-request="customUpload"
            list-type="picture-card"
            :file-list="fileList"
            :on-remove="handleRemove"
            multiple
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.isActive" active-text="上架" inactive-text="下架" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProduct" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getMerchantProductPage, addProduct, updateProduct, deleteProduct, uploadProductImages } from '@/api/product'

const userStore = useUserStore()
const baseUrl = 'http://localhost:8081'

const searchQuery = ref('')
const categoryFilter = ref('')
const products = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const fileList = ref<any[]>([])

const formData = ref({
  id: 0,
  name: '',
  category: '咖啡豆',
  price: 0,
  stock: 0,
  description: '',
  isActive: true
})

const loadProducts = async () => {
  loading.value = true
  try {
    const merchantId = userStore.userInfo?.id
    if (!merchantId) {
      ElMessage.error('获取商家信息失败')
      return
    }

    // 使用新的商家商品接口
    const res = await getMerchantProductPage(currentPage.value, pageSize.value, merchantId.toString())
    
    if (res && res.records) {
      let filtered = res.records
      
      // 应用搜索过滤
      if (searchQuery.value) {
        filtered = filtered.filter(p => p.name.includes(searchQuery.value))
      }
      
      // 应用分类过滤
      if (categoryFilter.value) {
        filtered = filtered.filter(p => p.category === categoryFilter.value)
      }
      
      products.value = filtered
      total.value = res.total || filtered.length
    }
  } catch (error) {
    console.error('加载商品失败:', error)
    ElMessage.error('加载商品失败')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  isEdit.value = false
  formData.value = {
    id: 0,
    name: '',
    category: '咖啡豆',
    price: 0,
    stock: 0,
    description: '',
    isActive: true
  }
  fileList.value = []
  dialogVisible.value = true
}

const editProduct = (product: any) => {
  isEdit.value = true
  formData.value = {
    id: product.id,
    name: product.name,
    category: product.category,
    price: product.price,
    stock: product.stock,
    description: product.description || '',
    isActive: product.isActive
  }
  fileList.value = product.images?.map((img: string) => ({
    name: '图片',
    url: baseUrl + img
  })) || []
  dialogVisible.value = true
}

const deleteProductItem = async (product: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteProduct(product.id)
    ElMessage.success('删除成功')
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const customUpload = async (options: any) => {
  try {
    const res = await uploadProductImages(formData.value.id || 0, [options.file])
    const uploadedUrl = Array.isArray(res.data.data) ? res.data.data[0] : res.data.data
    fileList.value.push({
      name: options.file.name,
      url: baseUrl + uploadedUrl
    })
    options.onSuccess(res.data)
  } catch (error) {
    options.onError(error)
  }
}

const handleRemove = (file: any) => {
  fileList.value = fileList.value.filter(item => item.url !== file.url)
}

const saveProduct = async () => {
  if (!formData.value.name || !formData.value.price || !formData.value.stock) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  saving.value = true
  try {
    const images = fileList.value.map(file => file.url.replace(baseUrl, ''))
    const payload = {
      ...formData.value,
      merchantId: userStore.userInfo?.id,
      images
    }
    
    if (isEdit.value) {
      await updateProduct(formData.value.id, payload)
      ElMessage.success('更新成功')
    } else {
      await addProduct(payload)
      ElMessage.success('添加成功')
    }
    
    dialogVisible.value = false
    loadProducts()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.merchant-products {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
</style>

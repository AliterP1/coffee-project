<template>
  <div class="product-management">
    <div class="page-header">
      <h1>商品管理</h1>
      <el-button type="primary" @click="openAddProductDialog">
        <el-icon><Plus /></el-icon>
        添加商品
      </el-button>
    </div>
    
    <el-card class="filter-card" shadow="hover">
      <div class="filter-bar">
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索商品名称" 
          style="width: 300px;"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select 
          v-model="categoryFilter" 
          placeholder="筛选分类" 
          style="width: 150px;"
          clearable
        >
          <el-option label="全部分类" value="" />
          <el-option label="咖啡豆" value="咖啡豆" />
          <el-option label="咖啡器具" value="咖啡器具" />
          <el-option label="咖啡饮品" value="咖啡饮品" />
        </el-select>
        
        <el-select 
          v-model="statusFilter" 
          placeholder="筛选状态" 
          style="width: 150px;"
          clearable
        >
          <el-option label="全部状态" value="" />
          <el-option label="上架" value="active" />
          <el-option label="下架" value="inactive" />
        </el-select>
        
        <el-button type="primary" @click="searchProducts">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        
        <el-button @click="resetFilters">
          <el-icon><RefreshRight /></el-icon>
          重置
        </el-button>
      </div>
    </el-card>
    
    <el-card class="table-card" shadow="hover">
      <el-table 
        :data="products" 
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column label="商品图片" width="100">
          <template #default="scope">
            <div class="product-image-wrapper" v-if="scope.row.images?.length">
              <el-image
                :src="baseUrl + scope.row.images[0]"
                fit="cover"
                class="product-thumb"
                :preview-src-list="scope.row.images.map((img: string) => baseUrl + img)"
                preview-teleported
              />
              <div v-if="scope.row.images.length > 1" class="image-badge">
                +{{ scope.row.images.length - 1 }}
              </div>
            </div>
            <div v-else class="no-image">
              <el-icon><Picture /></el-icon>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="商品信息" min-width="200">
          <template #default="scope">
            <div class="product-info">
              <div class="product-name">{{ scope.row.name }}</div>
              <div class="product-desc">{{ scope.row.description || '暂无描述' }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="category" label="分类" width="100">
          <template #default="scope">
            <el-tag type="info" size="small">{{ scope.row.category }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="price" label="价格" width="100">
          <template #default="scope">
            <span class="price">¥{{ scope.row.price.toFixed(2) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="stock" label="库存" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.stock > 10 ? 'success' : 'warning'" size="small">
              {{ scope.row.stock }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="merchantId" label="商家ID" width="100" />
        
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 'active' ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="editProduct(scope.row)" link>
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button size="small" type="danger" @click="handleDeleteProduct(scope.row)" link>
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="totalProducts"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 添加/编辑商品对话框 -->
    <el-dialog 
      :title="editingProduct ? '编辑商品' : '添加商品'" 
      v-model="dialogVisible" 
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商家ID" prop="merchantId">
              <el-input-number v-model="formData.merchantId" :min="1" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="formData.category" style="width: 100%;">
                <el-option label="咖啡豆" value="咖啡豆" />
                <el-option label="咖啡器具" value="咖啡器具" />
                <el-option label="咖啡饮品" value="咖啡饮品" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入商品名称" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
              <el-input-number v-model="formData.price" :min="0" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="formData.stock" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="商品描述">
          <el-input 
            v-model="formData.description" 
            type="textarea" 
            :rows="4"
            placeholder="请输入商品描述"
          />
        </el-form-item>
        
        <el-form-item label="商品图片">
          <el-upload
            :http-request="customUpload"
            list-type="picture-card"
            :file-list="fileList"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemove"
            multiple
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio label="active">上架</el-radio>
            <el-radio label="inactive">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProduct" :loading="saving">
          {{ saving ? '保存中...' : '保存' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Search, RefreshRight, Edit, Delete, Picture 
} from '@element-plus/icons-vue'
import { 
  getProductPage, addProduct, updateProduct, 
  deleteProduct, uploadProductImages 
} from '@/api/product'
import type { ProductResponseDTO } from '@/api/product'

const baseUrl = 'http://localhost:8081'

const searchQuery = ref('')
const categoryFilter = ref('')
const statusFilter = ref('')
const products = ref<any[]>([])
const totalProducts = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const dialogVisible = ref(false)
const saving = ref(false)
const editingProduct = ref<any>(null)
const formRef = ref()
const fileList = ref<any[]>([])

const formData = ref({
  merchantId: 1,
  name: '',
  category: '咖啡豆',
  price: 0,
  stock: 0,
  description: '',
  status: 'active'
})

const rules = reactive({
  merchantId: [
    { required: true, message: '请输入商家ID', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '商品名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存', trigger: 'blur' }
  ]
})

const loadProducts = async () => {
  loading.value = true
  try {
    const response = await getProductPage(currentPage.value, pageSize.value)
    if (response && response.records) {
      products.value = response.records.map((product: ProductResponseDTO) => ({
        id: product.id || 0,
        name: product.name,
        category: product.category,
        price: product.price,
        stock: product.stock,
        status: product.isActive ? 'active' : 'inactive',
        description: product.description,
        merchantId: product.merchantId || 0,
        isActive: product.isActive,
        images: product.images
      }))
      totalProducts.value = response.total || 0
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

const searchProducts = async () => {
  loading.value = true
  try {
    const response = await getProductPage(1, pageSize.value)
    if (response && response.records) {
      let filteredProducts = response.records
      
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filteredProducts = filteredProducts.filter(product => 
          product.name.toLowerCase().includes(query)
        )
      }
      
      if (categoryFilter.value) {
        filteredProducts = filteredProducts.filter(product => 
          product.category === categoryFilter.value
        )
      }
      
      if (statusFilter.value) {
        filteredProducts = filteredProducts.filter(product => 
          (product.isActive ? 'active' : 'inactive') === statusFilter.value
        )
      }
      
      products.value = filteredProducts.map((product: ProductResponseDTO) => ({
        id: product.id || 0,
        name: product.name,
        category: product.category,
        price: product.price,
        stock: product.stock,
        status: product.isActive ? 'active' : 'inactive',
        description: product.description,
        merchantId: product.merchantId || 0,
        isActive: product.isActive,
        images: product.images
      }))
      totalProducts.value = filteredProducts.length
      currentPage.value = 1
    }
  } catch (error) {
    console.error('搜索商品失败:', error)
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  searchQuery.value = ''
  categoryFilter.value = ''
  statusFilter.value = ''
  currentPage.value = 1
  loadProducts()
}

const openAddProductDialog = () => {
  editingProduct.value = null
  formData.value = {
    merchantId: 1,
    name: '',
    category: '咖啡豆',
    price: 0,
    stock: 0,
    description: '',
    status: 'active'
  }
  fileList.value = []
  dialogVisible.value = true
}

const editProduct = (product: any) => {
  editingProduct.value = product
  formData.value = {
    merchantId: product.merchantId || 1,
    name: product.name,
    category: product.category,
    price: product.price,
    stock: product.stock,
    description: product.description || '',
    status: product.status
  }
  fileList.value = product.images?.map((img: string) => ({
    name: '图片',
    url: baseUrl + img
  })) || []
  dialogVisible.value = true
}

const handleDeleteProduct = async (product: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除商品 ${product.name} 吗？此操作不可恢复！`,
      '删除商品',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    await deleteProduct(product.id)
    ElMessage.success('商品删除成功')
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除商品失败')
    }
  }
}

const customUpload = async (options: any) => {
  try {
    const res = await uploadProductImages(editingProduct.value?.id || 0, [options.file])
    options.onSuccess(res.data)
  } catch (err) {
    options.onError(err)
  }
}

const handleUploadSuccess = (response: any, file: any) => {
  const uploadedUrl = Array.isArray(response.data) ? response.data[0] : response.data.url || ''
  if (uploadedUrl) {
    const updatedFile = {
      uid: file.uid,
      name: file.name,
      url: baseUrl + uploadedUrl,
      status: 'success'
    }
    const index = fileList.value.findIndex(item => item.uid === file.uid)
    if (index !== -1) {
      fileList.value[index] = updatedFile
    } else {
      fileList.value.push(updatedFile)
    }
  }
}

const handleRemove = (file: any) => {
  fileList.value = fileList.value.filter(item => item.url !== file.url)
}

const saveProduct = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    saving.value = true
    try {
      const images = fileList.value
        .filter(file => file.url)
        .map(file => file.url.replace(baseUrl, ''))
      
      const payload = {
        ...formData.value,
        images,
        isActive: formData.value.status === 'active'
      }
      
      if (editingProduct.value) {
        const response = await updateProduct(editingProduct.value.id, payload)
        if (response.data.code === 200) {
          ElMessage.success('商品更新成功')
          dialogVisible.value = false
          loadProducts()
        } else {
          ElMessage.error(response.data.message || '更新失败')
        }
      } else {
        const response = await addProduct(payload)
        if (response.data.code === 200) {
          ElMessage.success('商品添加成功')
          dialogVisible.value = false
          loadProducts()
        } else {
          ElMessage.error(response.data.message || '添加失败')
        }
      }
    } catch (error) {
      console.error('保存商品失败:', error)
      ElMessage.error('保存失败')
    } finally {
      saving.value = false
    }
  })
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  loadProducts()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadProducts()
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.product-management {
  animation: fadeIn 0.5s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
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
  font-weight: 600;
  color: #303133;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.table-card {
  border-radius: 12px;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.product-image-wrapper {
  position: relative;
  width: 70px;
  height: 70px;
}

.product-thumb {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s;
}

.product-thumb:hover {
  transform: scale(1.1);
}

.image-badge {
  position: absolute;
  bottom: 2px;
  right: 2px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
}

.no-image {
  width: 70px;
  height: 70px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  border-radius: 8px;
  font-size: 24px;
}

.product-info {
  padding: 4px 0;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.product-desc {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price {
  font-size: 14px;
  font-weight: 600;
  color: #f56c6c;
}

.pagination-container {
  padding: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
}

:deep(.el-upload-list__item) {
  width: 100px;
  height: 100px;
}

@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
  }
  
  .filter-bar > * {
    width: 100% !important;
  }
}
</style>

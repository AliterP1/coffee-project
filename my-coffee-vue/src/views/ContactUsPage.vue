<template>
  <div class="contact-container">
    <Header />
    
    <!-- 页面标题区域 -->
    <div class="page-header">
      <h1>联系我们</h1>
      <p>有任何问题或建议？请随时联系我们</p>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="contact-content">
      <!-- 联系信息卡片 -->
      <div class="contact-info">
        <h2>联系方式</h2>
        
        <div class="info-item">
          <el-icon class="info-icon"><MapLocation /></el-icon>
          <div>
            <h3>地址</h3>
            <p>四川省成都市锦江区太古里路1号</p>
          </div>
        </div>
        
        <div class="info-item">
          <el-icon class="info-icon"><Phone /></el-icon>
          <div>
            <h3>电话</h3>
            <p>+86 123 4567 8901</p>
          </div>
        </div>
        
        <div class="info-item">
          <el-icon class="info-icon"><Message /></el-icon>
          <div>
            <h3>邮箱</h3>
            <p>contact@mycoffee.com</p>
          </div>
        </div>
        
        <div class="info-item">
          <el-icon class="info-icon"><Clock /></el-icon>
          <div>
            <h3>营业时间</h3>
            <p>周一至周五: 9:00 - 21:00</p>
            <p>周六至周日: 10:00 - 22:00</p>
          </div>
        </div>
        
        <!-- 社交媒体链接 -->
        <div class="social-links">
          <h3>关注我们</h3>
          <div class="social-icons">
            <el-link href="#" target="_blank" class="social-icon">
              <el-icon size="24"><Weibo /></el-icon>
            </el-link>
            <el-link href="#" target="_blank" class="social-icon">
              <el-icon size="24"><Wechat /></el-icon>
            </el-link>
            <el-link href="#" target="_blank" class="social-icon">
              <el-icon size="24"><Instagram /></el-icon>
            </el-link>
            <el-link href="#" target="_blank" class="social-icon">
              <el-icon size="24"><Facebook /></el-icon>
            </el-link>
          </div>
        </div>
      </div>
      
      <!-- 联系表单 -->
      <div class="contact-form">
        <h2>发送消息</h2>
        
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="80px"
          class="form-wrapper"
        >
          <el-form-item label="姓名" prop="name">
            <el-input v-model="formData.name" placeholder="请输入您的姓名" />
          </el-form-item>
          
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="formData.email" type="email" placeholder="请输入您的邮箱" />
          </el-form-item>
          
          <el-form-item label="主题" prop="subject">
            <el-input v-model="formData.subject" placeholder="请输入消息主题" />
          </el-form-item>
          
          <el-form-item label="消息" prop="message">
            <el-input
              v-model="formData.message"
              type="textarea"
              :rows="5"
              placeholder="请输入您的消息内容"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="submitForm" :loading="isSubmitting">
              {{ isSubmitting ? '发送中...' : '发送消息' }}
            </el-button>
          </el-form-item>
        </el-form>
        
        <!-- 提交成功提示 -->
        <el-dialog
          v-model="showSuccessDialog"
          title="提交成功"
          width="30%"
          :before-close="handleClose"
        >
          <p>感谢您的留言！我们将尽快回复您。</p>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="showSuccessDialog = false">确定</el-button>
            </span>
          </template>
        </el-dialog>
      </div>
    </div>
    
    <!-- 地图区域 -->
    <div class="map-section">
      <h2>我们的位置</h2>
      <div class="map-container">
        <!-- 这里使用占位图替代实际地图 -->
        <div class="map-placeholder">
          <img :src="mapImage" alt="咖啡店位置地图" class="map-image" />
          <div class="map-overlay">
            <p>点击查看实际地图</p>
          </div>
        </div>
      </div>
    </div>
    
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import {
  MapLocation, Phone, Message, Clock
} from '@element-plus/icons-vue';
import Header from '@/components/Header.vue';
import Footer from '@/components/Footer.vue';
import { useUserStore } from '@/stores/user';

// 地图图片
import mapImage from '@/assets/coffeeImage/building-66789_1920.jpg';

// 表单数据
const formData = reactive({
  name: '',
  email: '',
  subject: '',
  message: ''
});

// 用户存储
const userStore = useUserStore();

// 页面加载时初始化表单数据
onMounted(() => {
  // 如果用户已登录，自动填充邮箱
  if (userStore.userInfo?.email) {
    formData.email = userStore.userInfo.email;
    // 可以选择同时填充用户名
    if (userStore.userInfo.username) {
      formData.name = userStore.userInfo.username;
    }
  }
});

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入您的姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入您的邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  subject: [
    { required: true, message: '请输入消息主题', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  message: [
    { required: true, message: '请输入您的消息内容', trigger: 'blur' },
    { min: 10, max: 500, message: '长度在 10 到 500 个字符', trigger: 'blur' }
  ]
};

// 表单引用
const formRef = ref<any>();

// 提交状态
const isSubmitting = ref(false);

// 成功提示对话框
const showSuccessDialog = ref(false);

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    isSubmitting.value = true;
    
    // 模拟API请求
    setTimeout(() => {
      isSubmitting.value = false;
      showSuccessDialog.value = true;
      
      // 重置表单
      formRef.value.resetFields();
    }, 1500);
  } catch (error) {
    ElMessage.error('表单验证失败，请检查输入');
  }
};

// 关闭对话框时的处理
const handleClose = () => {
  showSuccessDialog.value = false;
};
</script>

<style scoped>
.contact-container {
  min-height: 100vh;
  background-color: #f8f5f2;
}

.page-header {
  text-align: center;
  padding: 40px 20px;
  height: 150px;
  background-image: url("@/assets/coffeeImage/pexels-coffee-1869647_1920.jpg");
  background-size: cover;
  background-position: center;
  color: white;
}

.page-header h1 {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
  font-weight: 600;
}

.page-header p {
  font-size: 1.2rem;
  opacity: 0.9;
}

.contact-content {
  max-width: 1200px;
  margin: 0 auto 3rem;
  /* padding: 0 1rem; */
  padding: 40px 1rem;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.contact-info,
.contact-form {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.contact-info h2,
.contact-form h2 {
  font-size: 1.8rem;
  color: #3d4246;
  margin-bottom: 1.5rem;
  font-weight: 600;
  position: relative;
  padding-bottom: 0.8rem;
}

.contact-info h2::after,
.contact-form h2::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, #a87b5e 0%, #8c5e46 100%);
}

.info-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 1.5rem;
  gap: 1rem;
}

.info-icon {
  color: #a87b5e;
  font-size: 1.5rem;
  margin-top: 0.2rem;
}

.info-item h3 {
  font-size: 1.1rem;
  color: #3d4246;
  margin-bottom: 0.3rem;
  font-weight: 500;
}

.info-item p {
  color: #6e4a2e;
  margin: 0;
  line-height: 1.6;
}

.social-links {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid #f0e6e0;
}

.social-links h3 {
  font-size: 1.1rem;
  color: #3d4246;
  margin-bottom: 1rem;
  font-weight: 500;
}

.social-icons {
  display: flex;
  gap: 1rem;
}

.social-icon {
  color: #a87b5e;
  transition: color 0.2s ease, transform 0.2s ease;
}

.social-icon:hover {
  color: #8c5e46;
  transform: scale(1.1);
}

.form-wrapper {
  margin-top: 1rem;
}

.el-form-item {
  margin-bottom: 1.5rem;
}

.el-form-item__label {
  color: #3d4246;
  font-weight: 500;
}

.el-input__inner,
.el-textarea__inner {
  border-color: #d2c5b7;
  border-radius: 6px;
  transition: border-color 0.2s ease;
}

.el-input__inner:focus,
.el-textarea__inner:focus {
  border-color: #a87b5e;
  box-shadow: 0 0 0 2px rgba(168, 123, 94, 0.1);
}

.el-button--primary {
  background: linear-gradient(135deg, #a87b5e 0%, #8c5e46 100%);
  border: none;
  border-radius: 6px;
  padding: 0.5rem 2rem;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.el-button--primary:hover {
  background: linear-gradient(135deg, #8c5e46 0%, #6e4a2e 100%);
  transform: translateY(-1px);
}

.map-section {
  max-width: 1200px;
  margin: 0 auto 3rem;
  padding: 0 1rem;
}

.map-section h2 {
  font-size: 1.8rem;
  color: #3d4246;
  margin-bottom: 1.5rem;
  font-weight: 600;
  text-align: center;
}

.map-container {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.map-placeholder {
  position: relative;
  width: 100%;
  height: 400px;
  overflow: hidden;
  border-radius: 8px;
}

.map-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.map-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
  font-weight: 500;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.map-placeholder:hover .map-image {
  transform: scale(1.03);
}

.map-placeholder:hover .map-overlay {
  opacity: 1;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .contact-content {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .page-header h1 {
    font-size: 2rem;
  }
  
  .contact-info h2,
  .contact-form h2,
  .map-section h2 {
    font-size: 1.6rem;
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: 2rem 1rem;
  }
  
  .contact-info,
  .contact-form {
    padding: 1.5rem;
  }
  
  .map-placeholder {
    height: 300px;
  }
}

@media (max-width: 480px) {
  .page-header h1 {
    font-size: 1.8rem;
  }
  
  .page-header p {
    font-size: 1rem;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .el-form-item__label {
    width: 100%;
    text-align: left;
    padding: 0 0 0.5rem 0;
  }
}
</style>
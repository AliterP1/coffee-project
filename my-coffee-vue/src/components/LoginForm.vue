<template>
  <div class="login-container">
      <div class="video-background">
      <video 
        autoplay 
        muted 
        loop 
        class="background-video"
      >
        <source src="@/assets/video/玛琪玛.mp4" type="video/mp4">
      </video>
      <div class="video-overlay"></div>
    </div>
    <div class="login-card">
      <!-- 左侧：品牌展示区域 -->
      <div class="login-brand">
        <div class="brand-content">
          <div class="brand-logo">
            <img src="//cdn.shopify.com/s/files/1/0021/1946/7095/files/dlpageicon144_300x300.png?v=1614731846" alt="ListCup" />
          </div>
          <h1>ListCup</h1>
          <p>成都精品咖啡</p>
          <div class="brand-description">
            <h3>欢迎来到我们的咖啡世界</h3>
            <p>品味生活，从一杯好咖啡开始。加入我们，探索精品咖啡的无限可能。</p>
          </div>
          <div class="brand-features">
            <div class="feature-item">
              <span class="feature-icon">☕</span>
              <span>精选咖啡豆</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🌟</span>
              <span>专业烘焙</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">💝</span>
              <span>贴心服务</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧：登录表单区域 -->
      <div class="login-form-section">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>登录您的账户继续购物</p>
        </div>
        
        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label for="email">邮箱地址</label>
            <input
              id="email"
              v-model="loginForm.email"
              type="email"
              placeholder="请输入邮箱地址"
              required
              class="form-input"
            />
          </div>
          
          <div class="form-group">
            <label for="password">密码</label>
            <input
              id="password"
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              required
              class="form-input"
            />
          </div>
          
          <div class="form-options">
            <label class="remember-me">
              <input type="checkbox" v-model="loginForm.remember" />
              记住我
            </label>
            <a href="#" class="forgot-password">忘记密码？</a>
          </div>
          
          <button type="submit" class="login-button" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>
        
        <div class="login-divider">
          <span>或者</span>
        </div>
        
        <div class="social-login">
          <button class="social-button facebook" @click="handleSocialLogin('facebook')">
            <img src="@/assets/social/facebook.png" alt="Facebook" class="social-icon" />
            使用 Facebook 登录
          </button>
        </div>
        
        <div class="register-link">
          还没有账户？ <a href="/register" @click="goToRegister">立即注册</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router' // 使用自包含 API 文件
import { useUserStore } from '@/stores/user'


const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const loginForm = reactive({
  email: '',
  password: '',
  remember: false
})

const handleLogin = async () => {
 if (!loginForm.email || !loginForm.password) return
  loading.value = true

  try {
    await userStore.login({
      email: loginForm.email,
      password: loginForm.password
    })
    // 使用后端返回的角色判断
    const role = userStore.userInfo?.role
    if (role === 'admin') {
      router.push('/admin/dashboard')
    } else {
      router.push('/')
    }
  } catch (err: any) {
    alert(err.message || '登录失败，请检查邮箱和密码')
  } finally {
    loading.value = false
  }
}

const handleSocialLogin = (provider: string) => {
  console.log(`使用 ${provider} 登录`)
  // 这里可以实现社交媒体登录逻辑
}

const goToRegister = () => {
  console.log('跳转到注册页面')
  // 这里可以实现跳转到注册页面的逻辑
   router.push('/register')
}
</script>

<style scoped>
.login-container {
  width: 100%;
  max-width: 90%;
  font-family: 'Segoe UI', 'Arial', sans-serif;
  position: relative;
  min-height: 450px;
  margin: 0 auto;
}

.video-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  overflow: hidden;
  border-radius: 12px;
}

.background-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 12px;
}

.login-card {
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  display: flex;
  min-height: 450px;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

/* 左侧品牌区域 */
.login-brand {
  flex: 1;
  color: white;
  padding: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.brand-content {
  text-align: center;
  max-width: 320px;
}

.brand-logo img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin-bottom: 1.5rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.brand-content h1 {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.brand-content > p {
  font-size: 1.2rem;
  opacity: 0.9;
  margin-bottom: 2rem;
}

.brand-description {
  margin-bottom: 2.5rem;
}

.brand-description h3 {
  font-size: 1.3rem;
  margin-bottom: 1rem;
  font-weight: 600;
}

.brand-description p {
  font-size: 1rem;
  line-height: 1.6;
  opacity: 0.85;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1rem;
}

.feature-icon {
  font-size: 1.2rem;
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.1));
}

/* 右侧表单区域 */
.login-form-section {
  flex: 1;
  padding: 2rem;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(8px);
  display: flex;
  flex-direction: column;
  justify-content: center;
  max-width: 450px;
}

.form-header {
  text-align: center;
  margin-bottom: 2.5rem;
}

.form-header h2 {
  color: #4b2e1e;
  font-size: 2rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.form-header p {
  color: #8c7b6b;
  font-size: 1rem;
}

.login-form {
  margin-bottom: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #4b2e1e;
  font-weight: 500;
  font-size: 0.9rem;
}

.form-input {
  width: 90%;
  padding: 1rem;
  border: 2px solid #e8e0d7;
  border-radius: 12px;
  font-size: 1rem;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
  background: #fafafa;
}

.form-input:focus {
  outline: none;
  border-color: #a87b5e;
  box-shadow: 0 0 0 3px rgba(168, 123, 94, 0.1);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  font-size: 0.9rem;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #6e4a2e;
  cursor: pointer;
}

.forgot-password {
  color: #a87b5e;
  text-decoration: none;
  transition: color 0.3s ease;
}

.forgot-password:hover {
  color: #8c5e46;
  text-decoration: underline;
}

.login-button {
  width: 100%;
  padding: 1rem;
  background: linear-gradient(135deg, #a87b5e 0%, #8c5e46 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(168, 123, 94, 0.3);
}

.login-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(168, 123, 94, 0.4);
}

.login-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.login-divider {
  position: relative;
  text-align: center;
  margin: 2rem 0;
}

.login-divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #e8e0d7;
}

.login-divider span {
  background: white;
  padding: 0 1rem;
  color: #8c7b6b;
  font-size: 0.9rem;
  position: relative;
}

.social-login {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 2rem;
}

.social-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 0.875rem;
  border: 2px solid #e8e0d7;
  border-radius: 12px;
  background: white;
  color: #4b2e1e;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.social-button:hover {
  border-color: #a87b5e;
  background: #fafafa;
  transform: translateY(-1px);
}

.social-icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
}

.register-link {
  text-align: center;
  color: #8c7b6b;
  font-size: 0.95rem;
}

.register-link a {
  color: #a87b5e;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
}

.register-link a:hover {
  color: #8c5e46;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-card {
    flex-direction: column;
    min-height: auto;
  }
  
  .login-brand {
    padding: 2rem;
    min-height: 300px;
  }
  
  .brand-content {
    max-width: 280px;
  }
  
  .brand-logo img {
    width: 60px;
    height: 60px;
  }
  
  .brand-content h1 {
    font-size: 2rem;
  }
  
  .login-form-section {
    padding: 2rem;
    max-width: none;
  }
}

@media (max-width: 480px) {
  .login-card {
    margin: 1rem;
    border-radius: 12px;
  }
  
  .login-brand {
    padding: 1.5rem;
    min-height: 250px;
  }
  
  .login-form-section {
    padding: 1.5rem;
  }
  
  .form-header h2 {
    font-size: 1.75rem;
  }
  
  .form-options {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
}
</style>
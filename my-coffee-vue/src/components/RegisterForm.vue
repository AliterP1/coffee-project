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
            <h3>加入我们的咖啡世界</h3>
            <p>创建账户，开启您的精品咖啡之旅。享受专属优惠和个性化推荐。</p>
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
      
      <!-- 右侧：注册表单区域 -->
      <div class="login-form-section">
        <div class="form-header">
          <h2>创建账户</h2>
          <p>加入我们，享受更多咖啡优惠</p>
        </div>
        
        <form @submit.prevent="handleRegister" class="login-form">
          <div class="form-group">
            <label for="email">邮箱地址</label>
            <input
              id="email"
              v-model="registerForm.email"
              type="email"
              placeholder="请输入邮箱地址"
              required
              class="form-input"
            />
          </div>
          
          <div class="form-group">
            <label for="username">用户名</label>
            <input
              id="username"
              v-model="registerForm.username"
              type="text"
              placeholder="请设置用户名"
              required
              class="form-input"
            />
          </div>
          
          <div class="form-group">
            <label for="password">密码</label>
            <input
              id="password"
              v-model="registerForm.password"
              type="password"
              placeholder="请设置密码（至少8位）"
              required
              minlength="8"
              class="form-input"
            />
          </div>
          
          <div class="form-group">
            <label for="confirmPassword">确认密码</label>
            <input
              id="confirmPassword"
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              required
              minlength="8"
              class="form-input"
            />
          </div>
          
          <div class="form-options">
            <label class="remember-me">
              <input type="checkbox" v-model="registerForm.agreeTerms" required />
              我已阅读并同意<a href="#" class="terms-link">服务条款</a>
            </label>
          </div>
          
          <button type="submit" class="login-button" :disabled="loading">
            {{ loading ? '注册中...' : '注册' }}
          </button>
        </form>
        
        <!-- <div class="login-divider">
          <span>或者</span>
        </div> -->
        
        <!-- <div class="social-login">
          <button class="social-button facebook" @click="handleSocialRegister('facebook')">
            <img src="@/assets/social/facebook.png" alt="Facebook" class="social-icon" />
            使用 Facebook 注册
          </button>
        </div> -->

        <div class="register-link">
          已有账户？ <a href="/login" @click="goToLogin">立即登录</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/user'

const router = useRouter()
const loading = ref(false)

const registerForm = reactive({
  email: '',
  username: '',
  password: '',
  confirmPassword: '',
  agreeTerms: false
})

const handleRegister = async () => {
  if (!registerForm.email || !registerForm.password || !registerForm.username || !registerForm.confirmPassword) {
    alert('请填写所有必填字段')
    return
  }
  
  if (registerForm.password !== registerForm.confirmPassword) {
    alert('两次输入的密码不一致，请重新输入')
    return
  }
  
  if (!registerForm.agreeTerms) {
    alert('请阅读并同意服务条款和隐私政策')
    return
  }
  
  loading.value = true

  try {
    await register({
      email: registerForm.email,
      username: registerForm.username,
      password: registerForm.password,
    })
    alert('注册成功，请登录')
    router.push('/login')
  } catch (err: any) {
    alert(err.message || '注册失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  console.log('跳转到登录页面')
  router.push('/login')
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
  min-height: 550px;
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
  align-items: flex-start;
  margin-bottom: 2rem;
  font-size: 0.9rem;
}

.remember-me {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.5rem;
  color: #6e4a2e;
  cursor: pointer;
}

.terms-link {
  color: #a87b5e;
  text-decoration: none;
}

.terms-link:hover {
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
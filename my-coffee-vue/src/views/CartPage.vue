<template>
  <div class="cart-page">
    <!-- 头部导航 -->
    <Header />

    <!-- 购物车内容区域 -->
    <main class="cart-main">
      <!-- 页面标题 -->
      <div class="cart-page-header">
        <div class="title-container">
          <h1>我的购物车</h1>
          <p class="subtitle">轻松管理您的选购商品</p>
        </div>
      </div>

      <!-- 购物车列表组件 -->
      <CartList />
    </main>

    <!-- 页脚 -->
    <Footer />
  </div>
</template>

<script setup lang="ts">
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'
import CartList from '@/components/CartList.vue'
</script>

<style scoped>
.cart-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f5f2;
}

.cart-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.cart-page-header {
  padding: 40px 20px;
  height: 180px;
  background-image: url("@/assets/coffeeImage/coffee-2560260_1920.jpg");
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.cart-page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(75, 46, 30, 0.7);
}

.title-container {
  text-align: center;
  color: white;
  position: relative;
  z-index: 1;
}

.title-container h1 {
  font-size: 42px;
  margin: 0 0 12px 0;
  font-weight: 700;
  letter-spacing: 1px;
}

.title-container .subtitle {
  font-size: 18px;
  margin: 0;
  opacity: 0.9;
  font-weight: 300;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .cart-page-header {
    padding: 30px 16px;
    height: 150px;
  }
  
  .title-container h1 {
    font-size: 32px;
  }
  
  .title-container .subtitle {
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  .cart-page-header {
    padding: 20px 12px;
    height: 120px;
  }
  
  .title-container h1 {
    font-size: 28px;
  }
  
  .title-container .subtitle {
    font-size: 14px;
  }
}
</style>
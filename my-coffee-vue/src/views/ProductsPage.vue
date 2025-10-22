<template>
  <div class="products-page">
    <Header />
    
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>咖啡商品</h1>
      <p>精选优质咖啡产品，满足您的味蕾享受</p>
    </div>
    
    <!-- 主要内容区域 -->
    <main class="main-content">
      <ProductsList />
    </main>
    
    <Footer />
  </div>
</template>

<script setup lang="ts">
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'
import ProductsList from '@/components/ProductsList.vue'
</script>

<style scoped>
.products-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f8f9fa;
}

.page-header {
  text-align: center;
  padding: 40px 20px;
  height: 150px;
  background-image: url("@/assets/coffeeImage/coffee-2560260_1920.jpg");
  background-size: cover;
  background-position: center;
  color: white;
}

.page-header h1 {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 12px 0;
}

.page-header p {
  font-size: 18px;
  opacity: 0.9;
  margin: 0;
}

.main-content {
  flex: 1;
  padding: 0 20px 40px 20px;
}
</style>
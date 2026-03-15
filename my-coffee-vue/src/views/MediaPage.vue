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
    
    <!-- 主要内容区域 -->
    <main class="main-content">
      <MediaList />
    </main>
    
    <Footer />
  </div>
</template>

<script setup lang="ts">
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'
import MediaList from '@/components/MediaList.vue'
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
  background-image: url("@/assets/coffeeImage/2857440-coffee-7870461_1920.jpg");
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

.main-content {
  flex: 1;
  padding: 0 20px 40px 20px;
  background-color: #faf7f4;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}
</style>
<template>
    <section class="feature-section" :class="{ reverse }">
      <div class="text">
        <h2>{{ title }}</h2>
        <p>{{ description }}</p>
      </div>
      <div class="image">
        <img :src="image" :alt="title" />
      </div>
    </section>
  </template>
  
  <script>
  export default {
    name: "FeatureSection",
    props: {
      title: String,
      description: String,
      image: String,
      reverse: Boolean,
    },
    // 说明：FeatureSection是一个静态展示组件，本身不包含轮播功能
    // 项目中已有专门的轮播组件：HeroCarousel.vue，它使用Swiper库实现了轮播效果
    // 
    // 如果需要让FeatureSection支持轮播，有两种方案：
    // 1. 直接使用现有的HeroCarousel组件
    // 2. 修改FeatureSection组件，添加类似HeroCarousel的轮播实现代码
  };
  </script>
  
  <style scoped>
  .feature-section {
    display: flex;
    align-items: center;
    padding: 3rem 2rem;
    gap: 2rem;
  }
  .feature-section.reverse {
    flex-direction: row-reverse;
  }
  .text {
    flex: 1;
  }
  .image {
    flex: 1;
  }
  .image img {
    width: 100%;
    border-radius: 12px;
  }
  @media (max-width: 768px) {
    .feature-section {
      flex-direction: column;
    }
  }
  </style>
  
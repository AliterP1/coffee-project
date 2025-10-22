<template>
    <div>
      <Header />
      <HeroCarousel />
  
      <!-- 精选咖啡地铺 -->
      <FeatureSection
        title="精选咖啡地铺"
        description="探寻城市角落里的咖啡香，每一家店都有自己的故事和独特的咖啡文化。"
        :image="cafeImage"
      />
  
      <!-- 专业咖啡师 -->
      <FeatureSection
        reverse
        title="专业咖啡师"
        description="经验丰富的咖啡师用心制作每一杯咖啡，从豆子的选择到萃取的温度，每个细节都精益求精。"
        :image="baristaImage"
      />

      <!-- 醇香咖啡豆 -->
      <FeatureSection
        title="醇香咖啡豆"
        description="来自世界各地的优质咖啡豆，经过精心烘焙，释放出独特的香气和醇厚的口感。"
        :image="beansImage"
      />

      <!-- 温馨咖啡店 -->
      <FeatureSection
        reverse
        title="温馨咖啡店"
        description="在温馨的咖啡店里，享受悠闲时光，品味生活的美好，让每一杯咖啡都成为难忘的回忆。"
        :image="buildingImage"
      />
      <Footer />
    </div>
  </template>
  
  <script lang="ts">
  import Header from "@/components/Header.vue";
  import HeroCarousel from "@/components/HeroCarousel.vue";
  import FeatureSection from "@/components/FeatureSection.vue";
  import Footer from "@/components/Footer.vue";
  import { defineComponent } from "vue";

  // 导入图片
  import cafeImage from "@/assets/coffeeImage/cafe-3537801_1920.jpg";
  import baristaImage from "@/assets/coffeeImage/barista-6197867_1920.jpg";
  import beansImage from "@/assets/coffeeImage/coffee-beans-5712780_1920.jpg";
  import buildingImage from "@/assets/coffeeImage/building-66789_1920.jpg";

  export default defineComponent({
    name: "HomePage",
    components: {
      Header,
      HeroCarousel,
      FeatureSection,
      Footer,
    },
    setup() {
      return {
        cafeImage,
        baristaImage,
        beansImage,
        buildingImage,
      };
    },
  });
  </script>
  
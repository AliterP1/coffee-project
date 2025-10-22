<template>
    <div class="hero-carousel">
      <swiper
        :slides-per-view="1"
        :loop="true"
        :autoplay="{ delay: 3000 }"
        navigation
        pagination
        class="my-swiper"
      >
        <swiper-slide v-for="(s, i) in slides" :key="i">
          <div class="slide" :style="{ backgroundImage: `url(${s.img})` }">
            <div class="caption">{{ s.caption }}</div>
          </div>
        </swiper-slide>
      </swiper>
    </div>
  </template>
  
  <script>
  import { Swiper, SwiperSlide } from "swiper/vue";
  import "swiper/css";
  import "swiper/css/navigation";
  import "swiper/css/pagination";
  
  export default {
    name: "HeroCarousel",
    components: { Swiper, SwiperSlide },
    data() {  
      return {
        slides: [
          { img: new URL("@/assets/coffeeImage/coffee-1576256_1920.jpg", import.meta.url).href, caption: "探索特色Coffee" },
          { img: new URL("@/assets/coffeeImage/coffee-2714970_1920.jpg", import.meta.url).href, caption: "加入本地Cafés" },
          { img: new URL("@/assets/coffeeImage/coffee-4618705_1920.jpg", import.meta.url).href, caption: "Enjoy the Cup Together" },
        ],
      };
    },
  };
  </script>
  
  <style scoped>
  .hero-carousel {
    width: 100%;
    height: 60vh;
  }
  .slide {
    width: 100%;
    height: 60vh;
    background-size: cover;
    background-position: center;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .caption {
    background: rgba(0,0,0,0.5);
    color: #fff;
    font-size: 1.8rem;
    padding: 0.5rem 1rem;
    border-radius: 8px;
  }
  </style>
  
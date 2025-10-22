<template>
  <header class="site-header">
    <!-- 左侧 Logo -->
    <div class="logo">
      <img src="//cdn.shopify.com/s/files/1/0021/1946/7095/files/dlpageicon144_300x300.png?v=1614731846" alt="ListCup" class="logo-img" />
      <span>成都精品咖啡</span>
    </div>

    <!-- 中间导航 -->
    <nav class="site-nav">
      <ul class="site-nav__list">
        <li class="site-nav__item" :class="{ 'site-nav--active': isActive('/') }">
          <a class="site-nav__link" href="/">首页</a>
        </li>
        <li class="site-nav__item" :class="{ 'site-nav--active': isActive('/products') }">
          <a class="site-nav__link" href="/products">商品</a>
        </li>
        <li class="site-nav__item" :class="{ 'site-nav--active': isActive('/blogs/media') }">
          <a class="site-nav__link" href="/blogs/media">新闻 & 媒体</a>
        </li>
        <li class="site-nav__item" :class="{ 'site-nav--active': isActive('https://solcommittee.com/') }">
          <a class="site-nav__link" href="https://solcommittee.com/" target="_blank" rel="noopener">SOL 委员会</a>
        </li>
        <li class="site-nav__item" :class="{ 'site-nav--active': isActive('/contact') }">
          <a class="site-nav__link" href="/contact">联系我们</a>
        </li>
      </ul>
    </nav>

    <!-- 右侧图标区域 -->
    <div class="header-icons">
      <!-- 搜索：图标不动，搜索框从图标右侧向左弹出 -->
      <div 
        class="search-wrapper" 
        :class="{ 'search-wrapper--open': searchOpen }"
        ref="searchWrapperRef"
      >
        <el-button
          class="search-icon-btn"
          type="text"
          @click="openSearch"
        >
          <el-icon size="24" style="color: #000"><Search /></el-icon>
        </el-button>
        <el-input
          v-model="searchQuery"
          placeholder="搜索"
          size="small"
          class="search-input"
          @keyup.enter="onSearch"
          clearable
          @blur="searchOpen = false"
          :class="{ 'search-input--active': searchOpen }"
        >
       
        </el-input>
      </div>

      <!-- 登录 -->
      <el-button type="text" class="icon-btn" @click="onLogin">
        <el-icon size="24" style="color: #000;"><User /></el-icon>
      </el-button>

      <!-- 购物车 -->
      <el-button type="text" class="icon-btn" @click="onCart">
        <el-icon size="24" style="color: #000;"><ShoppingCart /></el-icon>
      </el-button>
    </div>
  </header>
</template>

<script lang="ts">
import { defineComponent, ref, nextTick, onMounted, onUnmounted } from "vue";
import { useRoute } from "vue-router";
import { Search, User, ShoppingCart } from "@element-plus/icons-vue";
import { useUserStore } from '@/stores/user';

export default defineComponent({
  name: "Header",
  components: { Search, User, ShoppingCart },
  setup() {
    const route = useRoute();

    /** 判断链接是否激活 */
    const isActive = (path: string) => {
      if (path === "/") return route.path === "/";
      return route.path.startsWith(path);
    };

    const searchQuery = ref("");
    const searchOpen = ref(false);
    let searchWrapperRef = ref<HTMLElement | null>(null);

    // 点击外部区域关闭搜索框
    const handleClickOutside = (event: MouseEvent) => {
      if (searchOpen.value && searchWrapperRef.value) {
        const target = event.target as HTMLElement;
        if (!searchWrapperRef.value.contains(target)) {
          searchOpen.value = false;
        }
      }
    };

    const openSearch = () => {
      searchOpen.value = true;
      // 自动聚焦到输入框
      nextTick(() => {
        const input = document.querySelector(".search-input--active input") as HTMLInputElement;
        input?.focus();
      });
    };

    onMounted(() => {
      document.addEventListener('click', handleClickOutside);
    });

    onUnmounted(() => {
      document.removeEventListener('click', handleClickOutside);
    });

    const onSearch = () => {
      if (searchQuery.value.trim()) {
        window.location.href = `/search?q=${encodeURIComponent(searchQuery.value)}`;
      }
    };

    const onLogin = () => {
      const userStore = useUserStore();
      if (userStore.isLogin) {
        window.location.href = "/account";
      } else {
        window.location.href = "/login";
      }
    };

    const onCart = () => {
      window.location.href = "/cart";
    };

    return {
      isActive,
      searchQuery,
      searchOpen,
      searchWrapperRef,
      openSearch,
      onSearch,
      onLogin,
      onCart,
    };
  },
});
</script>

<style scoped>
.site-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.8rem 2rem;
  background-color: #fff;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: bold;
  font-size: 1.2rem;
}

.logo-img {
  width: 40px;
  height: 40px;
}

/* Dawn 风格导航 */
.site-nav__list {
  display: flex;
  gap: 1.5rem;
  list-style: none;
  margin: 0;
  padding: 0;
}

.site-nav__link {
  color: #333;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s ease, text-decoration 0.2s ease;
  text-underline-offset: 4px; /* 文字与下划线间距 */
}

.site-nav__link:hover {
  text-decoration: underline;
}

.site-nav--active .site-nav__link,
.site-nav__link.site-nav__link--active {
  text-decoration: underline;
}

.header-icons {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.search-input {
  width: 200px;
  height: 40px;
}

/* 搜索框动画 - 图标不动，搜索框从图标右侧向左弹出 */
.search-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon-btn {
  padding: 5px 20px;
  position: relative;
  color: #000;
  background: transparent;
  border: none;
  cursor: pointer;
}

.search-input {
  position: absolute;
  right: 0;
  width: 200px;
  opacity: 0;
  transform: translateX(20px); /* 初始位置在右侧 */
  transition: all 1s ease;
  pointer-events: none;
  z-index: 5;
}

.search-input--active {
  opacity: 1;
  transform: translateX(0); /* 向左滑动到最终位置 */
  pointer-events: auto;
  --el-input-focus-border-color: #000 !important;
}

/* 输入框占位区域，保持图标位置 */
.search-wrapper::before {
  content: '';
  width: 32px;
  height: 32px;
  display: block;
  flex-shrink: 0;
}
.search-trigger {
  color: #a87b5e;
  font-size: 18px;
  padding: 6px;
  border-radius: 50%;
  transition: background 0.2s ease, color 0.2s ease;
}
.search-trigger:hover {
  background: rgba(168, 123, 94, 0.1);
}

/* 图标按钮统一风格 */
.icon-btn {
  padding: 5px 5px;
  border-radius: 50%;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .site-header {
    padding: 0.8rem 1rem;
    flex-wrap: wrap;
    gap: 1rem;
  }
  
  .logo {
    font-size: 1rem;
  }
  
  .logo-img {
    width: 32px;
    height: 32px;
  }
  
  .site-nav {
    order: 3;
    width: 100%;
  }
  
  .site-nav__list {
    gap: 1rem;
    justify-content: center;
    font-size: 12px;
  }
  
  .site-nav__link {
    font-size: 12px;
  }
  
  .header-icons {
    gap: 0.5rem;
  }
  
  .search-input {
    width: 150px;
  }
  
  .search-wrapper {
    position: static;
  }
  
  .search-input {
    position: static;
    width: 120px;
    opacity: 1;
    transform: none;
    pointer-events: auto;
    margin-left: 0.5rem;
  }
  
  .search-icon-btn {
    display: none;
  }
  
  .search-wrapper::before {
    display: none;
  }
}

@media (max-width: 480px) {
  .site-header {
    padding: 0.6rem 0.8rem;
  }
  
  .site-nav__list {
    gap: 0.8rem;
  }
  
  .site-nav__link {
    font-size: 11px;
  }
  
  .search-input {
    width: 100px;
  }
  
  .header-icons {
    gap: 0.3rem;
  }
}
</style>

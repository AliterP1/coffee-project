<template>
  <footer class="site-footer">
    <!-- 上半部分：链接 + 邮件订阅 -->
    <div class="footer-top">
      <!-- 快速链接 -->
      <nav class="grid__item medium-up--one-half small--hide" id="AccessibleNav" role="navigation">
        <h4>快速链接</h4>
        <ul class="site-nav list--inline" id="SiteNav">
          <li :class="{ 'site-nav--active': isActive('/') }">
            <a href="/" class="site-nav__link site-nav__link--main" :class="{ 'site-nav__link--active': isActive('/') }">
              <span class="site-nav__label">首页</span>
            </a>
          </li>
          <li :class="{ 'site-nav--active': isActive('/products') }">
            <a href="/products" class="site-nav__link site-nav__link--main" :class="{ 'site-nav__link--active': isActive('/products') }">
              <span class="site-nav__label">商品</span>
            </a>
          </li>
          <li :class="{ 'site-nav--active': isActive('/blogs/media') }">
            <a href="/blogs/media" class="site-nav__link site-nav__link--main" :class="{ 'site-nav__link--active': isActive('/blogs/media') }">
              <span class="site-nav__label">新闻 &amp; 媒体</span>
            </a>
          </li>
          <li>
            <a href="https://solcommittee.com/" class="site-nav__link site-nav__link--main" target="_blank" rel="noopener" aria-describedby="a11y-external-message">
              <span class="site-nav__label">SOL 委员会</span>
            </a>
          </li>
          <li :class="{ 'site-nav--active': isActive('/contact') }">
            <a href="/contact" class="site-nav__link site-nav__link--main" :class="{ 'site-nav__link--active': isActive('/contact') }">
              <span class="site-nav__label">联系我们</span>
            </a>
          </li>
        </ul>
      </nav>

      <!-- 邮件订阅 -->
      <div class="footer-newsletter">
        <h4 class="newsletter-title">订阅我们的新闻邮件</h4>
        <div class="newsletter-form-wrapper">
          <form
            method="post"
            action="/contact#ContactFooter"
            id="ContactFooter"
            class="contact-form"
            @submit.prevent="subscribe"
            novalidate
          >
            <input type="hidden" name="form_type" value="customer" />
            <input type="hidden" name="utf8" value="✓" />
            <input type="hidden" name="contact[tags]" value="newsletter" />

            <div class="input-group">
              <input
                type="email"
                name="contact[email]"
                id="ContactFooter-email"
                class="input-group__field newsletter__input"
                v-model="email"
                placeholder="Email address"
                aria-label="Email address"
                aria-required="true"
                required
                autocorrect="off"
                autocapitalize="off"
              />
              <span class="input-group__btn">
                <button
                  type="submit"
                  class="btn newsletter__submit"
                  name="commit"
                  id="Subscribe"
                >
                  <span class="newsletter__submit-text--large">订阅</span>
                </button>
              </span>
            </div>

            <span
              v-if="errorMsg"
              id="ContactFooter-email-error"
              class="input-error-message"
            >
              <svg
                aria-hidden="true"
                focusable="false"
                role="presentation"
                class="icon icon-error"
                viewBox="0 0 14 14"
              >
                <path
                  d="M7 14A7 7 0 1 0 7 0a7 7 0 0 0 0 14zm-1.05-3.85A1.05 1.05 0 1 1 7 11.2a1.008 1.008 0 0 1-1.05-1.05zm.381-1.981l-.266-5.25h1.841l-.255 5.25h-1.32z"
                />
              </svg>
              {{ errorMsg }}
            </span>
          </form>
        </div>
      </div>
    </div>

    <hr class="footer-divider" />

    <!-- 下半部分：社交图标 + 版权信息 -->
    <div class="footer-bottom">
      <div class="social-icons">
        <el-link href="https://facebook.com/listcup" target="_blank" class="social-link">
          <img :src="facebookIcon" alt="Facebook" class="social-icon" />
          
        </el-link>
        <el-link href="https://instagram.com/listcup" target="_blank" class="social-link">
          <img :src="instagramIcon" alt="Instagram" class="social-icon" />
          
        </el-link>
        <el-link href="https://twitter.com/listcup" target="_blank" class="social-link">
          <img :src="twitterIcon" alt="Twitter" class="social-icon" />
        </el-link>
      </div>

      <div class="copy">
        © 2025, <a href="/">成都精品咖啡</a> Powered by Shopify
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRoute } from "vue-router";
import { ElLink } from "element-plus";
import facebookIcon from "@/assets/social/facebook.png";
import instagramIcon from "@/assets/social/instagram.png";
import twitterIcon from "@/assets/social/twitter.png";


const route = useRoute();

/** 判断链接是否激活 */
const isActive = (path: string) => {
  // 完全匹配或根路径特殊处理
  if (path === "/") return route.path === "/";
  return route.path.startsWith(path);
};

const email = ref("");
const errorMsg = ref<string>("");

const subscribe = () => {
  const val = email.value.trim();
  if (!val) {
    errorMsg.value = "Email address is required.";
    return;
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val)) {
    errorMsg.value = "Please enter a valid email address.";
    return;
  }
  // 模拟提交成功
  alert(`订阅成功: ${val}`);
  errorMsg.value = "";
  email.value = "";
};
</script>

<style scoped>
.site-footer {
  background: linear-gradient(135deg, #f8f5f2 0%, #e8e0d7 100%);
  padding: 4rem 1rem 2rem;
  font-family: 'Segoe UI', 'Arial', sans-serif;
  font-size: 15px;
  color: #4b2e1e;
  border-top: 1px solid #e0d7ce;
  box-shadow: inset 0 4px 8px rgba(0,0,0,0.02);
}

.footer-top {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 3rem;
  margin-bottom: 2.5rem;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
}

/* ---------- Shopify Dawn 导航样式 ---------- */
#AccessibleNav h4 {
  font-family: "Work Sans", sans-serif;
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: #3d4246;
}

.site-nav.list--inline {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 1.25rem;
}

.site-nav__link {
  text-decoration: none;
  font-family: "Work Sans", sans-serif;
  font-size: 14px;
  font-weight: 400;
  color: #3d4246;
  line-height: 1.5;
  transition: color 0.2s ease, text-decoration 0.2s ease;
  text-underline-offset: 4px;
}

.site-nav__link:hover {
  text-decoration: underline;
}

.site-nav__link--active,
.site-nav--active .site-nav__link {
  font-weight: 500;
  text-decoration: underline;
  text-underline-offset: 4px;
}

/* 小屏幕垂直排列 */
@media (max-width: 767px) {
  .site-nav.list--inline {
    flex-direction: column;
    gap: 0.75rem;
  }
}

.newsletter-input {
  width: 360px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.newsletter-input :deep(.el-input__inner) {
  border-radius: 10px 0 0 10px;
  border-right: none;
  height: 48px;
  font-size: 1rem;
}

.newsletter-input :deep(.el-input-group__append) {
  border-radius: 0 10px 10px 0;
  background: linear-gradient(135deg, #a87b5e 0%, #8c5e46 100%);
  color: #fff;
  border: none;
  height: 48px;
  font-size: 1rem;
}

.newsletter-input :deep(.el-input-group__append .el-button) {
  background: transparent;
  color: #fff;
  border: none;
}

.newsletter-input :deep(.el-input-group__append .el-button:hover) {
  background: rgba(255,255,255,0.1);
}

.footer-divider {
  border: none;
  border-top: 1px solid #d8c8b9;
  margin: 2.5rem auto;
  max-width: 1200px;
}

.footer-bottom {
  text-align: center;
  max-width: 1200px;
  margin: 0 auto;
}

.social-icons {
  display: flex;
  justify-content: center;
  gap: 2rem;
  margin-bottom: 1.5rem;
  
}

.social-icons :deep(.el-link) {
  color: #6e4a2e;
  font-size: 1rem;
  --el-link-hover-text-color: inherit !important; /* 取消 hover 时的主色 */
  --el-link-text-color: inherit !important;       /* 正常状态颜色 */
  --el-link-active-text-color: inherit !important;
  text-decoration: none !important;
  transition: color 0.3s ease, transform 0.2s ease;
}

.social-icons :deep(.el-link:hover) {
  color: #a87b5e;
  transform: translateY(-1px);
}

/* 社交媒体图标样式 */
.social-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}


.social-icon {
  width: 35px;
  height: 35px;
  object-fit: contain;
  transition: transform 0.3s ease;
}

.social-link:hover .social-icon {
  transform: scale(1.1);
}

/* ---------- Shopify Dawn Newsletter Style ---------- */
.newsletter-title {
  font-family: "Work Sans", sans-serif;
  font-size: 1.125rem;
  font-weight: 600;
  letter-spacing: 0.02em;
  margin-bottom: 1rem;
  color: #3d4246;
}

.newsletter-form-wrapper {
  max-width: 420px;
}

.contact-form .input-group {
  display: flex;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.newsletter__input {
  flex: 1 1 auto;
  height: 48px;
  padding: 0 1rem;
  border: 1px solid #d2c5b7;
  border-right: none;
  font-size: 1rem;
  font-family: inherit;
  color: #3d4246;
  background: #fff;
  outline: none;
  transition: border-color 0.2s;
}

.newsletter__input:focus {
  border-color: #a87b5e;
}

.newsletter__submit {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 48px;
  padding: 0 1.5rem;
  border: none;
  background: linear-gradient(135deg, #a87b5e 0%, #8c5e46 100%);
  color: #fff;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.3s ease;
}

.newsletter__submit:hover {
  background: linear-gradient(135deg, #8c5e46 0%, #6e4a2e 100%);
}

.newsletter__submit-text--large {
  letter-spacing: 0.02em;
}

.input-error-message {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: #c0392b;
}

.icon-error {
  width: 14px;
  height: 14px;
  fill: currentColor;
}

.copy {
  color: #8c6e58;
  font-size: 0.9rem;
}

.copy a {
  color: #6e4a2e;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.copy a:hover {
  color: #a87b5e;
}

@media (max-width: 768px) {
  .footer-top {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 2rem;
  }

  .footer-links .quick-links {
    align-items: center;
  }

  .newsletter-input {
    width: 100%;
    max-width: 300px;
  }
}
</style>

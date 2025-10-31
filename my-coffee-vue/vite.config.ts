import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import path from "path";

export default defineConfig({
  server: {
    allowedHosts: [
      'www.yuanshen.store',
      'yuanshen.store',
      // 添加允许的主机名
      // 如果你还需要通过其他域名访问，也可以一并添加
    ],
    // 配置代理以解决CORS跨域问题
    proxy: {
      '/api': {
        target: 'http://8.130.85.165:8081',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
  plugins: [vue()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "src"),
    }
  }
});
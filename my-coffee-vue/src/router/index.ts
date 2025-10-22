import { createRouter, createWebHistory } from 'vue-router'

// 路由规则
const router= createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes:[
    {
    path: "/",
    name: "Home",
    component: () => import('@/views/HomePage.vue')
    },
    {
    path: "/login",
    name: "Login",
    component: () => import('@/views/LoginPage.vue')
    },
    {
    path: "/register",
    name: "Register",
    component: () => import('@/views/RegisterPage.vue')
    },
    {
    path: "/products",
    name: "Products",
    component: () => import('@/views/ProductsPage.vue')
    },
    {
    path: "/cart",
    name: "Cart",
    component: () => import('@/views/CartPage.vue')
    },
    {
    path: "/account",
    name: "Account",
    component: () => import('@/views/AccountPage.vue')
    },
    {
    path: "/orders",
    name: "MyOrders",
    component: () => import('@/views/MyOrdersPage.vue')
    },
    {
    path: "/order/detail/:id",
    name: "OrderDetail",
    component: () => import('@/views/OrderDetailPage.vue')
    },
    {
    path: "/contact",
    name: "ContactUs",
    component: () => import('@/views/ContactUsPage.vue')
    },
    {
      path: "/blogs/media",
      name: "Blogs",
      component: () => import('@/views/MediaPage.vue')
    },
    {
      path: "/product/detail/:productId",
      name: "ProductDetail",
      component: () => import('@/views/ProductDetail.vue')
    },
    {
      path: "/content/detail/:contentId",
      name: "ContentDetail",
      component: () => import('@/views/ContentDetail.vue')
    },
    {
      path: "/search",
      name: "Search",
      component: () => import('@/views/SearchPage.vue')
    }
  ]
})

export default router;

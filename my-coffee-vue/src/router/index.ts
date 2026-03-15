import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

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
    path: "/payment/:id",
    name: "Payment",
    component: () => import('@/views/PaymentPage.vue')
    },
    {
    path: "/account",
    name: "Account",
    component: () => import('@/views/AccountPage.vue')
    },
    {
    path: "/profile",
    name: "Profile",
    component: () => import('@/views/ProfilePage.vue')
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
    },
    {
      path: "/admin",
      name: "Admin",
      component: () => import('@/views/admin/AdminLayout.vue'),
      children: [
        {
          path: "dashboard",
          name: "AdminDashboard",
          component: () => import('@/views/admin/Dashboard.vue')
        },
        {
          path: "users",
          name: "AdminUsers",
          component: () => import('@/views/admin/UserManagement.vue')
        },
        {
          path: "products",
          name: "AdminProducts",
          component: () => import('@/views/admin/ProductManagement.vue')
        },
        {
          path: "orders",
          name: "AdminOrders",
          component: () => import('@/views/admin/OrderManagement.vue')
        }
      ]
    },
    {
      path: "/merchant",
      name: "Merchant",
      component: () => import('@/views/merchant/MerchantLayout.vue'),
      children: [
        {
          path: "dashboard",
          name: "MerchantDashboard",
          component: () => import('@/views/merchant/MerchantDashboard.vue')
        },
        {
          path: "products",
          name: "MerchantProducts",
          component: () => import('@/views/merchant/MerchantProducts.vue')
        },
        {
          path: "orders",
          name: "MerchantOrders",
          component: () => import('@/views/merchant/MerchantOrders.vue')
        },
        {
          path: "reviews",
          name: "MerchantReviews",
          component: () => import('@/views/merchant/MerchantReviews.vue')
        },
        {
          path: "settings",
          name: "MerchantSettings",
          component: () => import('@/views/merchant/MerchantSettings.vue')
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, _, next) => {
  const userStore = useUserStore()
  
  // 需要管理员权限的路由
  const adminRoutes = ['Admin', 'AdminDashboard', 'AdminUsers', 'AdminProducts', 'AdminOrders']
  
  // 需要商家权限的路由
  const merchantRoutes = ['Merchant', 'MerchantDashboard', 'MerchantProducts', 'MerchantOrders', 'MerchantReviews', 'MerchantSettings']
  
  if (adminRoutes.includes(to.name as string)) {
    // 检查是否登录且是管理员
    if (!userStore.token || userStore.userInfo?.role !== 'admin') {
      next('/login')
      return
    }
  }
  
  if (merchantRoutes.includes(to.name as string)) {
    // 检查是否登录且是商家
    if (!userStore.token || userStore.userInfo?.role !== 'merchant') {
      next('/login')
      return
    }
  }
  
  // 已登录用户访问登录页，跳转到首页
  if (to.name === 'Login' && userStore.token) {
    next('/')
    return
  }
  
  next()
})

export default router;

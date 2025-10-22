import axios from 'axios'
import router from '@/router'
import { useUserStore } from '@/stores/user'


// 根据环境变量设置不同的 baseURL
const baseURL =
  '/api' // 使用代理地址，解决CORS跨域问题

// 创建 axios 实例
const service = axios.create({
  baseURL: baseURL, // 后端接口地址
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export default service
import { defineStore } from 'pinia'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo, removeUserInfo } from '@/utils/auth'
import { login as apiLogin, getUserById } from '@/api/user'
import type { UserResponseDTO } from '@/api/user'

interface UserInfo extends UserResponseDTO {
  token?: string
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: getUserInfo() || null as UserInfo | null,
  }),
  getters: {
    isLogin: (state) => !!state.token,
  },
  actions: {
    // 从token中解析用户信息（简化版，实际项目可能需要更复杂的JWT解析）
    parseToken(token: string) {
      try {
        // JWT通常由三部分组成，用.分隔
        const tokenParts = token.split('.');
        if (tokenParts.length >= 2) {
          // 第二部分是payload，包含用户信息
          const payload = tokenParts[1] ? JSON.parse(atob(tokenParts[1])) : null;
          return payload;
        }
      } catch (error) {
        console.error('解析token失败:', error);
      }
      return null;
    },
    
    // 初始化用户信息（页面刷新后自动调用）
    async initUserInfo() {
      if (this.token && !this.userInfo) {
        try {
          // 从token中解析用户信息
          const tokenInfo = this.parseToken(this.token);
          let userId = '1'; // 默认值
          
          // 尝试从token信息中获取用户ID
          if (tokenInfo) {
            userId = tokenInfo.userId || tokenInfo.sub || tokenInfo.id || '1';
          }
          
          const res = await getUserById(userId.toString());
          
          if (res.data && res.data.code === 200 && res.data.data) {
            this.userInfo = res.data.data;
          }
        } catch (error) {
          console.error('初始化用户信息失败:', error);
          // 如果获取失败，可以选择退出登录
          // this.logout();
        }
      }
    },
    async login(data: { email: string; password: string }) {
      try {
        const res = await apiLogin({
          email: data.email,
          password: data.password
        })
        

        const response = res.data
        if (!response || response.code !== 200 || !response.data) {
          // 登录失败，抛出错误
          throw new Error(response?.message || '登录失败，请检查邮箱和密码')
        }

        const userData = response.data
        this.userInfo = userData
        this.token = userData?.token || ''

        if (userData?.token) {
          setToken(userData.token)
          setUserInfo(userData) // 保存用户信息到本地存储
        }

        return true
      } catch (err: any) {
        console.error('登录失败:', err.message)
        throw err // 让组件去 catch
      }
    },


    async logout() {
      this.token = ''
      this.userInfo = null
      removeToken()
      removeUserInfo() // 移除本地存储的用户信息
    },
  },
})

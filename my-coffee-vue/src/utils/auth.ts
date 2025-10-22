
const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'userInfo'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

// 保存用户信息到本地存储
export function setUserInfo(userInfo: any): void {
  try {
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
  } catch (error) {
    console.error('保存用户信息失败:', error)
  }
}

// 从本地存储获取用户信息
export function getUserInfo(): any | null {
  try {
    const userInfoStr = localStorage.getItem(USER_INFO_KEY)
    return userInfoStr ? JSON.parse(userInfoStr) : null
  } catch (error) {
    console.error('获取用户信息失败:', error)
    return null
  }
}

// 移除本地存储的用户信息
export function removeUserInfo(): void {
  localStorage.removeItem(USER_INFO_KEY)
}

import request from '@/utils/request'

// 通用API响应包装类型
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface UserRequestDTO {
  username?: string
  password: string
  email: string
  phone?: string
  avatarUrl?: string
}

export interface UserResponseDTO {
  id: number
  username: string
  email?: string
  phone?: string
  avatarUrl?: string
  role: string
  createdAt: string
  status: 'active' | 'frozen' | 'deleted'
  token?: string
}

// 登录
export const login = (data: UserRequestDTO) =>
  request.post<ApiResponse<UserResponseDTO>>('/user/login', data)

// 注册
export const register = (data: UserRequestDTO) =>
  request.post<ApiResponse<UserResponseDTO>>('/user/register', data)

// 获取用户信息
export const getUserById = (id: string) =>
  request.get<ApiResponse<UserResponseDTO>>(`/user/${id}`)

// 获取用户分页
export const getUserPage = (page = 1, size = 10) =>
  request.get<ApiResponse<any>>(`/user`, { params: { page, size } })

// 修改密码
export const changePassword = (userId: number, oldPassword: string, newPassword: string) =>
  request.post<ApiResponse<null>>(`/user/change-password`, null, { params: { userId, oldPassword, newPassword } })

// 修改用户状态（admin 权限）
export const updateStatus = (userId: number, status: 'active' | 'frozen' | 'deleted') =>
  request.put<ApiResponse<null>>(`/user/status`, null, { params: { userId, status } })

// 上传头像
export const updateAvatar = (id: number, file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<ApiResponse<string>>(`/user/${id}/avatar`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

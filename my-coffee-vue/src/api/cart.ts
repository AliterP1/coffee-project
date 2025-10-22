import request from '@/utils/request'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface CartProduct {
  productId: number
  quantity: number
  price: number
  productName: string
  productImages: string[]
}

export interface CartData {
  id: number
  userId: number
  totalPrice: number
  status: string
  items: CartProduct[]
}

// 获取购物车
export const getUserCart = (userId: number) =>
  request.get<ApiResponse<CartData>>('/cart', { params: { userId } })

// 添加商品到购物车
export const addToCart = (userId: number, productId: number, quantity: number) =>
  request.post<ApiResponse<String>>('/cart/add', null, { params: { userId, productId, quantity } })

// 更新商品数量
export const updateCartItem = (userId: number, productId: number, quantity: number) =>
  request.put<ApiResponse<String>>('/cart/update', null, { params: { userId, productId, quantity } })

// 移除商品
export const removeFromCart = (userId: number, productId: number) =>
  request.delete<ApiResponse<String>>('/cart/remove', { params: { userId, productId } })

// 清空购物车
export const clearCart = (userId: number) =>
  request.delete<ApiResponse<String>>('/cart/clear', { params: { userId } })

import { getPage } from '@/api/page'
import request from '@/utils/request'
import type  { ApiResponse } from '@/api/page'

export interface ProductResponseDTO {
  id: number
  merchantId?: number
  name: string
  description: string
  price: number
  stock: number
  isActive: boolean
  images: string[]   // 后端返回 JSON -> List<string>
  category: string
}
export interface ProductCreateDTO {
  merchantId?: number
  name: string
  description: string
  price: number
  stock: number
  isActive: boolean
  images: string[]
  category: string
}

// 获取商品分页
export const getProductPage = (page = 1, size = 10) =>
  getPage<ProductResponseDTO>('/product', { page, size })

// 获取商家商品分页
export const getMerchantProductPage = (page = 1, size = 10, userId: string) =>
  getPage<ProductResponseDTO>('/product/userId', { page, size, userId })

// 新增商品
export const addProduct = (data: Partial<ProductResponseDTO>) =>
  request.post<ApiResponse<ProductResponseDTO>>('/product', data)

// 更新商品
export const updateProduct = (id: number, data: ProductCreateDTO) =>
  request.put<ApiResponse<ProductResponseDTO>>(`/product/${id}`, data)

// 删除商品
export const deleteProduct = (id: number) =>
  request.delete<ApiResponse<string>>(`/product/${id}`)

// 上传商品图片
export const uploadProductImages = (id: number, files: File[]) => {
  const formData = new FormData()
  files.forEach(file => formData.append('files', file))
  return request.post<ApiResponse<string[]>>(`/product/${id}/images`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

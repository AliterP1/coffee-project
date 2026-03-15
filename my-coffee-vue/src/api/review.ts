import request from '@/utils/request'
import { getPage,type ApiResponse } from '@/api/page'

export interface ReviewRequestDTO {
  userId: number
  productId: number
  orderId: number
  rating: number
  comment: string
  images: string[]
}

export interface ReviewResponseDTO {
  id: number
  userId: number
  username: string
  productId: number
  orderId: number
  rating: number
  comment: string
  images: string[]
  avatarUrl: string
}
// 上传评价图片
export const uploadReviewImages = (id: number, files: File[]) => {
  const formData = new FormData()
  files.forEach((file) => formData.append('files', file))
  return request.post<string[]>(`/review/${id}/images`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// 添加评价
export const addReview = (data: ReviewRequestDTO) =>
  request.post<ApiResponse<ReviewResponseDTO>>('/review/addReview', data)

// 获取商品评价分页
export const getProductReviews = (productId: number, page = 1, size = 10) =>
  getPage<ReviewResponseDTO[]>(`/review/product`, { productId, page, size })

// 获取所有评价分页
export const getReviewPage = (page = 1, size = 10) =>
  getPage<ReviewResponseDTO>(`/review`, { page, size })

import { getPage } from '@/api/page'
import request from '@/utils/request'

export interface CoffeeContentResponseDTO {
  id: number
  title: string
  content: string
  images: string[]
  authorId: number
  authorName: string
  createdAt: string
}

export interface CoffeeContentQuery {
  page?: number
  size?: number
}

// 获取咖啡内容分页
export const getCoffeeContent = (page = 1, size = 10) =>
  getPage<CoffeeContentResponseDTO>('/coffee', { page, size })


// 上传咖啡图片
export const uploadCoffeeImages = (id: string, files: File[]) => {
  const formData = new FormData()
  files.forEach((file) => formData.append('files', file))
  return request.post(`/coffee/${id}/images`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

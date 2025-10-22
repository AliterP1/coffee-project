import { getPage } from '@/api/page'

export interface SearchResultDTO {
  id: number
  title: string
  content: string
  images: string[] // 后端已返回 List<String>
  createdAt: string
  type: 'PRODUCT' | 'CONTENT'
  price?: number
  stock?: number
  category?: string
  authorName?: string
}

export const searchCoffeeContent = (search: string, page = 1, size = 10) =>
  getPage<SearchResultDTO>('/search', { search, page, size })
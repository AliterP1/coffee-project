import { getPage } from '@/api/page'


export interface ProductResponseDTO {
  id: number
  merchantId: number
  name: string
  description: string
  price: number
  stock: number
  isActive: boolean
  images: string[]   // 后端返回 JSON -> List<string>
  category: string
}


// 获取商品分页
export const getProductPage = (page = 1, size = 10) =>
  getPage<ProductResponseDTO>('/product', { page, size })

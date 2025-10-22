import { defineStore } from 'pinia'
import request from '@/utils/request'

interface Product {
  id: number
  name: string
  price: number
  stock: number
  images: string[]
  category: string
}

export const useProductStore = defineStore('product', {
  state: () => ({
    productList: [] as Product[],
    productDetail: null as Product | null,
    lastFetchTime: 0,
  }),
  actions: {
    async fetchProducts() {
      const now = Date.now()
      if (now - this.lastFetchTime < 60 * 1000 && this.productList.length) {
        return
      }
      const res = await request.get('/products')
      this.productList = res.data
      this.lastFetchTime = now
    },
    async fetchProductDetail(id: number) {
      const res = await request.get(`/products/${id}`)
      this.productDetail = res.data
    },
  },
})

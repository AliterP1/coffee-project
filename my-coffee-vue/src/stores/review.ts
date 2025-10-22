import { defineStore } from 'pinia'
import request from '@/utils/request'

interface Review {
  id: number
  userId: number
  productId: number
  rating: number
  comment: string
  createdAt: string
}

export const useReviewStore = defineStore('review', {
  state: () => ({
    reviewList: [] as Review[],
  }),
  actions: {
    async fetchReviews(productId: number) {
      const res = await request.get(`/products/${productId}/reviews`)
      this.reviewList = res.data
    },
    async addReview(data: any) {
      await request.post('/reviews', data)
      await this.fetchReviews(data.productId)
    },
  },
})

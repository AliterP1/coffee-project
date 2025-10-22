import { defineStore } from 'pinia'
import request from '@/utils/request'

interface Order {
  id: number
  totalPrice: number
  status: string
  createdAt: string
}

export const useOrderStore = defineStore('order', {
  state: () => ({
    orderList: [] as Order[],
    currentOrder: null as Order | null,
  }),
  actions: {
    async createOrder(data: any) {
      const res = await request.post('/orders', data)
      this.currentOrder = res.data
    },
    async fetchOrders() {
      const res = await request.get('/orders')
      this.orderList = res.data
    },
    async fetchOrderDetail(orderId: number) {
      const res = await request.get(`/orders/${orderId}`)
      this.currentOrder = res.data
    },
    async cancelOrder(orderId: number) {
      await request.post(`/orders/${orderId}/cancel`)
      await this.fetchOrders()
    },
  },
})

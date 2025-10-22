import { defineStore } from 'pinia'
import request from '@/utils/request'

interface CartItem {
  productId: number
  name: string
  price: number
  quantity: number
  image: string
}

export const useCartStore = defineStore('cart', {
  state: () => ({
    cartItems: [] as CartItem[],
  }),
  getters: {
    totalPrice: (state) =>
      state.cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0),
    totalQuantity: (state) =>
      state.cartItems.reduce((sum, item) => sum + item.quantity, 0),
  },
  actions: {
    async fetchCart() {
      const res = await request.get('/cart')
      this.cartItems = res.data
    },
    async addToCart(productId: number, quantity: number) {
      await request.post('/cart/add', { productId, quantity })
      await this.fetchCart()
    },
    async removeFromCart(productId: number) {
      await request.delete(`/cart/${productId}`)
      this.cartItems = this.cartItems.filter((i) => i.productId !== productId)
    },
    clearCart() {
      this.cartItems = []
    },
  },
})

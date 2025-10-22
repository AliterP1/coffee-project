import { defineStore } from 'pinia'
import request from '@/utils/request'

interface Address {
  id: number
  recipientName: string
  phone: string
  fullAddress: string
  isDefault: boolean
}

export const useAddressStore = defineStore('address', {
  state: () => ({
    addressList: [] as Address[],
  }),
  getters: {
    defaultAddress: (state) =>
      state.addressList.find((addr) => addr.isDefault),
  },
  actions: {
    async fetchAddresses() {
      const res = await request.get('/addresses')
      this.addressList = res.data
    },
    async addAddress(data: Address) {
      await request.post('/addresses', data)
      await this.fetchAddresses()
    },
    async updateAddress(id: number, data: Address) {
      await request.put(`/addresses/${id}`, data)
      await this.fetchAddresses()
    },
    async deleteAddress(id: number) {
      await request.delete(`/addresses/${id}`)
      this.addressList = this.addressList.filter((a) => a.id !== id)
    },
  },
})
